from typing import Optional

from fastapi import Header

from app.config import settings
from app.errors import ApiError


def require_device_key(x_device_key: Optional[str] = Header(default=None)) -> None:
    """Pilot-grade auth: one shared secret, one kiosk device. Swap for per-device
    issued keys before rolling out past a single store."""
    if not x_device_key or x_device_key != settings.device_api_key:
        raise ApiError(401, "unauthorized", "Missing or invalid X-Device-Key header")
