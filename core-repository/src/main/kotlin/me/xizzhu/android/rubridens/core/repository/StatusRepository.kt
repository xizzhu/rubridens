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
    fun loadLatest(userCredential: UserCredential, limit: Int): Flow<Data<List<Status>>>

    /**
     * Load [Status] newer than [newerThan]. If no newer statuses are available, an empty list will be emitted.
     */
    fun loadNewer(userCredential: UserCredential, newerThan: Status, limit: Int): Flow<Data<List<Status>>>

    /**
     * Load [Status] older than [olderThan]. If no older statuses are available, an empty list will be emitted.
     */
    fun loadOlder(userCredential: UserCredential, olderThan: Status, limit: Int): Flow<Data<List<Status>>>
}

internal class StatusRepositoryImpl(
    private val timelinesService: TimelinesService,
    private val statusCache: StatusCache,
) : StatusRepository {
    override fun loadLatest(userCredential: UserCredential, limit: Int): Flow<Data<List<Status>>> = flow {
        val local = readLatestSafely(
            instanceUrl = userCredential.instanceUrl,
            olderThan = Long.MAX_VALUE,
            limit = limit
        )
        if (local.isNotEmpty()) {
            emit(Data.Local(local))
        }

        // When fetching fails, propagate the error.
        val remote = fetchHome(
            userCredential = userCredential,
            minId = local.firstOrNull()?.id?.id ?: "",
            maxId = "",
            limit = limit
        )
        emit(Data.Remote(remote))
    }

    override fun loadNewer(userCredential: UserCredential, newerThan: Status, limit: Int): Flow<Data<List<Status>>> = flow {
        val local = readOldestSafely(
            instanceUrl = userCredential.instanceUrl,
            newerThan = newerThan.created.toEpochMilliseconds(),
            limit = limit
        )
        if (local.isNotEmpty()) {
            emit(Data.Local(local))
        }

        // Spare the server if we already have enough loaded.
        if (local.size >= limit) return@flow

        // When fetching fails, propagate the error.
        val remote = fetchHome(
            userCredential = userCredential,
            minId = local.firstOrNull()?.id?.id ?: newerThan.id.id,
            maxId = "",
            limit = limit,
        )
        emit(Data.Remote(remote))
    }

    override fun loadOlder(userCredential: UserCredential, olderThan: Status, limit: Int): Flow<Data<List<Status>>> = flow {
        val local = readLatestSafely(
            instanceUrl = userCredential.instanceUrl,
            olderThan = olderThan.created.toEpochMilliseconds(),
            limit = limit
        )
        if (local.isNotEmpty()) {
            emit(Data.Local(local))
        }

        // Spare the server if we already have enough loaded.
        if (local.size >= limit) return@flow

        // When fetching fails, propagate the error.
        val remote = fetchHome(
            userCredential = userCredential,
            minId = "",
            maxId = local.lastOrNull()?.id?.id ?: olderThan.id.id,
            limit = limit,
        )
        emit(Data.Remote(remote))
    }

    private suspend fun readLatestSafely(instanceUrl: String, olderThan: Long, limit: Int): List<Status> = runCatching<List<Status>> {
        statusCache.readLatest(instanceUrl = instanceUrl, olderThan = olderThan, limit = limit)
    }.getOrNull() ?: emptyList()

    private suspend fun readOldestSafely(instanceUrl: String, newerThan: Long, limit: Int): List<Status> = runCatching<List<Status>> {
        statusCache.readOldest(instanceUrl = instanceUrl, newerThan = newerThan, limit = limit)
    }.getOrNull() ?: emptyList()

    private suspend fun fetchHome(userCredential: UserCredential, minId: String, maxId: String, limit: Int): List<Status> =
        timelinesService.fetchHome(
            instanceUrl = userCredential.instanceUrl,
            userOAuthToken = userCredential.accessToken,
            minId = minId,
            maxId = maxId,
            limit = limit,
        ).also { runCatching { statusCache.save(it) } }
}
