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

package me.xizzhu.android.rubridens.core.repository

import me.xizzhu.android.rubridens.core.repository.network.MastodonAppsService
import me.xizzhu.android.rubridens.core.repository.network.MastodonOAuthService
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Retrofit
import retrofit2.create

data class ApplicationCredential(
        val instanceUrl: String,
        val clientId: String,
        val clientSecret: String,
        val accessToken: String,
        val vapidKey: String
)

interface AuthRepository {
    suspend fun loadApplicationCredential(instanceUrl: String): Result<ApplicationCredential>
}

internal class AuthRepositoryImpl : AuthRepository {
    override suspend fun loadApplicationCredential(instanceUrl: String): Result<ApplicationCredential> = kotlin.runCatching {
        // TODO reads from local cache

        val retrofit: Retrofit by inject(Retrofit::class.java) { parametersOf(instanceUrl) }
        val mastodonApplication = retrofit.create<MastodonAppsService>().create(
                clientName = "Rubridens",
                redirectUris = "urn:ietf:wg:oauth:2.0:oob",
                scopes = "read write follow push",
                website = "https://xizzhu.me/"
        )
        val mastodonOAuthToken = retrofit.create<MastodonOAuthService>().createToken(
                grantType = "client_credentials",
                clientId = mastodonApplication.clientId,
                clientSecret = mastodonApplication.clientSecret,
                redirectUri = "urn:ietf:wg:oauth:2.0:oob",
                scopes = "read write follow push",
                code = ""
        )
        val applicationCredential = ApplicationCredential(
                instanceUrl = instanceUrl,
                clientId = mastodonApplication.clientId,
                clientSecret = mastodonApplication.clientSecret,
                accessToken = mastodonOAuthToken.accessToken,
                vapidKey = mastodonApplication.vapidKey,
        )
        // TODO saves the created application credential
        applicationCredential
    }
}
