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

import android.app.Application
import android.content.Context
import io.mockk.mockk
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkKoinModules
import retrofit2.Retrofit
import kotlin.test.Test

class AppModuleTest : KoinTest {
    @Test
    fun verifyAppModules() {
        val mockedAndroidContext = module {
            single { mockk<Application>() }
            single { mockk<Context>() }
        }
        checkKoinModules(appModules + mockedAndroidContext) {
            withParameter<Retrofit> { "base_url" }
        }
    }
}
