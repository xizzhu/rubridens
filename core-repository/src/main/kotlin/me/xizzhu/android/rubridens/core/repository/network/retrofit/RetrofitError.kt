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

package me.xizzhu.android.rubridens.core.repository.network.retrofit

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import org.koin.java.KoinJavaComponent.inject

/**
 * See https://docs.joinmastodon.org/entities/error/
 */
@JsonClass(generateAdapter = true)
internal class MastodonError(
    @Json(name = "error") val error: String = "",
    @Json(name = "error_description") val description: String = "",
) {
    companion object {
        private val adapter: JsonAdapter<MastodonError> by lazy {
            val moshi: Moshi by inject(Moshi::class.java)
            moshi.adapter(MastodonError::class.java)
        }

        fun fromJson(string: String): MastodonError? = kotlin.runCatching { adapter.fromJson(string) }.getOrNull()
    }

    fun toErrorInfo(): NetworkException.HttpError.ErrorInfo = NetworkException.HttpError.ErrorInfo(
        error = error,
        description = description
    )
}
