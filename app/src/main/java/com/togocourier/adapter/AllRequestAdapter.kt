package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.Interface.AcceptRejectListioner
import com.togocourier.R
import com.togocourier.responceBean.AllRequestListResponce

import kotlinx.android.synthetic.main.item_request_list.view.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import com.togocourier.ui.activity.ChatActivity


/**
 * Created by Anil on 6/12/17.
 */
class AllRequestAdapter(context: Context?, arrayList: ArrayList<AllRequestListResponce.AppliedReqDataBean>?,acceptReject: AcceptRejectListioner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var context = context
    var list = arrayList
    var acceptRejectListioner=acceptReject


    override fun getItemCount(): Int {

        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_request_list, parent, false)
        return ViewHolder(v,context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var requestData = list?.get(position)
        holder.bind(position, requestData,acceptRejectListioner)
    }

    class ViewHolder(itemView: View,var context: Context?) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, requestData: AllRequestListResponce.AppliedReqDataBean?, acceptRejectListioner: AcceptRejectListioner) = with(itemView) {
            itemView.itemNameTxt.text = requestData?.applyUserName
            itemView.itemPriceTxt.text = "$ "+requestData?.bidPrice
            itemView.ratingBar.rating = requestData?.rating?.toFloat()!!
            itemView.item_contact_num.text = requestData?.applyUserCountry+" - "+requestData?.applyUserContact

            if (!TextUtils.isEmpty(requestData?.applyUserImage)) {
                Picasso.with(itemImg.context).load(requestData?.applyUserImage).placeholder(R.drawable.splash_logo).fit().into(itemImg)
            }
            itemView.acceptLay.setOnClickListener {
                defaultDialog(acceptRejectListioner,requestData)
            }
            itemView.rejectLay.setOnClickListener {

                acceptRejectListioner.OnClick(requestData?.requestId!!,"cancel",requestData?.bidPrice.toString())
            }
             itemView.ly_ratting.setOnClickListener {
                 acceptRejectListioner.OnClickUserId(requestData?.applyUserId.toString(),requestData?.postUserId)
             }

            itemView.itemChatIcon.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("otherUID", requestData?.applyUserId.toString())
                intent.putExtra("title", requestData?.title.toString())
                context.startActivity(intent)
            }

        }


        fun defaultDialog(acceptRejectListioner: AcceptRejectListioner,
                          requestData: AllRequestListResponce.AppliedReqDataBean?) {

            val alertDialog = AlertDialog.Builder(context)

            alertDialog.setTitle("Alert")

            alertDialog.setMessage("First you need to complete the payment procedure.")

            alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                acceptRejectListioner.OnClick(requestData?.requestId!!,"accept",requestData?.bidPrice.toString())
            })

            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
            })
            alertDialog.show()

        }


    }



}