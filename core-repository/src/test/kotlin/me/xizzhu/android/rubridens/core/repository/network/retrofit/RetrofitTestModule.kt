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

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

internal val retrofitTestModule = module {
    single { MockWebServer() }
    single { Moshi.Builder().build() }
    single {
        OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .build()
    }
    factory { (instanceUrl: String) ->
        Retrofit.Builder()
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .baseUrl(get<MockWebServer>().url("/$instanceUrl/"))
                .build()
    }
}
