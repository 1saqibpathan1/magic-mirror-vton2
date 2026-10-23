package com.magicmirror.vton.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogItemDto(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val currency: String,
    @SerialName("image_url") val imageUrl: String,
)

@Serializable
data class CatalogResponseDto(
    @SerialName("updated_at") val updatedAt: String? = null,
    val items: List<CatalogItemDto>,
)
