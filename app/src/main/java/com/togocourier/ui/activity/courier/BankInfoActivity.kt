package com.togocourier.ui.activity.courier

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.BankInfoBean
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_bank_info.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class BankInfoActivity : AppCompatActivity() {
    var bankId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_info)
        getBankInfo()

        signInBtn.setOnClickListener {
            if(signInBtn.text.equals("Remove Bank Account")){
                removeBankInfo()
            }else if(signInBtn.text.equals("Add bank account")){
                var intent = Intent(this@BankInfoActivity,CourierPaymentActivity::class.java)
                startActivity(intent)
            }


        }
        back.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        getBankInfo()
    }


    private fun getBankInfo() {
        if (Constant.isNetworkAvailable(this, main_bank)) {
           progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getBankInfo ,
                    Response.Listener { response ->

                       progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            main_bank.visibility = View.VISIBLE
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                                var gson = Gson()
                                var bankInfo = gson.fromJson(response,BankInfoBean::class.java)
                                setData(bankInfo)
                            } else {
                                accountHolderName.text = "N/A"
                                signInBtn.text  = "Add bank account"
                                acc_number.text = "N/A"

                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }
                                }


                                //Constant.snackbar(main_bank, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()

                            }
                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@BankInfoActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userId", PreferenceConnector.readString(this@BankInfoActivity, PreferenceConnector.USERID,""))
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
        }
    }

    fun setData(bankInfo: BankInfoBean) {
        bankId = bankInfo.result?.bankId.toString()

        if(!bankInfo.result?.accountHolder.equals("")){
            accountHolderName.text = bankInfo.result?.accountHolder
        }else   accountHolderName.text = "N/A"

        var temp:String = bankInfo.result?.bankAccNumber.toString()
        if(!temp.equals("")){
            val output = temp.substring(temp.length - 4)
            acc_number.text = "XXXXXXXX"+output
        }else acc_number.text ="N/A"

        signInBtn.text  = "Remove Bank Account"

    }


    private fun removeBankInfo() {
        if (Constant.isNetworkAvailable(this, main_bank)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deleteBankInfoById,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                signInBtn.text  = "Add bank account"
                                acc_number.text = "N/A"
                                accountHolderName.text = "N/A"

                            } else {


                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        main_bank.visibility = View.GONE
                                        Constant.snackbar(main_bank, message)
                                    }
                                }else{
                                    main_bank.visibility = View.GONE
                                    Constant.snackbar(main_bank, message)
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()

                            }
                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@BankInfoActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("bankId", bankId)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
        }
    }
}
