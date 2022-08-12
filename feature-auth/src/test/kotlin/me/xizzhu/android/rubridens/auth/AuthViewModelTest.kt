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

package me.xizzhu.android.rubridens.auth

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.xizzhu.android.rubridens.core.model.ApplicationCredential
import me.xizzhu.android.rubridens.core.model.Instance
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.InstanceRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var instanceRepository: InstanceRepository

    private lateinit var authViewModel: AuthViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)

        authViewModel = AuthViewModel(authRepository, instanceRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial view state`() = runTest {
        assertEquals(AuthViewModel.ViewState(loading = false, instanceInfo = null, errorInfo = null), authViewModel.viewState().first())
    }

    @Test
    fun `test selectInstance with instance info fetched first`() = runTest {
        coEvery {
            instanceRepository.fetch("xizzhu.me")
        } returns Instance(
            title = "title",
            stats = Instance.Stats(
                userCount = 64,
                statusCount = 1989,
            )
        )
        coEvery {
            authRepository.loadApplicationCredential("xizzhu.me")
        } coAnswers {
            delay(200)
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "client_id",
                clientSecret = "client_secret",
                accessToken = "access_token",
                vapidKey = "vapid_key",
            )
        }

        val viewAction = async { authViewModel.viewAction().first() }
        delay(100)

        authViewModel.selectInstance("xizzhu.me")

        assertEquals(
            AuthViewModel.ViewState(
                loading = true,
                instanceInfo = AuthViewModel.ViewState.InstanceInfo(
                    title = "title",
                    userCount = 64,
                    statusCount = 1989,
                ),
                errorInfo = null
            ),
            authViewModel.viewState().first()
        )
        assertEquals("xizzhu.me", (viewAction.await() as AuthViewModel.ViewAction.OpenLoginView).instanceUrl)
        assertEquals(
            AuthViewModel.ViewState(
                loading = false,
                instanceInfo = AuthViewModel.ViewState.InstanceInfo(
                    title = "title",
                    userCount = 64,
                    statusCount = 1989,
                ),
                errorInfo = null
            ),
            authViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectInstance with instance info fetched later`() = runTest {
        coEvery {
            instanceRepository.fetch("xizzhu.me")
        } coAnswers {
            delay(100)
            Instance(
                title = "title",
                stats = Instance.Stats(
                    userCount = 64,
                    statusCount = 1989,
                )
            )
        }
        coEvery {
            authRepository.loadApplicationCredential("xizzhu.me")
        } returns ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "access_token",
            vapidKey = "vapid_key",
        )

        val viewAction = async { authViewModel.viewAction().first() }
        delay(100)

        authViewModel.selectInstance("xizzhu.me")
        assertEquals("xizzhu.me", (viewAction.await() as AuthViewModel.ViewAction.OpenLoginView).instanceUrl)
        assertEquals(AuthViewModel.ViewState(loading = false, instanceInfo = null, errorInfo = null), authViewModel.viewState().first())
    }

    @Test
    fun `test selectInstance with fetching instance info failure`() = runTest {
        coEvery { instanceRepository.fetch("xizzhu.me") } throws RuntimeException("random exception")
        coEvery {
            authRepository.loadApplicationCredential("xizzhu.me")
        } returns ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "access_token",
            vapidKey = "vapid_key",
        )

        val viewAction = async { authViewModel.viewAction().first() }
        delay(100)

        authViewModel.selectInstance("xizzhu.me")
        assertEquals("xizzhu.me", (viewAction.await() as AuthViewModel.ViewAction.OpenLoginView).instanceUrl)
        assertEquals(AuthViewModel.ViewState(loading = false, instanceInfo = null, errorInfo = null), authViewModel.viewState().first())
    }

    @Test
    fun `test selectInstance with failure, but instance info fetched first`() = runTest {
        coEvery {
            instanceRepository.fetch("xizzhu.me")
        } returns Instance(
            title = "title",
            stats = Instance.Stats(
                userCount = 64,
                statusCount = 1989,
            )
        )
        coEvery {
            authRepository.loadApplicationCredential("xizzhu.me")
        } coAnswers {
            delay(100)
            throw RuntimeException("random exception")
        }

        authViewModel.selectInstance("xizzhu.me")
        delay(150)

        assertEquals(
            AuthViewModel.ViewState(
                loading = false,
                instanceInfo = AuthViewModel.ViewState.InstanceInfo(
                    title = "title",
                    userCount = 64,
                    statusCount = 1989,
                ),
                errorInfo = AuthViewModel.ViewState.ErrorInfo.FailedToSelectInstance,
            ),
            authViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectInstance with failure, also failed to fetch instance info`() = runTest {
        coEvery { instanceRepository.fetch("xizzhu.me") } throws RuntimeException("random exception")
        coEvery { authRepository.loadApplicationCredential("xizzhu.me") } throws RuntimeException("random exception")

        authViewModel.selectInstance("xizzhu.me")

        assertEquals(
            AuthViewModel.ViewState(
                loading = false,
                instanceInfo = null,
                errorInfo = AuthViewModel.ViewState.ErrorInfo.FailedToSelectInstance,
            ),
            authViewModel.viewState().first()
        )
    }

    @Test
    fun `test onLoginResult with successful login`() = runTest {
        val viewAction = async { authViewModel.viewAction().first() }
        delay(100)

        authViewModel.onLoginResult(true)

        assertTrue(viewAction.await() is AuthViewModel.ViewAction.PopBack)
    }

    @Test
    fun `test onLoginResult with failed login`() = runTest {
        authViewModel.onLoginResult(false)

        assertEquals(
            AuthViewModel.ViewState(loading = false, instanceInfo = null, errorInfo = AuthViewModel.ViewState.ErrorInfo.FailedToLogin),
            authViewModel.viewState().first()
        )
    }
}
