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

import kotlinx.coroutines.test.runTest
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.User
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class RoomUserCacheTest : BaseRoomTest() {
    private lateinit var roomUserCache: RoomUserCache

    @BeforeTest
    override fun setup() {
        super.setup()
        roomUserCache = RoomUserCache(appDatabase)
    }

    @Test
    fun `test read from empty database`() = runTest {
        assertTrue(roomUserCache.read(emptyList()).isEmpty())
        assertTrue(roomUserCache.read(listOf("user_id")).isEmpty())
    }

    @Test
    fun `test save then read`() = runTest {
        roomUserCache.save(listOf(
            User(
                id = EntityKey("xizzhu.me", "198964"),
                username = "xizzhu_username",
                displayName = "Keep Speech Free",
                avatarUrl = "https://xizzhu.me/images/logo.png",
            )
        ))
        assertTrue(roomUserCache.read(emptyList()).isEmpty())
        assertEquals(
            listOf(
                User(
                    id = EntityKey("xizzhu.me", "198964"),
                    username = "xizzhu_username",
                    displayName = "Keep Speech Free",
                    avatarUrl = "https://xizzhu.me/images/logo.png",
                ),
            ),
            roomUserCache.read(listOf("non_exist", "198964"))
        )

        roomUserCache.save(listOf(
            User(
                id = EntityKey("xizzhu.me", "198964"),
                username = "xizzhu_username",
                displayName = "Free Speech!",
                avatarUrl = "https://xizzhu.me/images/logo.png",
            ),
            User(
                id = EntityKey("xizzhu.me", "id_2"),
                username = "xizzhu_2",
                displayName = "display name 2",
                avatarUrl = "",
            )
        ))
        assertTrue(roomUserCache.read(emptyList()).isEmpty())
        assertEquals(
            listOf(
                User(
                    id = EntityKey("xizzhu.me", "198964"),
                    username = "xizzhu_username",
                    displayName = "Free Speech!",
                    avatarUrl = "https://xizzhu.me/images/logo.png",
                ),
                User(
                    id = EntityKey("xizzhu.me", "id_2"),
                    username = "xizzhu_2",
                    displayName = "display name 2",
                    avatarUrl = "",
                )
            ),
            roomUserCache.read(listOf("non_exist", "198964", "id_2"))
        )
    }
}
