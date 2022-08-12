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

import android.app.Activity
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.verify
import me.xizzhu.android.rubridens.auth.AuthActivity
import me.xizzhu.android.rubridens.home.HomeActivity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AppNavigatorTest {
    @MockK(relaxed = true)
    private lateinit var activity: Activity

    @MockK(relaxed = true)
    private lateinit var intent: Intent

    private lateinit var appNavigator: AppNavigator

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        appNavigator = AppNavigator()
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test goToAuthentication`() {
        mockkObject(AuthActivity.Companion)
        every { AuthActivity.newStartIntent(activity) } returns intent

        appNavigator.goToAuthentication(activity)

        verify(exactly = 1) { activity.startActivity(intent) }
    }

    @Test
    fun `test goToHome`() {
        mockkObject(HomeActivity.Companion)
        every { HomeActivity.newStartIntent(activity) } returns intent

        appNavigator.goToHome(activity)

        verify(exactly = 1) { activity.startActivity(intent) }
    }
}
