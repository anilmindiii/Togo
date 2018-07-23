package com.togocourier.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.togocourier.R
import com.togocourier.util.Constant.getHashKey
import com.togocourier.util.PreferenceConnector


class SplashActivity : AppCompatActivity() {
    val SPLASH_TIME_OUT = 1500
    val handler = Handler()
    var runnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        /*handler.postDelayed({

        }, SPLASH_TIME_OUT.toLong())*/
        runnable = object : Runnable {
            override fun run() {
                var intent: Intent? = null
                var isLogin=PreferenceConnector.readString(this@SplashActivity, PreferenceConnector.ISLOGIN, "")
                var id=PreferenceConnector.readString(this@SplashActivity, PreferenceConnector.USERID, "")

                if (isLogin.equals("yes")) {
                    intent = Intent(this@SplashActivity, HomeActivity::class.java)
                } else {
                    intent = Intent(this@SplashActivity, UserSelectionActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }
        handler.postDelayed(runnable, SPLASH_TIME_OUT.toLong())

        //getHashKey("com.togocourier",this)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        handler.removeCallbacks(runnable)
    }

}
