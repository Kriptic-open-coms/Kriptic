package com.kriptic.app

import android.app.Application
import com.kriptic.app.identity.IdentityRepository

class KripticApplication : Application() {

    lateinit var identityRepository: IdentityRepository
        private set

    override fun onCreate() {
        super.onCreate()
        identityRepository = IdentityRepository(this)
    }
}
