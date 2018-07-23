package com.togocourier.ui.fragment.customer

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.MyOnClick

import com.togocourier.R
import com.togocourier.adapter.MyCompletedPostAdapter
import com.togocourier.responceBean.MyPostResponce
import com.togocourier.ui.activity.PostDetailsActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_completed_post.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class CompletedPostFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    lateinit var coordinateLay: CoordinatorLayout
    var myCompletedPostAdapter: MyCompletedPostAdapter ?= null
    var completedList = ArrayList<MyPostResponce.UserDataBean>()
    var userType = "";


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = inflater!!.inflate(R.layout.fragment_completed_post, container, false)

        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
        //getMyCompletedPost(view)
        myCompletedPostAdapter = MyCompletedPostAdapter(context,completedList,object : MyOnClick {
            override fun deleteMyPost(postId: String, position: Int) {}
            override fun OnClick(id: String, requestId: String) {
                var intent = Intent(activity, PostDetailsActivity::class.java)
                intent.putExtra("POSTID", id)
                intent.putExtra("REQUESTID", requestId)
                intent.putExtra("FROM", Constant.MycompletedPost)
                startActivity(intent)
            }

        })
        view.mNewPstReclr.adapter = myCompletedPostAdapter
        return view
    }


    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): CompletedPostFragment {
            val fragment = CompletedPostFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onResume() {
        super.onResume()
        if(view != null){
            getMyCompletedPost(view!!)
        }
    }

    fun getMyCompletedPost(view: View) {
        if (Constant.isNetworkAvailable(context!!, coordinateLay)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "complete",
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
                                val gson = Gson()
                                var mypostResponce = gson.fromJson(response,MyPostResponce::class.java)
                                completedList.clear()
                                completedList.addAll(mypostResponce.getUserData()!!)
                                myCompletedPostAdapter?.notifyDataSetChanged()
                                view.noDataTxt.visibility = View.GONE
                            } else {

                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(context!!,activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        view.noDataTxt.visibility = View.VISIBLE
                                    }
                                }else{
                                    view.noDataTxt.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {error ->
                        view.progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                if(context != null){
                                    var helper = HelperClass(context!!,activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }

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
}