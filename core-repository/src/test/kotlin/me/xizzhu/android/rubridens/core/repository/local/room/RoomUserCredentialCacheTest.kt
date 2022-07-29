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
import me.xizzhu.android.rubridens.core.repository.model.UserCredential
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class RoomUserCredentialCacheTest : BaseRoomTest() {
    private lateinit var roomUserCredentialCache: RoomUserCredentialCache

    @BeforeTest
    override fun setup() {
        super.setup()
        roomUserCredentialCache = RoomUserCredentialCache(appDatabase)
    }

    @Test
    fun `test read from empty database`() = runTest {
        assertFalse(roomUserCredentialCache.hasCredential())
        assertTrue(roomUserCredentialCache.read().isEmpty())
    }

    @Test
    fun `test save then read`() = runTest {
        roomUserCredentialCache.save(UserCredential(
            instanceUrl = "xizzhu.me",
            username = "username",
            accessToken = "access_token",
        ))

        assertTrue(roomUserCredentialCache.hasCredential())
        assertEquals(
            listOf(
                UserCredential(
                    instanceUrl = "xizzhu.me",
                    username = "username",
                    accessToken = "access_token",
                )
            ),
            roomUserCredentialCache.read()
        )

        roomUserCredentialCache.save(UserCredential(
            instanceUrl = "xizzhu.me",
            username = "username",
            accessToken = "access_token_2",
        ))
        roomUserCredentialCache.save(UserCredential(
            instanceUrl = "xizzhu.me",
            username = "another_username",
            accessToken = "another_access_token",
        ))

        assertTrue(roomUserCredentialCache.hasCredential())
        assertEquals(
            listOf(
                UserCredential(
                    instanceUrl = "xizzhu.me",
                    username = "username",
                    accessToken = "access_token_2",
                ),
                UserCredential(
                    instanceUrl = "xizzhu.me",
                    username = "another_username",
                    accessToken = "another_access_token",
                )
            ),
            roomUserCredentialCache.read()
        )
    }
}
