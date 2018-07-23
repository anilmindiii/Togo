package com.togocourier.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.R
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.togocourier.responceBean.GetUserDataProfile
import com.togocourier.responceBean.UpdateProfileInfo
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.ui.activity.ReviewLIstActivity
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.title_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class ProfileFragment : Fragment() {

    private var from: String? = null
    private var userId: String? = null
    private var postUserId: String? = null
    private var userType:String = ""
    private var email:String = ""
    private var fullName:String = ""
    private var countryCode:String = ""
    private var contactNum:String = ""
    private var address:String = ""
    private var profileImg:String = ""
    private var lat = ""
    private var lng = ""
    private var codeId:String = ""
    private var ratting:Int = 0
    private var profileImageBitmap:Bitmap ?= null
    private var autocompleteFragment: SupportPlaceAutocompleteFragment? = null
    var getUserDataProfile:GetUserDataProfile ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            from = arguments?.getString(ARG_PARAM1)
            userId = arguments?.getString(ARG_PARAM2)
            postUserId = arguments?.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val view = inflater!!.inflate(R.layout.fragment_profile, container, false)
        activity?.tabRightIcon!!.visibility = View.GONE

        userType = PreferenceConnector?.readString(context!!,PreferenceConnector.USERTYPE,"").toString()
        email = PreferenceConnector?.readString(context!!,PreferenceConnector.USEREMAIL,"").toString()
        fullName = PreferenceConnector?.readString(context!!,PreferenceConnector.USERFULLNAME,"").toString()
        countryCode = PreferenceConnector?.readString(context!!,PreferenceConnector.USERCOUNTRYCODE,"").toString()
        contactNum = PreferenceConnector?.readString(context!!,PreferenceConnector.USERCONTACTNO,"").toString()
        address = PreferenceConnector?.readString(context!!,PreferenceConnector.USERADDRESS,"").toString()
        profileImg = PreferenceConnector?.readString(context!!,PreferenceConnector.USERPROFILEIMAGE,"").toString()
        lat = PreferenceConnector?.readString(context!!,PreferenceConnector.USERLATITUTE,"").toString()
        lng = PreferenceConnector?.readString(context!!,PreferenceConnector.USERLONGITUTE,"").toString()
        ratting = PreferenceConnector?.readInteger(context!!,PreferenceConnector.RATTING,0)

        var fRating = ratting.toFloat()
        view.ratingBar.rating = fRating
        view.iv_profile_img.isEnabled = false
        view.ly_location.visibility = View.GONE

        view.ly_address.isEnabled = false
        view.ly_address.isClickable = false

        openGoogleMap(view)

/*.....................................................................................*/

        if(from != null)
        {
            if(from!!.equals("AllRequestActivity")){
                view.tool_bar.visibility = View.VISIBLE
                view.iv_edit.setImageDrawable(getResources().getDrawable(R.drawable.circle_star_ico))
                view.btn_update.visibility = View.GONE
                //view.iv_edit.isEnabled = false
                view.ratingBar.visibility = View.VISIBLE

                getUserData(view,userId.toString())
            }else {
                view.tool_bar.visibility = View.GONE
            }

        }else {
            setDate(view)
            view.tool_bar.visibility = View.GONE
        }

/*............................................................................................*/
        view.back.setOnClickListener {
            activity!!.onBackPressed()
        }



        view.iv_profile_img.setOnClickListener {
            selectImage()
        }

        view.ly_ratting_bar.setOnClickListener {
            var intent = Intent(context,ReviewLIstActivity::class.java)
            intent.putExtra("useType",2)
            startActivity(intent)

        }

        view.iv_edit.setOnClickListener {

            if(from != null){
                if(from!!.equals("AllRequestActivity")){
                    var intent = Intent(context,ReviewLIstActivity::class.java)
                    intent.putExtra("useType",1)
                    intent.putExtra("userId",userId)
                    intent.putExtra("postUserId",postUserId)
                    startActivity(intent)
                }
            }
            else
            {
                view.btn_update.visibility = View.VISIBLE
                view.iv_edit.visibility = View.GONE
                view.ed_email.isEnabled = true
                view.ed_name.isEnabled = true
                view.ed_phone.isEnabled = true
                view.tv_location.isEnabled = true
                view.iv_profile_img.isEnabled = true
                view.ly_location.isEnabled = true
                view.ly_location.visibility = View.VISIBLE
                showKeyboard(view.ed_name)
            }
        }

        autocompleteFragment =  getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment) as SupportPlaceAutocompleteFragment
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                address = place.address.toString()
                view. tv_location.text = place.address.toString()
                lat = place.latLng.latitude.toString()
                lng = place.latLng.longitude.toString()
                openGoogleMap(view)
                Log.e("TAG", "lat: " + place.latLng.latitude + " long: " + place.latLng.longitude)
            }

            override fun onError(status: Status) {

            }
        })


        view.btn_update.setOnClickListener {
            if(isValidCustomerData(view)) updateProfile(view)

        }


        if(userType.equals(Constant.CUSTOMER)){
            if(from != null){
                if(from!!.equals("AllRequestActivity")){
                    view.ratingBar.visibility = View.VISIBLE
                    view.ly_ratting_bar.isEnabled = false
                }
            }
            else view.ratingBar.visibility = View.GONE

        }
        else if(userType.equals(Constant.COURIOR)){
            view.ratingBar.visibility = View.VISIBLE
        }
        return view
    }

    fun showKeyboard(ettext: EditText) {
        ettext.requestFocus()
        ettext.postDelayed(Runnable {
            val keyboard = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(ettext, 0)
        }, 200)
    }


    private fun selectImage() {
        var items: Array<CharSequence> = arrayOf(getString(R.string.text_take_photo), getString(R.string.text_chose_gellery), getString(R.string.text_cancel))
        var alert: AlertDialog.Builder = AlertDialog.Builder(context!!)
        alert.setTitle(getString(R.string.text_add_photo))
        alert.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->
            if (items[i].equals(getString(R.string.text_take_photo))) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (context!!.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                    } else {
                        intentToCaptureImage()
                    }
                } else {
                    intentToCaptureImage()
                }
            } else if (items[i].equals(getString(R.string.text_chose_gellery))) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (context!!.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                    } else {
                        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, Constant.SELECT_FILE)
                    }
                } else {
                    var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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

    fun setDate(view: View){
            Picasso.with(context).load(profileImg).placeholder(R.drawable.splash_logo).into(view.iv_profile)
            Picasso.with(context).load(profileImg).placeholder(R.drawable.splash_logo).into(view.iv_profile_img)
            view.ed_name.setText(fullName)
            view.ed_email.setText(email)
            view.ed_phone.setText(contactNum)
            view.tv_location.setText(address)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CAMERA) {
                if (data != null) {
                    profileImageBitmap = data.getExtras()!!.get("data") as Bitmap
                    val bytes = ByteArrayOutputStream()
                    profileImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    view!!.iv_profile_img.setImageBitmap(profileImageBitmap)
                    view!!.iv_profile.setImageBitmap(profileImageBitmap)
                }
            } else if (requestCode == Constant.SELECT_FILE) {
                if (data != null) {
                    val selectedImageUri = data.data

                    val projection = arrayOf(MediaStore.MediaColumns.DATA)
                    val cursorLoader = CursorLoader(context, selectedImageUri, projection, null, null, null)
                    val cursor = cursorLoader.loadInBackground()
                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    cursor.moveToFirst()
                    val selectedImagePath = cursor.getString(column_index)

                    profileImageBitmap = ImageUtil.decodeFile(selectedImagePath)
                    try {
                        profileImageBitmap = ImageUtil.modifyOrientation(profileImageBitmap!!, selectedImagePath)
                        val bytes = ByteArrayOutputStream()
                        profileImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        view!!.iv_profile_img.setImageBitmap(profileImageBitmap)
                        view!!.iv_profile.setImageBitmap(profileImageBitmap)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

        }
    }


    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        private val ARG_PARAM3 = "param3"


        fun newInstance(param1: String, param2: String, postUserId: String?): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            args.putString(ARG_PARAM3, postUserId)

            fragment.arguments = args
            return fragment
        }
    }

    private fun updateProfile(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.main)) {
            view.progressBar.visibility = View.VISIBLE

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.UpdateProfile, Response.Listener { response ->
                val resultResponse = String(response.data)
                view.progressBar.visibility = View.GONE
                //   progressBar.setVisibility(View.GONE);
                Log.e("TAG", "onResponse: " + resultResponse)
                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    if (status == "success") {
                        var gson = Gson()
                        var updateProfileInf = gson.fromJson(resultResponse, UpdateProfileInfo::class.java)
                        updateSession(updateProfileInf,view)
                        Constant.snackbar(view.main, getString(R.string.update_profile))
                        view.btn_update.visibility = View.GONE
                        view.iv_edit.visibility = View.VISIBLE

                        view.ed_email.isEnabled = false
                        view.ed_name.isEnabled = false
                        view.ed_phone.isEnabled = false
                        view.tv_location.isEnabled = false
                        view.iv_profile_img.isEnabled = false
                        view.ly_location.isEnabled = false
                        view.ly_location.visibility = View.GONE

                    } else {

                        var userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
                        if(userType.equals(Constant.COURIOR)){
                            if(message.equals("Currently you are inactivate user")){
                                var helper = HelperClass(context!!,activity!!)
                                helper.inActiveByAdmin("Admin inactive your account",true)
                            }else{
                                Constant.snackbar(view.main, message)
                            }
                        }else{
                            Constant.snackbar(view.main, message)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                view.progressBar.visibility = View.GONE
                var errorMessage = "Unknown error"
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
                        Snackbar.make(view.main, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                Log.i("Error", errorMessage)
                error.printStackTrace()
            }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("email", view.ed_email.text.toString())
                    params.put("fullName", view.ed_name.text.toString())
                    params.put("countryCode","+1")
                    params.put("contactNo", view.ed_phone.text.toString())
                    params.put("address", address)
                    params.put("latitude", lat)
                    params.put("longitude", lng)


                    return params
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (profileImageBitmap != null) {
                            params.put("profileImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(profileImageBitmap!!), "image/jpg"))
                        }

                        return params
                    }
            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(multipartRequest)

        }
    }
    
    private fun updateSession(updateProfileInf: UpdateProfileInfo, view: View) {
        PreferenceConnector.writeString(context!!, PreferenceConnector.USEREMAIL, updateProfileInf?.data?.email.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERPROFILEIMAGE, updateProfileInf?.data?.profileImage.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERFULLNAME, updateProfileInf?.data?.fullName.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERCOUNTRYCODE, updateProfileInf?.data?.countryCode.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERCONTACTNO,updateProfileInf?.data?.contactNo.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERADDRESS, updateProfileInf?.data?.address.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERLATITUTE, updateProfileInf?.data?.latitude.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERLONGITUTE, updateProfileInf?.data?.longitude.toString())

        updateProfileFCM(view)
    }

    private fun isEmailValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }

    fun isValidCustomerData(view: View): Boolean {
        val v = Validation()

        if (v.isEmpty(view.ed_name)) {
            Constant.snackbar(view.main, "Full Name can't be empty")
            view.ed_name.requestFocus()
            return false
        }
        else if (v.isEmpty(view.ed_email)) {
            Constant.snackbar(view.main, "Email Id can't be empty")
            view.ed_email.requestFocus()
            return false
        }
        else if (!isEmailValid(view.ed_email)) {
            Constant.snackbar(view.main, "Enter valid email")
            view.ed_name.requestFocus()
            return false
        }
        else if (v.isEmpty(view.ed_phone)) {
            Constant.snackbar(view.main, "Contact no can't be empty")
            view.ed_phone.requestFocus()
            return false
        }
        else if (!isContactNoValid(view.ed_phone)) {
            Constant.snackbar(view.main, "Enter valid contact no")
            view.ed_phone.requestFocus()
            return false
        }

        else if (v.isEmpty(view.tv_location)) {
            Constant.snackbar(view.main, "Please enter your address")
            view.tv_location.requestFocus()
            return false
        }
        return true
    }

    private fun isContactNoValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }
        return getValue.length > 6 && getValue.length < 12
    }

    fun openGoogleMap(view: View) {
       // val API_KEY = "AIzaSyBnFGTrGe8dJKMnrcinn1edleHCB_yZI5U"
        val API_KEY = "AIzaSyDI-QUWEEWFiV1W90w4PW2UWpIt04_DsmA"
        val url = "https://maps.googleapis.com/maps/api/staticmap?center=" + "&zoom=13&size=640x300&maptype=roadmap" +
                "&markers=color:red%7Clabel:S%7C" + lat + "," + lng + "&key=" + API_KEY

        Picasso.with(context).load(url).into(view.iv_map_img)
    }

    fun getUserData(view: View,userId:String) {
        if (Constant.isNetworkAvailable(context!!, view.main)) {
            view.progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getUserDetailById ,
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
                                var gson = Gson()
                               var userDataProfile = gson.fromJson(response, GetUserDataProfile::class.java)

                                Picasso.with(context).load(userDataProfile.userData?.profileImage).placeholder(R.drawable.splash_logo).into(view.iv_profile)
                                Picasso.with(context).load(userDataProfile.userData?.profileImage).placeholder(R.drawable.splash_logo).into(view.iv_profile_img)
                                view.ed_name.setText(userDataProfile.userData?.fullName)
                                view.ed_email.setText(userDataProfile.userData?.email)
                                view.ed_phone.setText(userDataProfile.userData?.contactNo)
                                view.tv_location.setText(userDataProfile.userData?.address)
                                view.ratingBar.rating = userDataProfile.userData?.rating!!.toFloat()

                            } else {

                                var userType = PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(context!!,activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(view.main, message)
                                    }
                                }else{
                                    Constant.snackbar(view.main, message)
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {error->
                        view.progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                if(context != null){
                                    var helper = HelperClass(context!!,activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("userId", userId)



                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)
        }
    }

    private fun updateProfileFCM(view: View) {
        var user  = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")
        user.email = PreferenceConnector.readString(context!!, PreferenceConnector.USEREMAIL, "")
        user.name = PreferenceConnector.readString(context!!, PreferenceConnector.USERFULLNAME, "")
        user.firebaseToken = FirebaseInstanceId.getInstance().token!!
        user.profilePic = PreferenceConnector.readString(context!!, PreferenceConnector.USERPROFILEIMAGE, "")


        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(user.uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                print("Dane")
            }else{
                print("notDane")
            }
        }

    }

}
