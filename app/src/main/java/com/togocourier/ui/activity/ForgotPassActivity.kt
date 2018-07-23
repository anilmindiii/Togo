package com.togocourier.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.togocourier.R
import com.togocourier.util.Constant
import com.togocourier.util.Validation
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ForgotPassActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        sendBtn.setOnClickListener {
            if(isValidData()){
                forgotPassword()
            }
        }
        iv_back.setOnClickListener {
            var intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    fun forgotPassword() {
        if (Constant.isNetworkAvailable(this@ForgotPassActivity, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.forgotPassword,
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
                                Constant.snackbar(mainLayout, message)
                                emailTxt.setText("")
                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@ForgotPassActivity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", emailTxt.text.toString())
                    return params
                }
            }

            stringRequest.setRetryPolicy(DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }
    }

    fun isValidData(): Boolean {
        var v = Validation()
        if (v.isEmpty(emailTxt)) {
            Constant.snackbar(mainLayout, "email id can't be empty")
            emailTxt.requestFocus()
            return false
        } else if (!isEmailValid(emailTxt)) {
            Constant.snackbar(mainLayout, "enter valid email")
            emailTxt.requestFocus()
            return false
        }
        return true
    }

    fun isEmailValid(editText: EditText): Boolean {
        var getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }
}
