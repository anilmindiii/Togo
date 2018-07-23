package com.togocourier.responceBean

/**
 * Created by chiranjib on 8/12/17.
 */
class MyPostResponce {

    /**
     * status : success
     * message :
     * userData : [{"id":"10","userId":"4","title":"ygdd","description":"normal","otherDetails":"it's urgent","pickupAdrs":"Indore, Madhya Pradesh, India","pickupLat":"22.719568","pickupLong":"75.857727","deliveryAdrs":"Dewas, Madhya Pradesh, India","deliverLat":"22.962267","deliverLong":"76.050797","collectiveDate":"30/12/2017","collectiveTime":"05:08:00","deliveryDate":"30/12/2017","deliveryTime":"17:08:00","quantity":"5","price":"100","itemImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/0637b03aafa812d18c43e9cc5ded240f.jpg","receiptImage":"","orderNo":"","postStatus":"pending","status":"1","crd":"2017-12-30 06:42:28","upd":"0000-00-00 00:00:00","requestData":[{"id":"14","cInfoId":"10","postUserId":"4","applyUserId":"5","bidPrice":"25.5","requestStatus":"accept","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2017-12-30 11:21:41","upd":"0000-00-00 00:00:00"}]}]
     */

    private var status: String? = null
    private var message: String? = null
    public var isTrack: String? = null
    private var userData: List<UserDataBean>? = null

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

    fun getUserData(): List<UserDataBean>? {
        return userData
    }

    fun setUserData(userData: List<UserDataBean>) {
        this.userData = userData
    }

    class UserDataBean {
        /**
         * id : 10
         * userId : 4
         * title : ygdd
         * description : normal
         * otherDetails : it's urgent
         * pickupAdrs : Indore, Madhya Pradesh, India
         * pickupLat : 22.719568
         * pickupLong : 75.857727
         * deliveryAdrs : Dewas, Madhya Pradesh, India
         * deliverLat : 22.962267
         * deliverLong : 76.050797
         * collectiveDate : 30/12/2017
         * collectiveTime : 05:08:00
         * deliveryDate : 30/12/2017
         * deliveryTime : 17:08:00
         * quantity : 5
         * price : 100
         * itemImage : http://mindiii.com/togocourier/uploads/courierpost/thumb/0637b03aafa812d18c43e9cc5ded240f.jpg
         * receiptImage :
         * orderNo :
         * postStatus : pending
         * status : 1
         * crd : 2017-12-30 06:42:28
         * upd : 0000-00-00 00:00:00
         * requestData : [{"id":"14","cInfoId":"10","postUserId":"4","applyUserId":"5","bidPrice":"25.5","requestStatus":"accept","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2017-12-30 11:21:41","upd":"0000-00-00 00:00:00"}]
         */

        var id: String? = null
        var userId: String? = null
        var title: String? = null
        var description: String? = null
        var otherDetails: String? = null
        var pickupAdrs: String? = null
        var pickupLat: String? = null
        var pickupLong: String? = null
        var deliveryAdrs: String? = null
        var deliverLat: String? = null
        var deliverLong: String? = null
        var collectiveDate: String? = null
        var collectiveTime: String? = null
        var deliveryDate: String? = null
        var deliveryTime: String? = null
        var quantity: String? = null
        var price: String? = null
        var itemImage: String? = null
        var receiptImage: String? = null
        var orderNo: String? = null
        var postStatus: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
        var requestData: List<RequestDataBean>? = null

        class RequestDataBean {
            /**
             * id : 14
             * cInfoId : 10
             * postUserId : 4
             * applyUserId : 5
             * bidPrice : 25.5
             * requestStatus : accept
             * deliveryStatus : pending
             * paymentStatus : 0
             * payBy : 0
             * status : 1
             * crd : 2017-12-30 11:21:41
             * upd : 0000-00-00 00:00:00
             * applyUserName:"pranjal"
             */

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
            var applyUserName: String? = null
            var applyUserCountry: String? = null
            var applyUserContact: String? = null
        }
    }
}