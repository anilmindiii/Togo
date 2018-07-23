package com.togocourier.ui.activity

import android.app.Activity
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.android.volley.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.RegistrationResponce
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private var mUri: Uri? = null
    private var mCurrentPhotoPath = ""
    private var imagePath = ""
    private var cardPath = ""
    private var userType = ""
    private var autocompleteFragment: PlaceAutocompleteFragment? = null
    private var profileImageBitmap: Bitmap? = null
    private var cardImageBitmap: Bitmap? = null
    private var address = ""
    private var lat = ""
    private var lng = ""
    var countryCode: String = ""
    private var codeId:String = ""
    private var gson = Gson()

    private var email:String = ""
    private var fullName:String = ""
    private var profileImg:String = ""
    private var uID:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        Constant.hideSoftKeyboard(this)

        profileImage.setOnClickListener(this)
        idCardLay.setOnClickListener(this)
        signUpBtn.setOnClickListener(this)
        signInLay.setOnClickListener(this)
        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
        if (userType.equals("1")) idCardLay.visibility = View.GONE
        autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                addressTxt.text = place.address
                address = place.address.toString()
                lat = place.latLng.latitude.toString()
                lng = place.latLng.longitude.toString()
                Log.e("TAG", "lat: " + place.latLng.latitude + " long: " + place.latLng.longitude)
            }

            override fun onError(status: Status) {

            }
        })

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.profileImage -> {
                selectImage()
            }
            R.id.idCardLay -> {
                selectFileCard()
            }
            R.id.signUpBtn -> {
                if (userType == "1") {
                    if (isValidCustomerData()) doRegistration()
                } else {
                    if (isValidCoriorData()) doRegistration()
                }
            }
            R.id.signInLay -> {
                finish()
            }

        }
    }


    private fun selectFileCard() {
        var items: Array<CharSequence> = arrayOf(getString(R.string.text_take_photo), getString(R.string.text_chose_gellery), getString(R.string.text_cancel))
        var alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.text_add_photo))
        alert.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->
            if (items[i].equals(getString(R.string.text_take_photo))) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                    } else {
                        intentToCaptureImageForCard()
                    }
                } else {
                    intentToCaptureImageForCard()
                }
            } else if (items[i].equals(getString(R.string.text_chose_gellery))) {
                getCardFile()
            } else if (items[i].equals(getString(R.string.text_cancel))) {
                dialogInterface.dismiss()
            }
        })
        alert.show()
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

 fun intentToCaptureImageForCard() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Constant.REQUEST_FILE_CAMERA)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        var timeSpam = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "JPEG_" + timeSpam + "_"
        var storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageFileName, ".jpg", storageDir)
        mCurrentPhotoPath = "file:" + image.absolutePath;
        return image
    }

    private fun getCardFile() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_FILE)
            } else {
                /*val intent = Intent()
                intent.setType("image*//*|application/pdf*//*")
                intent.setAction(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)*/
                val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
            }
        } else {
            /* val intent = Intent()
             intent.setType("image*//*|application/pdf*//*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)*/
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CAMERA) {
                /*    try {
                        var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, mUri)
                        profileImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, profileImage.width, profileImage.height, false))
                    } catch (e: Exception) {

                    }

                        profileImageBitmap = data.extras.get("data") as Bitmap?
                        profileImage.setImageBitmap(profileImageBitmap)
                        var bytes = ByteArrayOutputStream()
                        profileImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        var path = MediaStore.Images.Media.insertImage(contentResolver, profileImageBitmap, "Title", null)
                        imagePath = Uri.parse(path).path
                    }*/
                if (data != null) {
                    profileImageBitmap = data.getExtras()!!.get("data") as Bitmap
                    val bytes = ByteArrayOutputStream()
                    profileImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    profileImage.setImageBitmap(profileImageBitmap)
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

                    profileImageBitmap = ImageUtil.decodeFile(selectedImagePath)
                    try {
                        profileImageBitmap = ImageUtil.modifyOrientation(profileImageBitmap!!, selectedImagePath)
                        val bytes = ByteArrayOutputStream()
                        profileImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        profileImage.setImageBitmap(profileImageBitmap)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

            } else if (requestCode == Constant.REQUEST_FILE_GALLERY) {
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

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    idTxt.text = "Id Card is Loaded"
                }
            }
            else if(requestCode == Constant.REQUEST_FILE_CAMERA){
                if (data != null) {
                    cardImageBitmap = data.getExtras()!!.get("data") as Bitmap
                    val bytes = ByteArrayOutputStream()
                    cardImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    idTxt.text = "Id Card is Loaded"
                }
            }

        }
    }

    fun isValidCustomerData(): Boolean {
        val v = Validation()

        if (v.isEmpty(userTxt)) {
            Constant.snackbar(mainLayout, "Full Name can't be empty")
            userTxt.requestFocus()
            return false
        } else if (v.isEmpty(emailTxt)) {
            Constant.snackbar(mainLayout, "Email Id can't be empty")
            emailTxt.requestFocus()
            return false
        } else if (!isEmailValid(emailTxt)) {
            Constant.snackbar(mainLayout, "Enter valid email")
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
        } else if (v.isEmpty(contactTxt)) {
            Constant.snackbar(mainLayout, "Contact no can't be empty")
            contactTxt.requestFocus()
            return false
        } else if (!isContactNoValid(contactTxt)) {
            Constant.snackbar(mainLayout, "Enter valid contact no")
            contactTxt.requestFocus()
            return false
        } else if (v.isEmpty(addressTxt)) {
            Constant.snackbar(mainLayout, "Please enter your address")
            //contactTxt.requestFocus()
            return false
        }
        return true
    }

    fun isValidCoriorData(): Boolean {
        val v = Validation()

        if (v.isEmpty(userTxt)) {
            Constant.snackbar(mainLayout, "Full Name can't be empty")
            userTxt.requestFocus()
            return false
        } else if (v.isEmpty(emailTxt)) {
            Constant.snackbar(mainLayout, "Email Id can't be empty")
            emailTxt.requestFocus()
            return false
        } else if (!isEmailValid(emailTxt)) {
            Constant.snackbar(mainLayout, "Enter valid email")
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
        } else if (v.isEmpty(contactTxt)) {
            Constant.snackbar(mainLayout, "Contact no can't be empty")
            contactTxt.requestFocus()
            return false
        } else if (!isContactNoValid(contactTxt)) {
            Constant.snackbar(mainLayout, "Enter valid contact no")
            contactTxt.requestFocus()
            return false
        } else if (v.isEmpty(addressTxt)) {
            Constant.snackbar(mainLayout, "Please enter your address")
            contactTxt.requestFocus()
            return false
        } else if (cardImageBitmap == null) {
            Constant.snackbar(mainLayout, "Please Add your Id Card")
            return false
        }
        return true
    }

    private fun isEmailValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }

    private fun isPasswordValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return getValue.length > 5
    }

    private fun isContactNoValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }
        return getValue.length > 6 && getValue.length < 12
    }

    private fun doRegistration() {
        signUpBtn.isEnabled = false
        if (Constant.isNetworkAvailable(this@SignUpActivity, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.Registration_Url, Response.Listener { response ->
                val resultResponse = String(response.data)

                //   progressBar.setVisibility(View.GONE);
                Log.e("TAG", "onResponse: " + resultResponse)
                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")

                    if (status == "success") {
                        val registrationResponce = gson.fromJson(resultResponse, RegistrationResponce::class.java)
                        setSession(registrationResponce)
                        PreferenceConnector.writeString(this@SignUpActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())


                        fullName = PreferenceConnector?.readString(this,PreferenceConnector.USERFULLNAME,"").toString()
                        uID = PreferenceConnector?.readString(this,PreferenceConnector.USERID,"").toString()
                        userType = PreferenceConnector?.readString(this,PreferenceConnector.USERTYPE,"").toString()
                        profileImg = PreferenceConnector?.readString(this,PreferenceConnector.USERPROFILEIMAGE,"").toString()
                        email =  PreferenceConnector?.readString(this,PreferenceConnector.USEREMAIL,"")

                        firebaseChatRegister()
                    } else {
                        Constant.snackbar(mainLayout, message)
                    }
                    signUpBtn.isEnabled = true
                } catch (e: JSONException) {
                    e.printStackTrace()
                    signUpBtn.isEnabled = true
                }
            }, Response.ErrorListener { error ->
                signUpBtn.isEnabled = true
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

                    params.put("userType", PreferenceConnector.readString(this@SignUpActivity, PreferenceConnector.USERTYPE, ""))
                    //    params.put("contactNo", PreferenceConnector.readString(SignUpActivity.this, PreferenceConnector.USERNO, ""));
                    params.put("deviceType", "2")
                    params.put("deviceToken", FirebaseInstanceId.getInstance().token.toString())
                    params.put("socialId", "")
                    params.put("socialType", "")
                    params.put("email", emailTxt.text.toString())
                    params.put("fullName", userTxt.text.toString())
                    params.put("countryCode", "+1")
                    params.put("contactNo", contactTxt.text.toString())
                    params.put("address", address)
                    params.put("latitude", lat)
                    params.put("longitude", lng)
                    params.put("password", pwdTxt.text.toString())
                    if (profileImageBitmap == null) {
                        params.put("profileImage", "")
                    }
                    if (cardImageBitmap == null) {
                        params.put("uploadIdCard", "")
                    }
                    return params
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (profileImageBitmap != null) {
                            params.put("profileImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(profileImageBitmap!!), "image/jpg"))
                        }
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

    private fun setSession(registrationResponce: RegistrationResponce) {
        PreferenceConnector.clear(this)
        PreferenceConnector.writeString(this, PreferenceConnector.USERTYPE, registrationResponce.getUserData()?.userType.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERID, registrationResponce.getUserData()?.id.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USEREMAIL, registrationResponce.getUserData()?.email.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERSOCIALID, registrationResponce.getUserData()?.socialId.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERSOCIALTYPE, registrationResponce.getUserData()?.socialType.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERPROFILEIMAGE, registrationResponce.getUserData()?.profileImage.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERFULLNAME, registrationResponce.getUserData()?.fullName.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERCOUNTRYCODE, registrationResponce.getUserData()?.countryCode.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERCONTACTNO, registrationResponce.getUserData()?.contactNo.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERADDRESS, registrationResponce.getUserData()?.address.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERLATITUTE, registrationResponce.getUserData()?.latitude.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERLONGITUTE, registrationResponce.getUserData()?.longitude.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERAUTHTOKEN, registrationResponce.getUserData()?.authToken.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.ISLOGIN, "yes")
        PreferenceConnector.writeInteger(this, PreferenceConnector.RATTING, registrationResponce.getUserData()?.rating!!)

        var session = SessionManager(this)
        session.createSessionAuthToken(registrationResponce.getUserData()?.authToken.toString())


        //startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }


    fun firebaseChatRegister() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(uID+"@togo.com", "123456")
                .addOnCompleteListener(this, { task ->
                    Log.e("TAG", "performFirebaseRegistration:onComplete:" + task.isSuccessful)
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
                .addOnCompleteListener(this@SignUpActivity) { task ->
                    Log.d("TAG", "performFirebaseLogin:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
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
        user.firebaseToken = FirebaseInstanceId.getInstance().token.toString()
        user.profilePic = image

        database.child(Constant.ARG_USERS)
                .child(user.uid)
                .setValue(user)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        if(userType.equals(Constant.COURIOR)){
                            var helper = HelperClass(this,this)
                            helper.inActiveByAdmin("Registration done, but you cannot access the app before admin give you login access",true)
                        }
                        else{
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this,"data not store at firebase server",Toast.LENGTH_SHORT).show()
                    }
                }
    }


}

