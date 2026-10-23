package com.magicmirror.vton.camera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiangdg.ausbc.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.bean.ICamera
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import java.io.File

/**
 * Wraps jiangdongguo/AndroidUSBCamera (libausbc, JitPack) for the USB UVC
 * webcam mounted above the standee. Chosen over Camera2 directly because
 * stock Android 11 won't enumerate an arbitrary USB webcam as a Camera2
 * device unless the box's own firmware adds UVC HAL support — this library
 * talks to the webcam over USB host mode instead, so it works regardless of
 * what the box vendor did or didn't add.
 *
 * ⚠️ UNVERIFIED AGAINST A COMPILER: this project was authored in an
 * environment without the Android SDK or network access to Google's/Maven's
 * repositories (see root README), so this file follows the usage pattern
 * documented in the library's README (github.com/jiangdongguo/AndroidUSBCamera)
 * but was never actually compiled against the real libausbc:3.3.3 sources.
 * This is the single file most likely to need a small correction once
 * Android Studio resolves the dependency and shows real autocomplete/errors
 * — most likely around the exact ICameraStateCallBack.State enum values or
 * the capture callback's method names below. Nothing outside this file and
 * UvcCameraController.kt depends on these specifics; see CameraController.
 */
class UvcCameraFragment : CameraFragment() {

    var controller: UvcCameraController? = null
        set(value) {
            field = value
            value?.attachFragment(this)
        }

    private lateinit var previewView: AspectRatioTextureView

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        previewView = AspectRatioTextureView(requireContext())
        return previewView
    }

    override fun getCameraView(): IAspectRatio = previewView

    override fun getCameraViewContainer(): ViewGroup? = null

    override fun onCameraState(self: ICamera, code: ICameraStateCallBack.State, msg: String?) {
        controller?.onCameraStateChanged(code == ICameraStateCallBack.State.OPENED)
    }

    /** Captures a still frame to [outFile] and reports the result back. */
    fun capturePhoto(outFile: File, onResult: (File?) -> Unit) {
        // The README shows `captureImage(callback, savePath)` without spelling
        // out the callback's exact type. ICaptureCallBack with
        // onBegin/onError/onComplete(path) is the conventional shape for this
        // library family (mirrors onCameraState's pattern above) — verify
        // against com.jiangdg.ausbc.callback.ICaptureCallBack once resolved.
        captureImage(object : com.jiangdg.ausbc.callback.ICaptureCallBack {
            override fun onBegin() = Unit
            override fun onError(error: String?) = onResult(null)
            override fun onComplete(path: String?) {
                onResult(path?.let(::File) ?: outFile.takeIf { it.exists() })
            }
        }, outFile.absolutePath)
    }
}
