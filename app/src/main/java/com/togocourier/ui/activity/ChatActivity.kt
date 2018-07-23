package com.togocourier.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.togocourier.R
import com.togocourier.adapter.ChattingAdapter
import com.togocourier.fcm_services.FcmNotificationBuilder
import com.togocourier.responceBean.Chat
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.ImageUtil
import com.togocourier.util.PreferenceConnector
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class ChatActivity : AppCompatActivity() {

    var otherUID = ""
    var title = ""
    var myUid: String = ""
    var blockedId: String = ""
    var chatNode = ""
    private var galleryBitMap: Bitmap? = null
    var userInfoFcm = UserInfoFCM()
    private var chatAdapter: ChattingAdapter? = null
    private var chatList = ArrayList<Chat>()
    var DatabaseReference = FirebaseDatabase.getInstance()
    private var image_FirebaseURL: Uri? = null
    private var map = HashMap<String,Chat>()
    var emojiPopup:EmojiPopup ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        myUid = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
        if (intent != null) {
            otherUID = intent.getStringExtra("otherUID")
            title = intent.getStringExtra("title")
        }

        if (myUid.toInt() < otherUID.toInt()) {
            chatNode = myUid + "_" + otherUID
        } else {
            chatNode = otherUID + "_" + myUid
        }

         emojiPopup = EmojiPopup.Builder.fromRootView(view_emoji).build(message);

        gettingDataFromUserTable(otherUID)
        getBlockDate()
        getChat()

        block_button.setOnClickListener {
            getBlockDate()

             if (blockedId.equals("")){
                blockChatDialog("Are you want to block user?")
            }else if(blockedId.equals(myUid)){
                 blockChatDialog("Are you want to unblock user?")
             }else if(blockedId.equals(otherUID)){
                 blockChatDialog("Are you want to block user?")
             }else if(blockedId.equals("Both")){
                 blockChatDialog("Are you want to unblock user?")
             }
        }

        send_message.setOnClickListener {
            getBlockDate()
            if (blockedId.equals(myUid)) {
                alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
            } else if (blockedId.equals(otherUID)) {
                alertMSG("You are blocked by " + userInfoFcm.name + ". Can't send any message")
            } else if (blockedId.equals("Both")) {
                alertMSG("You block " + userInfoFcm.name + ". Can't send any message")

            } else {
                send_msg()
            }

        }

        emoji.setOnClickListener() {
            emojiPopup?.toggle()
            // Toggles visibility of the Popup.
            //emojiPopup.dismiss(); // Dismisses the Popup.
            //emojiPopup.isShowing(); // Returns true when Popup is showing.

        }

        iv_back.setOnClickListener { onBackPressed() }

        gallery.setOnClickListener {

            if (blockedId.equals(myUid)) {
                alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
            } else if (blockedId.equals(otherUID)) {
                alertMSG("You are blocked by " + userInfoFcm.name + ". Can't send any message")
            } else if (blockedId.equals("Both")) {
                alertMSG("You block " + userInfoFcm.name + ". Can't send any message")

            } else {
                getCardFile()
            }
        }

        message.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
               // recycler_view.scrollToPosition((chatList.size - 1))

/*
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager!!.toggleSoftInputFromWindow(
                        main_chat_activity.getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0)*/

                emojiPopup?.dismiss();
                return false
            }

        })


        capture_image.setOnClickListener {

            if (blockedId.equals(myUid)) {
                alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
            } else if (blockedId.equals(otherUID)) {
                alertMSG("You are blocked by " + userInfoFcm.name + ". Can't send any message")
            } else if (blockedId.equals("Both")) {
                alertMSG("You block " + userInfoFcm.name + ". Can't send any message")

            } else {
                captureImageFromCamera()
            }
        }

        delete_button.setOnClickListener {
            deleteChatDialog("Are you sure you want to delete conversation?")

        }

    }

    private fun send_msg() {
        var key = DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.child(chatNode)!!.push().key
        var msg = message.text.toString()
        var firebase_id: String = FirebaseAuth.getInstance().currentUser?.uid!!
        var firebaseToken: String = FirebaseInstanceId.getInstance().token!!

        if (msg.equals("") && image_FirebaseURL != null) {
            msg = image_FirebaseURL.toString()
        } else if (msg.equals("")) {
            return
        }

        var otherChat = Chat()
        otherChat.message = msg
        otherChat.name = "othername"
        otherChat.deleteby = ""
        otherChat.title = title
        otherChat.firebaseId = firebase_id
        otherChat.timestamp = ServerValue.TIMESTAMP
        otherChat.uid = otherUID
        otherChat.firebaseToken = firebaseToken
        otherChat.key = key.toString()
        otherChat.profilePic = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")

        var myChat = Chat()
        myChat.message = msg
        myChat.name = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
        myChat.deleteby = ""
        myChat.title = title
        myChat.firebaseId = firebase_id
        myChat.timestamp = ServerValue.TIMESTAMP
        myChat.uid = myUid
        myChat.firebaseToken = firebaseToken
        myChat.key = key.toString()
        myChat.profilePic = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")

        DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.child(chatNode)!!.child(key).setValue(myChat)
        DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.child(myUid)!!.child(otherUID).setValue(myChat)
        DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.child(otherUID)!!.child(myUid).setValue(otherChat)
        progressBar.visibility = View.GONE
        message.setText("")
        image_FirebaseURL = null

        var myName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")

        if (PreferenceConnector.readString(this, PreferenceConnector.ISNOTIFICATIONON, "").equals("ON")) {

            if(msg.startsWith("https://firebasestorage.googleapis.com/") ){
                sendPushNotificationToReceiver(title, "Image", myName, myUid, firebaseToken)
            }else{
                sendPushNotificationToReceiver(title, msg, myName, myUid, firebaseToken)
            }


        }

    }

    fun getChat() {
        FirebaseDatabase.getInstance()
                .reference
                .child(Constant.ARG_CHAT_ROOMS)
                .child(chatNode).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError?) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                var chat = dataSnapshot?.getValue(Chat::class.java)
                getChatDataInmap(chat!!)
                getBlockDate()

            }

            override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
                var chat = dataSnapshot?.getValue(Chat::class.java)
                getChatDataInmap(chat!!)
                getBlockDate()

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
               // var chat = dataSnapshot?.getValue(Chat::class.java)
               // getChatDataInmap(chat!!)
            }

        })
    }

    private fun getChatDataInmap(chat:Chat){
        var myImg = PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.USERPROFILEIMAGE, "")

        if (chat != null) {
            if (chat.deleteby.equals(myUid)) {
                return
            }else{
                map.put(chat.key,chat)
                var demoValues : Collection<Chat> = map.values
                chatList = ArrayList<Chat>(demoValues)


                    chatAdapter = ChattingAdapter(this@ChatActivity, chatList, userInfoFcm.profilePic, myImg.toString())
                    recycler_view.adapter = chatAdapter
                    recycler_view.scrollToPosition((map.size - 1))


            }
           // chat.key = chat!!.key
            //chatList.add(chat)
           // getChatDataInmap(chat)
        }
        shortList()

    }

    private fun shortList() {
        Collections.sort(chatList,object : Comparator<Chat> {
            override fun compare(a1: Chat?, a2: Chat?): Int {
                if (a1!!.timestamp == null || a2!!.timestamp == null)
                    return -1
                else {
                    val long1 :Long= a1.timestamp as Long
                    val long2 :Long= a2.timestamp as Long
                    return long1.compareTo(long2)
                }
            }

        })
        chatAdapter?.notifyDataSetChanged()
    }

    private fun gettingDataFromUserTable(firebaseUid: String) {
        DatabaseReference?.reference?.child(Constant.ARG_USERS)?.child(firebaseUid)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0?.getValue(UserInfoFCM::class.java) != null){
                            userInfoFcm = p0?.getValue(UserInfoFCM::class.java)!!
                            var myImg = PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.USERPROFILEIMAGE, "")

                            title_header.text = userInfoFcm.name
                            chatAdapter = ChattingAdapter(this@ChatActivity, chatList, userInfoFcm.profilePic, myImg.toString())
                            recycler_view.adapter = chatAdapter
                        }
                        recycler_view.scrollToPosition((map.size - 1))

                    }
                })
    }



    private fun captureImageFromCamera() {
        message.setText("")
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_CAMERA)
            } else {
                intentToCaptureImage()
            }
        } else {
            intentToCaptureImage()
        }
    }

    fun intentToCaptureImage() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Constant.REQUEST_CAMERA)
    }

    private fun getCardFile() {
        message.setText("")
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_FILE)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {

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
        if (requestCode == Constant.REQUEST_CAMERA) {
            if (data != null) {
                galleryBitMap = data.getExtras()!!.get("data") as Bitmap
                val bytes = ByteArrayOutputStream()
                galleryBitMap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                //profileImage.setImageBitmap(profileImageBitmap)
                val uri = getImageUri(this, galleryBitMap!!)
                creatFirebaseProfilePicUrl(uri)
                progressBar.visibility = View.VISIBLE
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

                galleryBitMap = ImageUtil.decodeFile(selectedImagePath)
                try {
                    galleryBitMap = ImageUtil.modifyOrientation(galleryBitMap!!, selectedImagePath)
                    val bytes = ByteArrayOutputStream()
                    galleryBitMap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    val uri = getImageUri(this, galleryBitMap!!)
                    creatFirebaseProfilePicUrl(uri)
                    progressBar.visibility = View.VISIBLE

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    fun creatFirebaseProfilePicUrl(selectedImageUri: Uri) {
        var storageRef: StorageReference
        var storage: FirebaseStorage
        var app: FirebaseApp

        app = FirebaseApp.getInstance()!!
        storage = FirebaseStorage.getInstance(app)

        storageRef = storage.getReference("chat_photos_togo" + getString(R.string.app_name))
        val photoRef = storageRef.child(selectedImageUri.lastPathSegment)
        photoRef.putFile(selectedImageUri).addOnCompleteListener(this, { task ->
            if (task.isSuccessful) {
                image_FirebaseURL = task.result.downloadUrl
                send_message.callOnClick()
            }
        })
    }

    private fun sendPushNotificationToReceiver(title: String, message: String, username: String, uid: String, firebaseToken: String) {

        FcmNotificationBuilder.initialize()
                .title(title)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(userInfoFcm.firebaseToken).send()
    }

    fun alertMSG(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            alertDialog.setCancelable(true)
        })

        alertDialog.show()
    }

    fun deleteChatDialog(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            for (allChat in chatList) {
                if (allChat.deleteby.equals(otherUID)) {
                    //should delete all chat here
                    DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.child(myUid)!!.child(otherUID).setValue(null)
                   // DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.child(otherUID)!!.child(myUid).setValue(null)
                    DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.child(chatNode)!!.child(allChat.key).setValue(null)

                } else if (allChat.deleteby.equals("")) {
                    allChat.deleteby = myUid
                    DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.child(chatNode)!!.child(allChat.key).setValue(allChat)
                    DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.child(myUid)!!.child(otherUID).setValue(null)
                   // DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.child(otherUID)!!.child(myUid).setValue(null)
                }
            }

            map.clear()
            chatList.clear()
            chatAdapter?.notifyDataSetChanged()
            alertDialog.setCancelable(true)
        })

        alertDialog.setNegativeButton("No",DialogInterface.OnClickListener{ dialog , which ->
            alertDialog.setCancelable(true)
        })
        alertDialog.show()
    }

    private fun getBlockDate() {
        DatabaseReference?.reference?.child(Constant.BlockUsers)?.child(chatNode)?.child(Constant.blockedBy)!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }
            override fun onDataChange(p0: DataSnapshot?) {
                if (p0?.getValue(String()::class.java) != null) {
                    blockedId = p0?.getValue(String()::class.java)!!

                    if (blockedId.equals("Both")) {
                        block_button.setImageResource(R.drawable.blocked_red)
                    } else if (blockedId.equals("")) {
                        block_button.setImageResource(R.drawable.blocked_grey)
                    } else if (blockedId.equals(otherUID)) {
                        block_button.setImageResource(R.drawable.blocked_grey)
                    } else if (blockedId.equals(myUid)) {
                        block_button.setImageResource(R.drawable.blocked_red) }

                }
            }
        })
    }

    fun blockChatDialog(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->

            if (blockedId.equals("Both")) {
                DatabaseReference?.reference?.child(Constant.BlockUsers)?.child(chatNode)?.child(Constant.blockedBy)!!.setValue(otherUID)
                block_button.setImageResource(R.drawable.blocked_red)
            }
            else if (blockedId.equals("")) {
                DatabaseReference?.reference?.child(Constant.BlockUsers)?.child(chatNode)?.child(Constant.blockedBy)!!.setValue(myUid)
                block_button.setImageResource(R.drawable.blocked_grey)
            }
            else if (blockedId.equals(otherUID)) {
                DatabaseReference?.reference?.child(Constant.BlockUsers)?.child(chatNode)?.child(Constant.blockedBy)!!.setValue("Both")
                block_button.setImageResource(R.drawable.blocked_red)
            }
            else if (blockedId.equals(myUid)) {
                DatabaseReference?.reference?.child(Constant.BlockUsers)?.child(chatNode)?.child(Constant.blockedBy)!!.setValue("")
                block_button.setImageResource(R.drawable.blocked_red)
            }

            getBlockDate()

            alertDialog.setCancelable(true)
        })

        alertDialog.setNegativeButton("No",DialogInterface.OnClickListener{ dialog , which ->
            alertDialog.setCancelable(true)
        })
        alertDialog.show()
    }
}