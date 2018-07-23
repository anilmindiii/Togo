package com.togocourier.ui.activity.courier

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
import com.togocourier.adapter.NotificationAdapter
import com.togocourier.responceBean.NotificationBean
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_notification.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class NotificationActivity : AppCompatActivity() {

    var notifiactionAdapter  : NotificationAdapter?= null
    var notificationList = ArrayList<NotificationBean.DataBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_notification)
        notificationList()
        tool_bar.visibility = View.VISIBLE
        back.setOnClickListener {
            onBackPressed()
        }
        notifiactionAdapter = NotificationAdapter(this,notificationList)
        recycler_view.adapter = notifiactionAdapter
    }

    private fun notificationList() {
        if (Constant.isNetworkAvailable(this, main_notification)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getNotificationListData,
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
                                val gson = Gson()
                                val notificationBean = gson.fromJson(response, NotificationBean::class.java)
                                notificationList.addAll(notificationBean.data!!)
                                notifiactionAdapter?.notifyDataSetChanged()
                                no_data_found.visibility= View.GONE
                            } else {
                                no_data_found.visibility= View.VISIBLE
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
                        }                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NotificationActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("userId", PreferenceConnector.readString(this@NotificationActivity, PreferenceConnector.USERID, ""))

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
