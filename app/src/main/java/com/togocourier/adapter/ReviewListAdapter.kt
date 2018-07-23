package com.togocourier.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.R
import com.togocourier.responceBean.ReviewListInfo
import kotlinx.android.synthetic.main.reviewlist_items.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by mindiii on 8/2/18.
 */

class ReviewListAdapter (var reviewList : List<ReviewListInfo.ResultBean>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.reviewlist_items,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        holder.bind(reviewList,position)
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

        fun bind(reviewList: List<ReviewListInfo.ResultBean>, position: Int) = with(itemView) {
            itemView.user_name.text = reviewList.get(position).fullName
            itemView.ratingBar.rating = reviewList.get(position).rating?.toFloat()!!
            itemView.title.text = reviewList.get(position).title

            var date_before:String = reviewList.get(position).showtime.toString()

            itemView.showtime.text =  formateDateFromstring("dd/MM/yy", "MM/dd/yyyy", date_before);

            itemView.review.text = reviewList.get(position).review

            if(!reviewList.get(position).profileImage.equals("")){

                Picasso.with(context).load(reviewList.get(position).profileImage).placeholder(R.drawable.splash_logo).into(itemView.itemImage)
            }
        }


        fun formateDateFromstring(inputFormat: String, outputFormat: String, inputDate: String): String {

            var parsed: Date? = null
            var outputDate = ""

            val df_input = SimpleDateFormat(inputFormat, Locale.getDefault())
            val df_output = SimpleDateFormat(outputFormat, Locale.getDefault())

            try {
                parsed = df_input.parse(inputDate)
                outputDate = df_output.format(parsed)

            } catch (e: ParseException) {
                //Log(TAG, "ParseException - dateFormat");
            }

            return outputDate

        }

    }



}
