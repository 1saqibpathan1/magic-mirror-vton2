from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel


class CatalogItem(BaseModel):
    id: str
    name: str
    category: str
    price: float
    currency: str
    image_url: str


class CatalogResponse(BaseModel):
    updated_at: Optional[datetime] = None
    items: List[CatalogItem]


class TryonSubmitResponse(BaseModel):
    job_id: str
    status: str


class TryonStatusResponse(BaseModel):
    job_id: str
    status: str
    result_image_url: Optional[str] = None
    share_url: Optional[str] = None
    error: Optional[str] = None
