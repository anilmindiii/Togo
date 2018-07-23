package com.togocourier.Interface

/**
 * Created by chiranjib on 13/12/17.
 */
interface AcceptRejectListioner {
    fun OnClick(id: String,status:String,bitPrice:String)
    fun OnClickUserId(userId: String, postUserId: String?)
}