package com.togocourier.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.togocourier.Interface.getAddressListner
import com.togocourier.R
import com.togocourier.responceBean.DeliverAddressInfo
import kotlinx.android.synthetic.main.address_item_layout.view.*

/**
 * Created by Anil chourasiya on 5/2/18.
 */
class DeliveryAddressAdapter(var matchTxt:String,var addressList:ArrayList<DeliverAddressInfo.ResultBean>,
                             var address:getAddressListner) : RecyclerView.Adapter<RecyclerView.ViewHolder> (){



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.address_item_layout,parent,false)
        return ViewHolder(v,address)

    }

    override fun getItemCount(): Int {
       return addressList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        holder as ViewHolder
        holder.bind(addressList,position)


       if(matchTxt.equals(addressList.get(position).deliveryAdrs)){
           addressList.get(position).isCheckedItem = true

       }

        if(addressList.get(position).isCheckedItem){
            holder.itemView.checkbox.isChecked = true
        }else{
            holder.itemView.checkbox.isChecked = false
        }



        holder.itemView.setOnClickListener {
            for (value in addressList) {
                value.isCheckedItem = false
            }
            addressList.get(position).isCheckedItem = true
            address?.getAddress(addressList.get(position).deliveryAdrs.toString(),addressList.get(position).deliverLat.toString(),addressList.get(position).deliverLong.toString())
            notifyDataSetChanged()
        }



    }


    class ViewHolder(itemView: View, var address: getAddressListner) : RecyclerView.ViewHolder(itemView){

        fun bind(addressList: ArrayList<DeliverAddressInfo.ResultBean>, position: Int) = with(itemView){
            itemView.tv_address.text = addressList.get(position).deliveryAdrs
            itemView.checkbox.isChecked = addressList.get(position).isCheckedItem

        }


    }

}