package com.magicmirror.vton.kiosk

import android.app.admin.DeviceAdminReceiver

/**
 * No special device-admin policies are requested (see
 * res/xml/device_admin_receiver.xml) — this class exists only so the app can
 * be provisioned as device owner, which is what makes startLockTask() work
 * silently for kiosk mode. See root README's "Kiosk provisioning" section.
 */
class MagicMirrorDeviceAdminReceiver : DeviceAdminReceiver()
