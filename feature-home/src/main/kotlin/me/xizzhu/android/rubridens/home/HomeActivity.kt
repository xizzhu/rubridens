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

import android.content.Context
import android.content.Intent
import me.xizzhu.android.rubridens.core.infra.BaseActivity
import me.xizzhu.android.rubridens.home.databinding.ActivityHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity<HomeViewModel.ViewAction, HomeViewModel.ViewState, ActivityHomeBinding, HomeViewModel>() {
    companion object {
        fun newStartIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
    }

    override val viewBinding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated() = with(viewBinding) {
        swipeRefresher.setOnRefreshListener { viewModel.loadLatest() }

        viewModel.loadLatest()
    }

    override fun onViewAction(viewAction: HomeViewModel.ViewAction) = when (viewAction) {
        is HomeViewModel.ViewAction.OpenStatus -> {
            navigator.goToStatus(this, viewAction.status)
        }
        is HomeViewModel.ViewAction.FavoriteStatus -> {
            // TODO
        }
        is HomeViewModel.ViewAction.ReblogStatus -> {
            // TODO
        }
        is HomeViewModel.ViewAction.ReplyToStatus -> {
            // TODO
        }
        is HomeViewModel.ViewAction.OpenUser -> {
            navigator.gotoUser(this, viewAction.user)
        }
        HomeViewModel.ViewAction.RequestUserCredential -> {
            navigator.goToAuthentication(this)
            finish()
        }
    }

    override fun onViewState(viewState: HomeViewModel.ViewState) = with(viewBinding) {
        swipeRefresher.isRefreshing = viewState.loading
        feed.setItems(viewState.items)
    }
}
