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
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.model.Data
import me.xizzhu.android.rubridens.core.model.Status
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
        scrollToPosition = -1,
    )
) {
    sealed class ViewAction {
        object RequestUserCredential : ViewAction()
        object ShowNetworkError : ViewAction()
    }

    data class ViewState(val loading: Boolean, val items: List<FeedItem<*>>, val scrollToPosition: Int)

    companion object {
        private const val STATUSES_TO_LOAD_PER_REQUEST = 20
    }

    private val loading = AtomicBoolean(false)

    // They are only accessed from "load", which is guaranteed to have only one "instance" running.
    // Therefore, no lock is needed.
    private var hasNewerStatuses = true
    private var newestStatus: Status? = null
    private var hasOlderStatuses = true
    private var oldestStatus: Status? = null

    fun freshLatest() = load {
        val userCredential = getUserCredential() ?: return@load

        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
                scrollToPosition = -1,
            )
        }

        homePresenter.clear()
        hasNewerStatuses = true
        hasOlderStatuses = true

        statusRepository.freshLatest(userCredential, STATUSES_TO_LOAD_PER_REQUEST)
            .onEach { data ->
                homePresenter.replace(data.data)

                newestStatus = data.data.firstOrNull()
                oldestStatus = data.data.lastOrNull()

                when (data) {
                    is Data.Local -> {
                        emitViewAction(ViewAction.ShowNetworkError)
                    }
                    is Data.Remote -> {
                        if (data.data.size < STATUSES_TO_LOAD_PER_REQUEST) {
                            hasNewerStatuses = false
                        }
                    }
                }

                val items = homePresenter.feedItems()
                emitViewState { currentViewState ->
                    currentViewState.copy(
                        loading = false,
                        items = items,
                        scrollToPosition = 0,
                    )
                }
            }
            .collect()
    }

    fun loadLatest() = load {
        val userCredential = getUserCredential() ?: return@load

        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
                scrollToPosition = -1,
            )
        }

        homePresenter.clear()
        hasNewerStatuses = true
        hasOlderStatuses = true

        statusRepository.loadLatest(userCredential, STATUSES_TO_LOAD_PER_REQUEST)
            .withIndex()
            .onEach { indexedValue ->
                val isLoading = when (val data = indexedValue.value) {
                    is Data.Local -> {
                        homePresenter.replace(data.data)
                        newestStatus = data.data.first()
                        oldestStatus = data.data.last()
                        true
                    }
                    is Data.Remote -> {
                        homePresenter.prepend(data.data)
                        if (data.data.size < STATUSES_TO_LOAD_PER_REQUEST) {
                            hasNewerStatuses = false
                        }
                        data.data.firstOrNull()?.let { newestStatus = it }
                        if (oldestStatus == null) {
                            oldestStatus = data.data.lastOrNull()
                        }
                        false
                    }
                }

                val items = homePresenter.feedItems()
                emitViewState { currentViewState ->
                    currentViewState.copy(
                        loading = isLoading,
                        items = items,
                        scrollToPosition = if (indexedValue.index == 0) 0 else -1,
                    )
                }
            }
            .catch { e -> handleLoadingException(e) }
            .collect()
    }

    fun loadNewer() = load {
        if (!hasNewerStatuses) return@load
        val newest = newestStatus ?: return@load
        val userCredential = getUserCredential() ?: return@load

        statusRepository.loadNewer(userCredential, newest, STATUSES_TO_LOAD_PER_REQUEST)
            .onEach { data ->
                homePresenter.prepend(data.data)

                if (data is Data.Remote && data.data.size < STATUSES_TO_LOAD_PER_REQUEST) {
                    hasNewerStatuses = false
                }
                data.data.firstOrNull()?.let { newestStatus = it }

                val items = homePresenter.feedItems()
                emitViewState { currentViewState -> currentViewState.copy(items = items, scrollToPosition = -1) }
            }
            .catch { e -> handleLoadingException(e) }
            .collect()
    }

    fun loadOlder() = load {
        if (!hasOlderStatuses) return@load
        val oldest = oldestStatus ?: return@load
        val userCredential = getUserCredential() ?: return@load

        statusRepository.loadOlder(userCredential, oldest, STATUSES_TO_LOAD_PER_REQUEST)
            .onEach { data ->
                homePresenter.append(data.data)

                if (data is Data.Remote && data.data.size < STATUSES_TO_LOAD_PER_REQUEST) {
                    hasOlderStatuses = false
                    homePresenter.noMoreItemsToAppend()
                }
                data.data.lastOrNull()?.let { oldestStatus = it }

                val items = homePresenter.feedItems()
                emitViewState { currentViewState -> currentViewState.copy(items = items, scrollToPosition = -1) }
            }
            .catch { e -> handleLoadingException(e) }
            .collect()
    }

    /**
     * NOTE: Always calls this helper function to load data. It makes sure only one loading is going on.
     */
    private inline fun load(crossinline block: suspend () -> Unit) {
        if (loading.getAndSet(true)) return

        viewModelScope.launch {
            block()
            loading.set(false)
        }
    }

    private fun handleLoadingException(e: Throwable) {
        if (e is NetworkException.Other) {
            emitViewAction(ViewAction.ShowNetworkError)
        }
        emitViewState { currentViewState -> currentViewState.copy(loading = false, scrollToPosition = -1) }
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
                    scrollToPosition = -1,
                )
            }
        }
        return userCredential
    }
}
