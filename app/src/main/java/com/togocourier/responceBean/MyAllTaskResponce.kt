package com.togocourier.responceBean

/**
 * Created by chiranjib on 2/1/18.
 */
class MyAllTaskResponce {

    /**
     * status : success
     * message :
     * appliedReqData : [{"requestId":"27","postId":"12","id":"27","cInfoId":"12","postUserId":"30","applyUserId":"5","bidPrice":"250","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2018-01-02 10:34:54","upd":"0000-00-00 00:00:00","profileImage":"http://mindiii.com/togocourier/uploads/profileImage/thumb/","fullName":"pankaj","address":"Bhopal, Madhya Pradesh, India","postTitle":"Keyboard","postImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/ce2fef05728af28df7a0c064779de88d.png","collectiveDate":"31/12/2017","collectiveTime":"11:00:00","pickupAdrs":"Toronto, ON, Canada","quantity":"5","postPrice":"500"},{"requestId":"26","postId":"14","id":"26","cInfoId":"14","postUserId":"4","applyUserId":"5","bidPrice":"150","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2018-01-02 09:08:42","upd":"0000-00-00 00:00:00","profileImage":"http://mindiii.com/togocourier/uploads/profileImage/thumb/","fullName":"pranjal","address":"Toronto, ON, Canada","postTitle":"My Phone","postImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/f99690f012abcf3b0d653108ab42dc40.jpg","collectiveDate":"03/01/2018","collectiveTime":"15:03:00","pickupAdrs":"Indore, Madhya Pradesh, India","quantity":"3","postPrice":"150.4"},{"requestId":"25","postId":"13","id":"25","cInfoId":"13","postUserId":"25","applyUserId":"5","bidPrice":"400","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2018-01-02 09:03:33","upd":"0000-00-00 00:00:00","profileImage":"http://mindiii.com/togocourier/uploads/profileImage/thumb/","fullName":"ram","address":"India","postTitle":"gitar","postImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/3dd7fe394c12de867f3209e0a631c219.png","collectiveDate":"02/01/2018","collectiveTime":"11:35:00","pickupAdrs":"Baba Kharak Singh Rd, Block 1, Kali Bari Lane, Type III, President's Estate, New Delhi, Delhi 110001, India","quantity":"23","postPrice":"233"},{"requestId":"17","postId":"8","id":"17","cInfoId":"8","postUserId":"30","applyUserId":"5","bidPrice":"99.9","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2017-12-30 13:41:10","upd":"0000-00-00 00:00:00","profileImage":"http://mindiii.com/togocourier/uploads/profileImage/thumb/","fullName":"pankaj","address":"Bhopal, Madhya Pradesh, India","postTitle":"Bottle","postImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/7b0baeac109949e62d8fac3ed9c3c260.png","collectiveDate":"29/12/2017","collectiveTime":"20:02:00","pickupAdrs":"Toronto, ON, Canada","quantity":"5","postPrice":"500"},{"requestId":"13","postId":"9","id":"13","cInfoId":"9","postUserId":"30","applyUserId":"5","bidPrice":"25.5","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2017-12-30 08:53:45","upd":"0000-00-00 00:00:00","profileImage":"http://mindiii.com/togocourier/uploads/profileImage/thumb/","fullName":"pankaj","address":"Bhopal, Madhya Pradesh, India","postTitle":"browser","postImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/1c6b8cffa1ecbb734619429310955c5a.png","collectiveDate":"30/12/2017","collectiveTime":"12:00:00","pickupAdrs":"502, Krishna Tower, Above ICICI Bank, Pipliyahana Main Road, Brajeshwari Extension, Greater Brajeshwari, Indore, Madhya Pradesh 452001, India","quantity":"2","postPrice":"120"},{"requestId":"2","postId":"2","id":"2","cInfoId":"2","postUserId":"30","applyUserId":"5","bidPrice":"400","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2017-12-29 05:40:51","upd":"0000-00-00 00:00:00","profileImage":"http://mindiii.com/togocourier/uploads/profileImage/thumb/","fullName":"pankaj","address":"Bhopal, Madhya Pradesh, India","postTitle":"Mouse","postImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/a2080c93f0a0474c98ce851edd9fc9f8.png","collectiveDate":"29/12/2017","collectiveTime":"10:00:00","pickupAdrs":"Toronto, ON, Canada","quantity":"5","postPrice":"500"}]
     */

    private var status: String? = null
    private var message: String? = null
    private var appliedReqData: List<AppliedReqDataBean>? = null

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String) {
        this.status = status
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getAppliedReqData(): List<AppliedReqDataBean>? {
        return appliedReqData
    }

    fun setAppliedReqData(appliedReqData: List<AppliedReqDataBean>) {
        this.appliedReqData = appliedReqData
    }

    class AppliedReqDataBean {
        /**
         * requestId : 27
         * postId : 12
         * id : 27
         * cInfoId : 12
         * postUserId : 30
         * applyUserId : 5
         * bidPrice : 250
         * requestStatus : pending
         * deliveryStatus : pending
         * paymentStatus : 0
         * payBy : 0
         * status : 1
         * crd : 2018-01-02 10:34:54
         * upd : 0000-00-00 00:00:00
         * profileImage : http://mindiii.com/togocourier/uploads/profileImage/thumb/
         * fullName : pankaj
         * address : Bhopal, Madhya Pradesh, India
         * postTitle : Keyboard
         * postImage : http://mindiii.com/togocourier/uploads/courierpost/thumb/ce2fef05728af28df7a0c064779de88d.png
         * collectiveDate : 31/12/2017
         * collectiveTime : 11:00:00
         * pickupAdrs : Toronto, ON, Canada
         * quantity : 5
         * postPrice : 500
         */

        var requestId: String? = null
        var postId: String? = null
        var id: String? = null
        var cInfoId: String? = null
        var postUserId: String? = null
        var applyUserId: String? = null
        var bidPrice: String? = null
        var requestStatus: String? = null
        var deliveryStatus: String? = null
        var paymentStatus: String? = null
        var payBy: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
        var profileImage: String? = null
        var fullName: String? = null
        var address: String? = null
        var postTitle: String? = null
        var postImage: String? = null
        var collectiveDate: String? = null
        var collectiveTime: String? = null
        var pickupAdrs: String? = null
        var quantity: String? = null
        var postPrice: String? = null
    }
}