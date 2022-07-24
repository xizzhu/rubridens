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

import me.xizzhu.android.rubridens.core.mvvm.BaseViewModel
import me.xizzhu.android.rubridens.core.repository.AuthRepository

class HomeViewModel(
    private val authRepository: AuthRepository,
) : BaseViewModel<HomeViewModel.ViewAction, HomeViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
    )
) {
    sealed class ViewAction

    data class ViewState(val loading: Boolean)

    fun loadLatest() {
        emitViewState { currentViewState ->
            currentViewState.copy(
                loading = true,
            )
        }
    }
}
