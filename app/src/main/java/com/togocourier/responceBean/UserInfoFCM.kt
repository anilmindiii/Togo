package com.togocourier.responceBean

import java.io.Serializable

/**
 * Created by mindiii on 6/9/17.
 */
class UserInfoFCM :Serializable {

    var notificationStatus :String = ""
    var uid :String = ""
    var email:String = ""
    var name :String = ""
    var firebaseToken:String = ""
    var profilePic:String = ""

  init {
      this.notificationStatus
      this.uid
      this.email
      this.name
      this.firebaseToken
      this.profilePic
  }


}
