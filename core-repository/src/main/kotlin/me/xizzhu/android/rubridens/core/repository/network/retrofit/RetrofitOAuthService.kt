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
import me.xizzhu.android.rubridens.core.repository.model.OAuthGrantType
import me.xizzhu.android.rubridens.core.repository.model.OAuthScope
import me.xizzhu.android.rubridens.core.repository.model.OAuthToken
import me.xizzhu.android.rubridens.core.repository.network.OAuthService
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

internal class RetrofitOAuthService : OAuthService {
    override fun getLoginUrl(instanceUrl: String, clientId: String, redirectUri: String, scopes: Set<OAuthScope>, forceLogin: Boolean): String {
        if (instanceUrl.isEmpty()) {
            throw IllegalArgumentException("instanceUrl is empty")
        }
        if (redirectUri.isEmpty()) {
            throw IllegalArgumentException("redirectUri is empty")
        }

        return HttpUrl.Builder()
                .scheme("https")
                .host(instanceUrl)
                .encodedPath("/oauth/authorize")
                .addQueryParameter("response_type", "code")
                .addQueryParameter("client_id", clientId)
                .addQueryParameter("redirect_uri", redirectUri)
                .addQueryParameter("scope", scopes.toMastodonString())
                .addQueryParameter("force_login", forceLogin.toString())
                .toString()
    }

    override fun getAuthCode(url: String): String? = url.toHttpUrlOrNull()?.queryParameter("code")

    override suspend fun createToken(
            instanceUrl: String,
            grantType: OAuthGrantType,
            clientId: String,
            clientSecret: String,
            redirectUri: String,
            scopes: Set<OAuthScope>,
            code: String
    ): OAuthToken {
        if (instanceUrl.isEmpty()) {
            throw IllegalArgumentException("instanceUrl is empty")
        }
        if (clientId.isEmpty()) {
            throw IllegalArgumentException("clientId is empty")
        }
        if (clientSecret.isEmpty()) {
            throw IllegalArgumentException("clientSecret is empty")
        }
        if (redirectUri.isEmpty()) {
            throw IllegalArgumentException("redirectUri is empty")
        }
        if (grantType == OAuthGrantType.AUTHORIZATION_CODE && code.isEmpty()) {
            throw IllegalArgumentException("code is empty but grantType is 'OAuthGrantType.AUTHORIZATION_CODE'")
        }

        return request<MastodonOAuthService, MastodonOAuthToken>(instanceUrl) {
            createToken(
                    grantType = grantType.toMastodonString(),
                    clientId = clientId,
                    clientSecret = clientSecret,
                    redirectUri = redirectUri,
                    scopes = scopes.toMastodonString(),
                    code = code
            )
        }.toOAuthToken()
    }
}

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
) {
    fun toOAuthToken(): OAuthToken = OAuthToken(
            accessToken = accessToken,
            tokenType = tokenType,
            scopes = scope.toOAuthScopes()
    )
}

internal fun createAuthHeader(token: String): String = "Bearer $token"

/**
 * See https://docs.joinmastodon.org/api/oauth-scopes/
 */
internal fun OAuthScope.toMastodonString(): String = when (this) {
    OAuthScope.READ -> "read"
    OAuthScope.WRITE -> "write"
    OAuthScope.FOLLOW -> "follow"
    OAuthScope.PUSH -> "push"
}

internal fun Set<OAuthScope>.toMastodonString(): String =
        (takeIf { it.isNotEmpty() } ?: setOf(OAuthScope.READ)).joinToString(separator = " ") { it.toMastodonString() }

internal fun String.toOAuthScopes(): Set<OAuthScope> =
        (split(" ").mapNotNull { value -> OAuthScope.values().firstOrNull { it.toMastodonString() == value } }
                .takeIf { it.isNotEmpty() })?.toSet() ?: setOf(OAuthScope.READ)

/**
 * See https://docs.joinmastodon.org/methods/apps/oauth/
 */
internal fun OAuthGrantType.toMastodonString(): String = when (this) {
    OAuthGrantType.AUTHORIZATION_CODE -> "authorization_code"
    OAuthGrantType.CLIENT_CREDENTIALS -> "client_credentials"
}
