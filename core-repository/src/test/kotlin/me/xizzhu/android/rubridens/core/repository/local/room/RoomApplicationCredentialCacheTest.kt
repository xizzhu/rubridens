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
import me.xizzhu.android.rubridens.core.model.ApplicationCredential
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
internal class RoomApplicationCredentialCacheTest : BaseRoomTest() {
    private lateinit var roomApplicationCredentialCache: RoomApplicationCredentialCache

    @BeforeTest
    override fun setup() {
        super.setup()
        roomApplicationCredentialCache = RoomApplicationCredentialCache(appDatabase)
    }

    @Test
    fun `test readByInstanceUrl from empty database`() = runTest {
        assertNull(roomApplicationCredentialCache.readByInstanceUrl("xizzhu.me"))
    }

    @Test
    fun `test save then readByInstanceUrl`() = runTest {
        roomApplicationCredentialCache.save(ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id",
            clientSecret = "client_secret",
            accessToken = "access_token",
            vapidKey = "vapid_key",
        ))

        assertNull(roomApplicationCredentialCache.readByInstanceUrl("non-exist"))
        assertEquals(
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "client_id",
                clientSecret = "client_secret",
                accessToken = "access_token",
                vapidKey = "vapid_key",
            ),
            roomApplicationCredentialCache.readByInstanceUrl("xizzhu.me")
        )

        roomApplicationCredentialCache.save(ApplicationCredential(
            instanceUrl = "xizzhu.me",
            clientId = "client_id_2",
            clientSecret = "client_secret_2",
            accessToken = "access_token_2",
            vapidKey = "vapid_key_2",
        ))
        assertEquals(
            ApplicationCredential(
                instanceUrl = "xizzhu.me",
                clientId = "client_id_2",
                clientSecret = "client_secret_2",
                accessToken = "access_token_2",
                vapidKey = "vapid_key_2",
            ),
            roomApplicationCredentialCache.readByInstanceUrl("xizzhu.me")
        )
    }
}
