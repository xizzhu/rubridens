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

import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.repository.network.TimelinesService
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

internal class RetrofitTimelinesService : TimelinesService {
    override suspend fun fetchHome(
        instanceUrl: String,
        userOAuthToken: String,
        sinceId: String,
        maxId: String,
        minId: String,
        limit: Int,
        localOnly: Boolean
    ): List<Status> {
        if (instanceUrl.isEmpty()) {
            throw IllegalArgumentException("instanceUrl is empty")
        }
        if (userOAuthToken.isEmpty()) {
            throw IllegalArgumentException("userOAuthToken is empty")
        }
        if (limit <= 0) {
            throw IllegalArgumentException("limit ($limit) is not positive")
        }

        return request<MastodonTimelinesService, List<MastodonStatus>>(instanceUrl) {
            fetchHome(createAuthHeader(userOAuthToken), sinceId, maxId, minId, limit, localOnly)
        }.map { it.toStatus(instanceUrl) }
    }
}

/**
 * See https://docs.joinmastodon.org/methods/timelines/
 */
internal interface MastodonTimelinesService {
    @GET("api/v1/timelines/home")
    suspend fun fetchHome(
        @Header("Authorization") authorization: String,
        @Query("since_id") sinceId: String,
        @Query("max_id") maxId: String,
        @Query("min_id") minId: String,
        @Query("limit") limit: Int,
        @Query("local") localOnly: Boolean
    ): List<MastodonStatus>
}
