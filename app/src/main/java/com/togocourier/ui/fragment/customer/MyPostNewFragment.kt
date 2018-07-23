package com.togocourier.ui.fragment.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
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
import com.togocourier.adapter.MyPostNewAdapter
import com.togocourier.responceBean.MyPostResponce
import com.togocourier.ui.activity.PostDetailsActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_my_post_new.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class MyPostNewFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mContext: Context? = null
    lateinit var coordinateLay: CoordinatorLayout
    private var gson = Gson()
    private var myPostArrayList: ArrayList<MyPostResponce.UserDataBean>? = null
    private var myNewPostAdapter: MyPostNewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view = inflater!!.inflate(R.layout.fragment_my_post_new, container, false)
        initializeView(view)
        //if(view != null) getMyNewPost(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.setFocusableInTouchMode(true)
        view?.setClickable(true)
        view?.requestFocus()

        if (view != null) {
            getMyNewPost(view!!)
        }

    }

    private fun initializeView(view: View) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        val layoutManager = LinearLayoutManager(mContext)
        myPostArrayList = ArrayList()
        view.mNewPstReclr.setLayoutManager(layoutManager)
        myNewPostAdapter = MyPostNewAdapter("MyPostNewFragment", mContext, myPostArrayList as ArrayList<MyPostResponce.UserDataBean>, object : MyOnClick {
            override fun deleteMyPost(postId: String, position: Int) {
                //delete Api calling
                deletePost(view, postId, position)
            }

            override fun OnClick(id: String, requestId: String) {
                var intent = Intent(activity, PostDetailsActivity::class.java)
                intent.putExtra("POSTID", id)
                intent.putExtra("FROM", Constant.newPost)
                startActivity(intent)
            }
        })
        view.mNewPstReclr.setAdapter(myNewPostAdapter)
    }

    override fun onAttach(context: Context?) {
        mContext = context
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        if (view != null) {
            getMyNewPost(view!!)
        }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"


        fun newInstance(param1: String, param2: String): MyPostNewFragment {
            val fragment = MyPostNewFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            view.progressBar?.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "new",
                    Response.Listener { response ->
                        view.progressBar?.visibility = View.GONE
                        var result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                myPostArrayList?.clear()

                                val myPostResponce = gson.fromJson(response, MyPostResponce::class.java)
                                myPostArrayList?.addAll(myPostResponce.getUserData()!!)
                                myNewPostAdapter?.notifyDataSetChanged()
                                view!!.noDataTxt.visibility = View.GONE
                            } else {
                                if (context != null) {
                                    var userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                                    if (userType.equals(Constant.COURIOR)) {
                                        if (message.equals("Currently you are inactivate user")) {
                                            var helper = HelperClass(context!!, activity!!)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        } else {
                                            view!!.noDataTxt.visibility = View.VISIBLE
                                        }
                                    } else {
                                        view!!.noDataTxt.visibility = View.VISIBLE
                                    }

                                }

                                myNewPostAdapter?.notifyDataSetChanged()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        view.progressBar?.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400) {
                                if (context != null) {
                                    var helper = HelperClass(context!!, activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(mContext!!).addToRequestQueue(stringRequest)
        }
    }

    fun deletePost(view: View, postId: String, position: Int) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            view.progressBar?.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deletePostById,
                    Response.Listener { response ->
                        view.progressBar?.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                myPostArrayList?.removeAt(position)
                                myNewPostAdapter?.notifyDataSetChanged()
                                getMyNewPost(view!!)

                            } else {

                                var userType = PreferenceConnector.readString(mContext!!, PreferenceConnector.USERTYPE, "")
                                if (userType.equals(Constant.COURIOR)) {
                                    if (message.equals("Currently you are inactivate user")) {
                                        var helper = HelperClass(mContext!!, activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    }
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        view.progressBar?.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400) {
                                if (context != null) {
                                    var helper = HelperClass(context!!, activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(mContext!!).addToRequestQueue(stringRequest)
        }
    }
}
