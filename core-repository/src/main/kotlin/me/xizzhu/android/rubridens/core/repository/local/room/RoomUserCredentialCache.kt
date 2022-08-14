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
import androidx.room.Query
import me.xizzhu.android.rubridens.core.model.UserCredential
import me.xizzhu.android.rubridens.core.repository.local.UserCredentialCache

internal class RoomUserCredentialCache(private val appDatabase: AppDatabase) : UserCredentialCache {
    override suspend fun hasCredential(): Boolean = appDatabase.userCredentialDao().count() > 0

    override suspend fun read(): List<UserCredential> = appDatabase.userCredentialDao().read().map { it.toUserCredential() }

    override suspend fun readByInstanceUrl(instanceUrl: String): List<UserCredential> =
        appDatabase.userCredentialDao().readByInstanceUrl(instanceUrl).map { it.toUserCredential() }

    override suspend fun save(userCredential: UserCredential) {
        appDatabase.userCredentialDao().save(UserCredentialEntity(userCredential))
    }
}

@Dao
internal interface UserCredentialDao {
    @Query("SELECT COUNT(*) FROM ${UserCredentialEntity.TABLE_NAME}")
    suspend fun count(): Int

    @Query("SELECT * FROM ${UserCredentialEntity.TABLE_NAME}")
    suspend fun read(): List<UserCredentialEntity>

    @Query("SELECT * FROM ${UserCredentialEntity.TABLE_NAME} WHERE ${UserCredentialEntity.COLUMN_NAME_INSTANCE_URL} = :instanceUrl")
    suspend fun readByInstanceUrl(instanceUrl: String): List<UserCredentialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(userCredential: UserCredentialEntity)
}

@Entity(
    tableName = UserCredentialEntity.TABLE_NAME,
    primaryKeys = [UserCredentialEntity.COLUMN_NAME_INSTANCE_URL, UserCredentialEntity.COLUMN_NAME_USERNAME]
)
internal class UserCredentialEntity(
    @ColumnInfo(name = COLUMN_NAME_INSTANCE_URL) val instanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_USERNAME) val username: String,
    @ColumnInfo(name = COLUMN_NAME_ACCESS_TOKEN) val accessToken: String,
) {
    companion object {
        const val TABLE_NAME = "user_credential"
        const val COLUMN_NAME_INSTANCE_URL = "instance_url"
        const val COLUMN_NAME_USERNAME = "username"
        const val COLUMN_NAME_ACCESS_TOKEN = "access_token"
    }

    constructor(userCredential: UserCredential) : this(
        instanceUrl = userCredential.instanceUrl,
        username = userCredential.username,
        accessToken = userCredential.accessToken,
    )

    fun toUserCredential(): UserCredential = UserCredential(
        instanceUrl = instanceUrl,
        username = username,
        accessToken = accessToken,
    )
}
