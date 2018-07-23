package com.togocourier.responceBean

/**
 * Created by mindiii on 23/1/18.
 */
class GetUserDataProfile {

    var status: String? = null
    var message: String? = null
    var userData: UserDataBean? = null


    class UserDataBean {

        var id: String? = null
        var fullName: String? = null
        var email: String? = null
        var password: String? = null
        var countryCode: String? = null
        var contactNo: String? = null
        var socialId: String? = null
        var socialType: String? = null
        var address: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var profileImage: String? = null
        var uploadIdCard: String? = null
        var userType: String? = null
        var deviceToken: String? = null
        var deviceType: String? = null
        var authToken: String? = null
        var chatId: String? = null
        var firebaseToken: String? = null
        var loginStatus: String? = null
        var verifyStatus: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
        var rating: Int = 0


    }
}