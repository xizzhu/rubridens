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

package me.xizzhu.android.rubridens.core.repository

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.Data
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.local.StatusCache
import me.xizzhu.android.rubridens.core.repository.network.TimelinesService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusRepositoryTest {
    @MockK
    private lateinit var timelinesService: TimelinesService

    @MockK
    private lateinit var statusCache: StatusCache

    @MockK(relaxed = true)
    private lateinit var userCredential: UserCredential

    private lateinit var statusRepository: StatusRepository

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
    private val testStatus2 = Status(
        id = EntityKey("xizzhu.me", "54321"),
        uri = "https://xizzhu.me/",
        created = Instant.parse("2021-11-05T11:22:33.444Z"),
        sender = testUser2,
        reblogger = null,
        rebloggedInstanceUrl = "xizzhu.me",
        inReplyToStatusId = testStatus1.id.id,
        inReplyToAccountId = testStatus1.sender.id.id,
        content = "FJB!",
        tags = emptyList(),
        mentions = emptyList(),
        media = emptyList(),
        card = null,
        repliesCount = 1234,
        reblogsCount = 0,
        favoritesCount = 7654321,
        reblogged = false,
        favorited = true,
    )

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        statusRepository = StatusRepositoryImpl(timelinesService, statusCache)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test loadLatest - both local and remote are empty`() = runTest {
        coEvery { statusCache.readLatest(any(), any()) } returns emptyList()
        coEvery { timelinesService.fetchHome(any(), any(), any(), any(), any(), any(), any()) } returns emptyList()

        assertEquals(
            listOf(Data.Remote(emptyList())),
            statusRepository.loadLatest(userCredential, 20).toList()
        )
    }

    @Test
    fun `test loadLatest - local is empty, but remote is not empty`() = runTest {
        coEvery { statusCache.readLatest(any(), any()) } returns emptyList()
        coEvery { timelinesService.fetchHome(any(), any(), any(), any(), any(), any(), any()) } returns listOf(testStatus2)

        assertEquals(
            listOf(Data.Remote(listOf(testStatus2))),
            statusRepository.loadLatest(userCredential, 20).toList()
        )
    }

    @Test
    fun `test loadLatest - local is not empty, but remote is empty`() = runTest {
        coEvery { statusCache.readLatest(any(), any()) } returns listOf(testStatus1)
        coEvery {
            timelinesService.fetchHome(any(), any(), any(), any(), any(), any(), any())
        } answers { answer ->
            assertEquals("", answer.invocation.args[3])
            assertEquals("12345", answer.invocation.args[4])

            emptyList()
        }

        assertEquals(
            listOf(Data.Local(listOf(testStatus1)), Data.Remote(emptyList())),
            statusRepository.loadLatest(userCredential, 20).toList()
        )
    }

    @Test
    fun `test loadLatest - local throws exception, but remote is not empty`() = runTest {
        coEvery { statusCache.readLatest(any(), any()) } throws RuntimeException("random error")
        coEvery { timelinesService.fetchHome(any(), any(), any(), any(), any(), any(), any()) } returns listOf(testStatus2)

        assertEquals(
            listOf(Data.Remote(listOf(testStatus2))),
            statusRepository.loadLatest(userCredential, 20).toList()
        )
    }

    @Test(expected = RuntimeException::class)
    fun `test loadLatest - local is not empty, but remote throws exception`() = runTest {
        coEvery { statusCache.readLatest(any(), any()) } returns listOf(testStatus1)
        coEvery { timelinesService.fetchHome(any(), any(), any(), any(), any(), any(), any()) } throws RuntimeException("random error")

        assertEquals(
            listOf(Data.Local(listOf(testStatus1))),
            statusRepository.loadLatest(userCredential, 20).take(1).toList()
        )

        statusRepository.loadLatest(userCredential, 20).toList()
    }

    @Test
    fun `test loadLatest - both local and remote are not empty`() = runTest {
        coEvery { statusCache.readLatest(any(), any()) } returns listOf(testStatus1)
        coEvery { timelinesService.fetchHome(any(), any(), any(), any(), any(), any(), any()) } returns listOf(testStatus2)

        assertEquals(
            listOf(
                Data.Local(listOf(testStatus1)),
                Data.Remote(listOf(testStatus2))
            ),
            statusRepository.loadLatest(userCredential, 20).toList()
        )
    }
}
