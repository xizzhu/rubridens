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

import android.content.Context
import android.content.Intent
import me.xizzhu.android.rubridens.core.infra.BaseActivity
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.status.databinding.ActivityStatusBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatusActivity : BaseActivity<StatusViewModel.ViewAction, StatusViewModel.ViewState, ActivityStatusBinding, StatusViewModel>() {
    companion object {
        private const val KEY_STATUS_INSTANCE_URL = "StatusActivity.KEY_STATUS_INSTANCE_URL"
        private const val KEY_STATUS_ID = "StatusActivity.KEY_STATUS_ID"

        fun newStartIntent(context: Context, status: Status): Intent {
            if (status.id.instanceUrl.isEmpty()) {
                throw IllegalArgumentException("instanceUrl is empty")
            }
            if (status.id.id.isEmpty()) {
                throw IllegalArgumentException("id is empty")
            }

            return Intent(context, StatusActivity::class.java)
                .putExtra(KEY_STATUS_INSTANCE_URL, status.id.instanceUrl)
                .putExtra(KEY_STATUS_ID, status.id.id)
        }
    }

    override val viewBinding: ActivityStatusBinding by lazy { ActivityStatusBinding.inflate(layoutInflater) }

    override val viewModel: StatusViewModel by viewModel()

    override fun onViewCreated() = with(viewBinding) {
        swipeRefresher.setOnRefreshListener { /* TODO */ }

        feed.init(
            openStatus = { status -> navigator.goToStatus(this@StatusActivity, status) },
            replyToStatus = { status -> /* TODO */ },
            reblogStatus = { status -> /* TODO */ },
            favoriteStatus = { status -> /* TODO */ },
            openUser = { user -> navigator.gotoUser(this@StatusActivity, user) },
            openMedia = { media -> navigator.gotoMedia(this@StatusActivity, media) },
            openTag = { tag -> navigator.goToTag(this@StatusActivity, tag) },
            openUrl = { url -> navigator.gotoUrl(this@StatusActivity, url) },
        )
    }

    override fun onViewAction(viewAction: StatusViewModel.ViewAction) = when (viewAction) {
        else -> {}
    }

    override fun onViewState(viewState: StatusViewModel.ViewState) = with(viewBinding) {
        swipeRefresher.isRefreshing = viewState.loading
        feed.setItems(viewState.items, viewState.scrollToPosition)
    }
}
