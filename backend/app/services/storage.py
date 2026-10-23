"""Local-disk media storage for the pilot.

Kept behind this small interface on purpose: a single-store pilot is fine on
local disk, but swapping this module for one backed by S3 / GCS is the only
change needed to move to multi-store later — nothing above this layer
(routers, worker) touches the filesystem directly.
"""
import shutil
from pathlib import Path

from fastapi import UploadFile

from app.config import settings

MEDIA_ROOT = Path(settings.media_root)


def _dir(subdir: str) -> Path:
    d = MEDIA_ROOT / subdir
    d.mkdir(parents=True, exist_ok=True)
    return d


def save_upload(file: UploadFile, subdir: str, filename: str) -> str:
    dest = _dir(subdir) / filename
    with dest.open("wb") as out:
        shutil.copyfileobj(file.file, out)
    return filename


def save_bytes(data: bytes, subdir: str, filename: str) -> str:
    dest = _dir(subdir) / filename
    dest.write_bytes(data)
    return filename


def read_bytes(subdir: str, filename: str) -> bytes:
    return (_dir(subdir) / filename).read_bytes()


def exists(subdir: str, filename: str) -> bool:
    return (_dir(subdir) / filename).exists()


def url_for(subdir: str, filename: str) -> str:
    return f"{settings.base_url}/media/{subdir}/{filename}"
