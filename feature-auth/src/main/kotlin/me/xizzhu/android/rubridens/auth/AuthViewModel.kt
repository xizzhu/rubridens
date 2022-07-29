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
import me.xizzhu.android.rubridens.core.repository.InstanceRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val instanceRepository: InstanceRepository,
) : BaseViewModel<AuthViewModel.ViewAction, AuthViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        instanceInfo = null,
        errorInfo = null,
    )
) {
    sealed class ViewAction {
        class OpenLoginView(val instanceUrl: String) : ViewAction()
        object PopBack : ViewAction()
    }

    data class ViewState(val loading: Boolean, val instanceInfo: InstanceInfo?, val errorInfo: ErrorInfo?) {
        data class InstanceInfo(val title: String, val userCount: Long, val statusCount: Long)

        sealed class ErrorInfo {
            object FailedToLogin : ErrorInfo()
            object FailedToSelectInstance : ErrorInfo()
        }
    }

    fun selectInstance(instanceUrl: String) {
        emitViewState { currentViewState ->
            currentViewState.copy(loading = true, instanceInfo = null, errorInfo = null)
        }

        viewModelScope.launch {
            kotlin.runCatching {
                instanceRepository.fetch(instanceUrl)
            }.onSuccess { instance ->
                emitViewState { currentViewState ->
                    currentViewState.copy(
                        instanceInfo = ViewState.InstanceInfo(
                            title = instance.title,
                            userCount = instance.stats.userCount,
                            statusCount = instance.stats.statusCount,
                        ),
                    )
                }
            }
        }

        viewModelScope.launch {
            kotlin.runCatching {
                authRepository.loadApplicationCredential(instanceUrl)
            }.onSuccess {
                emitViewAction(ViewAction.OpenLoginView(instanceUrl = instanceUrl))
                emitViewState { currentViewState -> currentViewState.copy(loading = false) }
            }.onFailure {
                emitViewState { currentViewState ->
                    currentViewState.copy(
                        loading = false,
                        errorInfo = ViewState.ErrorInfo.FailedToSelectInstance,
                    )
                }
            }
        }
    }

    fun onLoginResult(loginSuccessful: Boolean) {
        if (loginSuccessful) {
            emitViewAction(ViewAction.PopBack)
        } else {
            emitViewState { currentViewState ->
                currentViewState.copy(
                    loading = false,
                    errorInfo = ViewState.ErrorInfo.FailedToLogin,
                )
            }
        }
    }
}
