package com.togocourier.util
import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.vanniktech.emoji.EmojiManager



/**
 * Created by chiranjib on 28/11/17.
 */
class MyApplication: Application() {

    /*companion object {

    }*/
    private var activeActivity: Activity? = null
    override fun onCreate() {
        super.onCreate()
        //setupActivityListener()
        EmojiManager.install(GoogleEmojiProvider())
    }

    private fun setupActivityListener() {
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                activeActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                activeActivity = null
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun getActiveActivity(): Activity? {
        return activeActivity
    }


}