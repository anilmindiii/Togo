package com.togocourier.responceBean

/**
 * Created by mindiii on 8/2/18.
 */
class ReviewListInfo {

    var status: String? = null
    var message: String? = null
    var result: List<ResultBean>? = null

    class ResultBean {

        var title: String? = null
        var postId: String? = null
        var receiverId: String? = null
        var rating: String? = null
        var review: String? = null
        var fullName: String? = null
        var profileImage: String? = null
        var showtime: String? = null

    }
}