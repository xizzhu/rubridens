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

package me.xizzhu.android.rubridens.auth

import android.content.Context
import android.content.Intent
import android.view.View
import me.xizzhu.android.rubridens.auth.databinding.ActivityLoginBinding
import me.xizzhu.android.rubridens.core.mvvm.BaseActivity
import me.xizzhu.android.rubridens.core.view.fadeOut
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class LoginActivity : BaseActivity<LoginViewModel.ViewAction, LoginViewModel.ViewState, ActivityLoginBinding, LoginViewModel>() {
    companion object {
        private const val KEY_INSTANCE_URL = "LoginActivity.KEY_INSTANCE_URL"

        fun newStartIntent(context: Context, instanceUrl: String): Intent {
            if (instanceUrl.isEmpty()) {
                throw IllegalArgumentException("instanceUrl is empty")
            }

            return Intent(context, LoginActivity::class.java)
                .putExtra(KEY_INSTANCE_URL, instanceUrl)
        }
    }

    override val viewBinding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated() {
        viewBinding.webView.onPageLoaded = { url, originalUrl ->
            viewModel.onPageLoaded(url, originalUrl)
        }

        viewModel.login(intent.getStringExtra(KEY_INSTANCE_URL)!!)
    }

    override fun onViewAction(viewAction: LoginViewModel.ViewAction) = when (viewAction) {
        is LoginViewModel.ViewAction.OpenLoginView -> {
            viewBinding.webView.load(viewAction.loginUrl)
        }
        is LoginViewModel.ViewAction.PopBack -> {
            setResult(RESULT_OK, Intent().putExtra(AuthActivity.KEY_LOGIN_SUCCESSFUL, viewAction.loginSuccessful))
            finish()
        }
    }

    override fun onViewState(viewState: LoginViewModel.ViewState) = with(viewBinding) {
        if (viewState.loading) {
            loadingSpinner.visibility = View.VISIBLE
        } else {
            loadingSpinner.fadeOut()
        }

        webView.visibility = if (viewState.hideWebView) View.GONE else View.VISIBLE
    }
}
