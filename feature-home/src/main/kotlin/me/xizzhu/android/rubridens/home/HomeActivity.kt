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
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.rubridens.core.infra.BaseActivity
import me.xizzhu.android.rubridens.core.view.toast
import me.xizzhu.android.rubridens.home.databinding.ActivityHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity<HomeViewModel.ViewAction, HomeViewModel.ViewState, ActivityHomeBinding, HomeViewModel>() {
    companion object {
        private const val LOAD_MORE_ITEMS_POSITION_THRESHOLD = 6

        fun newStartIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
    }

    override val viewBinding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated() = with(viewBinding) {
        swipeRefresher.setOnRefreshListener { viewModel.freshLatest() }

        feed.init(
            openStatus = { status -> navigator.goToStatus(this@HomeActivity, status) },
            replyToStatus = { status -> /* TODO */ },
            reblogStatus = { status -> /* TODO */ },
            favoriteStatus = { status -> /* TODO */ },
            shareStatus = { status -> /* TODO */ },
            openUser = { user -> navigator.gotoUser(this@HomeActivity, user) },
            openMedia = { media -> navigator.gotoMedia(this@HomeActivity, media) },
            openTag = { tag -> navigator.goToTag(this@HomeActivity, tag) },
            openUrl = { url -> navigator.gotoUrl(this@HomeActivity, url) },
        )
        feed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (feed.firstVisibleItemPosition() <= LOAD_MORE_ITEMS_POSITION_THRESHOLD) {
                    viewModel.loadNewer()
                } else if (feed.itemCount() - feed.lastVisibleItemPosition() <= LOAD_MORE_ITEMS_POSITION_THRESHOLD) {
                    viewModel.loadOlder()
                }
            }
        })

        viewModel.loadLatest()
    }

    override fun onViewAction(viewAction: HomeViewModel.ViewAction) = when (viewAction) {
        HomeViewModel.ViewAction.RequestUserCredential -> {
            navigator.goToAuthentication(this)
            finish()
        }
        HomeViewModel.ViewAction.ShowNetworkError -> {
            toast(R.string.error_network_failure)
        }
    }

    override fun onViewState(viewState: HomeViewModel.ViewState) = with(viewBinding) {
        swipeRefresher.isRefreshing = viewState.loading
        feed.setItems(viewState.items, viewState.scrollToPosition)
    }
}
