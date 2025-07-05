package com.nucleon.iot

import android.app.Application
import com.google.firebase.FirebaseApp
import com.nucleon.iot.utils.PreferenceManager

class NucleonApplication : Application() {

    companion object {
        lateinit var instance: NucleonApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Preferences
        PreferenceManager.init(this)
    }
}