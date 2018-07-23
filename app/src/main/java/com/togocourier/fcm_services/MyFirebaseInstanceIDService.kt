package com.togocourier.fcm_services

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by chiranjib on 1/12/17.
 */
class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    private val TAG = "MyFirebaseIIDService"

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.e(TAG, "Refreshed token: " + refreshedToken)
    }
}