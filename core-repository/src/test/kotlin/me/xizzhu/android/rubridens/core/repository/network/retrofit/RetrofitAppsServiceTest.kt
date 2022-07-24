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
import me.xizzhu.android.rubridens.core.repository.model.ApplicationCredential
import me.xizzhu.android.rubridens.core.repository.model.OAuthScope
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import okhttp3.mockwebserver.MockResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RetrofitAppsServiceTest : BaseRetrofitTest() {
    private lateinit var retrofitAppsService: RetrofitAppsService

    @BeforeTest
    override fun setup() {
        super.setup()
        retrofitAppsService = RetrofitAppsService()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test create with empty instanceUrl`() = runTest {
        retrofitAppsService.create(
            instanceUrl = "",
            clientName = "client_name",
            redirectUris = "redirect_uris",
            scopes = setOf(OAuthScope.READ),
            website = "https://xizzhu.me/",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test create with empty clientName`() = runTest {
        retrofitAppsService.create(
            instanceUrl = "instance_url",
            clientName = "",
            redirectUris = "redirect_uris",
            scopes = setOf(OAuthScope.READ),
            website = "https://xizzhu.me/",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test create with empty redirectUril`() = runTest {
        retrofitAppsService.create(
            instanceUrl = "instance_url",
            clientName = "client_name",
            redirectUris = "",
            scopes = setOf(OAuthScope.READ),
            website = "https://xizzhu.me/",
        )
    }

    @Test
    fun `test create with successful response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                                    {
                                        "client_id": "my_client_id",
                                        "client_secret": "my_client_secret",
                                        "vapid_key": "my_vapid_key"
                                    }
                                """.trimIndent()
                )
        )

        assertEquals(
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "my_client_id",
                clientSecret = "my_client_secret",
                accessToken = "",
                vapidKey = "my_vapid_key",
            ),
            retrofitAppsService.create("xizzhu.me", "client_name")
        )
    }

    @Test(expected = NetworkException.HttpError::class)
    fun `test create with non-200 response code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        retrofitAppsService.create("xizzhu.me", "client_name")
    }

    @Test(expected = NetworkException.MalformedResponseError::class)
    fun `test create with malformed JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        retrofitAppsService.create("xizzhu.me", "client_name")
    }
}
