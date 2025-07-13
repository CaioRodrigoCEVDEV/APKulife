package com.example.ulife

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ajusta a janela para modo de tela cheia
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        // Configurações da WebView
        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
        }

        // Suporte a dialogs (alert, confirm, window.open etc.)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val newWebView = WebView(view!!.context)
                newWebView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        // Carrega tudo dentro da WebView
                        request?.url?.let { view?.loadUrl(it.toString()) }
                        return true
                    }
                }

                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }
        }

        // Intercepta links externos (como WhatsApp), mas deixa OAuth do Google dentro da WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return try {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        val host = Uri.parse(url).host.orEmpty()
                        // Hosts que permanecem na WebView (incluindo OAuth do Google)
                        val trustedHosts = listOf(
                            "server.isalvei.com.br",
                            "google.com",
                            "accounts.google.com",
                            "googleusercontent.com"
                        )
                        if (host !in trustedHosts) {
                            // Abre link não confiável no navegador externo
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            true
                        } else {
                            // Carrega dentro da WebView
                            false
                        }
                    } else {
                        false
                    }
                } catch (e: ActivityNotFoundException) {
                    Log.w("MainActivity", "Nenhum app encontrado para abrir: $url", e)
                    false
                } catch (e: Exception) {
                    Log.e("MainActivity", "Erro ao processar URL: $url", e)
                    false
                }
            }
        }

        WebView.setWebContentsDebuggingEnabled(true)
        webView.loadUrl("https://server.isalvei.com.br/login")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
