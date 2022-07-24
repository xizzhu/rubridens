/*
 * Copyright (C) 2022 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.rubridens.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi

@SuppressLint("SetJavaScriptEnabled")
class LoginWebView : WebView {
    var onPageLoaded: ((url: String, originalUrl: String) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        webViewClient = object : WebViewClient() {
            @RequiresApi(Build.VERSION_CODES.O)
            @CallSuper
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                // we don't want to kill the app
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                onPageLoaded?.invoke(url, view.originalUrl ?: "")
            }
        }
        settings.javaScriptEnabled = true

        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        clearHistory()
        clearCache(true)

        // disable auto-fill, see https://commonsware.com/blog/2017/06/13/securing-apps-android-8p0-autofill.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        } else {
            settings.saveFormData = false
        }
        clearFormData()
    }

    fun load(url: String) {
        if (url == originalUrl) {
            return
        }

        CookieManager.getInstance().removeAllCookies {
            CookieManager.getInstance().setAcceptCookie(true)
            loadUrl(url)
        }
    }
}
