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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import me.xizzhu.android.rubridens.auth.databinding.ActivityAuthBinding
import me.xizzhu.android.rubridens.core.mvvm.BaseActivity
import me.xizzhu.android.rubridens.core.view.fadeOut
import me.xizzhu.android.rubridens.core.view.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : BaseActivity<AuthViewModel.ViewAction, AuthViewModel.ViewState, ActivityAuthBinding, AuthViewModel>() {
    companion object {
        internal const val KEY_LOGIN_SUCCESSFUL = "AuthActivity.KEY_LOGIN_SUCCESSFUL"
        private const val KEY_INTENT_TO_START_ON_SUCCESS = "AuthActivity.KEY_INTENT_TO_START_ON_SUCCESS"

        fun newStartIntent(context: Context, intentToStartOnSuccess: Intent?): Intent =
                Intent(context, AuthActivity::class.java).apply {
                    intentToStartOnSuccess?.let { putExtra(KEY_INTENT_TO_START_ON_SUCCESS, it) }
                }
    }

    private val loginResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onLoginResult(result.data?.getBooleanExtra(KEY_LOGIN_SUCCESSFUL, false) == true)
        }
    }

    override val viewBinding: ActivityAuthBinding by lazy { ActivityAuthBinding.inflate(layoutInflater) }

    override val viewModel: AuthViewModel by viewModel()

    override fun onViewCreated() = with(viewBinding) {
        instance.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    selectInstance()
                    true
                }
                else -> false
            }
        }
        next.setOnClickListener { selectInstance() }
    }

    private fun selectInstance() {
        viewBinding.instance.text?.toString().takeUnless { it.isNullOrEmpty() }
                ?.let { instanceUrl ->
                    instanceUrl.trim().let { trimmed ->
                        when {
                            trimmed.startsWith("http://") -> trimmed.substring(7)
                            trimmed.startsWith("https://") -> trimmed.substring(8)
                            else -> trimmed
                        }
                    }
                }
                ?.let { viewModel.selectInstance(it) }
    }

    override fun onViewAction(viewAction: AuthViewModel.ViewAction) = when (viewAction) {
        is AuthViewModel.ViewAction.OpenLoginView -> {
            loginResultLauncher.launch(LoginActivity.newStartIntent(this, viewAction.instanceUrl))
        }
        AuthViewModel.ViewAction.PopBack -> {
            intent.getParcelableExtra<Intent>(KEY_INTENT_TO_START_ON_SUCCESS)?.let { intentToStartOnSuccess ->
                startActivity(intentToStartOnSuccess)
            }
            finish()
        }
    }

    override fun onViewState(viewState: AuthViewModel.ViewState) = with(viewBinding) {
        if (viewState.loading) {
            instance.hideKeyboard()
            instance.isEnabled = false
            next.isEnabled = false
            loadingSpinner.visibility = View.VISIBLE
        } else {
            instance.isEnabled = true
            next.isEnabled = true
            loadingSpinner.fadeOut()
        }

        if (viewState.errorInfo == null) {
            info.isVisible = true
            info.text = viewState.instanceInfo?.let { instanceInfo ->
                getString(
                        R.string.auth_instance_selection_text_instance_info,
                        instanceInfo.userCount,
                        instanceInfo.statusCount,
                        instanceInfo.title
                )
            }

            error.isVisible = false
        } else {
            info.isVisible = false

            error.isVisible = true
            error.setText(when (viewState.errorInfo) {
                AuthViewModel.ViewState.ErrorInfo.FailedToLogin -> R.string.auth_instance_selection_error_failed_to_login
                AuthViewModel.ViewState.ErrorInfo.FailedToSelectInstance -> R.string.auth_instance_selection_error_failed_to_select_instance
            })
        }
    }
}
