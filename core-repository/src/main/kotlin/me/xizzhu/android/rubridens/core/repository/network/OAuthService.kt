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

package me.xizzhu.android.rubridens.core.repository.network

import me.xizzhu.android.rubridens.core.model.OAuthGrantType
import me.xizzhu.android.rubridens.core.model.OAuthScope
import me.xizzhu.android.rubridens.core.model.OAuthToken

internal interface OAuthService {
    fun getLoginUrl(
        instanceUrl: String,
        clientId: String,
        redirectUri: String = "urn:ietf:wg:oauth:2.0:oob",
        scopes: Set<OAuthScope> = setOf(OAuthScope.READ, OAuthScope.WRITE, OAuthScope.FOLLOW, OAuthScope.PUSH),
        forceLogin: Boolean = false
    ): String

    fun getAuthCode(url: String): String?

    suspend fun createToken(
        instanceUrl: String,
        grantType: OAuthGrantType,
        clientId: String,
        clientSecret: String,
        redirectUri: String = "urn:ietf:wg:oauth:2.0:oob",
        scopes: Set<OAuthScope> = setOf(OAuthScope.READ, OAuthScope.WRITE, OAuthScope.FOLLOW, OAuthScope.PUSH),
        code: String = ""
    ): OAuthToken
}
