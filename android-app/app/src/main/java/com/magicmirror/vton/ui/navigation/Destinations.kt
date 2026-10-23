package com.magicmirror.vton.ui.navigation

object Destinations {
    const val ATTRACT = "attract"
    const val CATALOG = "catalog"
    const val CAPTURE = "capture/{garmentId}"
    const val RESULT = "result/{jobId}"

    fun capture(garmentId: String) = "capture/$garmentId"
    fun result(jobId: String) = "result/$jobId"
}
