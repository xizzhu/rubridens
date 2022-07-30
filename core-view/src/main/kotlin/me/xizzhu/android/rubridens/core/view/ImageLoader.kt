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

package me.xizzhu.android.rubridens.core.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream

fun ImageView.load(url: String, @DrawableRes fallback: Int = 0, placeholder: Bitmap? = null) {
    if ((context as? Activity)?.isDestroyed == true) return

    if (url.isEmpty()) {
        if (fallback != 0) {
            Glide.with(context).load(fallback).into(this)
        } else if (placeholder != null) {
            Glide.with(context).load(placeholder).into(this)
        }
        return
    }

    val requestBuilder = Glide.with(context).load(url)
    if (placeholder != null) {
        requestBuilder.placeholder(BitmapDrawable(resources, placeholder))
    } else if (fallback != 0) {
        requestBuilder.placeholder(fallback)
    }
    requestBuilder.error(fallback)
        .into(this)
}

@GlideModule
@Excludes(OkHttpLibraryGlideModule::class)
class ApplicationGlideModule : AppGlideModule(), KoinComponent {
    private val okHttpClient: OkHttpClient by inject()

    override fun isManifestParsingEnabled(): Boolean = false

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)

        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)

        builder.setLogLevel(android.util.Log.ERROR)

        builder.addGlobalRequestListener(object : RequestListener<Any?> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Any?>?, isFirstResource: Boolean): Boolean = false

            override fun onResourceReady(resource: Any?, model: Any?, target: Target<Any?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean = false
        })

        builder.setDefaultRequestOptions(
            RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(DecodeFormat.PREFER_ARGB_8888)
        )
    }
}
