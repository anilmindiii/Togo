package com.togocourier.ui.activity.customer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.AcceptRejectListioner
import com.togocourier.R
import com.togocourier.adapter.AllRequestAdapter
import com.togocourier.responceBean.AllRequestListResponce
import com.togocourier.ui.activity.ReviewLIstActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_all_request.*
import kotlinx.android.synthetic.main.title_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class AllRequestActivity : AppCompatActivity(), View.OnClickListener {

    private var postId = ""
    private var userType = ""
    private var adapter: AllRequestAdapter? = null
    private var allRequestList: ArrayList<AllRequestListResponce.AppliedReqDataBean>? = null
    private var gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_request)
        val bundle = intent.extras
        postId = bundle!!.getString("POSTID")
        userType = PreferenceConnector.readString(this@AllRequestActivity, PreferenceConnector.USERTYPE, "")
        initializeView()
        allRequestList = ArrayList()

        adapter = AllRequestAdapter(this@AllRequestActivity,
                allRequestList as ArrayList<AllRequestListResponce.AppliedReqDataBean>, object : AcceptRejectListioner {
            override fun OnClickUserId(userId: String, postUserId: String?) {
                //addFragment(ProfileFragment.newInstance("AllRequestActivity",userId,postUserId),true,R.id.mainLayout)

                var intent = Intent(this@AllRequestActivity, ReviewLIstActivity::class.java)
                intent.putExtra("useType",1)
                intent.putExtra("userId",userId)
                intent.putExtra("postUserId",postUserId)
                startActivity(intent)

            }

            override fun OnClick(id: String, status: String,bitPrice:String) {

                if(status.equals("cancel")){
                    acceptRejectRequest(id, status)
                }
                else if(status.equals("accept")) {
                    var intent = Intent(this@AllRequestActivity, CustmorPaymentActivity::class.java)
                    intent.putExtra("requestId",id)
                    intent.putExtra("bitPrice",bitPrice)
                    startActivity(intent)
                }
            }
        })
        requestPstReclr.adapter = adapter
        getAllRequest()



    }

    private fun initializeView() {
        backBtn.visibility = View.VISIBLE
        tabRightIcon.visibility = View.GONE
        titleTxt.text = "All Request"
        backBtn.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backBtn -> {
                finish()
            }
        }
    }

    private fun getAllRequest() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Get_All_Request_Url,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        Log.e("GETAllREQUEST", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if(status.equals("return")){
                                Constant.returnAlertDialogToMainActivity(this@AllRequestActivity,message)
                                return@Listener
                            }

                            if (status == "success") {
                                val allrequeatListResponce = gson.fromJson(response, AllRequestListResponce::class.java)
                                allRequestList?.clear()
                                allRequestList?.addAll(allrequeatListResponce.getAppliedReqData()!!)
                                adapter?.notifyDataSetChanged()
                            } else {

                                allRequestList?.clear()
                                adapter?.notifyDataSetChanged()

                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        noDataTxt.visibility=View.VISIBLE
                                    }
                                }else{
                                    noDataTxt.visibility=View.VISIBLE
                                }

                                //Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                /* @Override
            public Map<String, String> getParams() throws AuthFailureError {
                SimpleDateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDate = writeFormat.format(date);
                Map<String,String> header = new HashMap<>();
                header.put("month", todayDate);
                return header;
            }*/

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AllRequestActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userType", userType)
                    params.put("requestStatus", "pending")
                    params.put("postId", postId)
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



    private fun acceptRejectRequest(id: String, status: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Accept_reject_Request_Url,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETAllREQUEST", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if(status.equals("return")){
                                Constant.returnAlertDialogToMainActivity(this@AllRequestActivity,message)
                                return@Listener
                            }

                            if (status == "success") {
                                getAllRequest()
                            }
                            else {

                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(mainLayout, message)
                                    }
                                }else{
                                    Constant.snackbar(mainLayout, message)
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                /* @Override
            public Map<String, String> getParams() throws AuthFailureError {
                SimpleDateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDate = writeFormat.format(date);
                Map<String,String> header = new HashMap<>();
                header.put("month", todayDate);
                return header;
            }*/

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AllRequestActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("requestId", id)
                    params.put("requestStatus", status)
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

    fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) {
            val transaction = supportFragmentManager.beginTransaction()
            //  transaction.setCustomAnimations(R.anim.slide_right_out, R.anim.slide_right_in)
            transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }
    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)

        if (!fragmentPopped) {
            val transaction = supportFragmentManager.beginTransaction()
            // transaction.setCustomAnimations(R.anim.slide_right_out, R.anim.slide_right_in)
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }

    }

}


