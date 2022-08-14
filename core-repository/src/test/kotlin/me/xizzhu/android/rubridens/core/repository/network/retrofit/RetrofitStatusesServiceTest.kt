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
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import okhttp3.mockwebserver.MockResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RetrofitStatusesServiceTest : BaseRetrofitTest() {
    private lateinit var retrofitStatusesService: RetrofitStatusesService

    @BeforeTest
    override fun setup() {
        super.setup()
        retrofitStatusesService = RetrofitStatusesService()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test fetchHome with empty instanceUrl`() = runTest {
        retrofitStatusesService.fetch(null, EntityKey("", "id"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test fetchHome with empty userOAuthToken`() = runTest {
        retrofitStatusesService.fetch(null, EntityKey("xizzhu.me", ""))
    }

    @Test
    fun `test fetch with successful response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                        {
                            "id": "12345",
                            "created_at": "2021-11-05T11:22:33.444Z",
                            "account": {
                                "id": "67890",
                                "acct": "random_username@xizzhu.me",
                                "username": "random_username",
                                "display_name": "Random Display Name",
                                "avatar": "https://xizzhu.me/avatar1.jpg"
                            },
                            "uri": "https://xizzhu.me/",
                            "content": "Let's Go Brandon!",
                            "media_attachments": [],
                            "replies_count": 1,
                            "reblogs_count": 2,
                            "favourites_count": 3,
                            "reblogged": false,
                            "favourited": true
                        }
                    """.trimIndent()
                )
        )

        assertEquals(
            Status(
                id = EntityKey("xizzhu.me", "12345"),
                uri = "https://xizzhu.me/",
                created = Instant.parse("2021-11-05T11:22:33.444Z"),
                sender = User(
                    id = EntityKey("xizzhu.me", "67890"),
                    username = "random_username",
                    displayName = "Random Display Name",
                    avatarUrl = "https://xizzhu.me/avatar1.jpg"
                ),
                reblogger = null,
                rebloggedInstanceUrl = null,
                inReplyToStatusId = null,
                inReplyToAccountId = null,
                content = "Let's Go Brandon!",
                tags = emptyList(),
                mentions = emptyList(),
                media = emptyList(),
                card = null,
                repliesCount = 1,
                reblogsCount = 2,
                favoritesCount = 3,
                reblogged = false,
                favorited = true,
            ),
            retrofitStatusesService.fetch(null, EntityKey("xizzhu.me", "id"))
        )
    }

    @Test(expected = NetworkException.HttpError::class)
    fun `test fetch with non-200 response code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        retrofitStatusesService.fetch(null, EntityKey("xizzhu.me", "id"))
    }

    @Test(expected = NetworkException.MalformedResponseError::class)
    fun `test fetch with malformed JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        retrofitStatusesService.fetch(null, EntityKey("xizzhu.me", "id"))
    }
}
