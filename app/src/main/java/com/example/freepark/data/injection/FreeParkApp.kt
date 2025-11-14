package com.example.freepark.data.injection

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.cloudinary.android.MediaManager
import com.example.freepark.BuildConfig

//This generated Hilt component is attached to the Application object's lifecycle and provides dependencies to it.
// Additionally, it is the parent component of the app, which means that other components can access the dependencies that it provides
@HiltAndroidApp
class FreeParkApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = BuildConfig.CLOUDINARY_CLOUD_NAME
        config["api_key"] = BuildConfig.CLOUDINARY_API_KEY
        config["api_secret"] = BuildConfig.CLOUDINARY_API_SECRET
        MediaManager.init(this, config)
    }
}