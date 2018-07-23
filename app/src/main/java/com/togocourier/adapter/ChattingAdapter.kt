package com.togocourier.adapter

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.squareup.picasso.Picasso
import com.togocourier.R
import com.togocourier.responceBean.Chat
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.chat_left_side_view.view.*
import kotlinx.android.synthetic.main.chat_right_side_view.view.*
import kotlinx.android.synthetic.main.full_image_view_dialog.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by anil on 12/12/17.
 **/

private val VIEW_TYPE_ME  = 1
private val VIEW_TYPE_OTHER = 2


class ChattingAdapter (mContext:Context,chatList : ArrayList<Chat>,var otherSideImage:String,var myImg:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    private var chatList = chatList
    var mContext = mContext
    var uid = PreferenceConnector.readString(mContext,PreferenceConnector.USERID,"")



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var v : View? =  null

        when(viewType){
            VIEW_TYPE_ME -> {
                 v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_right_side_view,parent,false)
                return MyChatViewHolder(v!!,mContext)
            }
            VIEW_TYPE_OTHER ->{
                 v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_left_side_view,parent,false)
                return OtherChatViewHolder(v!!,mContext)
            }
        }

        return OtherChatViewHolder(v!!,mContext)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        var chat = chatList.get(position)

        if (TextUtils.equals(chatList.get(position).uid, uid)) {
            holder as MyChatViewHolder
            holder.bind(chat,myImg)
        } else {
            holder as OtherChatViewHolder
            holder.bind1(chat,otherSideImage)

        }


    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
         if (TextUtils.equals(chatList.get(position).uid,uid )) {
             return VIEW_TYPE_ME
        } else {
             return VIEW_TYPE_OTHER
        }
    }

    class MyChatViewHolder(itemView: View, mContext: Context):RecyclerView.ViewHolder(itemView){
        var mContext = mContext
        fun bind(chat: Chat, myImg: String) = with(itemView){
            //var sfd =  SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            var sfd =  SimpleDateFormat("hh:mm a")
            try {
                itemView.time_ago_.text = sfd.format( Date(chat.timestamp as Long))
            }catch (ex :IllegalArgumentException){

            }

            if (chat.message.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
                Picasso.with(context).load(chat.message).placeholder(R.drawable.splash_logo).into(itemView.chat_my_image_view)
                itemView.chat_my_image_view.visibility = View.VISIBLE
                itemView.my_message.visibility = View.GONE
            } else {
                itemView.my_message.visibility = View.VISIBLE
                itemView.my_message.text = chat.message
                itemView.chat_my_image_view.visibility = View.GONE
            }

            if(!myImg.equals("") && myImg != null){
                Picasso.with(mContext).load(myImg).placeholder(R.drawable.splash_logo).into(itemView.mySideImage)
            }

            itemView.chat_my_image_view.setOnClickListener {
                full_screen_photo_dialog(chat.message)
            }

        }

        fun full_screen_photo_dialog(image_url: String) {
            val openDialog = Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            openDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            openDialog.setContentView(R.layout.full_image_view_dialog)
            if(!image_url.equals("") && image_url != null){
                Picasso.with(mContext).load(image_url).placeholder(R.drawable.splash_logo).into(openDialog.photo_view)
            }
            openDialog.iv_back.setOnClickListener {
                openDialog.dismiss()
            }

            openDialog.show()
        }

    }

    class OtherChatViewHolder(itemView: View, mContext: Context):RecyclerView.ViewHolder(itemView){
        var mContext = mContext
        fun bind1(chat: Chat, otherSideImage: String) = with(itemView){

            var sfd =  SimpleDateFormat("hh:mm a");

            try {
                itemView.time_ago.text =  sfd.format( Date(chat.timestamp as Long))
            }catch (ex :IllegalArgumentException){

            }

            if (chat.message.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
                Picasso.with(context).load(chat.message).placeholder(R.drawable.splash_logo).into(itemView.chat_other_image_view)
                itemView.chat_other_image_view.visibility = View.VISIBLE
                itemView.other_message.visibility = View.GONE
            } else {
                itemView.other_message.visibility = View.VISIBLE
                itemView.other_message.text = chat.message
                itemView.chat_other_image_view.visibility = View.GONE
            }

            if(!otherSideImage.equals("") && otherSideImage != null){
                Picasso.with(mContext).load(otherSideImage).placeholder(R.drawable.splash_logo).into(itemView.profile_image_other_side)
            }

            itemView.chat_other_image_view.setOnClickListener {
                full_screen_photo_dialog(chat.message)
            }
        }

        fun full_screen_photo_dialog(image_url: String) {
            val openDialog = Dialog(mContext,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            openDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            openDialog.setContentView(R.layout.full_image_view_dialog)
            if(!image_url.equals("") && image_url != null){
                Picasso.with(mContext).load(image_url).placeholder(R.drawable.splash_logo).into(openDialog.photo_view)
            }
            openDialog.iv_back.setOnClickListener {
                openDialog.dismiss()
            }

            openDialog.show()
        }
    }


}