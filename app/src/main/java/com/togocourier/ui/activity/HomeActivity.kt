package com.togocourier.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.togocourier.R
import com.togocourier.ui.fragment.*
import com.togocourier.ui.fragment.courier.MyTaskFragment
import com.togocourier.ui.fragment.courier.NewPostFragment
import com.togocourier.ui.fragment.customer.MyPostFragment
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.title_bar.*


class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private var userType = ""
    private var clickedId = 0
    private var fm: FragmentManager? = null
    private var doubleBackToExitPressedOnce = false
    private var runnable: Runnable? = null
    private var fr: Fragment? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initializeView()
        var type  = intent.getStringExtra("type")
        var postId  = intent.getStringExtra("postId")
        var requestId  = intent.getStringExtra("requestId")

        if(type != null){
            if(type.equals("sendrequest")){
                var intent = Intent(this,PostDetailsActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("FROM", Constant.newPost)
                intent.putExtra("REQUESTID","")
                startActivity(intent)

            }else if(type.equals("deliverScreen")){
                var intent = Intent(this,PostDetailsActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("FROM", Constant.pendingPost)
                intent.putExtra("REQUESTID",requestId)
                startActivity(intent)

            }else if(type.equals("updateScreen")){
                var intent = Intent(this,PostDetailsActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("FROM", Constant.pendingTask)
                intent.putExtra("REQUESTID",requestId)
                startActivity(intent)

            }
            else if(type.equals("review") || type.equals("admin")){
                var intent = Intent(this,PostDetailsActivity::class.java)
                intent.putExtra("POSTID", postId)
                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                if(userType.equals(Constant.CUSTOMER)){
                    intent.putExtra("FROM", Constant.completeTask)
                }else{
                    intent.putExtra("FROM", Constant.MycompletedPost)

                }
                intent.putExtra("REQUESTID",requestId)
                startActivity(intent)
            }
        }
    }



    private fun initializeView() {
        fm = supportFragmentManager
        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
        clickedId = firstTabLay.id
        if (userType.equals(Constant.CUSTOMER)) {
            titleTxt.setText(getString(R.string.my_post))
            firstTabTxt.setText(getString(R.string.my_post))
            firstTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))

            secondTabTxt.setText(getString(R.string.notification))
            secondTabImage.setImageResource(R.drawable.inactive_notification_ico)

            replaceFragment(MyPostFragment(), false, R.id.fragmentPlace)

        } else {
            titleTxt.setText(getString(R.string.new_post))
            firstTabTxt.setText(getString(R.string.new_post))
            firstTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))

            secondTabTxt.setText(getString(R.string.my_task))
            secondTabImage.setImageResource(R.drawable.inactive_my_task_ico)

            replaceFragment(NewPostFragment(), false, R.id.fragmentPlace)
        }
        firstTabLay.setOnClickListener(this)
        secondTabLay.setOnClickListener(this)
        thirdTabLay.setOnClickListener(this)
        fourthTabLay.setOnClickListener(this)
        fifthTabLay.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.firstTabLay -> {
                if (clickedId != R.id.firstTabLay) {
                    manageFirstTabClick()
                }
            }
            R.id.secondTabLay -> {
                if (clickedId != R.id.secondTabLay) {
                    manageSecondTabClick()

                }
            }
            R.id.thirdTabLay -> {
                if (clickedId != R.id.thirdTabLay) {
                    manageThirdTabClick()
                    replaceFragment(ChatFragment(), false, R.id.fragmentPlace)
                }
            }
            R.id.fourthTabLay -> {
                if (clickedId != R.id.fourthTabLay) {
                    manageForthTabClick()
                    replaceFragment(ProfileFragment(), false, R.id.fragmentPlace)
                }
            }
            R.id.fifthTabLay -> {
                if (clickedId != R.id.fifthTabLay) {
                    manageFifthTabClick()
                    replaceFragment(SettingsFragment(), false, R.id.fragmentPlace)
                }
            }

        }
    }

    private fun manageFifthTabClick() {
        clickedId = R.id.fifthTabLay
        if (userType.equals(Constant.CUSTOMER)) {
            setCustomerTab()
        } else {
            setCourierTab()
        }
        fifthTabImage.setImageResource(R.drawable.active_setting_ico)
        fifthTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
        titleTxt.setText(getString(R.string.settings))
        replaceFragment(ONDevelopmentFragment(), false, R.id.fragmentPlace)
    }

    private fun manageForthTabClick() {
        clickedId = R.id.fourthTabLay
        if (userType.equals(Constant.CUSTOMER)) {
            setCustomerTab()
        } else {
            setCourierTab()
        }
        fourthTabImage.setImageResource(R.drawable.active_profile_ico)
        fourthTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
        titleTxt.setText(getString(R.string.profile))
        replaceFragment(ONDevelopmentFragment(), false, R.id.fragmentPlace)
    }

    private fun manageThirdTabClick() {
        clickedId = R.id.thirdTabLay
        if (userType.equals(Constant.CUSTOMER)) {
            setCustomerTab()
        } else {
            setCourierTab()

        }
        thirdTabImage.setImageResource(R.drawable.active_chat_ico)
        thirdTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
        titleTxt.setText(getString(R.string.chat))
        replaceFragment(ONDevelopmentFragment(), false, R.id.fragmentPlace)
    }

    private fun manageSecondTabClick() {
        /*var intent = Intent(this,MapActivity::class.java)
        startActivity(intent)*/

        clickedId = R.id.secondTabLay
        if (userType.equals(Constant.CUSTOMER)) {
            setCustomerTab()
            secondTabImage.setImageResource(R.drawable.active_notification_ico)
            titleTxt.setText(getString(R.string.notification))
            secondTabTxt.setText(getString(R.string.notification))
            secondTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
            replaceFragment(NotificationFragment(), false, R.id.fragmentPlace)
        } else {
            setCourierTab()
            secondTabImage.setImageResource(R.drawable.active_my_task_ico)
            secondTabTxt.setText(getString(R.string.my_task))
            titleTxt.setText(getString(R.string.my_task))
            secondTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
            replaceFragment(MyTaskFragment(), false, R.id.fragmentPlace)
        }
    }

    private fun manageFirstTabClick() {
        clickedId = R.id.firstTabLay
        if (userType.equals(Constant.CUSTOMER)) {
            titleTxt.setText(getString(R.string.my_post))
            setCustomerTab()
            firstTabImage.setImageResource(R.drawable.active_my_post_ico)
            firstTabTxt.setText(getString(R.string.my_post))
            firstTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
            replaceFragment(MyPostFragment(), false, R.id.fragmentPlace)
        } else {
            titleTxt.setText(getString(R.string.new_post))
            setCourierTab()
            firstTabImage.setImageResource(R.drawable.active_my_post_ico)
            firstTabTxt.setText(getString(R.string.new_post))
            firstTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
            replaceFragment(NewPostFragment(), false, R.id.fragmentPlace)
        }
    }


    private fun setCustomerTab() {
        firstTabImage.setImageResource(R.drawable.inactive_my_post_ico)
        firstTabTxt.setText(getString(R.string.my_post))
        firstTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        secondTabImage.setImageResource(R.drawable.inactive_notification_ico)
        secondTabTxt.setText(getString(R.string.notification))
        secondTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        thirdTabImage.setImageResource(R.drawable.inactive_chat_ico)
        thirdTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        fourthTabImage.setImageResource(R.drawable.inactive_profile_ico)
        fourthTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        fifthTabImage.setImageResource(R.drawable.inactive_setting_ico)
        fifthTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

    }

    private fun setCourierTab() {
        titleTxt.setText(getString(R.string.new_post))
        firstTabImage.setImageResource(R.drawable.inactive_my_post_ico)
        firstTabTxt.setText(getString(R.string.new_post))
        firstTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        secondTabImage.setImageResource(R.drawable.inactive_my_task_ico)
        secondTabTxt.setText(getString(R.string.my_task))
        secondTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        thirdTabImage.setImageResource(R.drawable.inactive_chat_ico)
        thirdTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        fourthTabImage.setImageResource(R.drawable.inactive_profile_ico)
        fourthTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        fifthTabImage.setImageResource(R.drawable.inactive_setting_ico)
        fifthTabTxt.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        var i = fm?.getBackStackEntryCount()
        if (i != null) {
            while (i > 0) {
                fm?.popBackStackImmediate()
                i--
            }
        }
        if (!fragmentPopped) {
            val transaction = supportFragmentManager.beginTransaction()
            // transaction.setCustomAnimations(R.anim.slide_right_out, R.anim.slide_right_in)
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }

    }

    override fun onBackPressed() {
        try {
            if (fm!!.backStackEntryCount > 0) {
                val backStackEntryCount = fm!!.backStackEntryCount
                val fragment = fm!!.getFragments()[backStackEntryCount - 1]
                fragment?.onResume()
                fm!!.popBackStackImmediate()
            } else {
                val handler = Handler()
                if (!doubleBackToExitPressedOnce) {
                    this.doubleBackToExitPressedOnce = true
                    //Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
                    Constant.snackbar(coordinateLay, "Click again to exit")
                    runnable = object : Runnable {
                        override fun run() {
                            doubleBackToExitPressedOnce = false
                        }
                    }
                    handler.postDelayed(runnable, 2000)
                } else {
                    handler.removeCallbacks(runnable)
                    super.onBackPressed()
                }
            }
        } catch (e: Exception) {
            Log.e("TAG", "onBackPressed: " + e.message.toString())
        }
    }
}
