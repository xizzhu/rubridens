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

package me.xizzhu.android.rubridens.home

import androidx.test.core.app.ApplicationProvider
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.repository.model.Media
import me.xizzhu.android.rubridens.core.repository.model.Status
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class HomePresenterTest {
    private lateinit var homePresenter: HomePresenter

    @BeforeTest
    fun setup() {
        homePresenter = HomePresenter(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test buildFeedItems with empty list`() {
        assertTrue(homePresenter.buildFeedItems(emptyList()).isEmpty())
    }

    @Test
    fun `test buildFeedItems with single item`() {
        assertEquals(
            listOf(
                FeedStatusHeaderItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "12345",
                    bloggerDisplayName = "Random Display Name",
                    bloggerProfileImageUrl = "https://xizzhu.me/avatar1.jpg",
                    rebloggedBy = null,
                    subtitle = "@random_username • Nov 5, 2021",
                ),
                FeedStatusTextItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "12345",
                    text = "Let's Go Brandon!"
                ),
                FeedStatusFooterItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "12345",
                    replies = "1",
                    reblogs = "2",
                    reblogged = false,
                    favorites = "3",
                    favorited = true,
                ),
            ),
            homePresenter.buildFeedItems(listOf(
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
                )
            ))
        )
    }

    @Test
    fun `test buildFeedItems with multiple items`() {
        assertEquals(
            listOf(
                FeedStatusHeaderItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "12345",
                    bloggerDisplayName = "Random Display Name",
                    bloggerProfileImageUrl = "https://xizzhu.me/avatar1.jpg",
                    rebloggedBy = null,
                    subtitle = "@random_username • Nov 5, 2021",
                ),
                FeedStatusTextItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "12345",
                    text = "Let's Go Brandon!"
                ),
                FeedStatusFooterItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "12345",
                    replies = "1",
                    reblogs = "2",
                    reblogged = false,
                    favorites = "3",
                    favorited = true,
                ),
                FeedStatusHeaderItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "54321",
                    bloggerDisplayName = "Display Name 2",
                    bloggerProfileImageUrl = "",
                    rebloggedBy = "random_username boosted",
                    subtitle = "@random_username_2@another_instance • Nov 5, 2021",
                ),
                FeedStatusTextItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "54321",
                    text = "FJB!"
                ),
                FeedStatusFooterItem(
                    statusInstanceUrl = "xizzhu.me",
                    statusId = "54321",
                    replies = "1K+",
                    reblogs = "",
                    reblogged = false,
                    favorites = "7M+",
                    favorited = true,
                ),
            ),
            homePresenter.buildFeedItems(listOf(
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
                        avatarUrl = "https://xizzhu.me/avatar1.jpg",
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
                            blurHash = "",
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
                    uri = "https://xizzhu.me/",
                    created = Instant.parse("2021-11-05T11:22:33.444Z"),
                    sender = User(
                        id = "09876",
                        instanceUrl = "another_instance",
                        username = "random_username_2",
                        displayName = "Display Name 2",
                        avatarUrl = "",
                    ),
                    reblogger = User(
                        id = "67890",
                        instanceUrl = "xizzhu.me",
                        username = "random_username",
                        displayName = "",
                        avatarUrl = "https://xizzhu.me/avatar1.jpg",
                    ),
                    rebloggedInstanceUrl = "xizzhu.me",
                    inReplyToStatusId = null,
                    inReplyToAccountId = null,
                    content = "FJB!",
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
                    repliesCount = 1234,
                    reblogsCount = 0,
                    favoritesCount = 7654321,
                    reblogged = false,
                    favorited = true,
                ),
            ))
        )
    }
}
