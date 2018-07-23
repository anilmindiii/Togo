package com.togocourier.responceBean

/**
 * Created by chiranjib on 5/12/17.
 */
class RegistrationResponce {
    /**
     * status : success
     * message : User authentication successfully done!
     * userData : {"id":"6","email":"test1@gmail.com","socialId":"","socialType":"","profileImage":"http://mindiii.com/togocourier/uploads/profile/thumb/c7841de8eaa2ba8cd2b8f6d347503743.jpg","fullName":"test","countryCode":"+91","contactNo":"123456789","address":"Indore, Madhya Pradesh, India","latitude":"22.719568699999996","longitude":"75.8577258","userType":"1","authToken":"c006247edba0527ea2b9091ef66f32be2c8bcc75","chatId":"","firebaseToken":""}
     */

    private var status: String? = null
    private var message: String? = null
    private var userData: UserDataBean? = null

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

    fun getUserData(): UserDataBean? {
        return userData
    }

    fun setUserData(userData: UserDataBean) {
        this.userData = userData
    }

    class UserDataBean {
        /**
         * id : 6
         * email : test1@gmail.com
         * socialId :
         * socialType :
         * profileImage : http://mindiii.com/togocourier/uploads/profile/thumb/c7841de8eaa2ba8cd2b8f6d347503743.jpg
         * fullName : test
         * countryCode : +91
         * contactNo : 123456789
         * address : Indore, Madhya Pradesh, India
         * latitude : 22.719568699999996
         * longitude : 75.8577258
         * userType : 1
         * authToken : c006247edba0527ea2b9091ef66f32be2c8bcc75
         * chatId :
         * firebaseToken :
         */

        var id: String? = null
        var email: String? = null
        var socialId: String? = null
        var socialType: String? = null
        var profileImage: String? = null
        var fullName: String? = null
        var countryCode: String? = null
        var contactNo: String? = null
        var address: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var userType: String? = null
        var authToken: String? = null
        var chatId: String? = null
        var firebaseToken: String? = null
        var notificationStatus: String? = null
        var uploadIdCard: String? = null
        var rating: Int = 0
    }
}