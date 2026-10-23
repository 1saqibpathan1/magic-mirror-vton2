package com.magicmirror.vton.data.network

import com.magicmirror.vton.data.network.dto.CatalogItemDto
import com.magicmirror.vton.data.network.dto.CatalogResponseDto
import com.magicmirror.vton.data.network.dto.TryonStatusResponseDto
import com.magicmirror.vton.data.network.dto.TryonSubmitResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/** Mirrors API_CONTRACT.md at the project root — keep both in sync. */
interface ApiService {

    @GET("api/v1/catalog")
    suspend fun getCatalog(@Query("category") category: String? = null): CatalogResponseDto

    @GET("api/v1/catalog/{itemId}")
    suspend fun getCatalogItem(@Path("itemId") itemId: String): CatalogItemDto

    @Multipart
    @POST("api/v1/tryon")
    suspend fun submitTryon(
        @Part personImage: MultipartBody.Part,
        @Part("garment_id") garmentId: RequestBody,
    ): TryonSubmitResponseDto

    @GET("api/v1/tryon/{jobId}")
    suspend fun getTryonStatus(@Path("jobId") jobId: String): TryonStatusResponseDto
}
