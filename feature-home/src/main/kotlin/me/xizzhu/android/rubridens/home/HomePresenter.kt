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

package me.xizzhu.android.rubridens.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import me.xizzhu.android.rubridens.core.view.feed.FeedNoMoreStatusItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusCardItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusMediaItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusThreadItem

class HomePresenter {
    private val dispatcher = Dispatchers.Default.limitedParallelism(1)

    private val feedItems = ArrayList<FeedItem<*>>()

    suspend fun feedItems(): List<FeedItem<*>> = withContext(dispatcher) {
        feedItems.toList()
    }

    suspend fun clear() = withContext(dispatcher) {
        feedItems.clear()
    }

    suspend fun replace(statuses: List<Status>): Unit = withContext(dispatcher) {
        feedItems.clear()
        feedItems.addAll(statuses.toFeedItems())
    }

    suspend fun prepend(statuses: List<Status>): Unit = withContext(dispatcher) {
        feedItems.addAll(0, statuses.toFeedItems())
    }

    suspend fun append(statuses: List<Status>): Unit = withContext(dispatcher) {
        feedItems.addAll(statuses.toFeedItems())
    }

    suspend fun noMoreItemsToAppend(): Unit = withContext(dispatcher) {
        feedItems.add(FeedNoMoreStatusItem())
    }

    private fun List<Status>.toFeedItems(): List<FeedItem<*>> {
        val items = ArrayList<FeedItem<*>>(size * 6)
        forEach { status ->
            items.add(status.toFeedStatusHeaderItem())
            items.add(status.toFeedStatusTextItem())
            status.toFeedStatusMediaItem()?.let { items.add(it) }
            status.toFeedStatusCardItem()?.let { items.add(it) }
            status.toFeedStatusThreadItem()?.let { items.add(it) }
            items.add(status.toFeedStatusFooterItem())
        }
        return items
    }

    private fun Status.toFeedStatusHeaderItem(): FeedStatusHeaderItem = FeedStatusHeaderItem(
        status = this,
        hasAncestor = false,
        hasDescendant = false,
        blogger = sender,
        reblogger = reblogger,
    )

    private fun Status.toFeedStatusTextItem(): FeedStatusTextItem = FeedStatusTextItem(status = this, hasAncestor = false, hasDescendant = false)

    private fun Status.toFeedStatusMediaItem(): FeedStatusMediaItem? =
        if (media.any { media -> media.type == Media.Type.IMAGE || media.type == Media.Type.GIF || media.type == Media.Type.VIDEO }) {
            FeedStatusMediaItem(
                status = this,
                hasAncestor = false,
                hasDescendant = false,
            )
        } else {
            null
        }

    private fun Status.toFeedStatusCardItem(): FeedStatusCardItem? = card?.let { card ->
        FeedStatusCardItem(
            status = this,
            hasAncestor = false,
            hasDescendant = false,
        )
    }

    private fun Status.toFeedStatusThreadItem(): FeedStatusThreadItem? =
        if (inReplyToStatusId.isNullOrEmpty() || inReplyToAccountId.isNullOrEmpty()) {
            null
        } else {
            FeedStatusThreadItem(status = this)
        }

    private fun Status.toFeedStatusFooterItem(): FeedStatusFooterItem = FeedStatusFooterItem(
        status = this,
        hasAncestor = false,
        hasDescendant = false,
    )
}
