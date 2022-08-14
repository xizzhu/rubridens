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

package me.xizzhu.android.rubridens.core.repository.local.room

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.Card
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class RoomStatusCacheTest : BaseRoomTest() {
    private lateinit var roomStatusCache: RoomStatusCache

    private val testUser1 = User(
        id = EntityKey("xizzhu.me", "67890"),
        username = "random_username",
        displayName = "Random Display Name",
        avatarUrl = "https://xizzhu.me/avatar1.jpg",
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
        media = emptyList(),
        card = null,
        repliesCount = 1,
        reblogsCount = 2,
        favoritesCount = 3,
        reblogged = false,
        favorited = true,
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
        created = Instant.parse("2021-11-05T11:33:44.555Z"),
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

    @BeforeTest
    override fun setup() {
        super.setup()
        roomStatusCache = RoomStatusCache(appDatabase)
    }

    @Test
    fun `test read from empty database`() = runTest {
        assertTrue(roomStatusCache.readLatest("xizzhu.me").isEmpty())
        assertTrue(roomStatusCache.readOldest("xizzhu.me").isEmpty())
    }

    @Test
    fun `test save then read`() = runTest {
        roomStatusCache.save(listOf(testStatus1))
        assertEquals(listOf(testStatus1), roomStatusCache.readLatest("xizzhu.me"))
        assertTrue(roomStatusCache.readLatest("xizzhu.me", olderThan = testStatus1.created.toEpochMilliseconds()).isEmpty())
        assertEquals(listOf(testStatus1), roomStatusCache.readOldest("xizzhu.me"))
        assertTrue(roomStatusCache.readOldest("xizzhu.me", newerThan = testStatus1.created.toEpochMilliseconds()).isEmpty())

        roomStatusCache.save(listOf(testStatus2))
        assertEquals(listOf(testStatus2, testStatus1), roomStatusCache.readLatest("xizzhu.me"))
        assertEquals(listOf(testStatus1), roomStatusCache.readLatest("xizzhu.me", olderThan = testStatus2.created.toEpochMilliseconds()))
        assertEquals(listOf(testStatus2, testStatus1), roomStatusCache.readOldest("xizzhu.me"))
        assertEquals(listOf(testStatus2), roomStatusCache.readOldest("xizzhu.me", newerThan = testStatus1.created.toEpochMilliseconds()))
    }
}
