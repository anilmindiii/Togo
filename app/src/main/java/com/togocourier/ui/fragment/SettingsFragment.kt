package com.togocourier.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.togocourier.Interface.MyClickListner
import com.togocourier.R
import com.togocourier.ui.activity.UserSelectionActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.title_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import android.app.AlertDialog
import android.content.DialogInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.service.MyLocationService
import com.togocourier.ui.activity.AboutUsActivity
import com.togocourier.ui.activity.TermConditionActivity
import com.togocourier.ui.activity.courier.BankInfoActivity


class SettingsFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_settings, container, false)

        activity?.tabRightIcon?.visibility = View.GONE

        view.ly_change_password.setOnClickListener {
            var socialType = PreferenceConnector.readString(context!!, PreferenceConnector.USERSOCIALTYPE, "")
            var helper = HelperClass(context!!,activity!!)
            if(!socialType.equals("facebook")){
                helper.changePasswordDialog(object : MyClickListner {
                    override fun getPassword(oldPassword: String, newPassword: String, openDialog: Dialog) {
                        var oldPasswordSession = PreferenceConnector.readString(context!!, PreferenceConnector.PASSWORD, "")

                        if(!oldPasswordSession.equals(oldPassword)){
                            Toast.makeText(context!!,"Please enter correct old password",Toast.LENGTH_SHORT).show()
                            return
                        }else if(oldPasswordSession.equals(newPassword)){
                            Toast.makeText(context!!,"Old password and new password should be different",Toast.LENGTH_SHORT).show()
                            return
                        }else{
                            changePassword(view,newPassword,openDialog)
                        }

                    }
                })
            }else{
                val alertDialog = AlertDialog.Builder(context!!)
                alertDialog.setTitle("Alert")
                alertDialog.setCancelable(false)
                alertDialog.setMessage("You can't change your password in social login")
                alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                    alertDialog.setCancelable(true)

                })
                alertDialog.show()
            }
        }

        if(PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "").equals("ON")){
            view.toggle_notifation.setImageResource(R.drawable.on_ico)
        }else if(PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "").equals("OFF")){
            view.toggle_notifation.setImageResource(R.drawable.off_ico)
        }

        view.toggle_notifation.setOnClickListener {
            isNotificationONOFF(view)
        }

        view.ly_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut();
            context!!.stopService(Intent(context!!, MyLocationService::class.java))
            logOut(view)
        }

        var userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
        if(userType.equals(Constant.COURIOR)){
            //view.payment_info.visibility = View.VISIBLE
        }

        view.about_us.setOnClickListener {
            var intent = Intent(context!!,AboutUsActivity::class.java)
            startActivity(intent)
        }

        view.payment_info.setOnClickListener {
            var intent = Intent(context!!,BankInfoActivity::class.java)
            startActivity(intent)
        }

        view.ly_termncondition.setOnClickListener {
            var intent = Intent(context!!,TermConditionActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    private fun changePassword(view: View, newPassword: String, openDialog: Dialog) {
        if (Constant.isNetworkAvailable(context!!, view.main_settings)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.changePassword ,
                    Response.Listener { response ->

                        view.progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                openDialog.dismiss()
                                PreferenceConnector.writeString(context!!, PreferenceConnector.PASSWORD, newPassword)
                                clickToLogOut()

                            } else {
                                var userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(context!!,activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(view.main_settings, message)
                                    }
                                }else{
                                    Constant.snackbar(view.main_settings, message)
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {error->
                        view.progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                if(context != null){
                                    var helper = HelperClass(context!!,activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userId", PreferenceConnector.readString(context!!,PreferenceConnector.USERID,""))
                    params.put("password", newPassword)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)
    }
}

    private fun isNotificationONOFF(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.main_settings)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.updateNotiStatus ,
                    Response.Listener { response ->

                        view.progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            val notificationStatus = result.getString("notificationStatus")
                            if (status == "success") {
                                PreferenceConnector.writeString(context!!, PreferenceConnector.ISNOTIFICATIONON, notificationStatus)

                                if(PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "").equals("ON")){
                                    view.toggle_notifation.setImageResource(R.drawable.on_ico)
                                }else if(PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "").equals("OFF")){
                                    view.toggle_notifation.setImageResource(R.drawable.off_ico)
                                }
                                updateNotifationFCM()

                            } else {
                                var userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(context!!,activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(view.main_settings, message)
                                    }
                                }else{
                                    Constant.snackbar(view.main_settings, message)
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.progressBar.visibility = View.GONE
                        Toast.makeText(activity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)

        }
    }


    private fun updateNotifationFCM() {
        var user  = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")
        user.email = PreferenceConnector.readString(context!!, PreferenceConnector.USEREMAIL, "")
        user.name = PreferenceConnector.readString(context!!, PreferenceConnector.USERFULLNAME, "")
        user.firebaseToken = FirebaseInstanceId.getInstance().token!!
        user.profilePic = PreferenceConnector.readString(context!!, PreferenceConnector.USERPROFILEIMAGE, "")


        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(user.uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                print("Dane")
            }else{
                print("notDane")
            }
        }

    }

    private fun logOut(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.main_settings)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.logOut ,
                    Response.Listener { response ->

                        view.progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                                var myUid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")
                                FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(myUid).child("firebaseToken").setValue("")

                                PreferenceConnector.clear(context!!)

                                val intent = Intent(context, UserSelectionActivity::class.java)
                                startActivity(intent)
                                activity?.finish()

                            } else {
                                Constant.snackbar(view.main_settings, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.progressBar.visibility = View.GONE
                        PreferenceConnector.clear(context!!)
                        val intent = Intent(context, UserSelectionActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                        //Toast.makeText(activity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)

        }
    }

    fun clickToLogOut(){
        val builder1 = AlertDialog.Builder(context!!)
        builder1.setMessage("Password modification will expire your current session")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Ok",
                { dialog, id ->
                    PreferenceConnector.clear(context!!)
                    val intent = Intent(context, UserSelectionActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    dialog.cancel()
                })

        val alert11 = builder1.create()
        alert11.show()
    }

}
