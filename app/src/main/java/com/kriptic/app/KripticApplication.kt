package com.kriptic.app

import android.app.Application

class KripticApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application-level initialization (e.g. security providers, Tink, MapLibre context)
        instance = this
    }

    companion object {
        lateinit var instance: KripticApplication
            private set
    }
}
