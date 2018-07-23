package com.togocourier.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.togocourier.R
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.RemPreferenceConnector
import kotlinx.android.synthetic.main.activity_user_selection.*

class UserSelectionActivity : AppCompatActivity(), View.OnClickListener {
    var cllickId: Int = 0
    var userType = Constant.CUSTOMER
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)
        custmerLay.setOnClickListener(this)
        couriorLay.setOnClickListener(this)
        continueBtn.setOnClickListener(this)
        cllickId = R.id.custmerLay
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.custmerLay -> {
                if (cllickId == R.id.custmerLay) {
                    Toast.makeText(this, "Already choose as a Customer", Toast.LENGTH_SHORT).show()
                } else {
                    cllickId = R.id.custmerLay
                    userType = Constant.CUSTOMER
                    custmerBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_customer_icon))
                    custmerTxt.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    couriorBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_courier_icon))
                    couriorTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                }
            }
            R.id.couriorLay -> {
                if (cllickId == R.id.couriorLay) {
                    Toast.makeText(this, "Already choose as a Courior", Toast.LENGTH_SHORT).show()
                } else {
                    cllickId = R.id.couriorLay
                    userType = Constant.COURIOR
                    couriorBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_courier_icon))
                    couriorTxt.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    custmerBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_customer_icon))
                    custmerTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                }
            }
            R.id.continueBtn -> {
                PreferenceConnector.writeString(this,PreferenceConnector.USERTYPE,userType)

               var userType =  PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")

                if (userType.equals(Constant.CUSTOMER)){
                    var welcomeScreenShow = RemPreferenceConnector.readString(this@UserSelectionActivity, RemPreferenceConnector.WELCOMESCREENSHOW_USER, "")

                    if(!welcomeScreenShow.equals("save_cust")){
                        startActivity(Intent(this@UserSelectionActivity, WelcomeActivity::class.java))
                        //finish()
                    }else{
                        startActivity(Intent(this, SignInActivity::class.java))
                        //finish()
                    }

                }else if(userType.equals(Constant.COURIOR)){
                    var welcomeScreenShow = RemPreferenceConnector.readString(this@UserSelectionActivity, RemPreferenceConnector.WELCOMESCREENSHOW_COURIER, "")

                    if(!welcomeScreenShow.equals("save_cust")){
                        startActivity(Intent(this@UserSelectionActivity, welcomeActivityCourier::class.java))
                        //finish()
                    }else{
                        startActivity(Intent(this, SignInActivity::class.java))
                        //finish()
                    }
                }


            }
        }
    }

}
