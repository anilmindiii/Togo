package com.togocourier.responceBean

/**
 * Created by mindiii on 5/2/18.
 */
class DeliverAddressInfo{

    var status: String? = null
    var message: String? = null
    var result: List<ResultBean>? = null


    class ResultBean {


        var deliveryAdrs: String? = null
        var deliverLat: String? = null
        var deliverLong: String? = null
        var isCheckedItem:Boolean = false


    }
}