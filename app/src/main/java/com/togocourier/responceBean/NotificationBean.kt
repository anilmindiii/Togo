package com.togocourier.responceBean

/**
 * Created by ANIL on 30/1/18.
 */
class NotificationBean {

    var status: String? = null
    var message: String? = null
    var data: List<DataBean>? = null


    class DataBean {

        var postId: String? = null
        var requestId: String? = null
        var type: String? = null
        var sentById: String? = null
        var receiveById: String? = null
        var notiMsg: String? = null
        var crd: String? = null
        var fullName: String? = null
        var profileImage: String? = null
        var showtime: String? = null
    }
}