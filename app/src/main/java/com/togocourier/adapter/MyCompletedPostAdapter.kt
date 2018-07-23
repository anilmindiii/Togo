package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.responceBean.MyPostResponce
import com.togocourier.util.Constant
import kotlinx.android.synthetic.main.item_my_pending_post.view.*

/**
 * Created by mindiii on 22/1/18.
 */
class MyCompletedPostAdapter (context: Context?, arrayList: ArrayList<MyPostResponce.UserDataBean>?, click: MyOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var context = context
    var list = arrayList
    var myOnClick=click


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
        holder.bind(position, postData,myOnClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, postData: MyPostResponce.UserDataBean?, click: MyOnClick) = with(itemView) {
            //    var asign=/*postData?.requestData?.get(0)?.applyUserId*/

            itemView.asignLay.visibility = View.VISIBLE
            itemView.contactLay.visibility = View.VISIBLE

           // if(postData?.requestData?.)

            if(postData?.requestData?.get(0)?.applyUserName != null){
                itemView.itemAsignTo.text  = postData?.requestData?.get(0)?.applyUserName.toString()
                itemView.contact_num.text = postData?.requestData?.get(0)?.applyUserCountry.toString() +" - "+postData?.requestData?.get(0)?.applyUserContact.toString()
            }

            itemView.title.text = postData?.title
            itemView.itemQuntyTxt.text = postData?.quantity
            itemView.itemPrice.text = "$ " + postData?.requestData?.get(0)?.bidPrice
            itemView.itemPickDt.text =  postData?.collectiveDate+ " " + Constant.setTimeFormat(postData?.collectiveTime!!)
            itemView.itemPickaddr.text =  postData?.pickupAdrs
            Picasso.with(itemImage.context).load(postData?.itemImage).placeholder(R.drawable.splash_logo).fit().into(itemImage)
            itemView.setOnClickListener {
                click.OnClick(postData?.id!!,postData.requestData?.get(0)?.id.toString())
            }
        }

    }
}