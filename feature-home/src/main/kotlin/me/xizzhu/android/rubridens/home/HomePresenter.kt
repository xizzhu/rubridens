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

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.view.BlurHashDecoder
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusCardItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusMediaInfo
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusMediaItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusThreadItem
import me.xizzhu.android.rubridens.core.view.formatCount
import me.xizzhu.android.rubridens.core.view.formatDisplayName
import me.xizzhu.android.rubridens.core.view.formatRelativeTimestamp
import me.xizzhu.android.rubridens.core.view.formatSenderUsername

class HomePresenter(
    private val application: Application,
) {
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
        blogger = sender,
        bloggerDisplayName = sender.formatDisplayName(),
        bloggerProfileImageUrl = sender.avatarUrl,
        rebloggedBy = reblogger?.formatDisplayName()?.let { application.resources.getString(R.string.home_text_status_reblogged_by, it) },
        subtitle = "${formatSenderUsername()} • ${formatRelativeTimestamp()}",
    )

    private fun Status.toFeedStatusTextItem(): FeedStatusTextItem = FeedStatusTextItem(status = this)

    private fun Status.toFeedStatusMediaItem(): FeedItem<*>? =
        media.mapNotNull { media ->
            if (media.type == Media.Type.IMAGE || media.type == Media.Type.GIF || media.type == Media.Type.VIDEO) {
                FeedStatusMediaInfo(
                    media = media,
                    imageUrl = media.previewUrl.takeIf { it.isNotEmpty() } ?: media.url,
                    placeholder = BlurHashDecoder.decode(media.blurHash, 32, 18),
                    isPlayable = media.type == Media.Type.GIF || media.type == Media.Type.VIDEO,
                )
            } else {
                null
            }
        }.takeIf { mediaInfoList ->
            mediaInfoList.isNotEmpty()
        }?.let { mediaInfoList ->
            FeedStatusMediaItem(
                status = this,
                mediaInfo = mediaInfoList,
            )
        }

    private fun Status.toFeedStatusCardItem(): FeedStatusCardItem? = card?.let { card ->
        FeedStatusCardItem(
            status = this,
            title = card.title,
            description = card.description,
            author = card.author,
            imageUrl = card.previewUrl,
            placeholder = BlurHashDecoder.decode(card.blurHash, 16, 16),
            url = card.url,
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
        replies = repliesCount.formatCount(),
        reblogs = reblogsCount.formatCount(),
        reblogged = reblogged,
        favorites = favoritesCount.formatCount(),
        favorited = favorited,
    )
}
