package cn.lightink.reader.ui.book

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import cn.lightink.reader.R
import cn.lightink.reader.ktx.openFullscreen
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.ui.base.LifecycleActivity
import kotlinx.android.synthetic.main.activity_book_source_auth.*

class BookOriginWebActivity: LifecycleActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private val url by lazy { intent.getStringExtra(EXTRA_URL).orEmpty() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openFullscreen()
        setContentView(R.layout.activity_book_source_auth)
        mAuthWebView.settings.javaScriptEnabled = true
        mAuthWebView.settings.useWideViewPort = true
        mAuthWebView.settings.loadWithOverviewMode = true
        mAuthWebView.settings.domStorageEnabled = true
        mAuthWebView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        mAuthWebView.webViewClient = buildClient()
        mAuthWebView.webChromeClient = object : WebChromeClient() {}

        mAuthWebView.loadUrl(url)

        mTopbar.setNavigationOnClickListener {
            finish()
        }

    }

    private fun buildClient() = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (!URLUtil.isNetworkUrl(request?.url?.toString())) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(request?.url?.toString())))
                } catch (e: ActivityNotFoundException) {
                    //未安装网站指定的应用
                    toast(R.string.booksource_auth_not_install)
                }
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mAuthWebView.loadUrl(intent?.dataString!!)
    }

    override fun onBackPressed() {
        if (mAuthWebView.canGoBack()) {
            mAuthWebView.goBack()
        } else { finish() }
    }
}