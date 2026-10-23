package com.magicmirror.vton.data.repository

import com.magicmirror.vton.data.network.ApiResult
import com.magicmirror.vton.data.network.ApiService
import com.magicmirror.vton.data.network.dto.CatalogItemDto
import com.magicmirror.vton.data.network.safeApiCall

class CatalogRepository(private val api: ApiService) {

    suspend fun getCatalog(category: String? = null): ApiResult<List<CatalogItemDto>> =
        safeApiCall { api.getCatalog(category).items }

    suspend fun getItem(itemId: String): ApiResult<CatalogItemDto> =
        safeApiCall { api.getCatalogItem(itemId) }
}
