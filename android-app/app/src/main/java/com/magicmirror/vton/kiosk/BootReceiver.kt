package com.magicmirror.vton.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.magicmirror.vton.MainActivity

/** Auto-launches the kiosk after a power cycle, so the standee recovers
 * unattended overnight or after a crash reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(launchIntent)
    }
}
