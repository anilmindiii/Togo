package com.togocourier.util

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.togocourier.R

/**
 * Created by chiranjib on 5/12/17.
 */
class ProgressDialog(internal var context: Context) : Dialog(context) {
    init {
        // This is the fragment_search_details XML file that describes your Dialog fragment_search_details
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.progress_dialog)
    }
}