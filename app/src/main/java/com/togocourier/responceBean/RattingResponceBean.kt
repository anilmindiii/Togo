package com.togocourier.responceBean

/**
 * Created by mindiii on 22/1/18.
 */
class RattingResponceBean {

    var status: String? = null
    var message: String? = null
    var ratingreview: RatingreviewBean? = null


    class RatingreviewBean {

        var id: String? = null
        var postId: String? = null
        var giverId: String? = null
        var receiverId: String? = null
        var rating: String? = null
        var review: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null

    }
}