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

package me.xizzhu.android.rubridens.status

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.StatusContext
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import me.xizzhu.android.rubridens.core.view.feed.FeedItem

class StatusViewModel(
    private val authRepository: AuthRepository,
    private val statusRepository: StatusRepository,
    private val statusPresenter: StatusPresenter,
) : BaseViewModel<StatusViewModel.ViewAction, StatusViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        items = emptyList(),
        scrollToPosition = -1,
    )
) {
    sealed class ViewAction {
        object ShowNetworkError : ViewAction()
    }

    data class ViewState(val loading: Boolean, val items: List<FeedItem<*>>, val scrollToPosition: Int)

    fun loadStatus(statusId: EntityKey) {
        viewModelScope.launch { loadStatusInternal(statusId) }
    }

    private suspend fun loadStatusInternal(statusId: EntityKey) {
        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
                items = emptyList(),
                scrollToPosition = -1,
            )
        }

        val userCredential = authRepository.readUserCredentialsByInstanceUrl(statusId.instanceUrl).firstOrNull()
        combine(
            statusRepository.load(
                userCredential = userCredential,
                statusId = statusId,
            ),
            fetchStatusContext(
                userCredential = userCredential,
                statusId = statusId,
            )
        ) { status, statusContext ->
            val (items, scrollToPosition) = statusPresenter.buildFeedItems(status.data, statusContext)
            emitViewState { currentViewState ->
                currentViewState.copy(items = items, scrollToPosition = scrollToPosition)
            }
        }.catch { e ->
            if (e is NetworkException.Other) {
                emitViewAction(ViewAction.ShowNetworkError)
            }
        }.onCompletion {
            emitViewState { it.copy(loading = false) }
        }.collect()
    }

    private fun fetchStatusContext(userCredential: UserCredential?, statusId: EntityKey): Flow<StatusContext?> = flow {
        // We always need to fetch status context from backend, so emit null first to allow fast rendering.
        emit(null)

        emit(statusRepository.fetchContext(
            userCredential = userCredential,
            statusId = statusId,
        ))
    }
}
