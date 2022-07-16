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
import me.xizzhu.android.rubridens.auth.databinding.ActivityAuthBinding
import me.xizzhu.android.rubridens.core.mvvm.BaseActivity

class AuthActivity : BaseActivity<ActivityAuthBinding>() {
    companion object {
        fun newStartIntent(context: Context): Intent = Intent(context, AuthActivity::class.java)
    }

    override fun inflateViewBinding(): ActivityAuthBinding = ActivityAuthBinding.inflate(layoutInflater)
}
