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
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusMediaItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class HomePresenterTest {
    private val openStatus = { _: Status -> }
    private val replyToStatus = { _: Status -> }
    private val reblogStatus = { _: Status -> }
    private val favoriteStatus = { _: Status -> }
    private val openUser = { _: User -> }
    private val openMedia = { _: Media -> }
    private lateinit var homePresenter: HomePresenter

    private val testUser1 = User(
        id = "67890",
        instanceUrl = "xizzhu.me",
        username = "random_username",
        displayName = "Random Display Name",
        avatarUrl = "https://xizzhu.me/avatar1.jpg",
    )
    private val testMedia1 = Media(
        type = Media.Type.IMAGE,
        url = "https://xizzhu.me/media1.jpg",
        previewUrl = "https://xizzhu.me/media_preview1.jpg",
        blurHash = "",
    )
    private val testStatus1 = Status(
        id = "12345",
        instanceUrl = "xizzhu.me",
        uri = "https://xizzhu.me/",
        created = Instant.parse("2021-11-05T11:22:33.444Z"),
        sender = testUser1,
        reblogger = null,
        rebloggedInstanceUrl = null,
        inReplyToStatusId = null,
        inReplyToAccountId = null,
        content = "Let's Go Brandon!",
        tags = emptyList(),
        mentions = emptyList(),
        media = listOf(testMedia1),
        card = null,
        repliesCount = 1,
        reblogsCount = 2,
        favoritesCount = 3,
        reblogged = false,
        favorited = true,
    )

    private val testUser2 = User(
        id = "09876",
        instanceUrl = "another_instance",
        username = "random_username_2",
        displayName = "Display Name 2",
        avatarUrl = "",
    )
    private val testUser3 = User(
        id = "54321",
        instanceUrl = "another_instance",
        username = "random_username_3",
        displayName = "",
        avatarUrl = "",
    )
    private val testMedia2 = Media(
        type = Media.Type.VIDEO,
        url = "https://xizzhu.me/media2.mp4",
        previewUrl = "https://xizzhu.me/media_preview2.jpg",
        blurHash = ""
    )
    private val testStatus2 = Status(
        id = "54321",
        instanceUrl = "xizzhu.me",
        uri = "https://xizzhu.me/",
        created = Instant.parse("2021-11-05T11:22:33.444Z"),
        sender = testUser2,
        reblogger = testUser3,
        rebloggedInstanceUrl = "xizzhu.me",
        inReplyToStatusId = null,
        inReplyToAccountId = null,
        content = "FJB!",
        tags = emptyList(),
        mentions = emptyList(),
        media = listOf(testMedia2),
        card = null,
        repliesCount = 1234,
        reblogsCount = 0,
        favoritesCount = 7654321,
        reblogged = false,
        favorited = true,
    )

    @BeforeTest
    fun setup() {
        homePresenter = HomePresenter(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test buildFeedItems with empty list`() {
        assertTrue(homePresenter.buildFeedItems(emptyList(), openStatus, replyToStatus, reblogStatus, favoriteStatus, openUser, openMedia).isEmpty())
    }

    @Test
    fun `test buildFeedItems with single item`() {
        assertEquals(
            listOf(
                FeedStatusHeaderItem(
                    status = testStatus1,
                    blogger = testUser1,
                    bloggerDisplayName = "Random Display Name",
                    bloggerProfileImageUrl = "https://xizzhu.me/avatar1.jpg",
                    rebloggedBy = null,
                    subtitle = "@random_username • Nov 5, 2021",
                    openStatus = openStatus,
                    openBlogger = openUser,
                ),
                FeedStatusTextItem(
                    status = testStatus1,
                    text = "Let's Go Brandon!",
                    openStatus = openStatus,
                ),
                FeedStatusMediaItem(
                    status = testStatus1,
                    media = testMedia1,
                    imageUrl = "https://xizzhu.me/media_preview1.jpg",
                    placeholder = null,
                    isPlayable = false,
                    openStatus = openStatus,
                    openMedia = openMedia,
                ),
                FeedStatusFooterItem(
                    status = testStatus1,
                    replies = "1",
                    reblogs = "2",
                    reblogged = false,
                    favorites = "3",
                    favorited = true,
                    openStatus = openStatus,
                    replyToStatus = replyToStatus,
                    reblogStatus = reblogStatus,
                    favoriteStatus = favoriteStatus,
                ),
            ),
            homePresenter.buildFeedItems(
                listOf(testStatus1),
                openStatus = openStatus,
                replyToStatus = replyToStatus,
                reblogStatus = reblogStatus,
                favoriteStatus = favoriteStatus,
                openUser = openUser,
                openMedia = openMedia,
            )
        )
    }

    @Test
    fun `test buildFeedItems with multiple items`() {
        assertEquals(
            listOf(
                FeedStatusHeaderItem(
                    status = testStatus1,
                    blogger = testUser1,
                    bloggerDisplayName = "Random Display Name",
                    bloggerProfileImageUrl = "https://xizzhu.me/avatar1.jpg",
                    rebloggedBy = null,
                    subtitle = "@random_username • Nov 5, 2021",
                    openStatus = openStatus,
                    openBlogger = openUser,
                ),
                FeedStatusTextItem(
                    status = testStatus1,
                    text = "Let's Go Brandon!",
                    openStatus = openStatus,
                ),
                FeedStatusMediaItem(
                    status = testStatus1,
                    media = testMedia1,
                    imageUrl = "https://xizzhu.me/media_preview1.jpg",
                    placeholder = null,
                    isPlayable = false,
                    openStatus = openStatus,
                    openMedia = openMedia,
                ),
                FeedStatusFooterItem(
                    status = testStatus1,
                    replies = "1",
                    reblogs = "2",
                    reblogged = false,
                    favorites = "3",
                    favorited = true,
                    openStatus = openStatus,
                    replyToStatus = replyToStatus,
                    reblogStatus = reblogStatus,
                    favoriteStatus = favoriteStatus,
                ),
                FeedStatusHeaderItem(
                    status = testStatus2,
                    blogger = testUser2,
                    bloggerDisplayName = "Display Name 2",
                    bloggerProfileImageUrl = "",
                    rebloggedBy = "random_username_3 boosted",
                    subtitle = "@random_username_2@another_instance • Nov 5, 2021",
                    openStatus = openStatus,
                    openBlogger = openUser,
                ),
                FeedStatusTextItem(
                    status = testStatus2,
                    text = "FJB!",
                    openStatus = openStatus,
                ),
                FeedStatusMediaItem(
                    status = testStatus2,
                    media = testMedia2,
                    imageUrl = "https://xizzhu.me/media_preview2.jpg",
                    placeholder = null,
                    isPlayable = true,
                    openStatus = openStatus,
                    openMedia = openMedia,
                ),
                FeedStatusFooterItem(
                    status = testStatus2,
                    replies = "1K+",
                    reblogs = "",
                    reblogged = false,
                    favorites = "7M+",
                    favorited = true,
                    openStatus = openStatus,
                    replyToStatus = replyToStatus,
                    reblogStatus = reblogStatus,
                    favoriteStatus = favoriteStatus,
                ),
            ),
            homePresenter.buildFeedItems(
                listOf(testStatus1, testStatus2),
                openStatus = openStatus,
                replyToStatus = replyToStatus,
                reblogStatus = reblogStatus,
                favoriteStatus = favoriteStatus,
                openUser = openUser,
                openMedia = openMedia,
            )
        )
    }
}
