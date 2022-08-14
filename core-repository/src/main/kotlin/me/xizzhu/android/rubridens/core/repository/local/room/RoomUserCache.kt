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
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.repository.local.UserCache

internal class RoomUserCache(private val appDatabase: AppDatabase) : UserCache {
    override suspend fun read(userIds: List<String>): List<User> = appDatabase.userDao().readById(userIds).map { it.toUser() }

    override suspend fun save(users: List<User>) {
        appDatabase.userDao().save(users.map { UserEntity(it) })
    }
}

@Dao
internal interface UserDao {
    @Query("SELECT * FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_ID} IN (:userIds)")
    suspend fun readById(userIds: Collection<String>): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(cards: Collection<UserEntity>)
}

@Entity(
    tableName = UserEntity.TABLE_NAME,
    primaryKeys = [UserEntity.COLUMN_NAME_INSTANCE_URL, UserEntity.COLUMN_NAME_ID],
)
internal class UserEntity(
    @ColumnInfo(name = COLUMN_NAME_INSTANCE_URL) val instanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_ID) val id: String,
    @ColumnInfo(name = COLUMN_NAME_USERNAME) val username: String,
    @ColumnInfo(name = COLUMN_NAME_DISPLAY_NAME) val displayName: String,
    @ColumnInfo(name = COLUMN_NAME_AVATAR_URL) val avatarUrl: String,
) {
    companion object {
        const val TABLE_NAME = "user"
        const val COLUMN_NAME_INSTANCE_URL = "instance_url"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_USERNAME = "username"
        const val COLUMN_NAME_DISPLAY_NAME = "display_name"
        const val COLUMN_NAME_AVATAR_URL = "avatarUrl"
    }

    constructor(user: User) : this(
        instanceUrl = user.id.instanceUrl,
        id = user.id.id,
        username = user.username,
        displayName = user.displayName,
        avatarUrl = user.avatarUrl,
    )

    fun toUser(): User = User(
        id = EntityKey(instanceUrl, id),
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
    )
}
