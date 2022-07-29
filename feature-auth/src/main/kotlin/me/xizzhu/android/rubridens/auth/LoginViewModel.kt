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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel<LoginViewModel.ViewAction, LoginViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        hideWebView = false
    )
) {
    sealed class ViewAction {
        class OpenLoginView(val loginUrl: String) : ViewAction()
        class PopBack(val loginSuccessful: Boolean) : ViewAction()
    }

    data class ViewState(val loading: Boolean, val hideWebView: Boolean)

    private var instanceUrl: String = ""
    private var loginUrl: String = ""

    fun login(instanceUrl: String) {
        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                hideWebView = false
            )
        }
        this.instanceUrl = instanceUrl

        viewModelScope.launch {
            kotlin.runCatching {
                authRepository.getLoginUrl(instanceUrl)
            }.onSuccess { loginUrl ->
                this@LoginViewModel.loginUrl = loginUrl
                emitViewAction(ViewAction.OpenLoginView(loginUrl))
            }.onFailure {
                emitViewAction(ViewAction.PopBack(
                    loginSuccessful = false
                ))
            }
        }
    }

    fun onPageLoaded(url: String, originalUrl: String) {
        if (loginUrl.isNotEmpty() && loginUrl == originalUrl) {
            // login page loaded
            emitViewState { currentViewState ->
                currentViewState.copy(
                    loading = false
                )
            }
            return
        }

        val authCode = authRepository.getAuthCode(url)
        if (authCode?.isNotEmpty() == true) {
            // user has granted the access and we got the auth code
            emitViewState { currentViewState ->
                currentViewState.copy(
                    loading = true,
                    hideWebView = true
                )
            }

            viewModelScope.launch {
                val loginSuccessful = kotlin.runCatching { authRepository.createUserToken(instanceUrl, authCode) }.isSuccess
                emitViewAction(ViewAction.PopBack(
                    loginSuccessful = loginSuccessful
                ))
            }
        }
    }
}
