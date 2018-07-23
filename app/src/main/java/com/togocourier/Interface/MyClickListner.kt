package com.togocourier.Interface

import android.app.Dialog

/**
 * Created by anil on 17/11/17.
 */
interface MyClickListner {

    fun getPassword(oldPassword: String, newPassword: String, openDialog: Dialog)
}