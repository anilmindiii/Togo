package com.togocourier.ui.activity.customer

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.gson.Gson
import com.togocourier.Interface.getAddressListner
import com.togocourier.R
import com.togocourier.R.id.*
import com.togocourier.adapter.AddressAdapter
import com.togocourier.adapter.DeliveryAddressAdapter
import com.togocourier.responceBean.AddressInfo
import com.togocourier.responceBean.DeliverAddressInfo
import com.togocourier.ui.fragment.customer.MyPostFragment
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.select_address_dialog.*
import kotlinx.android.synthetic.main.title_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddPostActivity : AppCompatActivity(), View.OnClickListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private var firstLay: LinearLayout? = null
    private var secondLay: LinearLayout? = null
    private var picAddress = ""
    private var currentAddress = "Location not found"
    private var picLat = ""
    private var picLng = ""
    private var delAddress = ""
    private var delLat = ""
    private var delLng = ""
    private var addPicBitmap: Bitmap? = null
    private var reciptImgBitmap: Bitmap? = null
    private var fromDate: DatePickerDialog? = null
    private var myTime: TimePickerDialog? = null
    private var now: Calendar? = null
    private var gson = Gson()
    private var colTime = ""
    private var collectiveToTime = ""
    private var delTime = ""
    private var deliveryToTime = ""
    private var ImageType = ""
    var deliveraddList = ArrayList<DeliverAddressInfo.ResultBean>()
    var addressList = ArrayList<AddressInfo.ResultBean>()
    var sethourOfDay = 0
    var setminute = 0
    var setsecond = 0

    var hourOfDay_collective = 0
    var minute_collective = 0

    var hourOfDay_delivery = 0
    var minute_delivery = 0

    var year_live = 0
    var month_live = 0
    var day_live = 0

    //getting location
    private val TAG = "TOGO"
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    internal var mLocationRequest: LocationRequest = LocationRequest()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var lmgr: LocationManager? = null
    private var isGPSEnable: Boolean = false

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        Constant.hideSoftKeyboard(this)
        initializeView()

        postTitleTxt.addTextChangedListener(GenericTextWatcher(postTitleTxt, this))
        anyDelDetailTxt.addTextChangedListener(GenericTextWatcher(anyDelDetailTxt, this))
        itemDescTxt.addTextChangedListener(GenericTextWatcher(itemDescTxt, this))

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun initializeView() {

        now = Calendar.getInstance()
        titleTxt.setText("Add Courier Item")
        backBtn.visibility = View.VISIBLE
        addPic.setOnClickListener(this)
        btnAddPost.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        shptCltDtTxt.setOnClickListener(this)
        shptdelDtTxt.setOnClickListener(this)
        collectTimeLay.setOnClickListener(this)
        delTimeLay.setOnClickListener(this)
        ly_address.setOnClickListener(this)
        ly_delivered_add.setOnClickListener(this)
        ly_to_collective_time.setOnClickListener(this)
        ly_to_delivery_time.setOnClickListener(this)
        ly_image_recipt.setOnClickListener(this)

        firstLay = findViewById(R.id.firstLay)
        secondLay = findViewById(R.id.secondLay)
    }

    fun isGpsEnable(): Boolean {
        isGPSEnable = lmgr!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGPSEnable) {
            val ab = android.support.v7.app.AlertDialog.Builder(this)
            ab.setTitle(R.string.gps_not_enable)
            ab.setMessage(R.string.do_you_want_to_enable)
            ab.setCancelable(false)
            ab.setPositiveButton(R.string.settings, DialogInterface.OnClickListener { dialog, which ->
                isGPSEnable = true
                val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(`in`)
            })
            ab.show()
        }
        return isGPSEnable
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.addPic -> {
                ImageType = "addPic"
                selectImage()
            }
            R.id.shptCltDtTxt -> {
                setDateField(findViewById(R.id.shptCltDtTxt))
            }
            R.id.shptdelDtTxt -> {
                if (!toCollTxt.text.equals("")) {
                    setDateDeleField(findViewById(R.id.shptdelDtTxt))
                } else {
                    Toast.makeText(this, "Please select Shipment Collective time", Toast.LENGTH_LONG).show()

                }

            }
            R.id.collectTimeLay -> {
                if (!TextUtils.isEmpty(shptCltDtTxt.text)) {
                    setCollectiveTimeField(fromCollTxt)
                } else {
                    Toast.makeText(this, "Please select Shipment Collective Date", Toast.LENGTH_LONG).show()
                }
            }
            R.id.delTimeLay -> {
                if (!TextUtils.isEmpty(shptdelDtTxt.text)) {
                    setDeleveryTimeField()
                } else {
                    Toast.makeText(this, "Please select Shipment Delivery Date", Toast.LENGTH_LONG).show()
                }

            }
            R.id.btnAddPost -> {
                if (firstLay?.visibility == View.VISIBLE) {
                    if (isValidFirstStepData()) {
                        firstLay?.visibility = View.GONE
                        secondLay?.visibility = View.VISIBLE
                        btnAddPost.setText("Done")
                    }
                } else if (secondLay?.visibility == View.VISIBLE) {
                    if (isValidNextStepData()) addpost()
                }
            }
            R.id.backBtn -> {
                if (firstLay?.visibility == View.VISIBLE) {
                    finish()
                } else if (secondLay?.visibility == View.VISIBLE) {
                    firstLay?.visibility = View.VISIBLE
                    secondLay?.visibility = View.GONE
                    btnAddPost.setText("Next")
                }
            }
            R.id.ly_address -> {
                if (isGpsEnable())
                    selectAddressDialog(view)
            }
            R.id.ly_delivered_add -> {
                selectDeliverAddDialog(view)
            }
            R.id.ly_to_collective_time -> {
                setCollectiveTime_TO_Field(toCollTxt)
            }
            R.id.ly_to_delivery_time -> {
                setDeleveryTime_TO_Field(toDelTxt)
            }

            R.id.ly_image_recipt -> {
                ImageType = "ly_image_recipt"
                selectImage()
            }

        }
    }


    private fun selectImage() {
        var items: Array<CharSequence> = arrayOf(getString(R.string.text_take_photo), getString(R.string.text_chose_gellery), getString(R.string.text_cancel))
        var alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.text_add_photo))
        alert.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->
            if (items[i].equals(getString(R.string.text_take_photo))) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                    } else {
                        intentToCaptureImage()
                    }
                } else {
                    intentToCaptureImage()
                }
            } else if (items[i].equals(getString(R.string.text_chose_gellery))) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                    } else {
                        var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, Constant.SELECT_FILE)
                    }
                } else {
                    var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.SELECT_FILE)
                }
            } else if (items[i].equals(getString(R.string.text_cancel))) {
                dialogInterface.dismiss()
            }
        })
        alert.show()
    }

    fun intentToCaptureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(takePictureIntent, Constant.REQUEST_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.SELECT_FILE)
                } else {
                    Toast.makeText(this, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show();
                }
            }
            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, Constant.REQUEST_CAMERA)
                } else {
                    Toast.makeText(this, "YOUR  PERMISSION DENIED", Toast.LENGTH_LONG).show();
                }
            }
            Constant.MY_PERMISSIONS_REQUEST_FILE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /*  val intent = Intent()
                      intent.setType("image*//*|application/pdf*//*")
                    intent.setAction(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)*/
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
                } else {
                    Toast.makeText(this, "YOUR  PERMISSION DENIED", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CAMERA) {
                if (data != null) {
                    if (ImageType.equals("addPic")) {
                        addPicBitmap = data.getExtras()!!.get("data") as Bitmap
                        val bytes = ByteArrayOutputStream()
                        addPicBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        addPic.setImageBitmap(addPicBitmap)

                    } else if (ImageType.equals("ly_image_recipt")) {
                        reciptImgBitmap = data.getExtras()!!.get("data") as Bitmap
                        val bytes = ByteArrayOutputStream()
                        reciptImgBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        recptImgTxt.text = "Image uploaded"
                    }
                }
            } else if (requestCode == Constant.SELECT_FILE) {
                if (data != null) {
                    val selectedImageUri = data.data

                    val projection = arrayOf(MediaStore.MediaColumns.DATA)
                    val cursorLoader = CursorLoader(this, selectedImageUri, projection, null, null, null)
                    val cursor = cursorLoader.loadInBackground()
                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    cursor.moveToFirst()
                    val selectedImagePath = cursor.getString(column_index)

                    if (ImageType.equals("addPic")) {
                        addPicBitmap = ImageUtil.decodeFile(selectedImagePath)
                        try {
                            addPicBitmap = ImageUtil.modifyOrientation(addPicBitmap!!, selectedImagePath)
                            val bytes = ByteArrayOutputStream()
                            addPicBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                            addPic.setImageBitmap(addPicBitmap)

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else if (ImageType.equals("ly_image_recipt")) {
                        reciptImgBitmap = ImageUtil.decodeFile(selectedImagePath)
                        try {
                            reciptImgBitmap = ImageUtil.modifyOrientation(reciptImgBitmap!!, selectedImagePath)
                            val bytes = ByteArrayOutputStream()
                            reciptImgBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                            recptImgTxt.text = "Image uploaded"

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

            }

        }
    }

    fun isValidFirstStepData(): Boolean {
        val v = Validation()

        if (v.isEmpty(postTitleTxt)) {
            Constant.snackbar(mainLayout, "Title can't be empty")
            postTitleTxt.requestFocus()
            return false
        } else if (v.isEmpty(post_receiver_name)) {
            Constant.snackbar(mainLayout, "Receiver name can't be empty")
            postTitleTxt.requestFocus()
            return false
        } else if (v.isEmpty(contactTxt)) {
            Constant.snackbar(mainLayout, "Contact number can't be empty")
            postTitleTxt.requestFocus()
            return false
        } else if (!isContactNoValid(contactTxt)) {
            Constant.snackbar(mainLayout, "Enter valid contact no")
            contactTxt.requestFocus()
            return false
        } else if (v.isEmpty(shptCltDtTxt)) {
            Constant.snackbar(mainLayout, "Please select shipment collectiove date")
            return false
        } else if (v.isEmpty(fromCollTxt)) {
            Constant.snackbar(mainLayout, "Please select shipment collectiove time")
            return false
        } else if (v.isEmpty(shptdelDtTxt)) {
            Constant.snackbar(mainLayout, "Please select shipment delivery date")
            return false
        } else if (v.isEmpty(fromDelTxt)) {
            Constant.snackbar(mainLayout, "Please select shipment delivery time")
            return false
        } /*else if (!Constant.checkDeliveryDateTimeGrater(shptCltDtTxt.text.toString() + " " + colTime, shptdelDtTxt.text.toString() + " " + delTime)) {
            Constant.snackbar(mainLayout, "Collective time and delivery time should be  three hours differ")
            return false


        }*/ else if (v.isEmpty(toDelTxt)) {
            Constant.snackbar(mainLayout, "Please select shipment delivery time")
            return false
        } else if (v.isEmpty(quantityTxt)) {
            Constant.snackbar(mainLayout, "Quantity can't be empty")
            quantityTxt.requestFocus()
            return false

        } else if (v.isNotZero(quantityTxt)) {
            Constant.snackbar(mainLayout, "Quantity can't be zero")
            quantityTxt.requestFocus()
            return false
        } else if (v.isEmpty(priceTxt)) {
            Constant.snackbar(mainLayout, "Price can't be empty")
            priceTxt.requestFocus()
            return false
        } else if (v.isValidValue(priceTxt)) {
            Toast.makeText(this, "Price is not vaild", Toast.LENGTH_SHORT).show()
            priceTxt.requestFocus()
            return false

        } else if (v.isNotZero(priceTxt)) {
            Constant.snackbar(mainLayout, "Price can't be zero")
            priceTxt.requestFocus()
            return false
        } else if (v.isEmpty(pickAddTxt)) {
            Constant.snackbar(mainLayout, "Pickup Address can't be empty")
            return false
        }
        return true
    }


    private fun isContactNoValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }
        return getValue.length > 6 && getValue.length < 12
    }

    fun isValidNextStepData(): Boolean {
        val v = Validation()
        if (v.isEmpty(dellAddTxt)) {
            Constant.snackbar(mainLayout, "Delivery Address can't be empty")
            return false
        }/* else if (v.isEmpty(anyDelDetailTxt)) {
            Constant.snackbar(mainLayout, "Delivery details can't be empty")
            anyDelDetailTxt.requestFocus()
            return false
        } else if (v.isEmpty(itemDescTxt)) {
            Constant.snackbar(mainLayout, "Item description can't be empty")
            itemDescTxt.requestFocus()
            return false
        }*/
        return true
    }

    private fun addpost() {
        btnAddPost.isEnabled = false
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.Add_Post_Url, Response.Listener { response ->
                val resultResponse = String(response.data)
                progressBar.visibility = View.GONE
                Log.e("TAG", "onResponse: " + resultResponse)
                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    if (status == "success") {
                        MyPostFragment.isAddPost = true
                        finish()
                    } else {

                        var userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                        if (userType.equals(Constant.COURIOR)) {
                            if (message.equals("Currently you are inactivate user")) {
                                var helper = HelperClass(this, this)
                                helper.inActiveByAdmin("Admin inactive your account", true)
                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } else {
                            Constant.snackbar(mainLayout, message)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                btnAddPost.isEnabled = true
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                progressBar.visibility = View.GONE
                var errorMessage = "Unknown error"
                btnAddPost.isEnabled = true
                //  progressBar.setVisibility(View.GONE);
                if (networkResponse == null) {
                    if (error.javaClass == TimeoutError::class.java) {
                        errorMessage = "Request timeout"
                    } else if (error.javaClass == NoConnectionError::class.java) {
                        errorMessage = "Failed to connect server"
                    }
                } else {
                    val result = String(networkResponse.data)
                    try {
                        val response = JSONObject(result)
                        val status = response.getString("status")
                        val message = response.getString("message")

                        Log.e("Error Status", "" + status)
                        Log.e("Error Message", message)

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found"
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again"
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs"
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong"
                        }
                        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                Log.i("Error", errorMessage)
                error.printStackTrace()
            }) {

                // params.put("profileImage", new DataPart("profileImage.jpg", AppHelper.fullyReadFileToBytes(new File(Uri.parse(imagePath).getPath())), "image/jpg"));
                // params.put("uploadIdCard", new DataPart("id_card", AppHelper.fullyReadFileToBytes(new File(Uri.parse(cardPath).getPath())), "image/jpg"));


                /* @Override
                 public Map<String, String> getHeaders() throws AuthFailureError {
                     Map<String, String> header = new HashMap<>();
                     header.put("authToken", PreferenceConnector.readString(SignUpActivity.this, PreferenceConnector.AUTHTOKEN, ""));
                     return header;
                 }*/

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("authToken", PreferenceConnector.readString(this@AddPostActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return params
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("title", postTitleTxt.text.toString())
                    params.put("description", itemDescTxt.text.toString())
                    params.put("pickupAdrs", pickAddTxt.text.toString())
                    params.put("pickupLat", picLat)
                    params.put("pickupLong", picLng)
                    params.put("deliveryAdrs", dellAddTxt.text.toString())
                    params.put("deliverLat", delLat)
                    params.put("deliverLong", delLng)
                    params.put("collectiveDate", shptCltDtTxt.text.toString())
                    params.put("deliveryDate", shptdelDtTxt.text.toString())
                    params.put("collectiveTime", colTime)
                    params.put("deliveryTime", delTime)
                    params.put("quantity", quantityTxt.text.toString())
                    params.put("price", priceTxt.text.toString())
                    params.put("otherDetails", anyDelDetailTxt.text.toString())
                    params.put("orderNo", "")
                    params.put("receiptImage", "")

                    params.put("receiverContact", contactTxt.text.toString())
                    params.put("receiverName", post_receiver_name.text.toString())
                    params.put("rcvCountryCode", "+1")

                    params.put("collectiveToTime", collectiveToTime)
                    params.put("deliveryToTime", deliveryToTime)
                    params.put("orderNo", orderNoTxt.text.toString().trim())


                    if (addPicBitmap == null) {
                        params.put("itemImage", "")
                    }
                    return params
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (addPicBitmap != null) {
                            params.put("itemImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(addPicBitmap!!), "image/jpg"))
                        }
                        if (reciptImgBitmap != null) {
                            params.put("receiptImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(reciptImgBitmap!!), "image/jpg"))
                        }
                        return params
                    }
            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)
        }
    }

    private fun setDateField(view: TextView) {
        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            var date = year.toString() + "-" + (monthOfYear + 1).toString() + "-" + dayOfMonth
            var month1 = if (monthOfYear + 1 < 10) "0" + (monthOfYear + 1) else "" + (monthOfYear + 1)
            var day1 = if (dayOfMonth < 10) "0" + dayOfMonth else "" + dayOfMonth
            //var date1 = year.toString() + "-" + month1 + "-" + day1
            var date1 = month1 + "/" + day1 + "/" + year.toString()

            view.text = date1

            fromCollTxt.text = ""
            toCollTxt.text = ""

            shptdelDtTxt.text = ""
            fromDelTxt.text = ""
            toDelTxt.text = ""

            year_live = year
            month_live = monthOfYear
            day_live = day1.toInt()

        }, now!!.get(Calendar.YEAR), now!!.get(Calendar.MONTH), now!!.get(Calendar.DAY_OF_MONTH))
        fromDate?.setMinDate(Calendar.getInstance())
        fromDate?.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fromDate?.show(this.getFragmentManager(), "")
        fromDate?.setOnCancelListener(DialogInterface.OnCancelListener {
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })

    }

    private fun setDateDeleField(view: TextView) {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        try {
            cal.time = sdf.parse(shptCltDtTxt.text.toString())

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            var date = year.toString() + "-" + (monthOfYear + 1).toString() + "-" + dayOfMonth

            var month1 = if (monthOfYear + 1 < 10) "0" + (monthOfYear + 1) else "" + (monthOfYear + 1)
            var day1 = if (dayOfMonth < 10) "0" + dayOfMonth else "" + dayOfMonth
            // var date1 = year.toString() + "-" + month1 + "-" + day1
            var date1 = month1 + "/" + day1 + "/" + year.toString()
            view.text = date1

            fromDelTxt.text = ""
            toDelTxt.text = ""
        }, cal!!.get(Calendar.YEAR), cal!!.get(Calendar.MONTH), cal!!.get(Calendar.DAY_OF_MONTH))



        cal.set(year_live, month_live, day_live)

        fromDate?.setMinDate(cal)
        fromDate?.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fromDate?.show(this.getFragmentManager(), "")
        fromDate?.setOnCancelListener(DialogInterface.OnCancelListener {
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })

    }

    private fun setCollectiveTimeField(textview: TextView) {
        val c = Calendar.getInstance()
        val curr_seconds = c.get(Calendar.SECOND)
        val curr_minutes = c.get(Calendar.MINUTE)
        val curr_hour = c.get(Calendar.HOUR)

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            hourOfDay_collective = hourOfDay
            minute_collective = minute

            val hourString: String
            if (hourOfDay < 10)
                hourString = "0" + hourOfDay
            else
                hourString = "" + hourOfDay

            val minuteSting: String
            if (minute < 10)
                minuteSting = "0" + minute
            else
                minuteSting = "" + minute

            colTime = "" + hourString + ":" + minuteSting
            textview.text = Constant.setTimeFormat(colTime)

            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {

                cal.time = sdf.parse(shptCltDtTxt.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 1)
                val (hourString: String, minuteSting: String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

                sethourOfDay = hourString.toInt()
                setminute = minuteSting.toInt()
                setsecond = 0
                collectiveToTime = "" + hourString + ":" + minuteSting
                toCollTxt.text = Constant.setTimeFormat("" + hourString + ":" + minuteSting)


                shptdelDtTxt.text = ""
                fromDelTxt.text = ""
                toDelTxt.text = ""
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)

        myTime!!.show(this.getFragmentManager(), "")
        //   myTime.is24HourMode();
        myTime!!.setMinTime(curr_hour, curr_minutes, curr_seconds)
        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }

    private fun setCollectiveTime_TO_Field(textview: TextView) {
        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            if (toCollTxt.text.equals("")) {
                hourOfDay_collective = hourOfDay
                minute_collective = minute
            }

            val hourString: String
            if (hourOfDay < 10)
                hourString = "0" + hourOfDay
            else
                hourString = "" + hourOfDay

            val minuteSting: String
            if (minute < 10)
                minuteSting = "0" + minute
            else
                minuteSting = "" + minute

            collectiveToTime = "" + hourString + ":" + minuteSting
            if (!collectiveToTime.equals("")) {
                textview.text = Constant.setTimeFormat(collectiveToTime)
            } else {
                textview.text = Constant.setTimeFormat(colTime)
            }


            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {

                cal.time = sdf.parse(shptCltDtTxt.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 0)
                val (hourString: String, minuteSting: String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

                sethourOfDay = hourString.toInt()
                setminute = minuteSting.toInt()
                setsecond = 0

                toCollTxt.text = Constant.setTimeFormat("" + hourString + ":" + minuteSting)

                shptdelDtTxt.text = ""
                fromDelTxt.text = ""
                toDelTxt.text = ""
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)

        myTime!!.show(this.getFragmentManager(), "")
        myTime!!.setMinTime(hourOfDay_collective, minute_collective + 1, 0)
        //   myTime.is24HourMode();
        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }

    private fun setDeleveryTimeField() {

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            val orTime = "" + hourOfDay + ":" + minute
            val depTime = "" + hourOfDay + ":" + minute
            val (hourString: String, minuteSting: String) = timeFormat(hourOfDay, minute)
            if (toDelTxt.text.equals("")) {
                hourOfDay_delivery = hourOfDay
                minute_delivery = minute
            }

            delTime = "" + hourString + ":" + minuteSting
            fromDelTxt.text = Constant.setTimeFormat(delTime)

            val cal = Calendar.getInstance()
            //val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {

                cal.time = sdf.parse(shptdelDtTxt.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 1)

                hourOfDay_delivery = hourOfDay
                minute_delivery = minute

                val (hourString: String, minuteSting: String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                var temp = Constant.setTimeFormat("" + hourString + ":" + minuteSting)
                toDelTxt.text = temp
                deliveryToTime = "" + hourString + ":" + minuteSting


            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)
        myTime!!.show(this.getFragmentManager(), "")
        //myTime!!.is24HourMode();

        if (shptCltDtTxt.text.toString().equals(shptdelDtTxt.text.toString())) {
            myTime!!.setMinTime(sethourOfDay, setminute, setsecond)
        }

        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }

    private fun setDeleveryTime_TO_Field(textview: TextView) {
        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            val orTime = "" + hourOfDay + ":" + minute
            val depTime = "" + hourOfDay + ":" + minute
            val (hourString: String, minuteSting: String) = timeFormat(hourOfDay, minute)

            deliveryToTime = "" + hourString + ":" + minuteSting
            if (!deliveryToTime.equals("")) {
                textview.text = Constant.setTimeFormat(deliveryToTime)
            } else {
                textview.text = Constant.setTimeFormat(delTime)
            }


            val cal = Calendar.getInstance()
            // val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {
                cal.time = sdf.parse(shptdelDtTxt.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 0)

                val (hourString: String, minuteSting: String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                var temp = Constant.setTimeFormat("" + hourString + ":" + minuteSting)
                toDelTxt.text = temp
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)
        myTime!!.show(this.getFragmentManager(), "")
        //myTime!!.is24HourMode();
        myTime!!.setMinTime(hourOfDay_delivery, minute_delivery + 1, setsecond)
        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }

    private fun timeFormat(hourOfDay: Int, minute: Int): Pair<String, String> {
        val hourString: String
        if (hourOfDay < 10)
            hourString = "0" + hourOfDay
        else
            hourString = "" + hourOfDay

        val minuteSting: String
        if (minute < 10)
            minuteSting = "0" + minute
        else
            minuteSting = "" + minute
        return Pair(hourString, minuteSting)
    }

    fun selectAddressDialog(view: View) {
        var placePickAddFragment: PlaceAutocompleteFragment? = null

        val openDialog = Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setContentView(R.layout.select_address_dialog)
        openDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!TextUtils.isEmpty(currentAddress)) {
            openDialog.tv_current_address.text = currentAddress
        }

        openDialog.curr_loc_layout.setOnClickListener {

            /* if(!openDialog.tv_current_address.text.equals("Location not found")){
                 openDialog.dismiss()
                 return@setOnClickListener
             }*/

            if(!TextUtils.isEmpty(picLat) && !TextUtils.isEmpty(picLng) && !openDialog.tv_current_address.text.equals("Location not found")){
                var city = getFullAddress(picLat.toDouble(), picLng.toDouble())

                if (city.contains(Constant.Manhattan)) {
                    pickAddTxt.text = currentAddress
                    openDialog.dismiss()
                } else if (city.contains(Constant.Bronx)) {
                    pickAddTxt.text = currentAddress
                    openDialog.dismiss()
                } else if (city.contains(Constant.Queens)) {
                    pickAddTxt.text = currentAddress
                    openDialog.dismiss()
                } else if (city.contains(Constant.Brooklyn)) {
                    pickAddTxt.text = currentAddress
                    openDialog.dismiss()
                } else {
                    val alertDialog = android.app.AlertDialog.Builder(this@AddPostActivity)
                    alertDialog.setTitle("Alert")
                    alertDialog.setCancelable(false)
                    alertDialog.setMessage("Service are not available for this address")
                    alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                        alertDialog.setCancelable(true)
                    })
                    alertDialog.show()
                }
            }
        }

        openDialog.radio_btn_current_location.isChecked = false
        if (pickAddTxt.text.toString().trim().equals(currentAddress)) {
            openDialog.radio_btn_current_location.isChecked = true
        } else openDialog.radio_btn_current_location.isChecked = false

        var autocompleteFilter: AutocompleteFilter = AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .setCountry("US")
                .build()

        var adAdapter = AddressAdapter(picAddress, addressList, object : getAddressListner {
            override fun getAddress(address: String, pickupLat: String, pickupLong: String) {
                pickAddTxt.text = address
                picAddress = address
                picLat = pickupLat
                picLng = pickupLong

                openDialog.radio_btn_current_location.isChecked = false
                openDialog.dismiss()
            }

        })
        openDialog.recycler_view_address.adapter = adAdapter

        if (addressList.size == 0) {
            getAddressApiPickUp(view, openDialog, adAdapter, addressList)
        }

        placePickAddFragment = fragmentManager.findFragmentById(R.id.placePickAddFragment) as PlaceAutocompleteFragment

        placePickAddFragment.setFilter(autocompleteFilter)
        placePickAddFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                picAddress = place.address.toString()

                var city = getFullAddress(place.latLng.latitude, place.latLng.longitude)

                if (city.contains(Constant.Manhattan)) {
                    pickAddTxt.text = place.address
                    picLat = place.latLng.latitude.toString()
                    picLng = place.latLng.longitude.toString()
                    openDialog.dismiss()
                } else if (city.contains(Constant.Bronx)) {
                    pickAddTxt.text = place.address
                    picLat = place.latLng.latitude.toString()
                    picLng = place.latLng.longitude.toString()
                    openDialog.dismiss()
                } else if (city.contains(Constant.Queens)) {
                    pickAddTxt.text = place.address
                    picLat = place.latLng.latitude.toString()
                    picLng = place.latLng.longitude.toString()
                    openDialog.dismiss()
                } else if (city.contains(Constant.Brooklyn)) {
                    pickAddTxt.text = place.address
                    picLat = place.latLng.latitude.toString()
                    picLng = place.latLng.longitude.toString()
                    openDialog.dismiss()
                } else {
                    val alertDialog = android.app.AlertDialog.Builder(this@AddPostActivity)
                    alertDialog.setTitle("Alert")
                    alertDialog.setCancelable(false)
                    alertDialog.setMessage("Service are not available for this address")
                    alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                        alertDialog.setCancelable(true)
                    })
                    alertDialog.show()
                    openDialog.dismiss()
                }

            }

            override fun onError(status: Status) {

            }
        })

        openDialog.cancel_action.setOnClickListener {
            openDialog.dismiss()

        }
        openDialog.setOnDismissListener {
            var fm = placePickAddFragment?.fragmentManager
            val fragment = fm?.findFragmentById(R.id.placePickAddFragment) as PlaceAutocompleteFragment
            val ft = fm.beginTransaction()
            ft.remove(fragment)
            ft.commit()
        }


        openDialog.show()
    }

    fun selectDeliverAddDialog(view: View) {
        var placePickAddFragment: PlaceAutocompleteFragment? = null

        val openDialog = Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setContentView(R.layout.select_address_dialog)
        openDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        openDialog.title_dialog.text = "Delivery address"
        openDialog.current_loc_view.visibility = View.GONE

        var adAdapter = DeliveryAddressAdapter(delAddress, deliveraddList, object : getAddressListner {
            override fun getAddress(address: String, pickupLat: String, pickupLong: String) {
                dellAddTxt.text = address
                delAddress = address

                delLat = pickupLat
                delLng = pickupLong
                openDialog.radio_btn_current_location.isChecked = false
                openDialog.dismiss()
            }

        })
        openDialog.recycler_view_address.adapter = adAdapter
        if (deliveraddList.size == 0) {
            getAddressApiDeliver(view, openDialog, adAdapter, deliveraddList)
        }

        placePickAddFragment = fragmentManager.findFragmentById(R.id.placePickAddFragment) as PlaceAutocompleteFragment
        placePickAddFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                dellAddTxt.text = place.address
                delAddress = place.address.toString()
                delLat = place.latLng.latitude.toString()
                delLng = place.latLng.longitude.toString()

                openDialog.dismiss()

            }

            override fun onError(status: Status) {

            }
        })

        openDialog.cancel_action.setOnClickListener {
            openDialog.dismiss()

        }

        openDialog.setOnDismissListener {
            var fm = placePickAddFragment?.fragmentManager
            val fragment = fm?.findFragmentById(R.id.placePickAddFragment) as PlaceAutocompleteFragment
            val ft = fm.beginTransaction()
            ft.remove(fragment)
            ft.commit()
        }


        openDialog.show()
    }

    private fun getFullAddress(latitude: Double, longitude: Double): String {
        var geocoder = Geocoder(this@AddPostActivity, Locale.getDefault());

        var addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1);

        var city = addresses.get(0).getLocality();
        var state = addresses.get(0).getAdminArea();
        var zip = addresses.get(0).getPostalCode();
        var country = addresses.get(0).getCountryName();
        var adminArea = addresses.get(0).adminArea
        var s = addresses.get(0).locale
        var subLocal = addresses.get(0).subLocality
        var abc = addresses.get(0).featureName

        if (city == null) {
            city = ""
        }

        if (subLocal == null) {
            return city
        } else {
            return subLocal
        }


    }

    private fun getAddressApiPickUp(view: View, openDialog: Dialog, adAdapter: AddressAdapter, addressList: ArrayList<AddressInfo.ResultBean>) {
        if (Constant.isNetworkAvailable(this!!, view)) {
            openDialog.progressBar_dialog.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getPreviousPickupAddress,
                    Response.Listener { response ->

                        openDialog.progressBar_dialog.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                var adInfo = gson.fromJson(response, AddressInfo::class.java)

                                addressList.addAll(adInfo.result!!)
                                adAdapter!!.notifyDataSetChanged()

                            } else {
                                var userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType.equals(Constant.COURIOR)) {
                                    if (message.equals("Currently you are inactivate user")) {
                                        var helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        openDialog.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    openDialog.noDataTxt.visibility = View.VISIBLE
                                }


                                //  Constant.snackbar(view.main_settings, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        openDialog.progressBar_dialog.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@AddPostActivity!!, PreferenceConnector.USERAUTHTOKEN, ""))
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

    private fun getAddressApiDeliver(view: View, openDialog: Dialog, adAdapter: DeliveryAddressAdapter, addressList: ArrayList<DeliverAddressInfo.ResultBean>) {
        if (Constant.isNetworkAvailable(this!!, view)) {
            openDialog.progressBar_dialog.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getPreviousDeliveryAddress,
                    Response.Listener { response ->

                        openDialog.progressBar_dialog.visibility = View.GONE
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                var deliverAddInfo = gson.fromJson(response, DeliverAddressInfo::class.java)

                                addressList.addAll(deliverAddInfo.result!!)
                                adAdapter!!.notifyDataSetChanged()

                            } else {

                                var userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType.equals(Constant.COURIOR)) {
                                    if (message.equals("Currently you are inactivate user")) {
                                        var helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        openDialog.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    openDialog.noDataTxt.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        openDialog.progressBar_dialog.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@AddPostActivity!!, PreferenceConnector.USERAUTHTOKEN, ""))
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

    class GenericTextWatcher(var view: View, var mContext: Activity) : TextWatcher {
        var isOpen: Boolean = true

        fun textLimiteFull(msg: String) {
            val alertDialog = android.app.AlertDialog.Builder(mContext)
            alertDialog.setTitle("Alert")
            alertDialog.setCancelable(false)

            alertDialog.setMessage(msg)
            alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                alertDialog.setCancelable(true)

            })
            alertDialog.show()

        }

        override fun afterTextChanged(p0: Editable?) {
            var text: String = p0.toString()
            when (view.id) {
                postTitleTxt -> {
                    if (text.length == 50) {
                        if (isOpen) {
                            textLimiteFull(mContext.getString(R.string.limite_title))
                            isOpen = false
                        } else {
                            isOpen = true
                        }
                        return
                    }

                }
                anyDelDetailTxt -> {
                    if (text.length == 200) {
                        textLimiteFull(mContext.getString(R.string.details_limite))
                    }

                }
                itemDescTxt -> {
                    if (text.length == 200) {
                        textLimiteFull("Item description should be not more than 200 characters")
                    }
                }

            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


    }

    // location update.....................................................

    override fun onLocationChanged(p0: Location?) {
        delLat = p0?.getLatitude().toString()
        delLng = p0?.getLongitude().toString()

        picLat = p0?.getLatitude().toString()
        picLng = p0?.getLongitude().toString()

        if (delLat != null && delLat != null) {
            currentAddress = getCompleteAddressString(p0?.getLatitude()!!, p0?.getLongitude()!!)
            if (mGoogleApiClient!!.isConnected) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)

            }
        }
    }


    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("My Current address", strReturnedAddress.toString())
            } else {
                Toast.makeText(this, "No Address returned!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Canont get Address!", Toast.LENGTH_SHORT).show()

        }
        return strAdd
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {}
    override fun onConnectionFailed(p0: ConnectionResult) {}
    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    protected fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        Constant.ACCESS_FINE_LOCATION)
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        } else {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
        Log.d(TAG, "Location update started ..............: ")
    }

    protected fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this@AddPostActivity)
        Log.d(TAG, "Location update stopped .......................")
    }
    //....................................................................................................


}