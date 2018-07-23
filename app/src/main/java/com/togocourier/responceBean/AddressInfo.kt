package com.togocourier.responceBean

/**
 * Created by mindiii on 5/2/18.
 */
class AddressInfo {


    var status: String? = null
    var message: String? = null
    var result: List<ResultBean>? = null


    class ResultBean {

        var pickupAdrs: String? = null
        var pickupLat: String? = null
        var pickupLong: String? = null
        var isCheckedItem:Boolean = false


    }
}