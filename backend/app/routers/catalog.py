from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.auth import require_device_key
from app.database import get_db
from app.errors import ApiError
from app import models, schemas
from app.services import storage

router = APIRouter(prefix="/api/v1", tags=["catalog"], dependencies=[Depends(require_device_key)])


def _to_item(g: models.Garment) -> schemas.CatalogItem:
    return schemas.CatalogItem(
        id=g.id,
        name=g.name,
        category=g.category,
        price=g.price,
        currency=g.currency,
        image_url=storage.url_for("catalog", g.image_filename),
    )


@router.get("/catalog", response_model=schemas.CatalogResponse)
def list_catalog(category: Optional[str] = Query(default=None), db: Session = Depends(get_db)):
    stmt = select(models.Garment)
    if category:
        stmt = stmt.where(models.Garment.category == category)
    stmt = stmt.order_by(models.Garment.name)
    garments = list(db.scalars(stmt).all())
    updated_at = max((g.updated_at for g in garments), default=None)
    return schemas.CatalogResponse(items=[_to_item(g) for g in garments], updated_at=updated_at)


@router.get("/catalog/{item_id}", response_model=schemas.CatalogItem)
def get_catalog_item(item_id: str, db: Session = Depends(get_db)):
    garment = db.get(models.Garment, item_id)
    if garment is None:
        raise ApiError(404, "not_found", f"No garment with id {item_id}")
    return _to_item(garment)
