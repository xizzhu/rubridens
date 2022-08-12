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

package me.xizzhu.android.rubridens.core.repository.local.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import me.xizzhu.android.rubridens.core.model.ApplicationCredential
import me.xizzhu.android.rubridens.core.repository.local.ApplicationCredentialCache

internal class RoomApplicationCredentialCache(private val appDatabase: AppDatabase) : ApplicationCredentialCache {
    override suspend fun readByInstanceUrl(instanceUrl: String): ApplicationCredential? =
        appDatabase.applicationCredentialDao().readByInstanceUrl(instanceUrl)?.toApplicationCredential()

    override suspend fun save(applicationCredential: ApplicationCredential) {
        appDatabase.applicationCredentialDao().save(ApplicationCredentialEntity(applicationCredential))
    }
}

@Dao
internal interface ApplicationCredentialDao {
    @Query("SELECT * FROM ${ApplicationCredentialEntity.TABLE_NAME} WHERE ${ApplicationCredentialEntity.COLUMN_NAME_INSTANCE_URL} = :instanceUrl")
    suspend fun readByInstanceUrl(instanceUrl: String): ApplicationCredentialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(applicationCredential: ApplicationCredentialEntity)
}

@Entity(tableName = ApplicationCredentialEntity.TABLE_NAME)
internal class ApplicationCredentialEntity(
    @PrimaryKey @ColumnInfo(name = COLUMN_NAME_INSTANCE_URL) val instanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_CLIENT_ID) val clientId: String,
    @ColumnInfo(name = COLUMN_NAME_CLIENT_SECRET) val clientSecret: String,
    @ColumnInfo(name = COLUMN_NAME_ACCESS_TOKEN) val accessToken: String,
    @ColumnInfo(name = COLUMN_NAME_VAPID_KEY) val vapidKey: String,
) {
    companion object {
        const val TABLE_NAME = "application_credential"
        const val COLUMN_NAME_INSTANCE_URL = "instance_url"
        const val COLUMN_NAME_CLIENT_ID = "client_id"
        const val COLUMN_NAME_CLIENT_SECRET = "client_secret"
        const val COLUMN_NAME_ACCESS_TOKEN = "access_token"
        const val COLUMN_NAME_VAPID_KEY = "vapid_key"
    }

    constructor(applicationCredential: ApplicationCredential) : this(
        instanceUrl = applicationCredential.instanceUrl,
        clientId = applicationCredential.clientId,
        clientSecret = applicationCredential.clientSecret,
        accessToken = applicationCredential.accessToken,
        vapidKey = applicationCredential.vapidKey,
    )

    fun toApplicationCredential(): ApplicationCredential = ApplicationCredential(
        instanceUrl = instanceUrl,
        clientId = clientId,
        clientSecret = clientSecret,
        accessToken = accessToken,
        vapidKey = vapidKey,
    )
}
