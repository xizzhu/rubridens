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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.model.Data
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import java.util.concurrent.atomic.AtomicBoolean

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val statusRepository: StatusRepository,
    private val homePresenter: HomePresenter,
) : BaseViewModel<HomeViewModel.ViewAction, HomeViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        items = emptyList(),
    )
) {
    sealed class ViewAction {
        object RequestUserCredential : ViewAction()
        object ShowNetworkError : ViewAction()
    }

    data class ViewState(val loading: Boolean, val items: List<FeedItem<*>>)

    companion object {
        private const val STATUSES_TO_LOAD_PER_REQUEST = 20
    }

    private val loading = AtomicBoolean(false)

    private val hasNewerStatuses = AtomicBoolean(true)
    private val hasOlderStatuses = AtomicBoolean(true)

    fun loadLatest() = load {
        val userCredential = getUserCredential() ?: return@load

        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
            )
        }
        homePresenter.clear()
        statusRepository.loadLatest(userCredential, STATUSES_TO_LOAD_PER_REQUEST)
            .onEach { data ->
                val isLoading = when (data) {
                    is Data.Local -> {
                        homePresenter.replace(data.data)
                        true
                    }
                    is Data.Remote -> {
                        homePresenter.prepend(data.data)
                        false
                    }
                }

                val items = homePresenter.feedItems()
                emitViewState { currentViewState ->
                    currentViewState.copy(
                        loading = isLoading,
                        items = items,
                    )
                }
            }
            .catch { e ->
                if (e is NetworkException.Other) {
                    emitViewAction(ViewAction.ShowNetworkError)
                }
                emitViewState { currentViewState -> currentViewState.copy(loading = false) }
            }
            .collect()
    }

    fun loadNewer() = load {
        if (!hasNewerStatuses.get()) return@load
        val userCredential = getUserCredential() ?: return@load
    }

    fun loadOlder() = load {}

    private inline fun load(crossinline block: suspend () -> Unit) {
        if (loading.getAndSet(true)) return

        viewModelScope.launch {
            block()
            loading.set(false)
        }
    }

    private suspend fun getUserCredential(): UserCredential? {
        // TODO Supports multiple accounts
        val userCredential = authRepository.readUserCredentials().firstOrNull()
        if (userCredential == null) {
            emitViewAction(ViewAction.RequestUserCredential)

            homePresenter.clear()
            emitViewState { currentViewState ->
                currentViewState.copy(
                    loading = false,
                    items = emptyList(),
                )
            }
        }
        return userCredential
    }
}
