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
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
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

    fun loadLatest() = load {
        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
            )
        }

        val userCredential = getUserCredential() ?: return@load
        val items = buildFeedItems(statusRepository.loadLatest(userCredential))
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

    private fun openStatus(status: Status) {
        emitViewAction(ViewAction.OpenStatus(status))
    }

    private fun replyToStatus(status: Status) {
        emitViewAction(ViewAction.ReplyToStatus(status))
    }

    private fun reblogStatus(status: Status) {
        emitViewAction(ViewAction.ReblogStatus(status))
    }

    private fun favoriteStatus(status: Status) {
        emitViewAction(ViewAction.FavoriteStatus(status))
    }

    private fun openUser(user: User) {
        emitViewAction(ViewAction.OpenUser(user))
    }
}
