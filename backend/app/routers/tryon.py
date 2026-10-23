import uuid

from fastapi import APIRouter, BackgroundTasks, Depends, File, Form, UploadFile
from sqlalchemy.orm import Session

from app.auth import require_device_key
from app.config import settings
from app.database import get_db
from app.errors import ApiError
from app import models, schemas
from app.services import storage
from app.services.tryon_worker import run_tryon_job

router = APIRouter(prefix="/api/v1", tags=["tryon"], dependencies=[Depends(require_device_key)])

MAX_UPLOAD_BYTES = 10 * 1024 * 1024


@router.post("/tryon", response_model=schemas.TryonSubmitResponse, status_code=202)
async def submit_tryon(
    background_tasks: BackgroundTasks,
    person_image: UploadFile = File(...),
    garment_id: str = Form(...),
    db: Session = Depends(get_db),
):
    garment = db.get(models.Garment, garment_id)
    if garment is None:
        raise ApiError(404, "garment_not_found", f"No garment with id {garment_id}")

    contents = await person_image.read()
    if not contents:
        raise ApiError(400, "empty_image", "person_image is empty")
    if len(contents) > MAX_UPLOAD_BYTES:
        raise ApiError(413, "image_too_large", "person_image exceeds 10MB")

    job_id = f"job_{uuid.uuid4().hex[:12]}"
    storage.save_bytes(contents, "uploads", f"{job_id}.jpg")

    job = models.TryonJob(
        id=job_id,
        garment_id=garment_id,
        status="pending",
        person_image_filename=f"{job_id}.jpg",
    )
    db.add(job)
    db.commit()

    background_tasks.add_task(run_tryon_job, job_id)

    return schemas.TryonSubmitResponse(job_id=job.id, status=job.status)


@router.get("/tryon/{job_id}", response_model=schemas.TryonStatusResponse)
def get_tryon_status(job_id: str, db: Session = Depends(get_db)):
    job = db.get(models.TryonJob, job_id)
    if job is None:
        raise ApiError(404, "not_found", "No such job")

    result_image_url = storage.url_for("results", job.result_filename) if job.result_filename else None
    share_url = f"{settings.base_url}/s/{job.share_code}" if job.share_code else None

    return schemas.TryonStatusResponse(
        job_id=job.id,
        status=job.status,
        result_image_url=result_image_url,
        share_url=share_url,
        error=job.error_message,
    )
