"""Thin client for the FASHN try-on API (https://docs.fashn.ai).

Endpoints used:
  POST /v1/run          submit a try-on prediction, returns {"id": ..., "error": ...}
  GET  /v1/status/:id   poll for status/result

Kept deliberately separate from tryon_worker.py's orchestration (polling loop,
timeouts, DB updates) so that swapping in a self-hosted FASHN VTON v1.5 (or any
other provider) later only means changing this file.
"""
import base64

import httpx

from app.config import settings

FASHN_BASE_URL = "https://api.fashn.ai/v1"

# Our app-facing categories (tops/bottoms/dresses/outerwear, see models.py) mapped
# onto FASHN's own vocabulary (tops/bottoms/one-pieces/auto). Dresses are a
# one-piece garment; outerwear doesn't map cleanly onto any FASHN bucket, so we
# let FASHN's own detection ("auto") handle it rather than force a wrong category.
CATEGORY_MAP = {
    "tops": "tops",
    "bottoms": "bottoms",
    "dresses": "one-pieces",
    "outerwear": "auto",
}


class FashnError(Exception):
    def __init__(self, message: str, code: str = "provider_rejected"):
        self.code = code
        super().__init__(message)


def _headers() -> dict:
    return {
        "Authorization": f"Bearer {settings.fashn_api_key}",
        "Content-Type": "application/json",
    }


async def submit_tryon(person_image_bytes: bytes, garment_image_url: str, category: str) -> str:
    """Submits a try-on job. Returns FASHN's prediction id."""
    model_image = "data:image/jpeg;base64," + base64.b64encode(person_image_bytes).decode("ascii")
    payload = {
        "model_name": "tryon-v1.6",
        "inputs": {
            "model_image": model_image,
            "garment_image": garment_image_url,
            "category": CATEGORY_MAP.get(category, "auto"),
            "mode": "balanced",
        },
    }
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.post(f"{FASHN_BASE_URL}/run", headers=_headers(), json=payload)
    except httpx.HTTPError as e:
        raise FashnError(str(e), "provider_unreachable") from e

    try:
        data = resp.json()
    except ValueError:
        raise FashnError(f"Non-JSON response (HTTP {resp.status_code})", "provider_error")

    if resp.status_code >= 400 or data.get("error"):
        raise FashnError(data.get("error") or f"HTTP {resp.status_code}", "provider_rejected")

    return data["id"]


async def poll_tryon(prediction_id: str) -> dict:
    """Returns FASHN's raw status payload: {status, output[], error}."""
    async with httpx.AsyncClient(timeout=15.0) as client:
        resp = await client.get(f"{FASHN_BASE_URL}/status/{prediction_id}", headers=_headers())
    resp.raise_for_status()
    return resp.json()
