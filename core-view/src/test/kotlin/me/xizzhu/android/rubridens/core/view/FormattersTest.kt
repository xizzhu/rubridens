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

package me.xizzhu.android.rubridens.core.view

import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class FormattersTest {
    private val testUser1 = User(
        id = "user_1",
        instanceUrl = "xizzhu.me",
        username = "random_username",
        displayName = "Random Display Name",
        avatarUrl = "https://xizzhu.me/avatar1.jpg",
    )
    private val testUser2 = User(
        id = "user_2",
        instanceUrl = "another_instance",
        username = "random_username_2",
        displayName = "",
        avatarUrl = "",
    )

    private val testStatus1 = Status(
        id = "status_1",
        instanceUrl = "xizzhu.me",
        uri = "https://xizzhu.me/",
        created = Instant.parse("2021-11-05T11:22:33.444Z"),
        sender = testUser1,
        reblogger = null,
        rebloggedInstanceUrl = null,
        inReplyToStatusId = null,
        inReplyToAccountId = null,
        content = "<p>Let's Go Brandon!</p>",
        tags = emptyList(),
        mentions = emptyList(),
        media = emptyList(),
        card = null,
        repliesCount = 0,
        reblogsCount = 0,
        favoritesCount = 0,
        reblogged = false,
        favorited = false,
    )
    private val testStatus2 = Status(
        id = "status_2",
        instanceUrl = "xizzhu.me",
        uri = "https://xizzhu.me/",
        created = Instant.parse("2021-11-05T11:22:33.444Z"),
        sender = testUser2,
        reblogger = null,
        rebloggedInstanceUrl = "xizzhu.me",
        inReplyToStatusId = null,
        inReplyToAccountId = null,
        content = "FJB!",
        tags = emptyList(),
        mentions = emptyList(),
        media = emptyList(),
        card = null,
        repliesCount = 0,
        reblogsCount = 0,
        favoritesCount = 0,
        reblogged = false,
        favorited = false,
    )

    @Test
    fun `test formatCount`() {
        assertEquals("7M+", 7654321.formatCount())
        assertEquals("4K+", 4321.formatCount())
        assertEquals("321", 321.formatCount())
    }

    @Test
    fun `test formatDisplayName`() {
        assertEquals("Random Display Name", testUser1.formatDisplayName())
        assertEquals("random_username_2", testUser2.formatDisplayName())
    }

    @Test
    fun `test formatSenderUsername`() {
        assertEquals("@random_username", testStatus1.formatSenderUsername())
        assertEquals("@random_username_2@another_instance", testStatus2.formatSenderUsername())
    }

    @Test
    fun `test formatRelativeTimestamp`() {
        assertEquals("Nov 5, 2021", testStatus1.formatRelativeTimestamp())
    }

    @Test
    fun `test formatTextContent`() {
        assertEquals("Let's Go Brandon!", testStatus1.formatTextContent())
        assertEquals("FJB!", testStatus2.formatTextContent())
    }
}