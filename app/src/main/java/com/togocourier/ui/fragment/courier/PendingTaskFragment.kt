package com.togocourier.ui.fragment.courier

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
import com.togocourier.Interface.MyTaskListOnClick

import com.togocourier.R
import com.togocourier.adapter.MyTAskAdapter
import com.togocourier.responceBean.MyAllTaskResponce
import com.togocourier.ui.activity.PostDetailsActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_pending_task.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PendingTaskFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PendingTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PendingTaskFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mContext: Context? = null
    private var gson= Gson();
    lateinit var coordinateLay: CoordinatorLayout
    private var myTaskList: ArrayList<MyAllTaskResponce.AppliedReqDataBean>? = null
    private var adpter: MyTAskAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater!!.inflate(R.layout.fragment_pending_task, container, false)
        initializeView(view)
       // getMyTaskRequest(view)
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.setFocusableInTouchMode(true)
        view?.setClickable(true)
        view?.requestFocus()


    }

    private fun initializeView(view: View) {
        myTaskList = ArrayList()
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        val layoutManager = LinearLayoutManager(mContext)
        myTaskList = ArrayList()
        view.mPnTaskReclr.setLayoutManager(layoutManager)
        adpter= MyTAskAdapter(mContext,myTaskList,object :MyTaskListOnClick{
            override fun OnClick(postId: String, requestId: String) {
                var intent= Intent(activity, PostDetailsActivity::class.java)
                intent.putExtra("POSTID",postId)
                intent.putExtra("FROM",Constant.pendingTask)
                intent.putExtra("REQUESTID",requestId)
                startActivity(intent)
            }

        })
        view.mPnTaskReclr.adapter=adpter

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext=context
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
         * @return A new instance of fragment PendingTaskFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): PendingTaskFragment {
            val fragment = PendingTaskFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onResume() {
        super.onResume()
        getMyTaskRequest(view!!)
    }



    private fun getMyTaskRequest(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, view.mainLayout)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Get_All_Request_Url,
                    Response.Listener { response ->
                        if(view.progressBar != null){
                            view.progressBar.visibility = View.GONE
                        }

                        println("#" + response)
                        Log.e("PENDINGTASK", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val allrequeatListResponce = gson.fromJson(response, MyAllTaskResponce::class.java)
                                myTaskList?.clear()
                                myTaskList?.addAll(allrequeatListResponce.getAppliedReqData()!!)
                                 adpter?.notifyDataSetChanged()
                            } else {

                                myTaskList?.clear()
                                adpter?.notifyDataSetChanged()

                                var userType = PreferenceConnector.readString(mContext!!,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(mContext!!, activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        view.noDataTxt.visibility=View.VISIBLE
                                    }
                                }else{
                                    view.noDataTxt.visibility=View.VISIBLE
                                }
                              //  Constant.snackbar(view.mainLayout, message)
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
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userType", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERTYPE, ""))
                    params.put("requestStatus", "pending")
                    params.put("postId", "")
                    return params
                }
            }
            stringRequest.setRetryPolicy(DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(mContext!!).addToRequestQueue(stringRequest)
        }
    }
}// Required empty public constructor
