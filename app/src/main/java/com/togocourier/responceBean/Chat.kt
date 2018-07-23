package com.togocourier.responceBean

import java.io.Serializable

/**
 * Created by anil on 12/12/17.
 */
class Chat : Serializable{

    var name: String = ""
    var firebaseId: String = ""
    var message: String = ""
    var timestamp: Any ?=null
    var deleteby: String = ""
    var firebaseToken: String = ""
    var title:String = ""
    var profilePic: String = ""
    var uid: String = ""
    var key: String = ""



  init {

      this.uid
      this.name
      this.firebaseId
      this.message
      this.timestamp
      this.deleteby
      this.firebaseToken
      this.profilePic

  }
}