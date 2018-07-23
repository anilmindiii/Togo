package com.togocourier.responceBean

/**
 * Created by mindiii on 13/2/18.
 */
class BankInfoBean {

    var status: String? = null
    var message: String? = null
    var result: ResultBean? = null

    class ResultBean {

        var id: String? = null
        var userId: String? = null
        var cardId: String? = null
        var cardDigit: String? = null
        var cardCustomerId: String? = null
        var bankCustomerId: String? = null
        var bankId: String? = null
        var bankAccId: String? = null
        var bankAccNumber: String? = null
        var accountHolder: String? = null
        var address: String? = null
        var cardStatus: String? = null
        var bankStatus: String? = null
        var crd: String? = null
        var upd: String? = null

    }
}