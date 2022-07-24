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

import com.squareup.moshi.JsonDataException
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Retrofit
import retrofit2.create

internal object RetrofitFactory {
    fun get(instanceUrl: String): Retrofit {
        val retrofit: Retrofit by inject(Retrofit::class.java) { parametersOf(instanceUrl) }
        return retrofit
    }
}

internal inline fun <reified T, R> request(instanceUrl: String, block: T.() -> R) = try {
    block(RetrofitFactory.get(instanceUrl).create())
} catch (e: retrofit2.HttpException) {
    throw NetworkException.HttpError(
            code = e.code(),
            error = e.response()?.errorBody()?.string()?.let { MastodonError.fromJson(it)?.toErrorInfo() },
            cause = e,
    )
} catch (e: JsonDataException) {
    throw NetworkException.MalformedResponseError(e)
} catch (e: Exception) {
    throw NetworkException.Other(e)
}
