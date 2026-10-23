package com.magicmirror.vton.ui.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.magicmirror.vton.data.network.ApiResult
import com.magicmirror.vton.data.network.dto.TryonStatusResponseDto
import com.magicmirror.vton.data.repository.TryonRepository
import com.magicmirror.vton.util.rememberQrCodeBitmap

@Composable
fun ResultScreen(
    jobId: String,
    tryonRepository: TryonRepository,
    onTryAnother: () -> Unit,
    onDone: () -> Unit,
) {
    var status by remember { mutableStateOf<TryonStatusResponseDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(jobId) {
        when (val result = tryonRepository.getStatus(jobId)) {
            is ApiResult.Success -> status = result.data
            is ApiResult.Failure -> errorMessage = result.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Your try-on",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                errorMessage != null -> Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                status?.resultImageUrl == null -> CircularProgressIndicator()
                else -> AsyncImage(
                    model = status?.resultImageUrl,
                    contentDescription = "Your virtual try-on result",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        status?.shareUrl?.let { shareUrl ->
            val qrBitmap = rememberQrCodeBitmap(shareUrl, sizePx = 320)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp),
            ) {
                Text("Scan to take it with you", color = MaterialTheme.colorScheme.onSurfaceVariant)
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR code to download your photo",
                        modifier = Modifier.size(180.dp).padding(top = 8.dp),
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onTryAnother, modifier = Modifier.fillMaxWidth()) {
                Text("Try another outfit")
            }
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Done")
            }
        }
    }
}
