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
import com.squareup.moshi.JsonClass
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.repository.network.AccountsService
import retrofit2.http.GET
import retrofit2.http.Header

internal class RetrofitAccountsService : AccountsService {
    override suspend fun verifyCredentials(instanceUrl: String, userOAuthToken: String): User {
        if (instanceUrl.isEmpty()) {
            throw IllegalArgumentException("instanceUrl is empty")
        }
        if (userOAuthToken.isEmpty()) {
            throw IllegalArgumentException("userOAuthToken is empty")
        }

        return request<MastodonAccountsService, MastodonAccount>(instanceUrl) {
            verifyCredentials(createAuthHeader(userOAuthToken))
        }.toUser(instanceUrl)
    }
}

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
) {
    fun toUser(instanceUrl: String): User = User(
        id = id,
        instanceUrl = accountName.indexOf('@').takeIf { it >= 0 }?.let { accountName.substring(it + 1) } ?: instanceUrl,
        username = username,
        displayName = displayName.takeIf { it.isNotEmpty() } ?: "",
        avatarUrl = avatarUrl.takeIf { it.isNotEmpty() } ?: ""
    )
}
