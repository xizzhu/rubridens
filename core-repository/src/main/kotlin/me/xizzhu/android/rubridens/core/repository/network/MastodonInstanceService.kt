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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.xizzhu.android.rubridens.core.repository.Instance
import retrofit2.http.GET

/**
 * See https://docs.joinmastodon.org/methods/instance/
 */
internal interface MastodonInstanceService {
    @GET("api/v1/instance")
    suspend fun fetch(): MastodonInstance
}

/**
 * See https://docs.joinmastodon.org/entities/instance/
 */
@JsonClass(generateAdapter = true)
internal class MastodonInstance(
        @Json(name = "title") val title: String,
        @Json(name = "stats") val stats: MastodonStats,
) {
    @JsonClass(generateAdapter = true)
    class MastodonStats(
            @Json(name = "user_count") val userCount: Long,
            @Json(name = "status_count") val statusCount: Long,
    ) {
        fun toStats(): Instance.Stats = Instance.Stats(
                userCount = userCount,
                statusCount = statusCount
        )
    }

    fun toInstance(): Instance = Instance(
            title = title,
            stats = stats.toStats()
    )
}
