package com.magicmirror.vton.camera

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class UvcCameraController : CameraController {

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var fragment: UvcCameraFragment? = null

    internal fun attachFragment(fragment: UvcCameraFragment) {
        this.fragment = fragment
    }

    internal fun onCameraStateChanged(ready: Boolean) {
        _isReady.value = ready
    }

    override suspend fun capturePhoto(): Result<File> = suspendCancellableCoroutine { cont ->
        val f = fragment
        if (f == null || !_isReady.value) {
            cont.resume(Result.failure(IllegalStateException("Camera isn't ready yet")))
            return@suspendCancellableCoroutine
        }
        val outFile = File(f.requireContext().cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        f.capturePhoto(outFile) { file ->
            if (cont.isActive) {
                if (file != null && file.exists()) {
                    cont.resume(Result.success(file))
                } else {
                    cont.resume(Result.failure(IllegalStateException("Capture failed")))
                }
            }
        }
    }
}
