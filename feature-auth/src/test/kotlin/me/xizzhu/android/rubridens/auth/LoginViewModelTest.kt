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
import io.mockk.Ordering
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
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
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var authRepository: AuthRepository

    private lateinit var loginViewModel: LoginViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)

        loginViewModel = LoginViewModel(authRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial view state`() = runTest {
        assertEquals(LoginViewModel.ViewState(loading = false, hideWebView = false), loginViewModel.viewState().first())
    }

    @Test
    fun `test login with success`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } returns "xizzhu.me/login"

        val viewAction = async { loginViewModel.viewAction().first() }
        delay(100)

        loginViewModel.login("xizzhu.me")

        assertEquals(LoginViewModel.ViewState(loading = true, hideWebView = false), loginViewModel.viewState().first())
        assertEquals("xizzhu.me/login", (viewAction.await() as LoginViewModel.ViewAction.OpenLoginView).loginUrl)
        coVerify(exactly = 1) { authRepository.getLoginUrl("xizzhu.me") }
    }

    @Test
    fun `test login with failure`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } throws RuntimeException("random exception")

        val viewAction = async { loginViewModel.viewAction().first() }
        delay(100)

        loginViewModel.login("xizzhu.me")

        assertEquals(LoginViewModel.ViewState(loading = true, hideWebView = false), loginViewModel.viewState().first())
        assertFalse((viewAction.await() as LoginViewModel.ViewAction.PopBack).loginSuccessful)
        coVerify(exactly = 1) { authRepository.getLoginUrl("xizzhu.me") }
    }

    @Test
    fun `test onPageLoaded with login page loaded`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } returns "xizzhu.me/login"

        loginViewModel.login("xizzhu.me") // makes sure we have the login URL correctly set
        loginViewModel.onPageLoaded("url", "xizzhu.me/login")

        assertEquals(LoginViewModel.ViewState(loading = false, hideWebView = false), loginViewModel.viewState().first())
        coVerify(exactly = 1) { authRepository.getLoginUrl("xizzhu.me") }
    }

    @Test
    fun `test onPageLoaded with random page loaded`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } returns "xizzhu.me/login"

        loginViewModel.login("xizzhu.me") // makes sure we have the login URL correctly set
        loginViewModel.onPageLoaded("random", "xizzhu.me/random")

        assertEquals(LoginViewModel.ViewState(loading = true, hideWebView = false), loginViewModel.viewState().first())
        coVerify(ordering = Ordering.SEQUENCE) {
            authRepository.getLoginUrl("xizzhu.me")
            authRepository.getAuthCode("random")
        }
    }

    @Test
    fun `test onPageLoaded with auth code loaded successfully`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } returns "xizzhu.me/login"
        every { authRepository.getAuthCode("xizzhu.me?code=auth_code") } returns "auth_code"
        coEvery { authRepository.createUserToken("xizzhu.me", "auth_code") } returns mockk()

        loginViewModel.login("xizzhu.me") // makes sure we have the login URL correctly set

        val viewAction = async { loginViewModel.viewAction().first() }
        delay(100)

        loginViewModel.onPageLoaded("xizzhu.me?code=auth_code", "")

        assertEquals(LoginViewModel.ViewState(loading = true, hideWebView = true), loginViewModel.viewState().first())
        assertTrue((viewAction.await() as LoginViewModel.ViewAction.PopBack).loginSuccessful)
        coVerify(ordering = Ordering.SEQUENCE) {
            authRepository.getLoginUrl("xizzhu.me")
            authRepository.getAuthCode("xizzhu.me?code=auth_code")
            authRepository.createUserToken("xizzhu.me", "auth_code")
        }
    }

    @Test
    fun `test onPageLoaded with login page, then empty auth code`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } returns "xizzhu.me/login"
        every { authRepository.getAuthCode("xizzhu.me?code=auth_code") } returns ""

        loginViewModel.login("xizzhu.me") // makes sure we have the login URL correctly set
        loginViewModel.onPageLoaded("login", "xizzhu.me/login")
        loginViewModel.onPageLoaded("xizzhu.me?code=auth_code", "")

        assertEquals(LoginViewModel.ViewState(loading = false, hideWebView = false), loginViewModel.viewState().first())
        coVerify(ordering = Ordering.SEQUENCE) {
            authRepository.getLoginUrl("xizzhu.me")
            authRepository.getAuthCode("xizzhu.me?code=auth_code")
        }
    }

    @Test
    fun `test onPageLoaded with auth code failed to load`() = runTest {
        coEvery { authRepository.getLoginUrl("xizzhu.me") } returns "xizzhu.me/login"
        every { authRepository.getAuthCode("xizzhu.me?code=auth_code") } returns "auth_code"
        coEvery { authRepository.createUserToken("xizzhu.me", "auth_code") } throws RuntimeException("random exception")

        loginViewModel.login("xizzhu.me") // makes sure we have the login URL correctly set

        val viewAction = async { loginViewModel.viewAction().first() }
        delay(100)

        loginViewModel.onPageLoaded("xizzhu.me?code=auth_code", "")

        assertEquals(LoginViewModel.ViewState(loading = true, hideWebView = true), loginViewModel.viewState().first())
        assertFalse((viewAction.await() as LoginViewModel.ViewAction.PopBack).loginSuccessful)
        coVerify(ordering = Ordering.SEQUENCE) {
            authRepository.getLoginUrl("xizzhu.me")
            authRepository.getAuthCode("xizzhu.me?code=auth_code")
            authRepository.createUserToken("xizzhu.me", "auth_code")
        }
    }
}
