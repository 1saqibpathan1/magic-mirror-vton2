package com.magicmirror.vton.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.magicmirror.vton.camera.UvcCameraController
import com.magicmirror.vton.camera.UvcCameraPreview
import com.magicmirror.vton.data.network.ApiResult
import com.magicmirror.vton.data.repository.TryonRepository
import kotlinx.coroutines.launch

private sealed class CaptureState {
    data object Live : CaptureState()
    data object Submitting : CaptureState()
    data object Processing : CaptureState()
    data class Error(val message: String) : CaptureState()
}

@Composable
fun CaptureScreen(
    garmentId: String,
    cameraController: UvcCameraController,
    tryonRepository: TryonRepository,
    onResult: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var state by remember { mutableStateOf<CaptureState>(CaptureState.Live) }
    val cameraReady by cameraController.isReady.collectAsState()
    val scope = rememberCoroutineScope()

    fun captureAndSubmit() {
        scope.launch {
            state = CaptureState.Submitting
            val photoFile = cameraController.capturePhoto().getOrNull()
            if (photoFile == null) {
                state = CaptureState.Error("Couldn't capture a photo. Try again.")
                return@launch
            }

            when (val submitResult = tryonRepository.submitTryon(photoFile, garmentId)) {
                is ApiResult.Failure -> state = CaptureState.Error(submitResult.message)
                is ApiResult.Success -> {
                    state = CaptureState.Processing
                    when (val statusResult = tryonRepository.pollUntilDone(submitResult.data)) {
                        is ApiResult.Failure -> state = CaptureState.Error(statusResult.message)
                        is ApiResult.Success -> {
                            val status = statusResult.data
                            if (status.status == "completed") {
                                onResult(submitResult.data)
                            } else {
                                state = CaptureState.Error(status.error ?: "Couldn't generate your try-on.")
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        UvcCameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            when (val s = state) {
                is CaptureState.Live -> {
                    Button(
                        onClick = { captureAndSubmit() },
                        enabled = cameraReady,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(if (cameraReady) "Capture" else "Starting camera…")
                    }
                }
                is CaptureState.Submitting, is CaptureState.Processing -> {
                    Column(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = if (s is CaptureState.Submitting) "Uploading…" else "Styling your try-on…",
                            modifier = Modifier.padding(top = 12.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                is CaptureState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { state = CaptureState.Live }, modifier = Modifier.padding(top = 12.dp)) {
                            Text("Try again")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp),
            ) {
                Text("Back to catalog")
            }
        }
    }
}
