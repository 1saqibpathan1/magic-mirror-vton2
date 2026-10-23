package com.magicmirror.vton

import android.app.Application

class MagicMirrorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
