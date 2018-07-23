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
import com.togocourier.adapter.MyPendingPostAdapter
import com.togocourier.responceBean.MyPostResponce
import com.togocourier.ui.activity.PostDetailsActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_my_post_pending.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MyPostPendingFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MyPostPendingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPostPendingFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mContext: Context? = null
    private var arraylist = ArrayList<String>()
    lateinit var coordinateLay: CoordinatorLayout
    private var gson = Gson()
    private var myPostArrayList: ArrayList<MyPostResponce.UserDataBean>? = null
    private var myPendingPostAdapter: MyPendingPostAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_my_post_pending, container, false)
        if(view != null){
            initializeView(view)
           // getMyNewPost(view)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.setFocusableInTouchMode(true)
        view?.setClickable(true)
        view?.requestFocus()
    }


    override fun onResume() {
        super.onResume()
        if(view != null)  getMyNewPost(view!!)

    }

    private fun initializeView(view: View) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        val layoutManager = LinearLayoutManager(mContext)
        myPostArrayList = ArrayList()
        view.mPnPstReclr.setLayoutManager(layoutManager)
        myPendingPostAdapter = MyPendingPostAdapter(mContext, myPostArrayList as ArrayList<MyPostResponce.UserDataBean>, object : MyOnClick {
            override fun deleteMyPost(postId: String, position: Int) {
            }

            override fun OnClick(id: String,reuestId:String) {

                var intent=Intent(activity, PostDetailsActivity::class.java)
                intent.putExtra("POSTID",id)
                intent.putExtra("FROM",Constant.pendingPost)
                intent.putExtra("REQUESTID",reuestId)
                startActivity(intent)
               // Constant.snackbar(coordinateLay,getString(R.string.development_mode))
            }
        })
        view.mPnPstReclr.setAdapter(myPendingPostAdapter)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyPostPendingFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): MyPostPendingFragment {
            val fragment = MyPostPendingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "pending",
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
                                val myPostResponce = gson.fromJson(response, MyPostResponce::class.java)
                                myPostArrayList?.clear()
                                myPostArrayList?.addAll(myPostResponce.getUserData()!!)
                                myPendingPostAdapter?.notifyDataSetChanged()
                                view.noDataTxt.visibility = View.GONE
                            } else {
                                if(view != null && context != null){
                                    myPostArrayList?.clear()
                                    myPendingPostAdapter?.notifyDataSetChanged()

                                    var userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
                                    if(userType.equals(Constant.COURIOR)){
                                        if(message.equals("Currently you are inactivate user")){
                                            var helper = HelperClass(context!!,activity!!)
                                            helper.inActiveByAdmin("Admin inactive your account",true)
                                        }else{
                                            view!!.noDataTxt.visibility = View.VISIBLE
                                        }
                                    }else{
                                        view!!.noDataTxt.visibility = View.VISIBLE
                                    }
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {error->
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                if(context != null){
                                    var helper = HelperClass(context!!,activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }
                        view.progressBar.visibility = View.GONE
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
}
