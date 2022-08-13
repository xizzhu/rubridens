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

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.model.Data
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import java.util.concurrent.atomic.AtomicBoolean

class HomeViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val statusRepository: StatusRepository,
) : BaseViewModel<HomeViewModel.ViewAction, HomeViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        items = emptyList(),
    )
) {
    sealed class ViewAction {
        data class OpenStatus(val status: Status) : ViewAction()
        data class ReplyToStatus(val status: Status) : ViewAction()
        data class ReblogStatus(val status: Status) : ViewAction()
        data class FavoriteStatus(val status: Status) : ViewAction()
        data class OpenUser(val user: User) : ViewAction()
        data class OpenMedia(val media: Media) : ViewAction()
        data class OpenTag(val tag: String) : ViewAction()
        data class OpenUrl(val url: String) : ViewAction()
        object RequestUserCredential : ViewAction()
        object ShowNetworkError : ViewAction()
    }

    data class ViewState(val loading: Boolean, val items: List<FeedItem<*>>)

    private val loading = AtomicBoolean(false)

    private val hasNewerStatuses = AtomicBoolean(true)
    private val hasOlderStatuses = AtomicBoolean(true)

    private val homePresenter = HomePresenter(
        application = application,
        openStatus = ::openStatus,
        replyToStatus = ::replyToStatus,
        reblogStatus = ::reblogStatus,
        favoriteStatus = ::favoriteStatus,
        openUser = ::openUser,
        openMedia = ::openMedia,
        openTag = ::openTag,
        openUrl = ::openUrl,
    )

    fun loadLatest() = load {
        val userCredential = getUserCredential() ?: return@load

        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
            )
        }
        homePresenter.clear()
        statusRepository.loadLatest(userCredential)
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

    private fun openMedia(media: Media) {
        emitViewAction(ViewAction.OpenMedia(media))
    }

    private fun openTag(tag: String) {
        emitViewAction(ViewAction.OpenTag(tag))
    }

    private fun openUrl(url: String) {
        emitViewAction(ViewAction.OpenUrl(url))
    }
}
