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

package me.xizzhu.android.rubridens.core.repository.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * See https://docs.joinmastodon.org/methods/accounts/
 */
internal interface MastodonAccountsService {
    @GET("api/v1/accounts/verify_credentials")
    suspend fun verifyCredentials(@Header("Authorization") authorization: String): MastodonAccount
}

/**
 * See https://docs.joinmastodon.org/entities/account/
 */
@JsonClass(generateAdapter = true)
internal class MastodonAccount(
        @Json(name = "id") val id: String,
        @Json(name = "acct") val accountName: String,
        @Json(name = "username") val username: String,
        @Json(name = "display_name") val displayName: String = "",
        @Json(name = "avatar") val avatarUrl: String = "",
)
