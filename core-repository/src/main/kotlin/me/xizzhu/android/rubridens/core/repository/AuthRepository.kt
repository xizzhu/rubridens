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

import me.xizzhu.android.rubridens.core.repository.local.ApplicationCredentialCache
import me.xizzhu.android.rubridens.core.repository.local.UserCredentialCache
import me.xizzhu.android.rubridens.core.repository.model.ApplicationCredential
import me.xizzhu.android.rubridens.core.repository.model.OAuthGrantType
import me.xizzhu.android.rubridens.core.repository.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.network.AccountsService
import me.xizzhu.android.rubridens.core.repository.network.AppsService
import me.xizzhu.android.rubridens.core.repository.network.OAuthService

interface AuthRepository {
    suspend fun getLoginUrl(instanceUrl: String): String

    suspend fun loadApplicationCredential(instanceUrl: String): ApplicationCredential

    suspend fun createUserToken(instanceUrl: String, authCode: String): UserCredential

    suspend fun hasUserCredential(): Boolean
}

internal class AuthRepositoryImpl(
        private val accountsService: AccountsService,
        private val appsService: AppsService,
        private val oAuthService: OAuthService,
        private val applicationCredentialCache: ApplicationCredentialCache,
        private val userCredentialCache: UserCredentialCache,
) : AuthRepository {
    override suspend fun getLoginUrl(instanceUrl: String): String =
            oAuthService.getLoginUrl(instanceUrl, loadApplicationCredential(instanceUrl).clientId)

    override suspend fun loadApplicationCredential(instanceUrl: String): ApplicationCredential {
        applicationCredentialCache.readByInstanceUrl(instanceUrl)
                ?.takeIf { it.accessToken.isNotEmpty() }
                ?.let { return it }

        val partialAppCredential = appsService.create(
                instanceUrl = instanceUrl,
                clientName = "Rubridens",
                website = "https://xizzhu.me/"
        )
        val applicationToken = oAuthService.createToken(
                instanceUrl = instanceUrl,
                grantType = OAuthGrantType.CLIENT_CREDENTIALS,
                clientId = partialAppCredential.clientId,
                clientSecret = partialAppCredential.clientSecret,
        )
        val applicationCredential = ApplicationCredential(
                instanceUrl = instanceUrl,
                clientId = partialAppCredential.clientId,
                clientSecret = partialAppCredential.clientSecret,
                accessToken = applicationToken.accessToken,
                vapidKey = partialAppCredential.vapidKey,
        )
        applicationCredentialCache.save(applicationCredential)
        return applicationCredential
    }

    override suspend fun createUserToken(instanceUrl: String, authCode: String): UserCredential {
        val applicationCredential = loadApplicationCredential(instanceUrl)
        val userToken = oAuthService.createToken(
                instanceUrl = instanceUrl,
                grantType = OAuthGrantType.AUTHORIZATION_CODE,
                clientId = applicationCredential.clientId,
                clientSecret = applicationCredential.clientSecret,
                code = authCode,
        )
        val user = accountsService.verifyCredentials(instanceUrl, userToken.accessToken)
        val userCredential = UserCredential(
                username = user.username,
                instanceUrl = instanceUrl,
                accessToken = userToken.accessToken
        )
        userCredentialCache.save(userCredential)
        return userCredential
    }

    override suspend fun hasUserCredential(): Boolean = userCredentialCache.hasCredential()
}
