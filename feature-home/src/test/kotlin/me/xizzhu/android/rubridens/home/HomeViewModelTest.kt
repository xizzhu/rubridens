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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.xizzhu.android.rubridens.core.model.Data
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var statusRepository: StatusRepository

    @MockK
    private lateinit var homePresenter: HomePresenter

    private lateinit var homeViewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)

        homeViewModel = HomeViewModel(authRepository, statusRepository, homePresenter)
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
    fun `test loadX called multiple times`() = runTest {
        coEvery {
            authRepository.readUserCredentials()
        } coAnswers {
            delay(1000)
            listOf()
        }

        homeViewModel.loadLatest()
        homeViewModel.loadLatest()
        homeViewModel.loadOlder()
        homeViewModel.loadOlder()
        homeViewModel.loadNewer()
        homeViewModel.loadNewer()

        coVerify(exactly = 1) { authRepository.readUserCredentials() }
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
    fun `test loadLatest with local only`() = runTest {
        val userCredential = mockk<UserCredential>()
        val feedStatusHeaderItem = mockk<FeedStatusHeaderItem>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flowOf(Data.Local(listOf(mockk())), Data.Remote(emptyList()))
        coEvery { homePresenter.feedItems() } returns listOf(feedStatusHeaderItem)

        homeViewModel.loadLatest()

        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadLatest with remote only`() = runTest {
        val userCredential = mockk<UserCredential>()
        val feedStatusHeaderItem = mockk<FeedStatusHeaderItem>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flowOf(Data.Remote(listOf(mockk())))
        coEvery { homePresenter.feedItems() } returns listOf(feedStatusHeaderItem)

        homeViewModel.loadLatest()

        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadLatest with local and remote`() = runTest {
        val userCredential = mockk<UserCredential>()
        val feedStatusHeaderItem = mockk<FeedStatusHeaderItem>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flow {
            emit(Data.Local(listOf(mockk())))
            delay(100)
            emit(Data.Remote(emptyList()))
        }
        coEvery { homePresenter.feedItems() } returns listOf(feedStatusHeaderItem)

        homeViewModel.loadLatest()

        assertEquals(
            listOf(
                HomeViewModel.ViewState(loading = true, items = listOf(feedStatusHeaderItem)),
                HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem))
            ),
            homeViewModel.viewState().take(2).toList()
        )
    }

    @Test
    fun `test loadLatest with network error`() = runTest {
        val userCredential = mockk<UserCredential>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flow {
            throw NetworkException.Other(RuntimeException("random error"))
        }

        val viewAction = async { homeViewModel.viewAction().first() }
        delay(100)

        homeViewModel.loadLatest()

        assertEquals(HomeViewModel.ViewAction.ShowNetworkError, viewAction.await())
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = emptyList()),
            homeViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadLatest with random exception`() = runTest {
        val userCredential = mockk<UserCredential>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flow {
            throw RuntimeException("random error")
        }

        homeViewModel.loadLatest()

        assertEquals(
            HomeViewModel.ViewState(loading = false, items = emptyList()),
            homeViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadLatest then loadNewer`() = runTest {
        val userCredential = mockk<UserCredential>()
        val feedStatusHeaderItem = mockk<FeedStatusHeaderItem>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flowOf(Data.Remote(mutableListOf<Status>().apply { repeat(20) { add(mockk()) } }))

        var called = 0
        coEvery { homePresenter.feedItems() } answers { _ ->
            called++
            mutableListOf<FeedItem<*>>().apply { repeat(called) { add(feedStatusHeaderItem) } }
        }

        homeViewModel.loadLatest()
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )

        // Throws an exception, should emit the error.
        every { statusRepository.loadNewer(any(), any(), any()) } returns flow { throw NetworkException.Other(RuntimeException("random error")) }

        val viewAction = async { homeViewModel.viewAction().first() }
        delay(100)

        homeViewModel.loadNewer()

        assertEquals(HomeViewModel.ViewAction.ShowNetworkError, viewAction.await())
        verify(exactly = 1) { statusRepository.loadNewer(any(), any(), any()) }

        // Loads same amount as requested.
        every { statusRepository.loadNewer(any(), any(), any()) } returns flowOf(Data.Remote(mutableListOf<Status>().apply { repeat(20) { add(mockk()) } }))
        homeViewModel.loadNewer()
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem, feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )
        verify(exactly = 2) { statusRepository.loadNewer(any(), any(), any()) }

        // Loads less than requested, meaning no more data available after this.
        every { statusRepository.loadNewer(any(), any(), any()) } returns flowOf(Data.Remote(listOf(mockk())))
        homeViewModel.loadNewer()
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem, feedStatusHeaderItem, feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )
        verify(exactly = 3) { statusRepository.loadNewer(any(), any(), any()) }

        // Already loaded all data, should not bother the server.
        homeViewModel.loadNewer()
        verify(exactly = 3) { statusRepository.loadNewer(any(), any(), any()) }
    }

    @Test
    fun `test loadLatest then loadOlder`() = runTest {
        val userCredential = mockk<UserCredential>()
        val feedStatusHeaderItem = mockk<FeedStatusHeaderItem>()
        coEvery { authRepository.readUserCredentials() } returns listOf(userCredential)
        every { statusRepository.loadLatest(userCredential, any()) } returns flowOf(Data.Remote(listOf(mockk())))

        var called = 0
        coEvery { homePresenter.feedItems() } answers { _ ->
            called++
            mutableListOf<FeedItem<*>>().apply { repeat(called) { add(feedStatusHeaderItem) } }
        }

        homeViewModel.loadLatest()
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )

        // Throws an exception, should emit the error.
        every { statusRepository.loadOlder(any(), any(), any()) } returns flow { throw NetworkException.Other(RuntimeException("random error")) }

        val viewAction = async { homeViewModel.viewAction().first() }
        delay(100)

        homeViewModel.loadOlder()

        assertEquals(HomeViewModel.ViewAction.ShowNetworkError, viewAction.await())
        verify(exactly = 1) { statusRepository.loadOlder(any(), any(), any()) }

        // Loads same amount as requested.
        every { statusRepository.loadOlder(any(), any(), any()) } returns flowOf(Data.Remote(mutableListOf<Status>().apply { repeat(20) { add(mockk()) } }))
        homeViewModel.loadOlder()
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem, feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )
        verify(exactly = 2) { statusRepository.loadOlder(any(), any(), any()) }

        // Loads less than requested, meaning no more data available after this.
        every { statusRepository.loadOlder(any(), any(), any()) } returns flowOf(Data.Remote(listOf(mockk())))
        homeViewModel.loadOlder()
        assertEquals(
            HomeViewModel.ViewState(loading = false, items = listOf(feedStatusHeaderItem, feedStatusHeaderItem, feedStatusHeaderItem)),
            homeViewModel.viewState().first()
        )
        verify(exactly = 3) { statusRepository.loadOlder(any(), any(), any()) }

        // Already loaded all data, should not bother the server.
        homeViewModel.loadOlder()
        verify(exactly = 3) { statusRepository.loadOlder(any(), any(), any()) }
    }
}
