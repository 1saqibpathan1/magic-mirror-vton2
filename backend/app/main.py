from pathlib import Path

from fastapi import FastAPI, Request
from fastapi.exceptions import HTTPException as FastAPIHTTPException, RequestValidationError
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from app.config import settings
from app.database import Base, engine
from app.routers import catalog, health, share, tryon

for sub in ("catalog", "uploads", "results"):
    (Path(settings.media_root) / sub).mkdir(parents=True, exist_ok=True)

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Magic Mirror Pilot Backend", version="0.1.0")

app.mount("/media", StaticFiles(directory=settings.media_root), name="media")

app.include_router(health.router)
app.include_router(catalog.router)
app.include_router(tryon.router)
app.include_router(share.router)


@app.exception_handler(FastAPIHTTPException)
async def http_exception_handler(request: Request, exc: FastAPIHTTPException):
    detail = exc.detail
    if isinstance(detail, dict) and "code" in detail and "message" in detail:
        body = {"error": detail}
    else:
        body = {"error": {"code": "http_error", "message": str(detail)}}
    return JSONResponse(status_code=exc.status_code, content=body)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(status_code=422, content={"error": {"code": "validation_error", "message": str(exc.errors())}})
