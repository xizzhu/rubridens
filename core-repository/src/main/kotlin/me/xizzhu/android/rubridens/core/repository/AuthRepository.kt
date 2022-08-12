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

import me.xizzhu.android.rubridens.core.model.ApplicationCredential
import me.xizzhu.android.rubridens.core.model.OAuthGrantType
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.local.ApplicationCredentialCache
import me.xizzhu.android.rubridens.core.repository.local.UserCredentialCache
import me.xizzhu.android.rubridens.core.repository.network.AccountsService
import me.xizzhu.android.rubridens.core.repository.network.AppsService
import me.xizzhu.android.rubridens.core.repository.network.OAuthService

interface AuthRepository {
    suspend fun getLoginUrl(instanceUrl: String): String

    fun getAuthCode(url: String): String?

    suspend fun loadApplicationCredential(instanceUrl: String): ApplicationCredential

    suspend fun createUserToken(instanceUrl: String, authCode: String): UserCredential

    suspend fun hasUserCredential(): Boolean

    suspend fun readUserCredentials(): List<UserCredential>
}

internal class AuthRepositoryImpl(
    private val accountsService: AccountsService,
    private val appsService: AppsService,
    private val oAuthService: OAuthService,
    private val applicationCredentialCache: ApplicationCredentialCache,
    private val userCredentialCache: UserCredentialCache,
) : AuthRepository {
    override suspend fun getLoginUrl(instanceUrl: String): String =
        oAuthService.getLoginUrl(instanceUrl, loadApplicationCredential(instanceUrl).clientId.id)

    override fun getAuthCode(url: String): String? = oAuthService.getAuthCode(url)

    override suspend fun loadApplicationCredential(instanceUrl: String): ApplicationCredential {
        val cachedApplicationCredential = applicationCredentialCache.readByInstanceUrl(instanceUrl)
        if (cachedApplicationCredential?.accessToken?.isNotEmpty() == true) {
            return cachedApplicationCredential
        }

        val partialApplicationCredential = cachedApplicationCredential
            ?: appsService.create(
                instanceUrl = instanceUrl,
                clientName = "Rubridens",
                website = "https://xizzhu.me/"
            ).also {
                kotlin.runCatching { applicationCredentialCache.save(it) }
            }

        return partialApplicationCredential.copy(
            accessToken = oAuthService.createToken(
                instanceUrl = instanceUrl,
                grantType = OAuthGrantType.CLIENT_CREDENTIALS,
                clientId = partialApplicationCredential.clientId.id,
                clientSecret = partialApplicationCredential.clientSecret,
            ).accessToken
        ).also {
            kotlin.runCatching { applicationCredentialCache.save(it) }
        }
    }

    override suspend fun createUserToken(instanceUrl: String, authCode: String): UserCredential {
        val applicationCredential = loadApplicationCredential(instanceUrl)
        val userToken = oAuthService.createToken(
            instanceUrl = instanceUrl,
            grantType = OAuthGrantType.AUTHORIZATION_CODE,
            clientId = applicationCredential.clientId.id,
            clientSecret = applicationCredential.clientSecret,
            code = authCode,
        )
        return UserCredential(
            instanceUrl = instanceUrl,
            username = accountsService.verifyCredentials(instanceUrl, userToken.accessToken).username,
            accessToken = userToken.accessToken
        ).also {
            kotlin.runCatching { userCredentialCache.save(it) }
        }
    }

    override suspend fun hasUserCredential(): Boolean = userCredentialCache.hasCredential()

    override suspend fun readUserCredentials(): List<UserCredential> = userCredentialCache.read()
}
