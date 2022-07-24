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

import me.xizzhu.android.rubridens.core.repository.network.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RetrofitErrorTest : BaseRetrofitTest() {
    @Test
    fun `test fromJson with empty string`() {
        assertNull(MastodonError.fromJson(""))
    }

    @Test
    fun `test fromJson with empty json`() {
        assertEquals(NetworkException.HttpError.ErrorInfo("", ""), MastodonError.fromJson("{}")?.toErrorInfo())
    }

    @Test
    fun `test fromJson with only error fields`() {
        assertEquals(
                NetworkException.HttpError.ErrorInfo(
                        error = "invalid_grant",
                        description = ""
                ),
                MastodonError.fromJson(
                        """
                            {
                                "error": "invalid_grant",
                                "unknown_field": 123456789
                            }
                        """.trimIndent()
                )?.toErrorInfo()
        )
    }

    @Test
    fun `test fromJson with all fields`() {
        assertEquals(
                NetworkException.HttpError.ErrorInfo(
                        error = "invalid_grant",
                        description = "The provided authorization grant is invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to another client."
                ),
                MastodonError.fromJson(
                        """
                            {
                                "error": "invalid_grant",
                                "error_description": "The provided authorization grant is invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to another client."
                            }
                        """.trimIndent()
                )?.toErrorInfo()
        )
    }
}
