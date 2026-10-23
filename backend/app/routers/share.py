from fastapi import APIRouter, Depends
from fastapi.responses import HTMLResponse
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import get_db
from app.errors import ApiError
from app import models
from app.services import storage

# Public — no auth. This is what a shopper's phone opens from the QR code.
router = APIRouter(tags=["share"])


@router.get("/s/{code}", response_class=HTMLResponse)
def share_page(code: str, db: Session = Depends(get_db)):
    job = db.scalars(select(models.TryonJob).where(models.TryonJob.share_code == code)).first()
    if job is None or job.result_filename is None:
        raise ApiError(404, "not_found", "This link isn't valid or has expired")

    image_url = storage.url_for("results", job.result_filename)
    return f"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Your Virtual Try-On</title>
<style>
  body {{ margin:0; font-family:-apple-system,system-ui,sans-serif; background:#111319; color:#fff;
         display:flex; flex-direction:column; align-items:center; padding:28px 20px; text-align:center; }}
  h1 {{ font-size:1.25rem; font-weight:600; margin:8px 0 4px; }}
  p {{ color:#9a9aa5; margin:0 0 4px; font-size:.9rem; }}
  img {{ max-width:100%; width:420px; border-radius:16px; margin-top:18px; box-shadow:0 12px 32px rgba(0,0,0,.45); }}
  a.button {{ margin-top:22px; padding:13px 28px; background:#fff; color:#111; border-radius:999px;
             text-decoration:none; font-weight:600; font-size:.95rem; }}
</style>
</head>
<body>
  <h1>Your try-on</h1>
  <p>Saved from your in-store fitting session</p>
  <img src="{image_url}" alt="Your virtual try-on result">
  <a class="button" href="{image_url}" download="my-tryon.jpg">Download photo</a>
</body>
</html>"""
