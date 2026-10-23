package com.magicmirror.vton.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object KioskModeHelper {

    /** Hides status/nav bars so the UI fills the whole 55" panel. */
    fun enterImmersiveMode(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * Locks the screen so shoppers can't back out to the launcher or recents.
     * Only works silently if this app has been provisioned as device owner
     * (see root README's "Kiosk provisioning" section) — otherwise it's a
     * no-op, and the device should have manual Screen Pinning enabled instead
     * for an early bench test.
     */
    fun tryStartLockTask(activity: Activity) {
        val dpm = activity.getSystemService(DevicePolicyManager::class.java) ?: return
        val isDeviceOwner = dpm.isDeviceOwnerApp(activity.packageName)
        if (isDeviceOwner) {
            val admin = ComponentName(activity, MagicMirrorDeviceAdminReceiver::class.java)
            dpm.setLockTaskPackages(admin, arrayOf(activity.packageName))
            activity.startLockTask()
        }
    }
}
