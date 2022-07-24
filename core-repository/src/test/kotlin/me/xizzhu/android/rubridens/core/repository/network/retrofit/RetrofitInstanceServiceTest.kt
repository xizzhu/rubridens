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
import me.xizzhu.android.rubridens.core.repository.model.Instance
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import okhttp3.mockwebserver.MockResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RetrofitInstanceServiceTest : BaseRetrofitTest() {
    private lateinit var retrofitInstanceService: RetrofitInstanceService

    @BeforeTest
    override fun setup() {
        super.setup()
        retrofitInstanceService = RetrofitInstanceService()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test fetch with empty instanceUrl`() = runTest {
        retrofitInstanceService.fetch("")
    }

    @Test
    fun `test fetch with successful response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                                    {
                                        "title": "my_title",
                                        "stats": {
                                            "user_count": 89,
                                            "status_count": 1989
                                        }
                                    }
                                """.trimIndent()
                )
        )

        assertEquals(
            Instance(
                title = "my_title",
                stats = Instance.Stats(
                    userCount = 89,
                    statusCount = 1989,
                ),
            ),
            retrofitInstanceService.fetch("xizzhu.me")
        )
    }

    @Test(expected = NetworkException.HttpError::class)
    fun `test fetch with non-200 response code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        retrofitInstanceService.fetch("xizzhu.me")
    }

    @Test(expected = NetworkException.MalformedResponseError::class)
    fun `test fetch with malformed JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        retrofitInstanceService.fetch("xizzhu.me")
    }
}
