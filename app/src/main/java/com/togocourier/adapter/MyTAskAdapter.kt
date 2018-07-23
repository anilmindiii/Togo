package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.Interface.MyTaskListOnClick
import com.togocourier.R
import com.togocourier.responceBean.MyAllTaskResponce

import com.togocourier.util.Constant
import kotlinx.android.synthetic.main.item_my_pending_post.view.*

/**
 * Created by chiranjib on 6/12/17.
 */
class MyTAskAdapter(context: Context?, arrayList: List<MyAllTaskResponce.AppliedReqDataBean>?, myClick: MyTaskListOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var context = context
    var list = arrayList
    var myClick = myClick

    override fun getItemCount(): Int {

        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_my_pending_post, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var postData = list?.get(position)
        holder.bind(position, postData!!, myClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, postData: MyAllTaskResponce.AppliedReqDataBean, myClick: MyTaskListOnClick) = with(itemView) {
            itemView.title.text = postData?.postTitle
            itemView.itemQuntyTxt.text = postData?.quantity
            itemView.itemPrice.text = "$ " + postData?.bidPrice
            itemView.itemPickDt.text = postData?.collectiveDate + " " + Constant.setTimeFormat(postData?.collectiveTime!!)
            itemView.itemPickaddr.text = postData?.pickupAdrs
            Picasso.with(itemImage.context).load(postData?.postImage).placeholder(R.drawable.splash_logo).fit().into(itemImage)
            itemView.setOnClickListener {
                myClick.OnClick(postData?.postId!!,postData?.requestId!!)
            }

        }

    }
}