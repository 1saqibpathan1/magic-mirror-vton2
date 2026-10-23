package com.magicmirror.vton.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment

/** Compose entry point for the camera preview — hosts [UvcCameraFragment]
 * via the standard Fragment-in-Compose interop. */
@Composable
fun UvcCameraPreview(
    controller: UvcCameraController,
    modifier: Modifier = Modifier,
) {
    AndroidFragment<UvcCameraFragment>(modifier = modifier.fillMaxSize()) { fragment ->
        fragment.controller = controller
    }
}
