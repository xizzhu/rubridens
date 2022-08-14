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

import me.xizzhu.android.rubridens.core.infra.BaseViewModel
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import me.xizzhu.android.rubridens.core.repository.StatusRepository
import me.xizzhu.android.rubridens.core.view.feed.FeedItem

class StatusViewModel(
    private val authRepository: AuthRepository,
    private val statusRepository: StatusRepository,
) : BaseViewModel<StatusViewModel.ViewAction, StatusViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        items = emptyList(),
        scrollToPosition = -1,
    )
) {
    sealed class ViewAction {
    }

    data class ViewState(val loading: Boolean, val items: List<FeedItem<*>>, val scrollToPosition: Int)
}
