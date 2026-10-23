from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import String, Float, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column

from app.database import Base


def _now() -> datetime:
    return datetime.now(timezone.utc)


class Garment(Base):
    __tablename__ = "garments"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    # App-facing vocabulary: tops | bottoms | dresses | outerwear.
    # Mapped onto the try-on provider's own category vocabulary inside
    # services/fashn_client.py, never here.
    category: Mapped[str] = mapped_column(String, index=True)
    price: Mapped[float] = mapped_column(Float)
    currency: Mapped[str] = mapped_column(String, default="INR")
    image_filename: Mapped[str] = mapped_column(String)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, onupdate=_now)


class TryonJob(Base):
    __tablename__ = "tryon_jobs"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    garment_id: Mapped[str] = mapped_column(String, ForeignKey("garments.id"), index=True)
    status: Mapped[str] = mapped_column(String, default="pending")  # pending|processing|completed|failed
    person_image_filename: Mapped[str] = mapped_column(String)
    result_filename: Mapped[Optional[str]] = mapped_column(String, nullable=True)
    fashn_prediction_id: Mapped[Optional[str]] = mapped_column(String, nullable=True)
    error_message: Mapped[Optional[str]] = mapped_column(String, nullable=True)
    share_code: Mapped[Optional[str]] = mapped_column(String, nullable=True, unique=True, index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, onupdate=_now)
