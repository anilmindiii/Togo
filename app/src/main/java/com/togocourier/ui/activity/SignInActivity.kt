package com.togocourier.ui.activity

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
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.RegistrationResponce
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.service.MyLocationService
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.upload_id_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class SignInActivity : AppCompatActivity(), View.OnClickListener  ,
        com.google.android.gms.location.LocationListener,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private var callbackManager: CallbackManager? = null

    val RC_SIGN_IN = 101
    private val TAG = "TOGO"
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    internal var mLocationRequest: LocationRequest = LocationRequest()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var lat: Double? = null
    internal var lng:Double? = null
    internal var lmgr: LocationManager?=null
    private var isGPSEnable: Boolean = false
    var alertDialog:Dialog ?= null

    private var email:String = ""
    private var fullName:String = ""
    private var profileImg:String = ""
    private var uID:String = ""
    private var userType = ""
    private var cardImageBitmap: Bitmap? = null

    var mGoogleSignInClient : GoogleSignInClient?= null

    var address:String = ""
    var firebaseToken:String = ""

    private var gson = Gson()


    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        initializeView()

        FacebookSdk.sdkInitialize(getApplicationContext())
        callbackManager = CallbackManager.Factory.create()
        fbSetUpMethod()

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

         userType =  PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
        if (userType.equals(Constant.CUSTOMER)){
            RemPreferenceConnector.writeString(this, RemPreferenceConnector.WELCOMESCREENSHOW_USER, "save_cust")

        }else if(userType.equals(Constant.COURIOR)){
            RemPreferenceConnector.writeString(this, RemPreferenceConnector.WELCOMESCREENSHOW_COURIER, "save_cust")

        }

        firebaseToken = FirebaseInstanceId.getInstance().token.toString();
    }

    private fun initializeView() {
        signInBtn.setOnClickListener(this)
        fbLoginBtn.setOnClickListener(this)
        forgotPwdTxt.setOnClickListener(this)
        signUpLay.setOnClickListener(this)
        gmailLoginBtn.setOnClickListener(this)
        var emId = RemPreferenceConnector.readString(this, RemPreferenceConnector.REM_USEREMAIL, "")
        var pwd = RemPreferenceConnector.readString(this, RemPreferenceConnector.REM_PWD, "")

        if(!pwd.equals("")){
            checkbox_remember_me.isChecked = true
        }

        emailTxt.setText(emId)
        pwdTxt.setText(pwd)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.signInBtn -> {
                Constant.hideSoftKeyboard(this)
                if (isValidData()) doSignIn()
            }
            R.id.fbLoginBtn -> {
                if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
                    if(isGpsEnable())
                    LoginManager.getInstance().logInWithReadPermissions(
                            this, Arrays.asList("public_profile", "email"))
                }

            }
            R.id.forgotPwdTxt -> {
                var intent = Intent(this,ForgotPassActivity::class.java)
                startActivity(intent)
            }
            R.id.signUpLay -> {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            R.id.gmailLoginBtn -> {
                gmailLoginBtn.isEnabled = false
                signIn()
            }
        }
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
            ab.show() }
        return isGPSEnable
    }

    fun isValidData(): Boolean {
        var v: Validation = Validation()
        if (v.isEmpty(emailTxt)) {
            Constant.snackbar(mainLayout, "email id can't be empty")
            emailTxt.requestFocus()
            return false
        } else if (!isEmailValid(emailTxt)) {
            Constant.snackbar(mainLayout, "enter valid email")
            emailTxt.requestFocus()
            return false
        } else if (v.isEmpty(pwdTxt)) {
            Constant.snackbar(mainLayout, "Password can't be empty")
            pwdTxt.requestFocus()
            return false
        } else if (!isPasswordValid(pwdTxt)) {
            Constant.snackbar(mainLayout, "Password should be 6 character long")
            pwdTxt.requestFocus()
            return false
        }
        return true
    }

    fun isEmailValid(editText: EditText): Boolean {
        var getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }

    fun isPasswordValid(editText: EditText): Boolean {
        var getValue = editText.text.toString().trim()
        return getValue.length > 5
    }

    fun doSignIn() {
        signInBtn.isEnabled = false
        if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Log_In_Url,
                    Response.Listener { response ->
                        println("#" + response)
                        Log.e("LOGIN", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                                if(checkbox_remember_me.isChecked){
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_USEREMAIL, emailTxt.text.toString())
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_PWD, pwdTxt.text.toString())
                                }else{
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_USEREMAIL, "")
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_PWD, "")
                                }

                                val signInResponce = gson.fromJson(response, RegistrationResponce::class.java)
                                setSession(signInResponce)
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, signInResponce.getUserData()?.authToken.toString())
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())

                                fullName = PreferenceConnector?.readString(this,PreferenceConnector.USERFULLNAME,"").toString()
                                uID = PreferenceConnector?.readString(this,PreferenceConnector.USERID,"").toString()
                                userType = PreferenceConnector?.readString(this,PreferenceConnector.USERTYPE,"").toString()
                                profileImg = PreferenceConnector?.readString(this,PreferenceConnector.USERPROFILEIMAGE,"").toString()
                                email =  PreferenceConnector?.readString(this,PreferenceConnector.USEREMAIL,"")
                                loginFirebaseDataBase()

                                if(userType.equals(Constant.CUSTOMER)){
                                    stopService(Intent(this, MyLocationService::class.java))
                                }

                            } else {
                                signInBtn.isEnabled = true
                                progressBar.visibility = View.GONE

                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Currently you are inactive by admin, please contact admin",false)
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
                        Toast.makeText(this@SignInActivity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", emailTxt.text.toString())
                    params.put("password", pwdTxt.text.toString())
                    params.put("userType", userType)
                    params.put("deviceType", "2")
                    params.put("deviceToken", firebaseToken)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode === RC_SIGN_IN) {
            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CAMERA) {
                if (data != null) {

                    cardImageBitmap = data.getExtras()!!.get("data") as Bitmap
                    val bytes = ByteArrayOutputStream()
                    cardImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    //profileImage.setImageBitmap(profileImageBitmap)
                    alertDialog!!.idTxt.text = "Id Card is Loaded"
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

                    cardImageBitmap = ImageUtil.decodeFile(selectedImagePath)
                    try {
                        cardImageBitmap = ImageUtil.modifyOrientation(cardImageBitmap!!, selectedImagePath)
                        val bytes = ByteArrayOutputStream()
                        cardImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        //profileImage.setImageBitmap(profileImageBitmap)
                        alertDialog!!.idTxt.text = "Id Card is Loaded"

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

        }



    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            fullName =  account?.displayName.toString()
            email = account?.email.toString()
            profileImg =   account?.photoUrl.toString()
            var socialId =    account?.id.toString()
            var deviceToken = firebaseToken
           // doSocialRegistration(this, name, email, "", socialId, "google", profileImg, "")
            doRegistration(fullName,email,socialId,"gmail",profileImg,deviceToken)

        } catch (e: ApiException) {
            progressBar.visibility = View.GONE
            gmailLoginBtn.isEnabled = true
            Toast.makeText(this,"google auth error",Toast.LENGTH_SHORT).show();
            Log.w("LoginActivity.TAG", "signInResult:failed code=" + e.statusCode)

        }

    }

    private fun signIn() {
        progressBar.visibility = View.VISIBLE
        val signInIntent = mGoogleSignInClient?.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun setSession(signInResponce: RegistrationResponce) {
        PreferenceConnector.clear(this)
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERTYPE, signInResponce.getUserData()?.userType.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERID, signInResponce.getUserData()?.id.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USEREMAIL, signInResponce.getUserData()?.email.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERSOCIALID, signInResponce.getUserData()?.socialId.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERSOCIALTYPE, signInResponce.getUserData()?.socialType.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERPROFILEIMAGE, signInResponce.getUserData()?.profileImage.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERFULLNAME, signInResponce.getUserData()?.fullName.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERCOUNTRYCODE, signInResponce.getUserData()?.countryCode.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERCONTACTNO, signInResponce.getUserData()?.contactNo.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERADDRESS, signInResponce.getUserData()?.address.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERLATITUTE, signInResponce.getUserData()?.latitude.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERLONGITUTE, signInResponce.getUserData()?.longitude.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.ISLOGIN, "yes")
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.ISNOTIFICATIONON, signInResponce.getUserData()?.notificationStatus.toString())
        PreferenceConnector.writeInteger(this@SignInActivity, PreferenceConnector.RATTING, signInResponce.getUserData()?.rating!!)

        var session = SessionManager(this)
        session.createSessionAuthToken(signInResponce.getUserData()?.authToken.toString())

       // startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    fun  fbSetUpMethod(){
        LoginManager.getInstance().registerCallback(callbackManager,object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult?) {
                val sSocialId = loginResult?.getAccessToken()?.getUserId()
                val request = GraphRequest.newMeRequest(loginResult?.getAccessToken()) { `object`, response ->
                    try {
                        val jsonObject = JSONObject(`object`.toString())
                        var email_fb = ""
                        if (`object`.has("email")) {
                            email_fb = `object`.getString("email")
                        }
                        var socialId_ = `object`.getString("id")
                        val firstname = `object`.getString("first_name")
                        val lastname = `object`.getString("last_name")
                        val fullname = firstname + " " + lastname
                        val profileImage = "https://graph.facebook.com/$sSocialId/picture?type=large"
                        val deviceToken:String = firebaseToken
                        val params = HashMap<String, String>()
                        params.put("deviceToken",deviceToken)

                        doRegistration(fullname,email_fb,socialId_,"facebook",profileImage,deviceToken)
                    } catch (e: JSONException) {
                        e.printStackTrace() }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id, first_name, last_name, email, picture")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Toast.makeText(this@SignInActivity,"cancel login",Toast.LENGTH_SHORT).show() }

            override fun onError(exception: FacebookException?) {
                if (exception is FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut()
                    }
                }
                exception?.printStackTrace() }
        })
    }

    private fun doRegistration(fullname: String, email_fb: String, socialId_: String, socialType: String, profileImage: String, deviceToken: String) {
        if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.Registration_Url, Response.Listener { response ->
                val resultResponse = String(response.data)
                //progressBar.visibility = View.GONE
                //   progressBar.setVisibility(View.GONE);
                Log.e("TAG", "onResponse: " + resultResponse)
                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    gmailLoginBtn.isEnabled = true
                    if (status == "success") {
                        val registrationResponce = gson.fromJson(resultResponse, RegistrationResponce::class.java)

                        if(registrationResponce.getUserData()?.userType.equals(Constant.COURIOR)){

                            if(registrationResponce.getUserData()?.uploadIdCard.equals("")){
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, registrationResponce.getUserData()?.authToken.toString())
                                uploadIdCardDialog(socialType)
                                progressBar.visibility = View.GONE
                            }else{
                                setSession(registrationResponce)
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, registrationResponce.getUserData()?.authToken.toString())

                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())
                                fullName = PreferenceConnector?.readString(this,PreferenceConnector.USERFULLNAME,"").toString()
                                uID = PreferenceConnector?.readString(this,PreferenceConnector.USERID,"").toString()
                                //userType = PreferenceConnector?.readString(this,PreferenceConnector.USERTYPE,"").toString()
                                profileImg = PreferenceConnector?.readString(this,PreferenceConnector.USERPROFILEIMAGE,"").toString()
                                email =  PreferenceConnector?.readString(this,PreferenceConnector.USEREMAIL,"")
                                loginFirebaseDataBase()
                            }

                        }else if(registrationResponce.getUserData()?.userType.equals(Constant.CUSTOMER)){
                            setSession(registrationResponce)
                            PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, registrationResponce.getUserData()?.authToken.toString())

                            PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())
                            fullName = PreferenceConnector?.readString(this,PreferenceConnector.USERFULLNAME,"").toString()
                            uID = PreferenceConnector?.readString(this,PreferenceConnector.USERID,"").toString()
                           // userType = PreferenceConnector?.readString(this,PreferenceConnector.USERTYPE,"").toString()
                            profileImg = PreferenceConnector?.readString(this,PreferenceConnector.USERPROFILEIMAGE,"").toString()
                            email =  PreferenceConnector?.readString(this,PreferenceConnector.USEREMAIL,"")
                            loginFirebaseDataBase()
                        }




                    } else {
                        progressBar.visibility = View.GONE
                        gmailLoginBtn.isEnabled = true

                        if(userType.equals(Constant.COURIOR)){
                            if(message.equals("Currently you are inactivate user")){
                                var helper = HelperClass(this,this)
                                helper.inActiveByAdmin("Currently you are inactive by admin, please contact admin",false)
                            }else{
                                Constant.snackbar(mainLayout, message)
                            }
                        }else{
                            Constant.snackbar(mainLayout, message)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    gmailLoginBtn.isEnabled = true
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                progressBar.visibility = View.GONE
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
                        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                Log.i("Error", errorMessage)
                error.printStackTrace()
            }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("userType", userType)
                    params.put("deviceType", "2")
                    params.put("deviceToken", deviceToken)
                    params.put("socialType", socialType)
                    params.put("email", email_fb)
                    params.put("fullName", fullname)
                    params.put("socialId",socialId_)
                    params.put("address", address)
                    params.put("latitude", lat.toString())
                    params.put("longitude", lng.toString())
                    params.put("profileImage", profileImage)

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

    // location update.....................................................

    override fun onLocationChanged(p0: Location?) {
        lat = p0?.getLatitude()
        lng = p0?.getLongitude()
        if (lat != null && lng != null) {
            address = getCompleteAddressString(p0?.getLatitude()!!,p0?.getLongitude()!!)
            if(mGoogleApiClient!!.isConnected){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)

            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdates()
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
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this@SignInActivity)
        Log.d(TAG, "Location update stopped .......................")
    }
    //....................................................................................................


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
                Toast.makeText(this,"No Address returned!",Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this,"Canont get Address!",Toast.LENGTH_SHORT).show()

        }
        return strAdd
    }

    fun firebaseChatRegister() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(uID+"@togo.com", "123456")
                .addOnCompleteListener(this, { task ->
                    Log.e("TAG", "performFirebaseRegistration:onComplete:" + task.isSuccessful)

                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {

                        addUserFirebaseDatabase(email,fullName,profileImg,userType,uID)
                    } else {
                        loginFirebaseDataBase()
                    }
                })
    }

    private fun loginFirebaseDataBase() {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(uID+"@togo.com", "123456")
                .addOnCompleteListener(this@SignInActivity) { task ->
                    Log.d("TAG", "performFirebaseLogin:onComplete:" + task.isSuccessful)
                    signInBtn.isEnabled = true
                    if (task.isSuccessful) {


                      /*  val intent = Intent(applicationContext, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()*/

                        addUserFirebaseDatabase(email,fullName,profileImg,userType,uID)
                    } else {
                        firebaseChatRegister()
                    }
                }
    }

    private fun addUserFirebaseDatabase(email: String, name: String, image: String, userType: String,uID:String) {
        val database = FirebaseDatabase.getInstance().reference
        var user  = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(this!!, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = uID
        user.email = email
        user.name = name
        user.firebaseToken = firebaseToken
        user.profilePic = image

        database.child(Constant.ARG_USERS)
                .child(user.uid)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this,"data not store at firebase server",Toast.LENGTH_SHORT).show()
                    }

                    progressBar.visibility = View.GONE
                }
    }


    fun uploadIdCardDialog(socialType:String) {
        alertDialog = Dialog(this)
        alertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog?.setContentView(R.layout.upload_id_layout)
        alertDialog?.setCancelable(false)
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.idCardLay?.setOnClickListener {
            selectImage()
        }
        alertDialog?.close_popup?.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialog?.doneBtn?.setOnClickListener {
            if(cardImageBitmap != null){
                uploadIdCardApi(socialType)
            }else Toast.makeText(this,"Please Add your Id Card",Toast.LENGTH_SHORT).show()

        }
        alertDialog?.show()

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
                    val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                    val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
                } else {
                    Toast.makeText(this, "YOUR  PERMISSION DENIED", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private fun uploadIdCardApi(socialType:String) {
        if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.uploadFbInfoIdCard, Response.Listener { response ->
                val resultResponse = String(response.data)
                progressBar.visibility = View.GONE
                //   progressBar.setVisibility(View.GONE);
                Log.e("TAG", "onResponse: " + resultResponse)
                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    if (status == "success") {
                        alertDialog?.dismiss()
                        /*if(isGpsEnable()){

                        }*/

                           /* if(socialType.equals("facebook")){
                                LoginManager.getInstance().logInWithReadPermissions(
                                        this, Arrays.asList("public_profile", "email"))
                            }else if(socialType.equals("gmail")){
                                signIn()
                            }*/

                        var helper = HelperClass(this,this)
                        helper.inActiveByAdmin("Registration done, but you cannot access the app before admin give you login access",false)


                    }


                    else {

                        if(userType.equals(Constant.COURIOR)){
                            if(message.equals("Currently you are inactivate user")){
                                var helper = HelperClass(this,this)
                                helper.inActiveByAdmin("Registration done, but you cannot access the app before admin give you login access",false)
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
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                progressBar.visibility = View.GONE
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

                        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                Log.i("Error", errorMessage)
                error.printStackTrace()
            }) {


                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }


                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (cardImageBitmap != null) {
                            params.put("uploadIdCard", DataPart("idCard.jpg", AppHelper.getFileDataFromDrawable(cardImageBitmap!!), "image/jpg"))

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


}
