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

import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.rubridens.core.repository.local.ApplicationCredentialCache
import me.xizzhu.android.rubridens.core.repository.local.UserCredentialCache
import me.xizzhu.android.rubridens.core.repository.model.ApplicationCredential
import me.xizzhu.android.rubridens.core.repository.model.OAuthGrantType
import me.xizzhu.android.rubridens.core.repository.model.OAuthScope
import me.xizzhu.android.rubridens.core.repository.model.OAuthToken
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.repository.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.network.AccountsService
import me.xizzhu.android.rubridens.core.repository.network.AppsService
import me.xizzhu.android.rubridens.core.repository.network.OAuthService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRepositoryImplTest {
    @MockK
    private lateinit var accountsService: AccountsService

    @MockK
    private lateinit var appsService: AppsService

    @MockK
    private lateinit var oAuthService: OAuthService

    @MockK
    private lateinit var applicationCredentialCache: ApplicationCredentialCache

    @MockK
    private lateinit var userCredentialCache: UserCredentialCache

    private lateinit var authRepositoryImpl: AuthRepositoryImpl

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        authRepositoryImpl = AuthRepositoryImpl(accountsService, appsService, oAuthService, applicationCredentialCache, userCredentialCache)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test loadApplicationCredential with local cache`() = runTest {
        coEvery { applicationCredentialCache.readByInstanceUrl("xizzhu.me") } returns ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "access_token",
            vapidKey = "vapid_key",
        )

        assertEquals(
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "client_id",
                clientSecret = "client_secret",
                accessToken = "access_token",
                vapidKey = "vapid_key",
            ),
            authRepositoryImpl.loadApplicationCredential("xizzhu.me")
        )
        coVerify(exactly = 1) { applicationCredentialCache.readByInstanceUrl("xizzhu.me") }
    }

    @Test
    fun `test loadApplicationCredential with local cache missing accessToken`() = runTest {
        coEvery {
            applicationCredentialCache.readByInstanceUrl("xizzhu.me")
        } returns ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "",
            vapidKey = "vapid_key",
        )
        coEvery {
            oAuthService.createToken("xizzhu.me", OAuthGrantType.CLIENT_CREDENTIALS, "client_id", "client_secret")
        } returns OAuthToken(
            accessToken = "access_token",
            tokenType = "bearer",
            scopes = setOf(OAuthScope.READ, OAuthScope.WRITE, OAuthScope.FOLLOW, OAuthScope.PUSH),
        )

        assertEquals(
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "client_id",
                clientSecret = "client_secret",
                accessToken = "access_token",
                vapidKey = "vapid_key",
            ),
            authRepositoryImpl.loadApplicationCredential("xizzhu.me")
        )
        coVerify(ordering = Ordering.SEQUENCE) {
            applicationCredentialCache.readByInstanceUrl("xizzhu.me")
            oAuthService.createToken("xizzhu.me", OAuthGrantType.CLIENT_CREDENTIALS, "client_id", "client_secret")
            applicationCredentialCache.save(any())
        }
    }

    @Test
    fun `test loadApplicationCredential without local cache`() = runTest {
        coEvery {
            applicationCredentialCache.readByInstanceUrl("xizzhu.me")
        } returns null
        coEvery {
            appsService.create("xizzhu.me", "Rubridens", website = "https://xizzhu.me/")
        } returns ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "",
            vapidKey = "vapid_key",
        )
        coEvery {
            oAuthService.createToken("xizzhu.me", OAuthGrantType.CLIENT_CREDENTIALS, "client_id", "client_secret")
        } returns OAuthToken(
            accessToken = "access_token",
            tokenType = "bearer",
            scopes = setOf(OAuthScope.READ, OAuthScope.WRITE, OAuthScope.FOLLOW, OAuthScope.PUSH),
        )

        assertEquals(
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "client_id",
                clientSecret = "client_secret",
                accessToken = "access_token",
                vapidKey = "vapid_key",
            ),
            authRepositoryImpl.loadApplicationCredential("xizzhu.me")
        )
        coVerify(ordering = Ordering.SEQUENCE) {
            applicationCredentialCache.readByInstanceUrl("xizzhu.me")
            appsService.create("xizzhu.me", "Rubridens", website = "https://xizzhu.me/")
            applicationCredentialCache.save(any())
            oAuthService.createToken("xizzhu.me", OAuthGrantType.CLIENT_CREDENTIALS, "client_id", "client_secret")
            applicationCredentialCache.save(any())
        }
    }

    @Test
    fun `test createUserToken`() = runTest {
        coEvery {
            oAuthService.createToken("xizzhu.me", OAuthGrantType.AUTHORIZATION_CODE, "client_id", "client_secret", code = "code")
        } returns OAuthToken(
            accessToken = "access_token",
            tokenType = "bearer",
            scopes = setOf(OAuthScope.READ, OAuthScope.WRITE, OAuthScope.FOLLOW, OAuthScope.PUSH),
        )
        coEvery {
            accountsService.verifyCredentials("xizzhu.me", "access_token")
        } returns User(
            id = "user_id",
            instanceUrl = "xizzhu.me",
            username = "username",
            displayName = "display_name",
            avatarUrl = ""
        )

        authRepositoryImpl = spyk(authRepositoryImpl)
        coEvery { authRepositoryImpl.loadApplicationCredential("xizzhu.me") } returns ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "access_token",
            vapidKey = "vapid_key",
        )

        assertEquals(
            UserCredential(
                instanceUrl = "xizzhu.me",
                username = "username",
                accessToken = "access_token",
            ),
            authRepositoryImpl.createUserToken("xizzhu.me", "code")
        )
        coVerify(ordering = Ordering.SEQUENCE) {
            oAuthService.createToken("xizzhu.me", OAuthGrantType.AUTHORIZATION_CODE, "client_id", "client_secret", code = "code")
            accountsService.verifyCredentials("xizzhu.me", "access_token")
            userCredentialCache.save(any())
        }
    }
}
