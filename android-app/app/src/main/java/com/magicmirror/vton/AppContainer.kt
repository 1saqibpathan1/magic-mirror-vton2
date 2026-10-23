package com.magicmirror.vton

import android.content.Context
import com.magicmirror.vton.data.network.NetworkModule
import com.magicmirror.vton.data.repository.CatalogRepository
import com.magicmirror.vton.data.repository.TryonRepository

/**
 * Hand-rolled DI container — deliberately no Hilt/Dagger for a pilot this
 * size. One instance lives on the Application and is handed down through
 * MainActivity into the nav graph.
 */
class AppContainer(context: Context) {

    private val apiService = NetworkModule.createApiService(
        baseUrl = BuildConfig.BASE_URL,
        deviceApiKey = BuildConfig.DEVICE_API_KEY,
    )

    val catalogRepository: CatalogRepository = CatalogRepository(apiService)
    val tryonRepository: TryonRepository = TryonRepository(apiService)
}
