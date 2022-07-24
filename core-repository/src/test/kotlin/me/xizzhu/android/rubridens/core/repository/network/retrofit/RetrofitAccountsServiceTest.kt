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
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import okhttp3.mockwebserver.MockResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RetrofitAccountsServiceTest : BaseRetrofitTest() {
    private lateinit var retrofitAccountsService: RetrofitAccountsService

    @BeforeTest
    override fun setup() {
        super.setup()
        retrofitAccountsService = RetrofitAccountsService()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test verifyCredentials with empty instanceUrl`() = runTest {
        retrofitAccountsService.verifyCredentials(instanceUrl = "", userOAuthToken = "user_token")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test verifyCredentials with empty userOAuthToken`() = runTest {
        retrofitAccountsService.verifyCredentials(instanceUrl = "instance_url", userOAuthToken = "")
    }

    @Test
    fun `test verifyCredentials with successful response`() = runTest {
        mockWebServer.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                """
                                    {
                                        "id": "198964",
                                        "acct": "xizzhu_acct",
                                        "username": "xizzhu_username",
                                        "display_name": "Keep Speech Free",
                                        "avatar": "https://xizzhu.me/images/logo.png"
                                    }
                                """.trimIndent()
                        )
        )

        assertEquals(
                User(
                        id = "198964",
                        instanceUrl = "xizzhu.me",
                        username = "xizzhu_username",
                        displayName = "Keep Speech Free",
                        avatarUrl = "https://xizzhu.me/images/logo.png",
                ),
                retrofitAccountsService.verifyCredentials("xizzhu.me", "user_token")
        )
    }

    @Test
    fun `test verifyCredentials with successful response and acct from another instance`() = runTest {
        mockWebServer.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                """
                                    {
                                        "id": "198964",
                                        "acct": "xizzhu@another",
                                        "username": "xizzhu",
                                        "display_name": "Keep Speech Free",
                                        "avatar": "https://xizzhu.me/images/logo.png"
                                    }
                                """.trimIndent()
                        )
        )

        assertEquals(
                User(
                        id = "198964",
                        instanceUrl = "another",
                        username = "xizzhu",
                        displayName = "Keep Speech Free",
                        avatarUrl = "https://xizzhu.me/images/logo.png",
                ),
                retrofitAccountsService.verifyCredentials("xizzhu.me", "user_token")
        )
    }

    @Test(expected = NetworkException.HttpError::class)
    fun `test verifyCredentials with non-200 response code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        retrofitAccountsService.verifyCredentials("xizzhu.me", "user_token")
    }

    @Test(expected = NetworkException.MalformedResponseError::class)
    fun `test verifyCredentials with malformed JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        retrofitAccountsService.verifyCredentials("xizzhu.me", "user_token")
    }
}
