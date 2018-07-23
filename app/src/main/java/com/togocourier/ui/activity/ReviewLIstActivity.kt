package com.togocourier.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.adapter.ReviewListAdapter
import com.togocourier.responceBean.ReviewListInfo
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_review_list.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ReviewLIstActivity : AppCompatActivity() {

    var reviewlist = ArrayList<ReviewListInfo.ResultBean>()
    var reviewAdapter : ReviewListAdapter ?= null
    var useType:String =""
    var userId:String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_list)

        useType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"");

        if(intent.getStringExtra("userId") != null){
            userId = intent.getStringExtra("userId")
        }



        if(useType.equals(Constant.COURIOR)){
            getReviewList(Constant.getAllReviewForCourier, userId,useType)
        }else{
            no_data_found.text = getString(R.string.no_review_found)
            getReviewList(Constant.getAllReviewForCourier+"?userId="+userId, userId, useType)
        }

        reviewAdapter = ReviewListAdapter(reviewlist)
        recycler_view.adapter = reviewAdapter

        back.setOnClickListener {
            onBackPressed()
        }

    }


    fun getReviewList(getAllReviewType: String, userId: String, useType: String) {
        if (Constant.isNetworkAvailable(this, main)) {
         progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + getAllReviewType ,
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
                                var gson = Gson()
                                var reviewListInfo = gson.fromJson(response, ReviewListInfo::class.java)
                                reviewlist.addAll(reviewListInfo.result!!)
                                reviewAdapter?.notifyDataSetChanged()


                            } else {
                                if(this.useType.equals(Constant.COURIOR)){

                                }else{
                                    no_data_found.text = getString(R.string.no_review_found)
                                }
                                no_data_found.visibility = View.VISIBLE
                                //Constant.snackbar(main, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }                    }) {



                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@ReviewLIstActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
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
