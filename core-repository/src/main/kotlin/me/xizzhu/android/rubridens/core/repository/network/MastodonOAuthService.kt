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
import retrofit2.http.POST

/**
 * See https://docs.joinmastodon.org/methods/apps/oauth/
 */
internal interface MastodonOAuthService {
    @POST("oauth/token")
    @FormUrlEncoded
    suspend fun createToken(
            @Field("grant_type") grantType: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("scopes") scopes: String,
            @Field("code") code: String
    ): MastodonOAuthToken
}

/**
 * See https://docs.joinmastodon.org/entities/token/
 */
@JsonClass(generateAdapter = true)
internal class MastodonOAuthToken(
        @Json(name = "access_token") val accessToken: String,
        @Json(name = "token_type") val tokenType: String,
        @Json(name = "scope") val scope: String,
)
