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

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.Card
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusCardItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusMediaItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusThreadItem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomePresenterTest {
    private lateinit var homePresenter: HomePresenter

    private val testUser1 = User(
        id = EntityKey("xizzhu.me", "67890"),
        username = "random_username",
        displayName = "Random Display Name",
        avatarUrl = "https://xizzhu.me/avatar1.jpg",
    )
    private val testMedia1 = Media(
        type = Media.Type.IMAGE,
        url = "https://xizzhu.me/media1.jpg",
        previewUrl = "",
        blurHash = "",
    )
    private val testStatus1 = Status(
        id = EntityKey("xizzhu.me", "12345"),
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
    private val feedItems1 = listOf(
        FeedStatusHeaderItem(
            status = testStatus1,
            hasAncestor = false,
            hasDescendant = false,
            blogger = testUser1,
            reblogger = null,
        ),
        FeedStatusTextItem(
            status = testStatus1,
            hasAncestor = false,
            hasDescendant = false,
        ),
        FeedStatusMediaItem(
            status = testStatus1,
            hasAncestor = false,
            hasDescendant = false,
        ),
        FeedStatusFooterItem(
            status = testStatus1,
            hasAncestor = false,
            hasDescendant = false,
        ),
    )

    private val testUser2 = User(
        id = EntityKey("another_instance", "09876"),
        username = "random_username_2",
        displayName = "Display Name 2",
        avatarUrl = "",
    )
    private val testUser3 = User(
        id = EntityKey("another_instance", "54321"),
        username = "random_username_3",
        displayName = "",
        avatarUrl = "",
    )
    private val testMedia2 = Media(
        type = Media.Type.VIDEO,
        url = "https://xizzhu.me/media2.mp4",
        previewUrl = "https://xizzhu.me/media_preview2.jpg",
        blurHash = "",
    )
    private val testMedia3 = Media(
        type = Media.Type.AUDIO,
        url = "https://xizzhu.me/media3.mp3",
        previewUrl = "",
        blurHash = "",
    )
    private val testMedia4 = Media(
        type = Media.Type.GIF,
        url = "https://xizzhu.me/media4.gif",
        previewUrl = "https://xizzhu.me/media_preview4.jpg",
        blurHash = "",
    )
    private val testCard2 = Card(
        type = Card.Type.LINK,
        url = "https://xizzhu.me/",
        title = "card_title",
        description = "card_description",
        author = "card_author",
        previewUrl = "https://xizzhu.me/media_preview2.jpg",
        blurHash = "",
    )
    private val testStatus2 = Status(
        id = EntityKey("xizzhu.me", "54321"),
        uri = "https://xizzhu.me/",
        created = Instant.parse("2021-11-05T11:22:33.444Z"),
        sender = testUser2,
        reblogger = testUser3,
        rebloggedInstanceUrl = "xizzhu.me",
        inReplyToStatusId = testStatus1.id.id,
        inReplyToAccountId = testStatus1.sender.id.id,
        content = "FJB!",
        tags = emptyList(),
        mentions = emptyList(),
        media = listOf(testMedia2, testMedia3, testMedia4),
        card = testCard2,
        repliesCount = 1234,
        reblogsCount = 0,
        favoritesCount = 7654321,
        reblogged = false,
        favorited = true,
    )
    private val feedItems2 = listOf(
        FeedStatusHeaderItem(
            status = testStatus2,
            hasAncestor = false,
            hasDescendant = false,
            blogger = testUser2,
            reblogger = testUser3,
        ),
        FeedStatusTextItem(
            status = testStatus2,
            hasAncestor = false,
            hasDescendant = false,
        ),
        FeedStatusMediaItem(
            status = testStatus2,
            hasAncestor = false,
            hasDescendant = false,
        ),
        FeedStatusCardItem(
            status = testStatus2,
            hasAncestor = false,
            hasDescendant = false,
        ),
        FeedStatusThreadItem(
            status = testStatus2,
        ),
        FeedStatusFooterItem(
            status = testStatus2,
            hasAncestor = false,
            hasDescendant = false,
        ),
    )

    private val testStatus3 = Status(
        id = EntityKey("xizzhu.me", "55555"),
        uri = "https://xizzhu.me/",
        created = Instant.parse("2016-11-08T11:22:33.444Z"),
        sender = testUser1,
        reblogger = null,
        rebloggedInstanceUrl = null,
        inReplyToStatusId = null,
        inReplyToAccountId = null,
        content = "History has been made!",
        tags = emptyList(),
        mentions = emptyList(),
        media = emptyList(),
        card = null,
        repliesCount = 1,
        reblogsCount = 2,
        favoritesCount = 3,
        reblogged = true,
        favorited = false,
    )
    private val feedItems3 = listOf(
        FeedStatusHeaderItem(
            status = testStatus3,
            hasAncestor = false,
            hasDescendant = false,
            blogger = testUser1,
            reblogger = null,
        ),
        FeedStatusTextItem(
            status = testStatus3,
            hasAncestor = false,
            hasDescendant = false,
        ),
        FeedStatusFooterItem(
            status = testStatus3,
            hasAncestor = false,
            hasDescendant = false,
        ),
    )

    @BeforeTest
    fun setup() {
        homePresenter = HomePresenter()
    }

    @Test
    fun `test initial state`() = runTest {
        assertTrue(homePresenter.feedItems().isEmpty())
    }

    @Test
    fun `test replace with empty list`() = runTest {
        homePresenter.replace(emptyList())
        assertTrue(homePresenter.feedItems().isEmpty())
    }

    @Test
    fun `test replace with single item`() = runTest {
        homePresenter.replace(listOf(testStatus1))
        assertEquals(feedItems1, homePresenter.feedItems())
    }

    @Test
    fun `test replace with multiple items`() = runTest {
        homePresenter.replace(listOf(testStatus1, testStatus2, testStatus3))
        assertEquals(feedItems1 + feedItems2 + feedItems3, homePresenter.feedItems())
    }

    @Test
    fun `test replace, append, prepend, and clear`() = runTest {
        homePresenter.replace(listOf(testStatus1))
        homePresenter.append(listOf(testStatus2))
        homePresenter.prepend(listOf(testStatus3))
        assertEquals(feedItems3 + feedItems1 + feedItems2, homePresenter.feedItems())

        homePresenter.clear()
        assertTrue(homePresenter.feedItems().isEmpty())
    }
}
