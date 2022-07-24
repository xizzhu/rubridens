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

package me.xizzhu.android.rubridens.core.repository.network.retrofit

import androidx.annotation.CallSuper
import okhttp3.mockwebserver.MockWebServer
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseRetrofitTest {
    protected val mockWebServer: MockWebServer by inject(MockWebServer::class.java)

    @CallSuper
    @BeforeTest
    open fun setup() {
        startKoin { modules(retrofitTestModule) }
        mockWebServer.start()
    }

    @CallSuper
    @AfterTest
    open fun tearDown() {
        stopKoin()
        mockWebServer.shutdown()
    }
}
