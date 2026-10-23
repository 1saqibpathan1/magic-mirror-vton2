# API Contract — Magic Mirror Pilot

Contract between the Android kiosk app and the pilot backend. Both halves are built against this document; change it first if either side needs to change.

Base URL: configurable (e.g. `https://pilot.yourdomain.com/api/v1`), set on the Android side via `local.properties` / build config, not hardcoded.

## Auth

Every `/api/v1/*` request (except `/health`) must include:

```
X-Device-Key: <shared secret, set via backend env var DEVICE_API_KEY>
```

This is pilot-grade (one store, one device) — a static shared secret is enough. Missing/invalid key → `401` with the standard error body.

The public share page (`GET /s/{code}`) requires no auth — it's what a shopper's phone opens from the QR code.

## Error format

Non-2xx responses use:
```json
{ "error": { "code": "string", "message": "human-readable string" } }
```

## Endpoints

### `GET /api/v1/health`
No auth. Returns `{ "status": "ok" }`. For ops/monitoring.

### `GET /api/v1/catalog`
Query params: `category` (optional string filter).

```json
{
  "updated_at": "2026-10-18T12:00:00Z",
  "items": [
    {
      "id": "grm_001",
      "name": "Floral Wrap Dress",
      "category": "dresses",
      "price": 2499.0,
      "currency": "INR",
      "image_url": "https://.../grm_001.jpg"
    }
  ]
}
```

`category` is one of: `tops`, `bottoms`, `dresses`, `outerwear` (app-facing vocabulary — the backend maps this to whatever the try-on provider's own category values are, so the app and catalog never need to know provider-specific terms).

`image_url` is a clean product/flat-lay shot — used both as the catalog thumbnail and as the garment reference image sent to the try-on provider.

### `GET /api/v1/catalog/{item_id}`
Same item shape as above, 404 if not found.

### `POST /api/v1/tryon`
`multipart/form-data`:
- `person_image`: JPEG file (the webcam capture)
- `garment_id`: string, must match a catalog item id

Returns `202 Accepted`:
```json
{ "job_id": "job_abc123", "status": "pending" }
```

Backend validates the garment id, stores the person image, and kicks off the provider call asynchronously (don't block the HTTP response on a multi-second generation).

### `GET /api/v1/tryon/{job_id}`
Polled by the app (suggested interval: 1.5s, timeout ~45s).

```json
{
  "job_id": "job_abc123",
  "status": "completed",
  "result_image_url": "https://.../media/results/job_abc123.jpg",
  "share_url": "https://.../s/9fQ2kT",
  "error": null
}
```

`status` ∈ `pending | processing | completed | failed`. `result_image_url` / `share_url` are null until `completed`. On `failed`, `error` holds a short human-readable reason (e.g. `"provider_timeout"`, `"provider_error"`) — the app shows a generic "couldn't generate your try-on, please try again" regardless of the exact code.

`result_image_url` and `share_url` are **backend-hosted**, not the provider's raw result link — the backend downloads and re-hosts the image so it survives past the provider's own link expiry.

### `GET /s/{code}` (public, not under `/api/v1`)
Opened by a shopper's phone from the QR code. Returns a small mobile-friendly HTML page showing the photo with a download button/link. No auth, no JSON.

## Notes for both sides

- Images the app sends: JPEG, person roughly centered/full-body, reasonable size (backend should cap/reject >10MB).
- The backend is the only thing that holds the try-on provider's API key — it is never shipped in the APK.
- Catalog is backend-owned; the app never bundles garment data, so merchandising updates don't need an app rebuild.
