package com.togocourier.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.togocourier.R
import com.togocourier.adapter.ChatListAdapter
import com.togocourier.responceBean.Chat
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.title_bar.*
import java.util.*

class ChatFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var chatListAdapter: ChatListAdapter ?= null
    private var histortList = ArrayList<Chat>()
    var userList = ArrayList<UserInfoFCM>()
    private var map = HashMap<String,Chat>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view  =  inflater!!.inflate(R.layout.fragment_chat, container, false)

        activity?.tabRightIcon?.visibility = View.GONE
        chatListAdapter = ChatListAdapter(context!!,histortList)
        view.recycler_view.adapter = chatListAdapter
        getChatHistory(view)
        return view
    }

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    fun  getChatHistory(view: View){
        view.progressBar_chat.visibility = View.VISIBLE
        var myUid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID,"")

        FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0?.getValue() == null){
                    view.progressBar_chat.visibility  = View.GONE
                    view.no_chat_found.visibility  = View.VISIBLE
                }
            }

        })


        FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(myUid). limitToLast(20).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot?) {
                var chat  = p0?.getValue(Chat::class.java)!!
                //map.put(chat.uid,chat)
                var key = p0?.key
                val iter = histortList.iterator()
                while (iter.hasNext()) {
                    val str = iter.next()

                    if (str.uid.equals(key))
                        iter.remove()

                    if(histortList.size == 0){
                        view.no_chat_found.visibility  = View.VISIBLE
                    }else{
                        view.no_chat_found.visibility  = View.GONE
                    }
                }
                chatListAdapter?.notifyDataSetChanged()
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                var chat  = p0?.getValue(Chat::class.java)!!
                var key = p0?.key
                gettingDataFromUserTable(view,key!!,chat,"onChildChanged")
                view.no_chat_found.visibility  = View.GONE
                view.progressBar_chat.visibility  = View.GONE
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                var chat  = p0?.getValue(Chat::class.java)!!
                //map.put(chat.uid,chat)
                var key = p0?.key
                gettingDataFromUserTable(view,key!!,chat,"onChildAdded")
                view.no_chat_found.visibility = View.GONE
                view.progressBar_chat.visibility  = View.GONE
            }
        })
    }

    private fun gettingDataFromUserTable(view: View, id: String, chat: Chat, from:String) {

        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {

                var user = p0?.getValue(UserInfoFCM::class.java)
                userList.add(user!!)

                for (userValue in userList){
                    if(userValue.uid.equals(id)){
                        chat.profilePic = userValue.profilePic
                        chat.name = userValue.name
                        chat.firebaseToken = userValue.firebaseToken
                        chat.uid = id
                        chat.timestamp = chat.timestamp
                    }
                }

                map.put(chat.uid,chat)

                var demoValues : Collection<Chat> = map.values
                histortList = ArrayList<Chat>(demoValues)

                if(context != null){
                    chatListAdapter = ChatListAdapter(context!!,histortList)
                    view.recycler_view.adapter = chatListAdapter
                }else{
                    chatListAdapter?.notifyDataSetChanged()
                }
                shortList()
            }
        })
    }

    private fun shortList() {
        Collections.sort(histortList,object : Comparator<Chat> {
            override fun compare(a1: Chat?, a2: Chat?): Int {
                if (a1!!.timestamp == null || a2!!.timestamp == null)
                    return -1
                else {
                    val long1 :Long= a1.timestamp as Long
                    val long2 :Long= a2.timestamp as Long
                    return long2.compareTo(long1)
                }
            }

        })
        chatListAdapter?.notifyDataSetChanged()
    }
}
