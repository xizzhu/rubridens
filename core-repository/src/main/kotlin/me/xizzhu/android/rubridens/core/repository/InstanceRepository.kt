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

import me.xizzhu.android.rubridens.core.repository.model.Instance
import me.xizzhu.android.rubridens.core.repository.network.InstanceService

interface InstanceRepository {
    suspend fun fetch(instanceUrl: String): Instance
}

internal class InstanceRepositoryImpl(private val instanceService: InstanceService) : InstanceRepository {
    override suspend fun fetch(instanceUrl: String): Instance = instanceService.fetch(instanceUrl)
}
