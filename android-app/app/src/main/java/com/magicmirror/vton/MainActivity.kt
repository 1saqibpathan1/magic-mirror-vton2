package com.magicmirror.vton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.magicmirror.vton.camera.UvcCameraController
import com.magicmirror.vton.kiosk.KioskModeHelper
import com.magicmirror.vton.ui.navigation.MagicMirrorNavHost
import com.magicmirror.vton.ui.theme.MagicMirrorTheme

class MainActivity : ComponentActivity() {

    private val cameraController = UvcCameraController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        KioskModeHelper.enterImmersiveMode(this)
        KioskModeHelper.tryStartLockTask(this)

        val appContainer = (application as MagicMirrorApp).container

        setContent {
            MagicMirrorTheme {
                MagicMirrorNavHost(appContainer = appContainer, cameraController = cameraController)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            KioskModeHelper.enterImmersiveMode(this)
        }
    }
}
