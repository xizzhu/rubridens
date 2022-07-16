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

package me.xizzhu.android.rubridens.core.mvvm

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseActivity<ViewAction, ViewState, VB : ViewBinding, VM : BaseViewModel<ViewAction, ViewState>> : AppCompatActivity() {
    protected abstract val viewBinding: VB
    protected abstract val viewModel: VM

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBinding.root)
        onViewCreated()

        viewModel.viewAction().onEach(::onViewAction).launchIn(lifecycleScope)
        viewModel.viewState().onEach(::onViewState).launchIn(lifecycleScope)
    }

    protected abstract fun onViewCreated()

    protected abstract fun onViewAction(viewAction: ViewAction)

    protected abstract fun onViewState(viewState: ViewState)
}
