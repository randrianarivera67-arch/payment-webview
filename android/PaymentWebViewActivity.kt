package com.smsgateway.payment

/* =========================================================================
 *  PaymentWebViewActivity
 *  WebView ho an'ny dépôt / retrait (ULTRA PREMIUM).
 *
 *  Rehefa tsindrin'ny client ny bouton "COMPOSER LE CODE":
 *    → ny pejy web miantso  AndroidBridge.dialUssd(code)  na  tel:<code>
 *    → ity activity ity manao ACTION_CALL → mandeha mivantana ny USSD
 *    → ny operateur no mangataka ny PIN, ny CLIENT no manoratra azy
 *
 *  ⚠️ PERMISSION ilaina ao amin'ny AndroidManifest.xml:
 *      <uses-permission android:name="android.permission.CALL_PHONE" />
 *  ary mila angatahina amin'ny runtime (Android 6+).
 * ========================================================================= */

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PaymentWebViewActivity : Activity() {

    companion object {
        // ⚠️ Ovay amin'ny URL Vercel-nao (na PAYMENT_URL + "?order=..&token=..")
        private const val PAYMENT_URL = "https://votre-site-payment.vercel.app"
        private const val REQ_CALL = 1001
    }

    private lateinit var webView: WebView
    private var orderId = ""
    private var token = ""
    private var pendingUssd: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = intent.getStringExtra("orderId") ?: ""
        token = intent.getStringExtra("token") ?: ""

        webView = WebView(this)
        setContentView(webView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webView.addJavascriptInterface(WebBridge(), "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            // Tononin'ny pejy ny tel: → atao ACTION_CALL (mandeha ny USSD)
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && url.startsWith("tel:")) {
                    runUssd(Uri.decode(url.removePrefix("tel:")))
                    return true
                }
                return false
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                val js = "window.PaymentApp && window.PaymentApp.setContext(" +
                        "{orderId:'$orderId', token:'$token'});"
                view?.evaluateJavascript(js, null)
            }
        }
        webView.loadUrl(PAYMENT_URL)
    }

    // Ampandehanina ny USSD (mila CALL_PHONE)
    private fun runUssd(code: String) {
        val ussd = Uri.encode(code)              // # → %23 ho an'ny USSD
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ussd"))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            pendingUssd = code
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQ_CALL)
        }
    }

    override fun onRequestPermissionsResult(rc: Int, perms: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(rc, perms, res)
        if (rc == REQ_CALL && res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED) {
            pendingUssd?.let { runUssd(it) }
        } else {
            // raha lavina ny permission → manokatra ny dialer fotsiny
            pendingUssd?.let { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(it)))) }
        }
        pendingUssd = null
    }

    private inner class WebBridge {
        @JavascriptInterface fun getOrderId(): String = orderId
        @JavascriptInterface fun getAuthToken(): String = token

        // Antsoin'ny bouton "COMPOSER LE CODE"
        @JavascriptInterface
        fun dialUssd(code: String) = runOnUiThread { runUssd(code) }

        @JavascriptInterface
        fun onPaymentComplete(status: String, completedOrderId: String) {
            runOnUiThread {
                setResult(RESULT_OK, Intent().apply {
                    putExtra("status", status); putExtra("orderId", completedOrderId)
                })
                finish()
            }
        }
        @JavascriptInterface fun close() = runOnUiThread { finish() }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
