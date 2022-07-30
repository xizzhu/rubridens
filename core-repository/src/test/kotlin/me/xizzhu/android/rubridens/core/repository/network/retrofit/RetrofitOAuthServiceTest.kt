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

import kotlinx.coroutines.test.runTest
import me.xizzhu.android.rubridens.core.model.OAuthGrantType
import me.xizzhu.android.rubridens.core.model.OAuthScope
import me.xizzhu.android.rubridens.core.model.OAuthToken
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import okhttp3.mockwebserver.MockResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RetrofitOAuthServiceTest : BaseRetrofitTest() {
    private lateinit var retrofitOAuthService: RetrofitOAuthService

    @BeforeTest
    override fun setup() {
        super.setup()
        retrofitOAuthService = RetrofitOAuthService()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test getLoginUrl with empty instanceUrl`() {
        retrofitOAuthService.getLoginUrl(
            instanceUrl = "",
            clientId = "client_id",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
            forceLogin = true,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test getLoginUrl with empty redirectUri`() {
        retrofitOAuthService.getLoginUrl(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            redirectUri = "",
            scopes = setOf(OAuthScope.READ),
            forceLogin = true,
        )
    }

    @Test
    fun `test getLoginUrl`() {
        assertEquals(
            "https://xizzhu.me/oauth/authorize?response_type=code&client_id=my_client_id&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&scope=read%20write%20follow%20push&force_login=false",
            retrofitOAuthService.getLoginUrl(instanceUrl = "xizzhu.me", clientId = "my_client_id")
        )
    }

    @Test
    fun `test getAuthCode with invalid URL`() {
        assertNull(retrofitOAuthService.getAuthCode(""))
        assertNull(retrofitOAuthService.getAuthCode("not_a_url"))
        assertNull(retrofitOAuthService.getAuthCode("not_a_url?code=my_auth_code"))
    }

    @Test
    fun `test getAuthCode with valid URL but missing code`() {
        assertNull(retrofitOAuthService.getAuthCode("https://xizzhu.me/redirect_uri?not_code=my_auth_code"))
    }

    @Test
    fun `test getAuthCode`() {
        assertEquals("my_auth_code", retrofitOAuthService.getAuthCode("https://xizzhu.me/redirect_uri?code=my_auth_code"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test createToken with empty instanceUrl`() = runTest {
        retrofitOAuthService.createToken(
            instanceUrl = "",
            grantType = OAuthGrantType.CLIENT_CREDENTIALS,
            clientId = "client_id",
            clientSecret = "client_secret",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test createToken with empty clientId`() = runTest {
        retrofitOAuthService.createToken(
            instanceUrl = "xizzhu.me",
            grantType = OAuthGrantType.CLIENT_CREDENTIALS,
            clientId = "",
            clientSecret = "client_secret",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test createToken with empty clientSecret`() = runTest {
        retrofitOAuthService.createToken(
            instanceUrl = "xizzhu.me",
            grantType = OAuthGrantType.CLIENT_CREDENTIALS,
            clientId = "client_id",
            clientSecret = "",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test createToken with empty redirectUri`() = runTest {
        retrofitOAuthService.createToken(
            instanceUrl = "xizzhu.me",
            grantType = OAuthGrantType.CLIENT_CREDENTIALS,
            clientId = "client_id",
            clientSecret = "client_secret",
            redirectUri = "",
            scopes = setOf(OAuthScope.READ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test createToken with AUTHORIZATION_CODE and empty code`() = runTest {
        retrofitOAuthService.createToken(
            instanceUrl = "xizzhu.me",
            grantType = OAuthGrantType.AUTHORIZATION_CODE,
            clientId = "client_id",
            clientSecret = "client_secret",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
            code = "",
        )
    }

    @Test
    fun `test createToken with successful response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                                    {
                                        "access_token": "my_access_token",
                                        "token_type": "Bearer",
                                        "scope": "read write follow push"
                                    }
                                """.trimIndent()
                )
        )

        assertEquals(
            OAuthToken(
                accessToken = "my_access_token",
                tokenType = "Bearer",
                scopes = setOf(OAuthScope.READ, OAuthScope.WRITE, OAuthScope.FOLLOW, OAuthScope.PUSH)
            ),
            retrofitOAuthService.createToken(
                instanceUrl = "xizzhu.me",
                grantType = OAuthGrantType.CLIENT_CREDENTIALS,
                clientId = "client_id",
                clientSecret = "client_secret",
                redirectUri = "urn:ietf:wg:oauth:2.0:oob",
                scopes = setOf(OAuthScope.READ),
            )
        )
    }

    @Test(expected = NetworkException.HttpError::class)
    fun `test createToken with non-200 response code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        retrofitOAuthService.createToken(
            instanceUrl = "xizzhu.me",
            grantType = OAuthGrantType.CLIENT_CREDENTIALS,
            clientId = "client_id",
            clientSecret = "client_secret",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
        )
    }

    @Test(expected = NetworkException.MalformedResponseError::class)
    fun `test createToken with malformed JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        retrofitOAuthService.createToken(
            instanceUrl = "xizzhu.me",
            grantType = OAuthGrantType.CLIENT_CREDENTIALS,
            clientId = "client_id",
            clientSecret = "client_secret",
            redirectUri = "urn:ietf:wg:oauth:2.0:oob",
            scopes = setOf(OAuthScope.READ),
        )
    }

    @Test
    fun `test OAuthScope toMastodonString`() {
        assertEquals("read", OAuthScope.READ.toMastodonString())
        assertEquals("write", OAuthScope.WRITE.toMastodonString())
        assertEquals("follow", OAuthScope.FOLLOW.toMastodonString())
        assertEquals("push", OAuthScope.PUSH.toMastodonString())

        assertEquals("read", emptySet<OAuthScope>().toMastodonString())
        assertEquals("push", setOf(OAuthScope.PUSH).toMastodonString())
        assertEquals("write push", setOf(OAuthScope.WRITE, OAuthScope.PUSH).toMastodonString())
    }

    @Test
    fun `test string toOAuthScopes`() {
        assertEquals(setOf(OAuthScope.READ), "".toOAuthScopes())
        assertEquals(setOf(OAuthScope.WRITE), "write".toOAuthScopes())
        assertEquals(setOf(OAuthScope.WRITE, OAuthScope.FOLLOW), "follow write lol".toOAuthScopes())
    }

    @Test
    fun `test OAuthGrantType toMastodonString`() {
        assertEquals("authorization_code", OAuthGrantType.AUTHORIZATION_CODE.toMastodonString())
        assertEquals("client_credentials", OAuthGrantType.CLIENT_CREDENTIALS.toMastodonString())
    }
}
