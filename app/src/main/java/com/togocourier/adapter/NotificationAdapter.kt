package com.togocourier.adapter

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.R
import com.togocourier.responceBean.NotificationBean
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.PostDetailsActivity
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.notification_item.view.*

/**
 * Created by anil on 5/12/17.
 */
class NotificationAdapter(var activity: FragmentActivity, var notificationList: ArrayList<NotificationBean.DataBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var listItem = notificationList!!.get(position)
        holder.bind(listItem)


    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v : View = LayoutInflater.from(parent?.context).inflate(R.layout.notification_item,parent,false)
        return ViewHolder(v,activity)
    }

    override fun getItemCount(): Int {

        return notificationList.size
    }

    class ViewHolder(itemView: View, var activity: FragmentActivity):RecyclerView.ViewHolder(itemView){

        fun bind(notificationItem: NotificationBean.DataBean) = with(itemView){
            itemView.name.text = notificationItem.fullName
            itemView.day_ago.text = notificationItem.showtime
            itemView.noti_msg.text = notificationItem.notiMsg

            if(!notificationItem.profileImage.equals("")){

                Picasso.with(context).load(notificationItem.profileImage).placeholder(R.drawable.splash_logo).into(itemView.profile_img)
            }

            itemView.setOnClickListener {
                var type =  notificationItem.type
                var postId =  notificationItem.postId
                val requestId =  notificationItem.requestId

                if(type != null) {
                    if (type.equals("sendrequest")) {
                        var intent = Intent(context, PostDetailsActivity::class.java)
                        intent.putExtra("POSTID", postId)
                        intent.putExtra("FROM", Constant.newPost)
                        intent.putExtra("REQUESTID", "")
                        context.startActivity(intent)

                    } else if (type.equals("deliverScreen")) {
                        var intent = Intent(context, PostDetailsActivity::class.java)
                        intent.putExtra("POSTID", postId)
                        intent.putExtra("FROM", Constant.pendingPost)
                        intent.putExtra("REQUESTID", requestId)
                        context.startActivity(intent)

                    } else if (type.equals("updateScreen")) {
                        var intent = Intent(context, PostDetailsActivity::class.java)
                        intent.putExtra("POSTID", postId)
                        intent.putExtra("FROM", Constant.pendingTask)
                        intent.putExtra("REQUESTID", requestId)
                        context.startActivity(intent)

                    }else if(type.equals("addpost")){
                        var intent = Intent(activity, HomeActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                    else if(type.equals("review")){
                        var intent = Intent(activity,PostDetailsActivity::class.java)
                        intent.putExtra("POSTID", postId)
                        var userType = PreferenceConnector.readString(context,PreferenceConnector.USERTYPE,"")
                        if(userType.equals(Constant.CUSTOMER)){
                            intent.putExtra("FROM", Constant.completeTask)
                        }else{
                            intent.putExtra("FROM", Constant.MycompletedPost)

                        }
                        intent.putExtra("REQUESTID",requestId)
                        activity.startActivity(intent)
                    }

                }
            }
        }

        fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
            val backStackName = fragment.javaClass.name
            val fragmentManager = activity.supportFragmentManager
            val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
            if (!fragmentPopped) {
                val transaction = fragmentManager.beginTransaction()
                transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
                if (addToBackStack)
                    transaction.addToBackStack(backStackName)
                transaction.commit()
            }
        }
    }
}