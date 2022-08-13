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

package me.xizzhu.android.rubridens.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.xizzhu.android.rubridens.core.model.Data
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.local.StatusCache
import me.xizzhu.android.rubridens.core.repository.network.TimelinesService

interface StatusRepository {
    /**
     * If locally cached [Status] are available, emits the latest ones, then fetches immediate newer ones from the server and emits them.
     * Otherwise, fetches the latest [Status] from server and emits them.
     */
    fun loadLatest(userCredential: UserCredential): Flow<Data<List<Status>>>
}

internal class StatusRepositoryImpl(
    private val timelinesService: TimelinesService,
    private val statusCache: StatusCache,
) : StatusRepository {
    override fun loadLatest(userCredential: UserCredential): Flow<Data<List<Status>>> = flow {
        val local = readLatestSafely(userCredential.instanceUrl, Long.MAX_VALUE)
        if (local.isNotEmpty()) {
            emit(Data.Local(local))
        }

        // When fetching fails, propagate the error.
        val remote = fetchHome(
            userCredential = userCredential,
            minId = local.firstOrNull()?.id?.id ?: "",
            maxId = "",
        )
        emit(Data.Remote(remote))
    }

    private suspend fun readLatestSafely(instanceUrl: String, olderThan: Long): List<Status> = runCatching<List<Status>> {
        statusCache.readLatest(instanceUrl, olderThan)
    }.getOrNull() ?: emptyList()

    private suspend fun fetchHome(userCredential: UserCredential, minId: String, maxId: String): List<Status> =
        timelinesService.fetchHome(
            instanceUrl = userCredential.instanceUrl,
            userOAuthToken = userCredential.accessToken,
            minId = minId,
            maxId = maxId,
        ).also { runCatching { statusCache.save(it) } }
}
