"""Background orchestration for a try-on job: call the provider (or simulate
it), poll until done, download+persist the result, update the DB row.

Runs as a FastAPI BackgroundTask after the /tryon endpoint has already
responded 202 — fine at pilot scale (one kiosk). If this ever needs to survive
a backend restart mid-job, or run across multiple workers, swap this for a
real queue (e.g. Celery/RQ) — the orchestration logic here wouldn't need to
change much, just how it's invoked.
"""
import asyncio
import secrets
import time

import httpx

from app.config import settings
from app.database import SessionLocal
from app import models
from app.services import storage, fashn_client

POLL_INTERVAL_SECONDS = 2.0
JOB_TIMEOUT_SECONDS = 60.0
MOCK_DELAY_SECONDS = 3.0


class TryonFailure(Exception):
    def __init__(self, code: str):
        self.code = code
        super().__init__(code)


async def _get_result_bytes(job: models.TryonJob, garment: models.Garment) -> bytes:
    if settings.mock_fashn_provider:
        # No network call at all — just prove the plumbing (upload -> job ->
        # processing -> completed -> result/share URL) works before a real
        # FASHN key is wired in. Stands in the garment photo as the "result".
        await asyncio.sleep(MOCK_DELAY_SECONDS)
        return storage.read_bytes("catalog", garment.image_filename)

    try:
        prediction_id = await fashn_client.submit_tryon(
            storage.read_bytes("uploads", job.person_image_filename),
            storage.url_for("catalog", garment.image_filename),
            garment.category,
        )
    except fashn_client.FashnError as e:
        raise TryonFailure(e.code) from e

    deadline = time.monotonic() + JOB_TIMEOUT_SECONDS
    while time.monotonic() < deadline:
        await asyncio.sleep(POLL_INTERVAL_SECONDS)
        payload = await fashn_client.poll_tryon(prediction_id)
        status = payload.get("status")

        if status == "completed":
            output = payload.get("output") or []
            if not output:
                raise TryonFailure("empty_result")
            async with httpx.AsyncClient(timeout=30.0) as client:
                resp = await client.get(output[0])
                resp.raise_for_status()
                return resp.content

        if status in ("failed", "canceled"):
            err = payload.get("error") or {}
            raise TryonFailure(err.get("name", "provider_failed"))

    raise TryonFailure("provider_timeout")


async def run_tryon_job(job_id: str) -> None:
    db = SessionLocal()
    try:
        job = db.get(models.TryonJob, job_id)
        if job is None:
            return
        garment = db.get(models.Garment, job.garment_id)
        if garment is None:
            job.status = "failed"
            job.error_message = "garment_not_found"
            db.commit()
            return

        job.status = "processing"
        db.commit()

        try:
            image_bytes = await _get_result_bytes(job, garment)
        except TryonFailure as e:
            job.status = "failed"
            job.error_message = e.code
            db.commit()
            return
        except Exception:
            job.status = "failed"
            job.error_message = "unexpected_error"
            db.commit()
            return

        result_filename = f"{job.id}.jpg"
        storage.save_bytes(image_bytes, "results", result_filename)

        job.result_filename = result_filename
        job.share_code = secrets.token_urlsafe(6)
        job.status = "completed"
        db.commit()
    finally:
        db.close()
