package com.magicmirror.vton.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TryonSubmitResponseDto(
    @SerialName("job_id") val jobId: String,
    val status: String,
)

@Serializable
data class TryonStatusResponseDto(
    @SerialName("job_id") val jobId: String,
    val status: String, // pending | processing | completed | failed
    @SerialName("result_image_url") val resultImageUrl: String? = null,
    @SerialName("share_url") val shareUrl: String? = null,
    val error: String? = null,
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBody,
)

@Serializable
data class ApiErrorBody(
    val code: String,
    val message: String,
)
