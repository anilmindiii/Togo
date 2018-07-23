package com.togocourier.util

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Toast
import com.togocourier.Interface.MyClickListner
import com.togocourier.Interface.RattingDialogValue
import com.togocourier.R
import com.togocourier.responceBean.RattingResponceBean
import com.togocourier.ui.activity.UserSelectionActivity
import kotlinx.android.synthetic.main.change_password_dialog.*
import kotlinx.android.synthetic.main.ratting_dialog.*

/**
 * Created by mindiii on 20/1/18.
 */
class HelperClass (var mContext:Context,var myActivity:Activity){
    var ratting : String = ""
    var review:String = ""

    fun rattingDialog(rattingResponce: RattingResponceBean?, ratting_Status:String, value: RattingDialogValue) {
        val alertDialog = Dialog(mContext)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.ratting_dialog)
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))



        if(ratting_Status.equals("YES")){
            alertDialog.btn_submit.visibility = View.GONE

            alertDialog.ed_review.setText(rattingResponce?.ratingreview?.review.toString())
            alertDialog.ed_review.isEnabled = false
            alertDialog.tv_review_title.text = "View Review"
            alertDialog.tv_write_review.text = "Your Review"

            when( rattingResponce?.ratingreview?.rating){
                "1" ->{
                    alertDialog.iv_horrible.setImageResource(R.drawable.star_active_ico)
                    alertDialog.tv_horrible.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))

                }
                "2" ->{
                    alertDialog.iv_bad.setImageResource(R.drawable.star_active_ico)
                    alertDialog.tv_bad.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
                }
                "3" ->{
                    alertDialog.iv_average.setImageResource(R.drawable.star_active_ico)
                    alertDialog.tv_average.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
                }
                "4" ->{
                    alertDialog.iv_good.setImageResource(R.drawable.star_active_ico)
                    alertDialog.tv_good.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
                }
                "5" ->{
                    alertDialog.iv_exellent.setImageResource(R.drawable.star_active_ico)
                    alertDialog.tv_exellent.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
                }
            }

        }else if(ratting_Status.equals("NO")){
            allClickMethods(alertDialog)
            alertDialog.ed_review.addTextChangedListener(GenericTextWatcher(alertDialog.ed_review,myActivity))
        }

        alertDialog.btn_submit.setOnClickListener {
            review = alertDialog.ed_review.text.toString()

            if(ratting.equals("")){
                alertDialog.tv_give_ratting.visibility = View.VISIBLE
            }

            if(review.equals("")){
               alertDialog.tv_give_review.visibility = View.VISIBLE
            }else{
                value.getRattingDialogValue(ratting,review)
                alertDialog.dismiss()
            }


        }
        alertDialog.iv_cancal.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun allClickMethods(alertDialog: Dialog) {
        alertDialog.ly_horrible.setOnClickListener {
            horribleClick(alertDialog)
            alertDialog.tv_give_ratting.visibility = View.GONE
        }

        alertDialog.ly_bad.setOnClickListener {
            badClick(alertDialog)
            alertDialog.tv_give_ratting.visibility = View.GONE
        }

        alertDialog.ly_average.setOnClickListener {
            averageClick(alertDialog)
            alertDialog.tv_give_ratting.visibility = View.GONE
        }

        alertDialog.ly_good.setOnClickListener {
            goodClick(alertDialog)
            alertDialog.tv_give_ratting.visibility = View.GONE
        }

        alertDialog.ly_exellent.setOnClickListener {
            exellentClick(alertDialog)
            alertDialog.tv_give_ratting.visibility = View.GONE
        }
    }

    private fun exellentClick(alertDialog: Dialog) {
        alertDialog.iv_horrible.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_horrible.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_bad.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_bad.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_average.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_average.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_good.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_good.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_exellent.setImageResource(R.drawable.star_active_ico)
        alertDialog.tv_exellent.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
        ratting = "5"
    }

    private fun goodClick(alertDialog: Dialog) {
        alertDialog.iv_horrible.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_horrible.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_bad.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_bad.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_average.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_average.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_good.setImageResource(R.drawable.star_active_ico)
        alertDialog.tv_good.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))

        alertDialog.iv_exellent.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_exellent.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))
        ratting = "4"
    }

    private fun averageClick(alertDialog: Dialog) {
        alertDialog.iv_horrible.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_horrible.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_bad.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_bad.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_average.setImageResource(R.drawable.star_active_ico)
        alertDialog.tv_average.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))

        alertDialog.iv_good.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_good.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_exellent.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_exellent.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))
        ratting = "3"
    }

    private fun badClick(alertDialog: Dialog) {
        alertDialog.iv_horrible.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_horrible.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_bad.setImageResource(R.drawable.star_active_ico)
        alertDialog.tv_bad.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))

        alertDialog.iv_average.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_average.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_good.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_good.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_exellent.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_exellent.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))
        ratting = "2"
    }

    private fun horribleClick(alertDialog: Dialog) {
        alertDialog.iv_horrible.setImageResource(R.drawable.star_active_ico)
        alertDialog.tv_horrible.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))

        alertDialog.iv_bad.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_bad.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_average.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_average.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_good.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_good.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))

        alertDialog.iv_exellent.setImageResource(R.drawable.star_inactive_ico)
        alertDialog.tv_exellent.setTextColor(ContextCompat.getColor(mContext, R.color.mdtp_dark_gray))
        ratting = "1"
    }

    /*...................................................................................................*/

    fun changePasswordDialog( myClickListner: MyClickListner) {
        val openDialog = Dialog(mContext)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setCancelable(false);
        openDialog.setContentView(R.layout.change_password_dialog)
        openDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        openDialog.tv_ok.setOnClickListener( {


            var oldPassword :String = openDialog.ed_old_password.text.toString()
            var newPassword :String = openDialog.new_password.text.toString()

            if( isValid(openDialog)){
                myClickListner?.getPassword(oldPassword,newPassword,openDialog)
            }

        })
        openDialog.tv_cancel.setOnClickListener({
            openDialog.dismiss()

        })
        openDialog.show()
    }

    private fun isValid(openDialog: Dialog): Boolean {
        val v = Validation()

        if (!v.isNullValue(openDialog.ed_old_password)) {
            Toast.makeText(mContext,"Enter old password", Toast.LENGTH_SHORT).show()
            openDialog.ed_old_password.requestFocus()
            return false

        }  else if (!v.isNullValue(openDialog.new_password)) {
            Toast.makeText(mContext,"Enter new password", Toast.LENGTH_SHORT).show()
            openDialog.new_password.requestFocus()
            return false

        } else if (!v.isPassword_Valid(openDialog.new_password)) {
            Toast.makeText(mContext,"Password atleast 6 character long", Toast.LENGTH_SHORT).show()
            openDialog.new_password.requestFocus()
            return false
        }
        return true
    }

    fun sessionExpairDialog() {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage("Your current session has expired, please login again")
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            PreferenceConnector.clear(mContext)
            var intent = Intent(myActivity,UserSelectionActivity::class.java)
            myActivity.startActivity(intent)
            myActivity.finish()

        })

        alertDialog.show()

    }




    fun inActiveByAdmin(msg: String,isCallUserSelectionScreen:Boolean) {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            if(isCallUserSelectionScreen){
                PreferenceConnector.clear(mContext)
                var intent = Intent(myActivity,UserSelectionActivity::class.java)
                myActivity.startActivity(intent)
                myActivity.finish()
            }
            else{

                PreferenceConnector.clear(mContext)

            }

            alertDialog.setCancelable(true);

        })

        alertDialog.show()

    }

    class GenericTextWatcher(var view: View,var mContext: Activity) : TextWatcher {
        var isOpen:Boolean = true
        fun textLimiteFull(msg:String) {
            val alertDialog = android.app.AlertDialog.Builder(mContext)
            alertDialog.setTitle("Alert")
            alertDialog.setCancelable(false)
            alertDialog.setMessage(msg)
            alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                alertDialog.setCancelable(true)

            })
            alertDialog.show()

        }
        override fun afterTextChanged(p0: Editable?) {
            var text : String  = p0.toString()
            if(text.length == 100){
                if(isOpen){
                    textLimiteFull(mContext.getString(R.string.review_limite))
                    isOpen = false
                }
                else{
                    isOpen = true
                }
                return
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


    }


}