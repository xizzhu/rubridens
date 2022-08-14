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

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ApplicationCredentialEntity::class,
        CardEntity::class,
        MediaEntity::class,
        StatusEntity::class,
        UserCredentialEntity::class,
        UserEntity::class,
    ],
    version = 1,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationCredentialDao(): ApplicationCredentialDao

    abstract fun cardDao(): CardDao

    abstract fun mediaDao(): MediaDao

    abstract fun statusDao(): StatusDao

    abstract fun userCredentialDao(): UserCredentialDao

    abstract fun userDao(): UserDao
}
