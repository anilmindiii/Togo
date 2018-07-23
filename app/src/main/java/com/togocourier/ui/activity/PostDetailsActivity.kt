package com.togocourier.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.togocourier.R
import com.togocourier.responceBean.PostDetailsResponce
import com.togocourier.ui.activity.customer.AllRequestActivity
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_post_details.*
import kotlinx.android.synthetic.main.title_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import android.widget.Toast
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.togocourier.Interface.RattingDialogValue
import com.togocourier.responceBean.RattingResponceBean
import com.togocourier.service.MyLocationService
import com.togocourier.ui.activity.courier.CourierPaymentActivity
import com.togocourier.ui.activity.customer.MapActivity
import com.togocourier.util.HelperClass
import com.togocourier.util.Validation
import kotlinx.android.synthetic.main.amount_info_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.full_image_view_dialog.*
import java.text.DecimalFormat


class PostDetailsActivity : AppCompatActivity(), View.OnClickListener,
        LocationListener,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private var userType = ""
    private var postId = ""
    private var postUserId = ""
    private var postStatus = ""
    private val gson = Gson()
    private var height = 0
    private var width = 0
    private var from = ""
    var statusAddBank = ""
    var otherUID = ""
    var data:PostDetailsResponce.PostDataBean = PostDetailsResponce.PostDataBean()
    var DeliveryStatus:String  = ""
    var DeliveryStatusTxt:String  = ""

    private var applyBTnTxt = "Apply"
    private var changeStatusBTnTxt = "Change Status"
    private var waitingReviewBTnTxt = "Waiting For Review"
    private var viewReviewBTnTxt = "View Review"

    //ownerEnd
    private var giveReviewBTnTxt = "Give Review"
    private var requestId = ""

    private var rattingResponce = RattingResponceBean()

    //for location update
    private val TAG = "TOGO"
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    internal var mLocationRequest: LocationRequest = LocationRequest()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var lat: Double? = null
    internal var lng:Double? = null
    internal var lmgr: LocationManager?=null
    private var isGPSEnable: Boolean = false
    private var bidPrice:Double ?= null
    private var commision:Double ?= null


    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)
        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")

        val bundle = intent.extras
        postId = bundle!!.getString("POSTID")
        from = bundle!!.getString("FROM")
        if (from.equals(Constant.pendingTask) || from.equals(Constant.completeTask)) {
            requestId = bundle!!.getString("REQUESTID")
        }
        if(from.equals(Constant.pendingPost) || from.equals(Constant.MycompletedPost)){
            requestId = bundle!!.getString("REQUESTID")
        }
        if (from.equals(Constant.pendingTask)){
            receiver_contact_field.visibility = View.GONE
            receiver_contact_div.visibility = View.GONE
        }

        if(from.equals(Constant.newPost)){
            if(bundle!!.getString("REQUESTID") != null){
                requestId = bundle!!.getString("REQUESTID")
            }
            if(userType.equals(Constant.COURIOR)){
                receiver_contact_field.visibility = View.GONE
                receiver_contact_div.visibility = View.GONE
            }

        }
        Log.e("POSTID: ", postId)
        initializeView(from)


        //getpostDetails(requestId)
        backBtn.setOnClickListener(this)
        tabRightIcon.setOnClickListener(this)
        btnChangeStatus.setOnClickListener(this)
        map_icon.setOnClickListener(this)
        ly_comission.setOnClickListener(this)



        //location update............
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

    }

    private fun initializeView(from: String) {

        val display = windowManager.defaultDisplay
        width = display.width
        height = display.height
        backBtn.visibility = View.VISIBLE
        tabRightIcon.visibility = View.VISIBLE
        titleTxt.text = "Details"
        loadingLay.visibility = View.VISIBLE

        if (userType.equals(Constant.CUSTOMER)) {
            tabRightIcon.setImageResource(R.drawable.group_ico)
            btnChangeStatus.visibility = View.GONE
            reqLay.visibility = View.VISIBLE
            if (from.equals(Constant.pendingPost)) {
                tabRightIcon.setImageResource(R.drawable.chat_icon)
                reqLay.visibility = View.GONE
            }else if(from.equals(Constant.MycompletedPost)){
                currentStatusTxt.text = "Delivered"
                currentStatusLay.visibility = View.VISIBLE
                btnChangeStatus.visibility = View.VISIBLE
                btnChangeStatus.text = viewReviewBTnTxt
                recptLay.visibility = View.VISIBLE
                if(!data?.receiptImage.equals("")){
                    Picasso.with(this)
                            .load(data?.receiptImage)
                            .into(reciptImage, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    reciptImage.visibility = View.VISIBLE
                                    tv_receipt.visibility = View.VISIBLE
                                }

                                override fun onError() {
                                    reciptImage.visibility = View.GONE
                                    tv_receipt.visibility = View.VISIBLE
                                }
                            })         }
                tabRightIcon.setImageResource(R.drawable.chat_icon)
                tabRightIcon.visibility = View.VISIBLE
                reqLay.visibility = View.GONE

            }
        } else if (userType.equals(Constant.COURIOR)) {
            tabRightIcon.setImageResource(R.drawable.chat_icon)
            recptLay.visibility = View.GONE

        }
    }

    /*...........top dialog for amount calculation..............*/
    internal fun amountCalculationDialog(bidPrice:Double,commision:Double) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.amount_info_layout)
        dialog.window!!.setLayout(width * 10 / 11, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialog.admin_commision.text = ""+commision + "%"
        var adminPrice:Double = ((bidPrice * commision) / 100)
        var yourPrice:Double = (bidPrice - adminPrice)
        var totalPrice:Double = (adminPrice + yourPrice)

        val df = DecimalFormat("###.##")


        dialog.your_price.text = "$"+df.format(yourPrice)
        dialog.admin_price.text = "$"+df.format(adminPrice)
        dialog.total_price.text = "$"+df.format(totalPrice)

        dialog.tv_ok.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.backBtn -> {
                finish()
            }
            R.id.tabRightIcon -> {
                if (userType.equals(Constant.CUSTOMER)) {
                    if (from.equals(Constant.pendingPost) || from.equals(Constant.MycompletedPost)) {
                        if(otherUID != null && !otherUID.equals("")){
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.putExtra("otherUID", otherUID)
                            intent.putExtra("title", data?.title.toString())
                            startActivity(intent)
                        }

                    } else {
                        val intent: Intent = Intent(this, AllRequestActivity::class.java)
                        intent.putExtra("POSTID", postId)
                        startActivity(intent)
                    }
                } else {
                    if(otherUID != null && !otherUID.equals("")){
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("otherUID", data?.userId.toString())
                        intent.putExtra("title", data?.title.toString())
                        startActivity(intent)
                    }


                }
            }
            R.id.btnChangeStatus -> {
                var helper = HelperClass(this,this)
                if(userType.equals(Constant.COURIOR)){

                    if(statusAddBank.equals("YES") || statusAddBank.equals("NO")){
                        manageChangeStatusButtonClick()
                    }
                   /* else if(statusAddBank.equals("NO")){
                        var msg = "First you need to add your bank account."
                        onCreateDialogg(msg,"NO")
                    }*/
                    var rattingStatus = data.ratingStatus.toString()
                    if(rattingStatus.equals("YES")){
                        helper.rattingDialog(rattingResponce,"YES",object : RattingDialogValue {
                            override fun getRattingDialogValue(ratting: String, review: String) {
                                saveRatting( data.requestData?.applyUserId.toString(),review,ratting)
                            }

                        })
                        //getReviewRatting(helper)
                    }
                }else if(userType.equals(Constant.CUSTOMER)){
                    //Review Dialog open here

                    var rattingStatus = data.ratingStatus.toString()

                    if(rattingStatus.equals("YES")){
                        helper.rattingDialog(rattingResponce,"YES",object : RattingDialogValue {
                            override fun getRattingDialogValue(ratting: String, review: String) {
                                saveRatting( data.requestData?.applyUserId.toString(),review,ratting)
                            }

                        })
                        // getReviewRatting(helper)
                    }
                    else if(rattingStatus.equals("NO")){
                        helper.rattingDialog(null,rattingStatus,object : RattingDialogValue {
                            override fun getRattingDialogValue(ratting: String, review: String) {
                                saveRatting( data.requestData?.applyUserId.toString(),review,ratting) }
                        })

                    }
                }
            }
            R.id.map_icon ->{
                if(isGpsEnable()){
                    var intent = Intent(this,MapActivity::class.java)
                    intent.putExtra("applyUserId",data.requestData?.applyUserId)
                    intent.putExtra("postId",data.postId)
                    startActivity(intent)
                }
            }
            R.id.ly_comission ->{
                amountCalculationDialog(bidPrice!!, commision!!)
            }

        }
    }

    private fun manageChangeStatusButtonClick() {
        when (btnChangeStatus.text) {
            applyBTnTxt -> {
                if(isGpsEnable()){
                    showPopUpBid()
                }
            }
            changeStatusBTnTxt -> {
                if(data?.requestData?.requestStatus.equals("delivered", ignoreCase = true)){

                }else bottomSheetDialog()

            }
        }
    }

    fun bottomSheetDialog() {

        val dialog = BottomSheetDialog(this@PostDetailsActivity)
        dialog.getWindow().setBackgroundDrawable( ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.bottom_sheet_layout)

        if (data?.requestData?.requestStatus.equals("accept", ignoreCase = true)){
            dialog.ly_pickup.isEnabled = true
            dialog.ly_out_for_delivery.isEnabled = false
            dialog.ly_delivered.isEnabled = false

            // DeliveryStatusTxt = "Item picked"
            // DeliveryStatus = "picked"
        }
        else if(data?.requestData?.requestStatus.equals("picked", ignoreCase = true)){
            dialog.tv_pickup.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
            dialog.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_delivered.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))

            dialog.iv_pickup.visibility = View.VISIBLE
            dialog.iv_out_for_delivery.visibility = View.GONE
            dialog.iv_delivered.visibility = View.GONE

            dialog.ly_pickup.isEnabled = false
            dialog.ly_out_for_delivery.isEnabled = true
            dialog.ly_delivered.isEnabled = false

            //DeliveryStatusTxt = "Out for delivery"
            //DeliveryStatus = "outForDeliver"
        }
        else if(data?.requestData?.requestStatus.equals("outForDeliver", ignoreCase = true)){
            dialog.tv_pickup.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
            dialog.tv_delivered.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))

            dialog.iv_pickup.visibility = View.GONE
            dialog.iv_out_for_delivery.visibility = View.VISIBLE
            dialog.iv_delivered.visibility = View.GONE

            dialog.ly_pickup.isEnabled = false
            dialog.ly_out_for_delivery.isEnabled = false
            dialog.ly_delivered.isEnabled = true

            //DeliveryStatusTxt = "Delivered"
            // DeliveryStatus = "delivered"
        }
        else if(data?.requestData?.requestStatus.equals("delivered", ignoreCase = true)){
            dialog.tv_pickup.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_delivered.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))

            dialog.iv_pickup.visibility = View.GONE
            dialog.iv_out_for_delivery.visibility = View.GONE
            dialog.iv_delivered.visibility = View.VISIBLE

            dialog.ly_pickup.isEnabled = false
            dialog.ly_out_for_delivery.isEnabled = false
            dialog.ly_delivered.isEnabled = false
        }

        dialog.ly_pickup.setOnClickListener {
            dialog.tv_pickup.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
            dialog.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_delivered.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))

            dialog.iv_pickup.visibility = View.VISIBLE
            dialog.iv_out_for_delivery.visibility = View.GONE
            dialog.iv_delivered.visibility = View.GONE

            dialog.ly_pickup.isEnabled = true
            dialog.ly_out_for_delivery.isEnabled = false
            dialog.ly_delivered.isEnabled = false

            DeliveryStatusTxt = "Item picked"
            DeliveryStatus = "picked"

        }

        dialog.ly_out_for_delivery.setOnClickListener {
            dialog.tv_pickup.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
            dialog.tv_delivered.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))

            dialog.iv_pickup.visibility = View.GONE
            dialog.iv_out_for_delivery.visibility = View.VISIBLE
            dialog.iv_delivered.visibility = View.GONE

            dialog.ly_pickup.isEnabled = false
            dialog.ly_out_for_delivery.isEnabled = true
            dialog.ly_delivered.isEnabled = false

            DeliveryStatusTxt = "Out for delivery"
            DeliveryStatus = "outForDeliver"
        }

        dialog.ly_delivered.setOnClickListener {
            dialog.tv_pickup.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this,R.color.mdtp_dark_gray))
            dialog.tv_delivered.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))

            dialog.iv_pickup.visibility = View.GONE
            dialog.iv_out_for_delivery.visibility = View.GONE
            dialog.iv_delivered.visibility = View.VISIBLE

            dialog.ly_pickup.isEnabled = false
            dialog.ly_out_for_delivery.isEnabled = false
            dialog.ly_delivered.isEnabled = true


            DeliveryStatusTxt = "Delivered"
            DeliveryStatus = "delivered"
        }

        dialog.btn_done.setOnClickListener {

            if(!DeliveryStatus.equals("")){

                changeDeliverStatus(DeliveryStatus)
                if(DeliveryStatus.equals("picked")){
                    data?.requestData?.requestStatus  = "picked"
                }else if(DeliveryStatus.equals("outForDeliver")){
                    data?.requestData?.requestStatus  = "outForDeliver"
                }else if(DeliveryStatus.equals("delivered")){
                    data?.requestData?.requestStatus  = "delivered"
                }
                dialog.dismiss()
            }

        }

        dialog.cancel_dialog.setOnClickListener {
            DeliveryStatus = ""
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getpostDetails(requestId: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Get_Post_Details_Url,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETPOSTDATA", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if(status.equals("return")){
                                Constant.returnAlertDialog(this@PostDetailsActivity,message)
                                return@Listener
                            }

                            if (status == "success") {
                                scrollView_main.visibility = View.VISIBLE
                                loadingLay.visibility = View.GONE
                                val postDetailsResponce = gson.fromJson(response, PostDetailsResponce::class.java)
                                statusAddBank = postDetailsResponce.getPostData()?.addBank!!
                                updateUi(postDetailsResponce)

                                if(from.equals(Constant.MycompletedPost)){
                                    if(userType.equals(Constant.CUSTOMER)){
                                        if(data.ratingStatus.toString().equals("YES")){
                                            btnChangeStatus.text = viewReviewBTnTxt
                                        }else{
                                            btnChangeStatus.text = giveReviewBTnTxt
                                        }

                                        currentStatusTxt.text = "Delivered"
                                    }
                                }else if(userType.equals(Constant.CUSTOMER)){
                                    if(data?.requestData?.requestStatus.equals("delivered", ignoreCase = true)){
                                        btnChangeStatus.visibility = View.VISIBLE
                                        currentStatusTxt.text = "Delivered"
                                        if(postDetailsResponce?.getPostData()!!.ratingStatus.equals("YES")){
                                            btnChangeStatus.text = viewReviewBTnTxt
                                        }else{
                                            btnChangeStatus.text = giveReviewBTnTxt
                                        }
                                    }
                                }
                            } else {

                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this, this)
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
                    Response.ErrorListener {error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    params.put("requestId", requestId)
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


    private fun updateUi(postDetailsResponce: PostDetailsResponce?) {
        data = postDetailsResponce?.getPostData()!!
        postId = data?.postId!!
        postUserId = data?.userId!!
//...........for chat
        otherUID = data?.requestData?.applyUserId.toString()

        //for bid price
        if(!TextUtils.isEmpty(bidPrice.toString()) && !TextUtils.isEmpty(data?.requestData?.commision.toString())){
            bidPrice = data?.requestData?.bidPrice?.toDouble()
            commision = data?.requestData?.commision?.toDouble()
        }

        recptLay.setOnClickListener {
            if(!data?.receiptImage.equals("")){
                Picasso.with(this)
                        .load(data?.receiptImage)
                        .into(reciptImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                full_screen_photo_dialog(data?.receiptImage.toString())
                            }
                            override fun onError() {
                                receiptImageNotFound("No receipt image available")
                            }
                        })
            }else{
                receiptImageNotFound("No receipt image available")
            }



        }

        postStatus = data?.postStatus!!
        if (!TextUtils.isEmpty(data?.itemImage)) {
            Picasso.with(productImage.context).load(data?.itemImage).placeholder(R.drawable.splash_logo).into(productImage)
        }
        itemNameTxt.text = data?.title

        if (userType.equals(Constant.CUSTOMER)) {
            itemCountTxt.text = data?.quantity + " Item"
            if(data.requestData?.drop_off_contact != null){
                drop_off_name.text = data.requestData?.drop_off_person
                drop_off_contact.text = data.requestData?.drop_off_ccode +" - "+ data.requestData?.drop_off_contact
            }else{
                drop_off_name.text = "N/A"
                drop_off_contact.text =  "N/A"
            }

            recptLay.visibility = View.VISIBLE
            if(!data?.receiptImage.equals("")){
                Picasso.with(this)
                        .load(data?.receiptImage)
                        .into(reciptImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                reciptImage.visibility = View.VISIBLE
                                tv_receipt.visibility = View.VISIBLE
                            }

                            override fun onError() {
                                reciptImage.visibility = View.GONE
                                tv_receipt.visibility = View.VISIBLE
                            }
                        })
            }


        } else {
            itemCountTxt.text = data?.quantity + " Items"
            ly_drop_person.visibility = View.GONE
            view_0.visibility = View.GONE
            ly_drop_contact.visibility = View.GONE
            view_1.visibility = View.GONE
        }
        collectDateTxt.text = data?.collectiveDate

        /* collectTimeTxt.text = Constant.setTimeFormat(data?.collectiveTime!!) + " - " +
                 Constant.setTimeFormat(Constant.setTimeRange(data?.collectiveDate!! + " " + data.collectiveTime))*/

        collectTimeTxt.text = Constant.setTimeFormat(data?.collectiveTime!!)+ " - " +Constant.setTimeFormat(data?.collectiveToTime.toString()!!)
        deliveryDateTxt.text = data?.deliveryDate

        /* deliveryTimeTxt.text = Constant.setTimeFormat(data?.deliveryTime!!) + " - " +
                 Constant.setTimeFormat(Constant.setTimeRange(data?.deliveryDate!! + " " + data.deliveryTime))*/
        deliveryTimeTxt.text = Constant.setTimeFormat(data?.deliveryTime!!)+ " - " +Constant.setTimeFormat(data?.deliveryToTime.toString()!!)
        pickUpAddrTxt.text = data?.pickupAdrs
        deliveryAddrTxt.text = data?.deliveryAdrs

        if(data.requestData?.bidPrice != null){
            itemPriceTxt.text = "$" + data?.requestData?.bidPrice
        }else{
            itemPriceTxt.text = "$" + data?.price
        }



        if (data?.otherDetails.equals("")) {
            orDetailTxt.text = "N/A"
        } else {
            orDetailTxt.text = data?.otherDetails
        }

        if (data?.description.equals("")) {
            orItmDescTxt.text = "N/A"
        } else {
            orItmDescTxt.text = data?.description
        }

        if(data?.orderNo.equals("")){
            orderNoTxt.text = "N/A"
        }else{
            orderNoTxt.text = data?.orderNo
        }

        if (userType.equals(Constant.CUSTOMER)) {
            reqTxt.text = "" + data?.requestCount!!
            currentStatusTxt.text = data?.postStatus?.substring(0, 1)!!.toUpperCase() +
                    data?.postStatus?.substring(1)!!.toLowerCase()

            applyButtonManageCUSTOMER(data)


        } else {
            if (data.requestData?.requestStatus != null) {
                currentStatusTxt.text = data?.requestData?.requestStatus?.substring(0, 1)!!.toUpperCase() +
                        data?.requestData?.requestStatus?.substring(1)!!.toLowerCase()
                applyButtonManageCourier(data)
            } else {
                currentStatusTxt.text = data?.postStatus?.substring(0, 1)!!.toUpperCase() +
                        data?.postStatus?.substring(1)!!.toLowerCase()
            }


        }

        if(postDetailsResponce?.getPostData()?.requestData?.bidPrice == null){
            currentStatusLay.visibility = View.GONE
            crStView.visibility = View.GONE
        }else if(postDetailsResponce?.getPostData()?.requestData?.requestStatus.equals("pending")){
            currentStatusLay.visibility = View.VISIBLE
            crStView.visibility = View.VISIBLE
            btnChangeStatus.visibility = View.GONE
        }

        if (userType.equals(Constant.CUSTOMER)){
            if (data?.requestData?.equals("pending")!!){
                currentStatusLay.visibility = View.VISIBLE
                crStView.visibility = View.VISIBLE
            }
        }

        /*.............new fiel added here................................*/
        reciver_name.text = data.receiverName
        contact_number.text = data.rcvCountryCode+ " - " + data.receiverContact


        var helper = HelperClass(this,this)
        var rattingStatus = data.ratingStatus.toString()

        if(rattingStatus.equals("YES")){
            getReviewRatting()
        }

    }

    private fun applyButtonManageCUSTOMER(data: PostDetailsResponce.PostDataBean) {
        if (data?.postStatus.equals("new", ignoreCase = true)) {
            currentStatusLay.visibility = View.GONE
            if(from.equals(Constant.MycompletedPost)){
                currentStatusLay.visibility = View.VISIBLE
            }
            crStView.visibility = View.GONE
            recptLay.visibility = View.GONE
        }else if(data?.requestData?.requestStatus.equals("delivered", ignoreCase = true)){
            btnChangeStatus.isEnabled = true
            map_icon.visibility = View.GONE
        }else if(data?.requestData?.requestStatus.equals("picked", ignoreCase = true)){
            currentStatusTxt.text = "Item picked"
            map_icon.visibility = View.VISIBLE
        }
        else if(data?.requestData?.requestStatus.equals("outForDeliver", ignoreCase = true)){
            currentStatusTxt.text = "Out for delivery"
            map_icon.visibility = View.VISIBLE
        }
        else if(data?.requestData?.requestStatus.equals("accept", ignoreCase = true)){
            currentStatusTxt.text = "Accepted"

        }

    }


    private fun applyButtonManageCourier(data: PostDetailsResponce.PostDataBean?) {
        if (data?.requestData?.requestStatus.equals("new", ignoreCase = true)) {
            btnChangeStatus.text = applyBTnTxt
        } else {
            btnChangeStatus.text = changeStatusBTnTxt
        }
        if(data?.requestData?.requestStatus.equals("delivered", ignoreCase = true)){

            if(data?.ratingStatus.equals( "YES")){
                btnChangeStatus.text = viewReviewBTnTxt
            }else{
                btnChangeStatus.text = waitingReviewBTnTxt
            }

            currentStatusTxt.text = "Delivered"
            recptLay.visibility = View.VISIBLE
            if(!data?.receiptImage.equals("")){
                Picasso.with(this)
                        .load(data?.receiptImage)
                        .into(reciptImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                reciptImage.visibility = View.VISIBLE
                                tv_receipt.visibility = View.VISIBLE
                            }

                            override fun onError() {
                                reciptImage.visibility = View.GONE
                                tv_receipt.visibility = View.VISIBLE
                            }
                        })

            }
            visible_receiverInfo()

        }else if(data?.requestData?.requestStatus.equals("picked", ignoreCase = true)){
            recptLay.visibility = View.VISIBLE
            currentStatusTxt.text = "Item picked"
            startService(Intent(this, MyLocationService::class.java))

            if(!data?.receiptImage.equals("")){
                Picasso.with(this)
                        .load(data?.receiptImage)
                        .into(reciptImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                reciptImage.visibility = View.VISIBLE
                                tv_receipt.visibility = View.VISIBLE
                            }

                            override fun onError() {
                                reciptImage.visibility = View.GONE
                                tv_receipt.visibility = View.VISIBLE
                            }
                        })
            }
            visible_receiverInfo()

        }
        else if(data?.requestData?.requestStatus.equals("outForDeliver", ignoreCase = true)){
            recptLay.visibility = View.VISIBLE
            currentStatusTxt.text = "Out for delivery"
            if(!data?.receiptImage.equals("")){
                Picasso.with(this)
                        .load(data?.receiptImage)
                        .into(reciptImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                reciptImage.visibility = View.VISIBLE
                                tv_receipt.visibility = View.VISIBLE
                            }

                            override fun onError() {
                                reciptImage.visibility = View.GONE
                                tv_receipt.visibility = View.VISIBLE
                            }
                        })
            }
            visible_receiverInfo()
        }
        else if(data?.requestData?.requestStatus.equals("accept", ignoreCase = true)){
            recptLay.visibility = View.VISIBLE
            currentStatusTxt.text = "Accepted"
            if(!data?.receiptImage.equals("")){
                Picasso.with(this)
                        .load(data?.receiptImage)
                        .into(reciptImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                reciptImage.visibility = View.VISIBLE
                                tv_receipt.visibility = View.VISIBLE
                            }

                            override fun onError() {
                                reciptImage.visibility = View.GONE
                                tv_receipt.visibility = View.VISIBLE
                            }
                        })
            }

            visible_receiverInfo()
        }

    }

    fun full_screen_photo_dialog(image_url: String) {
        val openDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        openDialog.setContentView(R.layout.full_image_view_dialog)
        if(!image_url.equals("") && image_url != null){
            Picasso.with(this).load(image_url).placeholder(R.drawable.splash_logo).into(openDialog.photo_view)
        }
        openDialog.iv_back.setOnClickListener {
            openDialog.dismiss()
        }

        openDialog.show()
    }


    internal fun showPopUpBid() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.popup_bid)
        dialog.window!!.setLayout(width * 10 / 11, LinearLayout.LayoutParams.WRAP_CONTENT)
        val cancelLay = dialog.findViewById<View>(R.id.cancelLay) as LinearLayout
        val applyBtn = dialog.findViewById<View>(R.id.applyBtn) as Button
        val popProgressBar = dialog.findViewById<View>(R.id.popProgressBar) as ProgressBar
        val popPriceTxt = dialog.findViewById<View>(R.id.popPriceTxt) as EditText
        dialog.setCancelable(true)
        cancelLay.setOnClickListener { dialog.dismiss() }
        applyBtn.setOnClickListener{

            if(isValidBidPrice(popPriceTxt)){
                Constant.hideSoftKeyboard(this)
                doBidRequest(popPriceTxt.text.toString(), popProgressBar, dialog)
            }

        }
        dialog.show()
    }

    fun isValidBidPrice(popPriceTxt: EditText): Boolean {
        val v = Validation()
        if (v.isEmpty(popPriceTxt)) {
            Toast.makeText(this,"Price can't be empty",Toast.LENGTH_SHORT).show()
            popPriceTxt.requestFocus()
            return false
        }else if (v.isValidValue(popPriceTxt)){
            Toast.makeText(this,"Price is not vaild",Toast.LENGTH_SHORT).show()
            popPriceTxt.requestFocus()
            return false

        }
        else if (v.isNotZero(popPriceTxt)) {
            Toast.makeText(this,"Price can't be zero",Toast.LENGTH_SHORT).show()
            popPriceTxt.requestFocus()
            return false
        }
        return true
    }


    private fun doBidRequest(price: String, popProgressBar: ProgressBar, dialog: Dialog) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            popProgressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Send_Request_Url,
                    Response.Listener { response ->
                        popProgressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETPOSTDATA", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if(status.equals("return")){
                                Constant.returnAlertDialog(this@PostDetailsActivity,message)
                                return@Listener
                            }

                            if (status == "success") {
                                dialog.dismiss()
                                val requestId = result.getString("requestId")
                                //getpostDetails(requestId)
                                currentStatusLay.visibility = View.VISIBLE
                                crStView.visibility = View.VISIBLE
                                currentStatusTxt.text = "Pending"
                                btnChangeStatus.visibility = View.GONE
                                //pending status shows and button hide
                                updateLocationToServer(lat.toString(),lng.toString(),progressBar)

                            } else {
                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this, this)
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
                    Response.ErrorListener {error->
                        popProgressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    params.put("postUserId", postUserId)
                    params.put("bidPrice", price)
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

    private fun visible_receiverInfo() {
        receiver_contact_field.visibility = View.VISIBLE
        receiver_contact_div.visibility = View.VISIBLE
        iv_eye_icon.visibility = View.VISIBLE
        ly_comission.visibility = View.VISIBLE
    }


    fun onCreateDialogg(msg: String,status:String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->

            if(status.equals("YES")){
                manageChangeStatusButtonClick()
            }
            else if(status.equals("NO")){
                var intent = Intent(this@PostDetailsActivity,CourierPaymentActivity::class.java)
                startActivity(intent)
            }

        })

        alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        alertDialog.show()

    }

    fun receiptImageNotFound(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        alertDialog.show()

    }

    fun changeDeliverStatus(deliveryStatus:String) {
        if (Constant.isNetworkAvailable(this@PostDetailsActivity, mainLayout)) {
            progressBar?.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.DeliveryStatus,
                    Response.Listener { response ->
                        progressBar?.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if(status.equals("return")){
                                Constant.returnAlertDialog(this@PostDetailsActivity,message)
                                return@Listener
                            }


                            if (status == "success") {
                                currentStatusTxt.text = DeliveryStatusTxt
                                if(result.getString("requestStatus").equals("delivered")){
                                    btnChangeStatus.text = waitingReviewBTnTxt
                                }
                                if(result.getString("requestStatus").equals("picked")){
                                    startService(Intent(this, MyLocationService::class.java))
                                }

                            } else {

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
                    Response.ErrorListener {error->
                        progressBar?.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("courierReqId",data.requestData?.id!! )
                    params.put("deliveryStatus", deliveryStatus)

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

    private fun saveRatting(receiverId:String,review:String,rating:String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.saveReviewRating,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if(status.equals("return")){
                                Constant.returnAlertDialog(this@PostDetailsActivity,message)
                                return@Listener
                            }

                            if (status == "success") {
                                //data.ratingStatus = "YES"
                                getpostDetails(requestId)
                                btnChangeStatus.text = viewReviewBTnTxt
                            } else {
                                Constant.snackbar(mainLayout, message)
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
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("receiverId", receiverId)
                    params.put("postId", postId)
                    params.put("requestId", requestId)

                    params.put("review", review)
                    params.put("rating", rating)
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

    private fun getReviewRatting() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getReviewByPostId,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETPOSTDATA", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                rattingResponce = gson.fromJson(response, RattingResponceBean::class.java)


                            } else {
                                Constant.snackbar(mainLayout, message)
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
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
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


    override fun onResume() {
        super.onResume()
        getpostDetails(requestId)

    }

    // location update.....................................................

    override fun onLocationChanged(p0: Location?) {
        lat = p0?.getLatitude()
        lng = p0?.getLongitude()
        if (lat != null && lng != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        if(lat == null && lng == null){
            startLocationUpdates() }
    }
    override fun onConnectionSuspended(p0: Int) {}
    override fun onConnectionFailed(p0: ConnectionResult) {}
    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect() }
    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect() }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates() }
    protected fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        Constant.ACCESS_FINE_LOCATION)
            } else {
                val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        } else {
            val pendingResult = LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this) }
        Log.d(TAG, "Location update started ..............: ")
    }
    protected fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        Log.d(TAG, "Location update stopped .......................")
    }
    //....................................................................................................

    fun isGpsEnable(): Boolean {
        isGPSEnable = lmgr?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!
        if (!isGPSEnable) {
            val ab = android.support.v7.app.AlertDialog.Builder(this)
            ab.setTitle(R.string.gps_not_enable)
            ab.setMessage(R.string.do_you_want_to_enable)
            ab.setPositiveButton(R.string.settings, DialogInterface.OnClickListener { dialog, which ->
                isGPSEnable = true
                val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(`in`)
            })
            ab.show() }
        return isGPSEnable
    }

    private fun updateLocationToServer(lat: String, lng: String, popProgressBar: ProgressBar) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            popProgressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.updateLatLong,
                    Response.Listener { response ->
                        popProgressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETPOSTDATA", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        popProgressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("latitude", lat)
                    params.put("longitude", lng )

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
}

