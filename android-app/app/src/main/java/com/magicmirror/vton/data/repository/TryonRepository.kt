package com.magicmirror.vton.data.repository

import com.magicmirror.vton.data.network.ApiResult
import com.magicmirror.vton.data.network.ApiService
import com.magicmirror.vton.data.network.dto.TryonStatusResponseDto
import com.magicmirror.vton.data.network.safeApiCall
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class TryonRepository(private val api: ApiService) {

    suspend fun submitTryon(personImageFile: File, garmentId: String): ApiResult<String> {
        val result = safeApiCall {
            val imagePart = MultipartBody.Part.createFormData(
                "person_image",
                personImageFile.name,
                personImageFile.asRequestBody("image/jpeg".toMediaType()),
            )
            val garmentPart = garmentId.toRequestBody("text/plain".toMediaType())
            api.submitTryon(imagePart, garmentPart)
        }
        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.jobId)
            is ApiResult.Failure -> result
        }
    }

    suspend fun getStatus(jobId: String): ApiResult<TryonStatusResponseDto> =
        safeApiCall { api.getTryonStatus(jobId) }

    /** Polls until the job reaches a terminal state or [timeoutMs] elapses. */
    suspend fun pollUntilDone(
        jobId: String,
        intervalMs: Long = 1500,
        timeoutMs: Long = 50_000,
    ): ApiResult<TryonStatusResponseDto> {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            when (val result = getStatus(jobId)) {
                is ApiResult.Failure -> return result
                is ApiResult.Success -> {
                    if (result.data.status == "completed" || result.data.status == "failed") {
                        return result
                    }
                }
            }
            delay(intervalMs)
        }
        return ApiResult.Failure("client_timeout", "Taking longer than expected.")
    }
}
