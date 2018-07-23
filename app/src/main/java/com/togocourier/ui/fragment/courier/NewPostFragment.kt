package com.togocourier.ui.fragment.courier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.MyOnClick

import com.togocourier.R
import com.togocourier.adapter.NewPostAdapter
import com.togocourier.responceBean.MyPostResponce
import com.togocourier.service.MyLocationService
import com.togocourier.ui.activity.PostDetailsActivity
import com.togocourier.ui.activity.courier.NotificationActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class NewPostFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mContext: Context? = null
    lateinit var coordinateLay: CoordinatorLayout
    lateinit var tabRightIcon: ImageView
    private var gson = Gson()
    private var myPostArrayList: ArrayList<MyPostResponce.UserDataBean>? = null
    private var NewPostAdapter: NewPostAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater!!.inflate(R.layout.fragment_new_post, container, false)
        initializeView(view)

        //getNewPost(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.setFocusableInTouchMode(true)
        view?.setClickable(true)
        view?.requestFocus()

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    private fun initializeView(view: View) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        tabRightIcon = activity?.findViewById<View>(R.id.tabRightIcon) as ImageView
        tabRightIcon.setOnClickListener(this)
        tabRightIcon.setImageResource(R.drawable.ring_ico)
        tabRightIcon.visibility=View.VISIBLE
        val layoutManager = LinearLayoutManager(mContext)
        myPostArrayList = ArrayList()
        view.nwPstReclr.setLayoutManager(layoutManager)
        NewPostAdapter = NewPostAdapter("NewPostFragment",mContext, myPostArrayList as ArrayList<MyPostResponce.UserDataBean>, object : MyOnClick {
            override fun deleteMyPost(postId: String, position: Int) {}
            override fun OnClick(id: String,requestId : String) {
                var intent = Intent(activity, PostDetailsActivity::class.java)
                intent.putExtra("POSTID", id)
                intent.putExtra("FROM", Constant.newPost)
                startActivity(intent)


            }
        })
        view.nwPstReclr.setAdapter(NewPostAdapter)
        NewPostAdapter?.notifyDataSetChanged()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tabRightIcon -> {
               mContext?.startActivity(Intent(mContext,NotificationActivity::class.java))
            }
        }

    }

    fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager?.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped!!) {
            val transaction = fragmentManager?.beginTransaction()
            //  transaction.setCustomAnimations(R.anim.slide_right_out, R.anim.slide_right_in)
            transaction?.add(containerId, fragment, backStackName)?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction?.addToBackStack(backStackName)
            transaction?.commit()
        }
    }


    override fun onResume() {
        super.onResume()
        getNewPost(view!!)
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): NewPostFragment {
            val fragment = NewPostFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    fun getNewPost(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Get_Courier_AllPost_List_Url
                    ,
                    Response.Listener { response ->
                        view.progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            myPostArrayList?.clear()
                            if (status == "success") {
                                val myPostResponce = gson.fromJson(response, MyPostResponce::class.java)
                                myPostArrayList?.addAll(myPostResponce.getUserData()!!)
                                NewPostAdapter?.notifyDataSetChanged()
                                view.noDataTxt.visibility = View.GONE


                                if(activity != null){
                                    if(myPostResponce.isTrack.equals("YES")){
                                        activity!!.startService(Intent(activity!!, MyLocationService::class.java))
                                    }else if(myPostResponce.isTrack.equals("NO")){
                                        activity!!.stopService(Intent(activity!!, MyLocationService::class.java))
                                    }
                                }

                            } else {
                                NewPostAdapter?.notifyDataSetChanged()
                                var userType = PreferenceConnector.readString(mContext!!,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(mContext!!, activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        view.noDataTxt.visibility = View.VISIBLE
                                    }
                                }else{
                                    view.noDataTxt.visibility = View.VISIBLE
                                }


                                //Constant.snackbar(coordinateLay, message)

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
                    param.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("userId", "0")

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
