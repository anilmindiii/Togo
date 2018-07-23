package com.togocourier.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.R
import com.togocourier.responceBean.Chat
import com.togocourier.ui.activity.ChatActivity
import kotlinx.android.synthetic.main.chat_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by anil on 12/12/17.
 */
class ChatListAdapter (var mContext: Context, histortList: ArrayList<Chat>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var histortList = histortList

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var chat  = histortList.get(position)
        holder.bind(position,chat)
    }

    override fun getItemCount(): Int {
        return histortList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_list_item,parent,false)
        return ViewHolder(v,mContext)
    }

    class ViewHolder(itemView: View, mContext: Context): RecyclerView.ViewHolder(itemView){


        fun bind(position: Int, chat: Chat) = with(itemView){

            if(chat.message.startsWith("https://firebasestorage.googleapis.com/") ){
                itemView.last_meassage.text = "Image uploaded"
            }else{
                itemView.last_meassage.text = chat.message
            }
            var sfd =  SimpleDateFormat("hh:mm a")
            try {
                itemView.date_time.text = sfd.format( Date(chat.timestamp as Long))
            }catch (ex :IllegalArgumentException){

            }

            itemView.title.text = chat.title
            itemView.opponent_name.text = chat.name

            if(!chat.profilePic.equals("")) Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.splash_logo).into(itemView.image_opponents)



            itemView.setOnClickListener {

                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("otherUID", chat.uid)
                intent.putExtra("title", chat.title)
                //intent.putExtra("chat", chat)
                context.startActivity(intent)
            }
        }

        fun converteTimestamp1(mileSegundos: String): CharSequence {
            return DateUtils.getRelativeTimeSpanString(java.lang.Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }
    }


}