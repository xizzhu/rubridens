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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.repository.model.Status
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.repository.model.UserCredential
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import java.util.concurrent.atomic.AtomicBoolean

class HomeViewModel(
    private val homePresenter: HomePresenter,
    private val authRepository: AuthRepository,
    private val statusRepository: StatusRepository,
) : BaseViewModel<HomeViewModel.ViewAction, HomeViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        items = emptyList(),
    )
) {
    sealed class ViewAction {
        class OpenStatus(val status: Status) : ViewAction()
        class ReplyToStatus(val status: Status) : ViewAction()
        class ReblogStatus(val status: Status) : ViewAction()
        class FavoriteStatus(val status: Status) : ViewAction()
        class OpenUser(val user: User) : ViewAction()
        object RequestUserCredential : ViewAction()
    }

    data class ViewState(val loading: Boolean, val items: List<FeedItem<*>>)

    private val loading = AtomicBoolean(false)

    private val statusesMutex = Mutex()
    private val statuses = hashMapOf<String, Status>()
    private val users = hashMapOf<String, User>()

    fun loadLatest() = load {
        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
            )
        }

        val userCredential = getUserCredential() ?: return@load
        val latest = statusRepository.loadLatest(userCredential)
        statusesMutex.withLock {
            statuses.clear()
            users.clear()
            latest.forEach { status ->
                statuses[homePresenter.createUniqueStatusId(status)] = status
                users[homePresenter.createUniqueUserId(status.sender)] = status.sender
            }
        }
        val items = buildFeedItems(latest)
        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = false,
                items = items,
            )
        }
    }

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
            emitViewState { currentViewState ->
                currentViewState.copy(
                    loading = false,
                    items = emptyList(),
                )
            }
        }
        return userCredential
    }

    private fun buildFeedItems(statuses: List<Status>): List<FeedItem<*>> = homePresenter.buildFeedItems(
        statuses = statuses,
        openStatus = ::openStatus,
        replyToStatus = ::replyToStatus,
        reblogStatus = ::reblogStatus,
        favoriteStatus = ::favoriteStatus,
        openUser = ::openUser,
    )

    private fun openStatus(uniqueStatusId: String) {
        viewModelScope.launch {
            val status = statusesMutex.withLock { statuses[uniqueStatusId] } ?: return@launch
            emitViewAction(ViewAction.OpenStatus(status))
        }
    }

    private fun replyToStatus(uniqueStatusId: String) {
        viewModelScope.launch {
            val status = statusesMutex.withLock { statuses[uniqueStatusId] } ?: return@launch
            emitViewAction(ViewAction.ReplyToStatus(status))
        }
    }

    private fun reblogStatus(uniqueStatusId: String) {
        viewModelScope.launch {
            val status = statusesMutex.withLock { statuses[uniqueStatusId] } ?: return@launch
            emitViewAction(ViewAction.ReblogStatus(status))
        }
    }

    private fun favoriteStatus(uniqueStatusId: String) {
        viewModelScope.launch {
            val status = statusesMutex.withLock { statuses[uniqueStatusId] } ?: return@launch
            emitViewAction(ViewAction.FavoriteStatus(status))
        }
    }

    private fun openUser(uniqueUserId: String) {
        viewModelScope.launch {
            val user = statusesMutex.withLock { users[uniqueUserId] } ?: return@launch
            emitViewAction(ViewAction.OpenUser(user))
        }
    }
}
