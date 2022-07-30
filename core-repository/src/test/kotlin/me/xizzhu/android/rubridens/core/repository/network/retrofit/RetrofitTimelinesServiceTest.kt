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
import me.xizzhu.android.rubridens.core.repository.model.Card
import me.xizzhu.android.rubridens.core.repository.model.Media
import me.xizzhu.android.rubridens.core.repository.model.Status
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import okhttp3.mockwebserver.MockResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RetrofitTimelinesServiceTest : BaseRetrofitTest() {
    private lateinit var retrofitTimelinesService: RetrofitTimelinesService

    @BeforeTest
    override fun setup() {
        super.setup()
        retrofitTimelinesService = RetrofitTimelinesService()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test fetchHome with empty instanceUrl`() = runTest {
        retrofitTimelinesService.fetchHome("", "user_token")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test fetchHome with empty userOAuthToken`() = runTest {
        retrofitTimelinesService.fetchHome("xizzhu.me", "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test fetchHome with empty negative limit`() = runTest {
        retrofitTimelinesService.fetchHome("xizzhu.me", "user_token", limit = -1)
    }

    @Test
    fun `test fetch with successful response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                        [
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
                                "replies_count": 1,
                                "reblogs_count": 2,
                                "favourites_count": 3,
                                "reblogged": false,
                                "favourited": true,
                                "media_attachments": [{
                            		"type": "image",
		                            "url": "https://xizzhu.me/media1.jpg",
		                            "preview_url": "https://xizzhu.me/media_preview1.jpg"
	                            }]
                            },
                            {
                                "id": "54321",
                                "created_at": "2021-12-25T00:11:22.333Z",
                                "account": {
                                    "id": "09876",
                                    "acct": "random_username_2",
                                    "username": "random_username_2",
                                    "avatar": "https://xizzhu.me/avatar2.jpg"
                                },
                                "uri": "https://xizzhu.me/pages/about/",
                                "content": "Merry Christmas!",
                                "replies_count": 4,
                                "reblogs_count": 5,
                                "favourites_count": 6,
                                "reblogged": true,
                                "favourited": false,
                                "card": {
                                    "type": "link",
                                    "url": "https://xizzhu.me/",
                                    "title": "Xizhi Zhu",
                                    "description": "Just some random description"
                                }
                            }
                        ]
                    """.trimIndent()
                )
        )

        assertEquals(
            listOf(
                Status(
                    id = "12345",
                    instanceUrl = "xizzhu.me",
                    uri = "https://xizzhu.me/",
                    created = Instant.parse("2021-11-05T11:22:33.444Z"),
                    sender = User(
                        id = "67890",
                        instanceUrl = "xizzhu.me",
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
                    media = listOf(
                        Media(
                            type = Media.Type.IMAGE,
                            url = "https://xizzhu.me/media1.jpg",
                            previewUrl = "https://xizzhu.me/media_preview1.jpg",
                            blurHash = ""
                        )
                    ),
                    card = null,
                    repliesCount = 1,
                    reblogsCount = 2,
                    favoritesCount = 3,
                    reblogged = false,
                    favorited = true,
                ),
                Status(
                    id = "54321",
                    instanceUrl = "xizzhu.me",
                    uri = "https://xizzhu.me/pages/about/",
                    created = Instant.parse("2021-12-25T00:11:22.333Z"),
                    sender = User(
                        id = "09876",
                        instanceUrl = "xizzhu.me",
                        username = "random_username_2",
                        displayName = "",
                        avatarUrl = "https://xizzhu.me/avatar2.jpg"
                    ),
                    reblogger = null,
                    rebloggedInstanceUrl = null,
                    inReplyToStatusId = null,
                    inReplyToAccountId = null,
                    content = "Merry Christmas!",
                    tags = emptyList(),
                    mentions = emptyList(),
                    media = emptyList(),
                    card = Card(
                        type = Card.Type.LINK,
                        url = "https://xizzhu.me/",
                        title = "Xizhi Zhu",
                        description = "Just some random description",
                        author = "",
                        previewUrl = "",
                        blurHash = ""
                    ),
                    repliesCount = 4,
                    reblogsCount = 5,
                    favoritesCount = 6,
                    reblogged = true,
                    favorited = false,
                )
            ),
            retrofitTimelinesService.fetchHome("xizzhu.me", "user_token")
        )
    }

    @Test(expected = NetworkException.HttpError::class)
    fun `test fetch with non-200 response code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        retrofitTimelinesService.fetchHome("xizzhu.me", "user_token")
    }

    @Test(expected = NetworkException.MalformedResponseError::class)
    fun `test fetch with malformed JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        retrofitTimelinesService.fetchHome("xizzhu.me", "user_token")
    }
}
