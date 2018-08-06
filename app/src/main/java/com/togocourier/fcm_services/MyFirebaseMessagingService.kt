package com.togocourier.fcm_services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.togocourier.R
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.ui.activity.HomeActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage?.getData()
        Log.e("FROM", remoteMessage?.getFrom())
        var type = data?.get("type")
        if(type.equals("chat")){
            sendChatNotification(data!!)
        }else{
            sendNotification(data!!)
        }

    }

    private fun sendNotification(data: Map<String, String>) {

        var receiveById = data?.get("receiveById")
        var type = data?.get("type")
        var title = data?.get("title")
        var message = data?.get("body")

        if(message.equals("")){

            var message = data?.get("notiMsg")
        }

        var postId = data?.get("postId")
        var requestId = data?.get("requestId")
        var sentById = data?.get("sentById")


        val icon = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("type",type)
        intent.putExtra("postId",postId)
        intent.putExtra("requestId",requestId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val CHANNEL_ID = "my_channel_01"// The id of the channel.
        val name = "Abc"// The user-visible name of the channel.
        val importance = NotificationManager.IMPORTANCE_HIGH
        var mChannel: NotificationChannel? = null


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TOGO")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.app_icon)
            notificationBuilder.color = resources.getColor(R.color.colorPrimary)
        } else {
            notificationBuilder.setSmallIcon(R.drawable.app_icon)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)

        }
        notificationManager.notify(5, notificationBuilder.build())


       /* val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("TOGO")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(5, notificationBuilder.build())*/
    }

    private fun sendChatNotification(data: Map<String, String>) {
        var username = data?.get("username")
        var ChatTitle = data?.get("username")
        var fcm_token = data?.get("fcm_token")
        var title = data?.get("title")

        var message = data?.get("text")
        if (message!!.startsWith("https://firebasestorage.googleapis.com/") || message.startsWith("content://")){
            message = "Image"
        }

        var opponentId = data?.get("uid")

        val icon = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("otherUID", opponentId)
        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

      /*  val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)*/
       /* val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(ChatTitle)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(5, notificationBuilder.build())
    */
        /*................................................................*/

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val CHANNEL_ID = "my_channel_01"// The id of the channel.
        val name = "Abc"// The user-visible name of the channel.
        val importance = NotificationManager.IMPORTANCE_HIGH
        var mChannel: NotificationChannel? = null


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(ChatTitle)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.app_icon)
            notificationBuilder.color = resources.getColor(R.color.colorPrimary)
        } else {
            notificationBuilder.setSmallIcon(R.drawable.app_icon)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)

        }
        notificationManager.notify(5, notificationBuilder.build())














    }
}