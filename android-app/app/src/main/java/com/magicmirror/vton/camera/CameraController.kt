package com.magicmirror.vton.camera

import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Abstraction over the physical capture device so the rest of the app never
 * touches the UVC/USB library directly. If the webcam integration ever needs
 * to change — a different library, or a box whose firmware happens to expose
 * the webcam through plain Camera2 — only the implementation behind this
 * interface should need to change.
 */
interface CameraController {
    val isReady: StateFlow<Boolean>

    /** Captures a still frame and returns it as a JPEG file, or a failure. */
    suspend fun capturePhoto(): Result<File>
}
