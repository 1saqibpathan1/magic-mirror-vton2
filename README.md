# Magic Mirror — Pilot Virtual Try-On Kiosk

A commercial virtual try-on app for a 55" digital standee (Android 11, 8GB RAM,
RK3566 box, USB webcam) backed by the FASHN API, for a single pilot store.

```
┌─────────────────────┐         ┌──────────────────────┐        ┌───────────────┐
│ 55" standee          │  HTTPS  │ Pilot backend         │  HTTPS │ FASHN API      │
│ Android 11 / RK3566  │────────▶│ FastAPI               │───────▶│ (or, later,    │
│  • USB webcam capture│         │  • catalog storage    │        │  self-hosted   │
│  • catalog browse UI │◀────────│  • FASHN key (proxy)  │◀───────│  FASHN VTON    │
│  • try-on + QR share │         │  • QR/result storage  │        │  v1.5)         │
└─────────────────────┘         └──────────────────────┘        └───────────────┘
```

The Android box never talks to FASHN directly and never holds the FASHN key —
it only talks to your backend, which proxies the try-on call. That's true
today with the hosted FASHN API and stays true later if you switch to a
self-hosted model: only `backend/app/services/fashn_client.py` would change,
not the Android app.

Two halves, two folders:
- `backend/` — FastAPI service: clothing catalog, FASHN proxy, QR take-home links.
- `android-app/` — Kotlin + Jetpack Compose kiosk app.
- `API_CONTRACT.md` — the REST contract between them. Change it first if either side needs to change.

## What's been verified vs. what hasn't

**Backend:** fully built and tested end-to-end in this session — installed,
ran, and exercised every endpoint (catalog, try-on submit → poll → complete,
share page, error cases) with a mock try-on provider. See "Try it right now"
below to reproduce that in about two minutes.

**Android app:** hand-authored but **not compiled** — this session's sandbox
has no Android SDK and its network proxy blocks Google's and Maven Central's
repositories (confirmed via direct test), so there was no way to run a real
Gradle build here. The code follows standard, well-established patterns
throughout, but the first time you open it in Android Studio is also the
first time any of it has been compiler-checked. Expect to fix a handful of
small things (imports, maybe a method signature) rather than a working build
on the very first try. One file carries deliberately extra risk — see below.

### The one file to check first: the USB webcam integration

`android-app/.../camera/UvcCameraFragment.kt` wraps a third-party library
(`jiangdongguo/AndroidUSBCamera`, aka `libausbc`) that lets the app talk to
the USB UVC webcam without needing the box's Android firmware to support it
as a native Camera2 device — that support isn't guaranteed across vendors,
so this library is the safer default. I confirmed the library is real, found
its actual JitPack coordinates and current version, and wrote this file
against its documented usage pattern — but couldn't compile it against the
real library sources. It's isolated on purpose: everything else in the app
talks to the camera only through `CameraController` (a plain interface), so
if this file needs a correction, nothing else should need to change.

## Try it right now (backend, 2 minutes)

```bash
cd backend
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env            # MOCK_FASHN_PROVIDER=true by default — no FASHN key needed yet
python3 seed_catalog.py         # creates 4 sample catalog rows
# drop any 4 jpg/png files into media/catalog/, named grm_001.jpg ... grm_004.jpg
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Then:
```bash
curl -H "X-Device-Key: change-me-device-key" http://localhost:8000/api/v1/catalog
```
should return the 4 sample garments. With `MOCK_FASHN_PROVIDER=true`, a
`POST /api/v1/tryon` completes in ~3 seconds without calling FASHN at all —
useful for building/demoing the Android app before you have a FASHN key, and
for automated testing.

## Setting up the real pilot

1. **FASHN account**: sign up at fashn.ai, get an API key.
2. **backend/.env**: set `FASHN_API_KEY`, set `MOCK_FASHN_PROVIDER=false`,
   set a real random `DEVICE_API_KEY`, and set `BASE_URL` to wherever this
   backend will actually be reachable from the standee (a LAN IP for local
   testing, a real domain once deployed — see `backend/Dockerfile`).
3. **Replace the sample catalog**: edit `backend/seed_catalog.py` with your
   real products (or point `app/routers/catalog.py` at your actual inventory
   system), and put real product photos in `media/catalog/`. FASHN works
   best with a clean flat-lay or mannequin shot per garment.
4. **android-app/local.properties**: copy `local.properties.example`, set
   `sdk.dir` to your Android SDK path, `magicMirror.baseUrl` to the same
   backend URL, `magicMirror.deviceApiKey` to match `DEVICE_API_KEY`.
5. Open `android-app/` in Android Studio, let it sync, fix anything it flags
   (see above), build, and run on the actual standee hardware — the USB
   webcam step in particular needs real hardware to verify, not an emulator.

`./gradlew` is already generated and working (wrapper jar included, targets
Gradle 8.9) — Android Studio will use it automatically.

## Kiosk provisioning (so shoppers can't back out to Android)

The app already does immersive fullscreen and attempts `startLockTask()` on
launch. For that lock to be silent (no Settings fiddling needed in-store),
the app should be the device's **device owner**, which requires a factory-fresh
box with no accounts or other device-admin apps on it yet:

```bash
adb install app-release.apk
adb shell dpm set-device-owner com.magicmirror.vton/.kiosk.MagicMirrorDeviceAdminReceiver
```

Then relaunch the app. If a box can't be factory-reset before setup, the app
still runs fine — it just won't be able to lock task mode silently; enabling
Screen Pinning manually in Android Settings is the fallback. The app also
registers a boot receiver, so it relaunches itself after a power cycle.
Some RK3566 box vendors ship their own kiosk/MDM settings too — worth
checking your specific box's documentation for additional lockdown options
(disabling the system update prompt, hiding other preloaded apps, etc.).

## Open-source model for later: FASHN VTON v1.5

When you're ready to self-host instead of calling the hosted API:
[FASHN VTON v1.5](https://github.com/fashn-AI/fashn-vton-1.5) — Apache-2.0
code license, 972M params, ~2GB weights, runs on a single consumer/prosumer
GPU (best on Ampere+: RTX 30/40-series, A100, H100). Only
`backend/app/services/fashn_client.py` and `tryon_worker.py` would need to
change — the Android app and the rest of the backend are unaffected, since
they only ever talk to your own `/api/v1/tryon` endpoint. Before relying on
this commercially, double-check the license terms of the model weights and
the bundled human-parser component specifically — the repo's Apache-2.0
notice is clearest about the code itself.

## Known limitations / next steps

- Catalog and try-on auth use one shared static device key — fine for one
  kiosk, not for multi-store (swap for per-device issued keys first).
- No database migrations (SQLite + `create_all` on startup) — add Alembic
  before this grows past a single pilot.
- Background jobs run in-process via FastAPI `BackgroundTasks` — fine at one
  kiosk's volume; move to a real queue (Celery/RQ) if this scales to many
  stores hitting one backend.
- No HTTPS enforced — `usesCleartextTraffic` is on for local/LAN pilot
  testing; put the backend behind HTTPS (and remove that manifest flag)
  before a real in-store deployment.
- Minification (`isMinifyEnabled`) is off on the Android release build for
  now, to remove one more unverified variable — turn it on once you've
  tested a build on real hardware (starter ProGuard rules are already in
  `app/proguard-rules.pro`).
- Launcher icon is a placeholder vector mark — swap before shipping.
