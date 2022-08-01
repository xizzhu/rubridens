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

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var homePresenter: HomePresenter

    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var statusRepository: StatusRepository

    private lateinit var homeViewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)

        homeViewModel = HomeViewModel(homePresenter, authRepository, statusRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial view state`() = runTest {
        assertEquals(HomeViewModel.ViewState(loading = false, items = emptyList()), homeViewModel.viewState().first())
    }

    @Test
    fun `test loadLatest without user credential`() = runTest {
        coEvery { authRepository.readUserCredentials() } returns listOf()

        val viewAction = async { homeViewModel.viewAction().first() }
        delay(100)

        homeViewModel.loadLatest()

        assertEquals(HomeViewModel.ViewAction.RequestUserCredential, viewAction.await())
        assertEquals(HomeViewModel.ViewState(loading = false, items = emptyList()), homeViewModel.viewState().first())
    }

    @Test
    fun `test loadLatest`() = runTest {
        val userCredential = mockk<UserCredential>()
        val user = mockk<User>()
        val status = mockk<Status>().apply { every { sender } returns user }
        val feedStatusHeaderItem = FeedStatusHeaderItem(
            status = status,
            blogger = user,
            bloggerDisplayName = "Random Display Name",
            bloggerProfileImageUrl = "https://xizzhu.me/avatar1.jpg",
            rebloggedBy = null,
            subtitle = "@random_username • Nov 5, 2021",
            openStatus = mockk(),
            openBlogger = mockk(),
        )
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        coEvery { statusRepository.loadLatest(userCredential) } returns listOf(status)
        every { homePresenter.buildFeedItems(listOf(status), any(), any(), any(), any(), any(), any(), any(), any()) } returns listOf(feedStatusHeaderItem)

        homeViewModel.loadLatest()

        assertEquals(HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem)), homeViewModel.viewState().first())
    }
}
