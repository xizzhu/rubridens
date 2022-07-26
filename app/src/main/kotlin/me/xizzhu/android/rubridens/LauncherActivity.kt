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

package me.xizzhu.android.rubridens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.xizzhu.android.rubridens.core.infra.Navigator
import me.xizzhu.android.rubridens.core.repository.AuthRepository
import org.koin.android.ext.android.inject

class LauncherActivity : ComponentActivity() {
    private val authRepository: AuthRepository by inject()
    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (authRepository.hasUserCredential()) {
                navigator.goToHome(this@LauncherActivity)
            } else {
                navigator.goToAuthentication(this@LauncherActivity)
            }

            finish()
        }
    }
}
