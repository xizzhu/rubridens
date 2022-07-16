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
 * See https://docs.joinmastodon.org/methods/apps/
 */
internal interface MastodonAppsService {
    @POST("api/v1/apps")
    @FormUrlEncoded
    suspend fun create(
            @Field("client_name") clientName: String,
            @Field("redirect_uris") redirectUris: String,
            @Field("scopes") scopes: String,
            @Field("website") website: String
    ): MastodonApplication
}

/**
 * See https://docs.joinmastodon.org/entities/application/
 */
@JsonClass(generateAdapter = true)
internal class MastodonApplication(
        @Json(name = "id") val id: String = "",
        @Json(name = "client_id") val clientId: String = "",
        @Json(name = "client_secret") val clientSecret: String = "",
        @Json(name = "vapid_key") val vapidKey: String = "",
)
