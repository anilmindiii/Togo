package com.togocourier.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.togocourier.R
import com.togocourier.util.Constant
import kotlinx.android.synthetic.main.activity_term_condition.*

class TermConditionActivity : AppCompatActivity() {
    var URL = Constant.baseWebViewUrl+Constant.webviewURL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_condition)

        back.setOnClickListener {
            onBackPressed()
        }


        progressBar.visibility = View.VISIBLE
       webview.getSettings().setJavaScriptEnabled(true)
        webview.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                //progressBar.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)


            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                progressBar.visibility = View.GONE
            }

        })
        webview.loadUrl(URL)

        back.setOnClickListener {
           onBackPressed()
        }
    }
}
