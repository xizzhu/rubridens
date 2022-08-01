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
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.view.BlurHashDecoder
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusCardItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusMediaItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import me.xizzhu.android.rubridens.core.view.formatCount
import me.xizzhu.android.rubridens.core.view.formatDisplayName
import me.xizzhu.android.rubridens.core.view.formatRelativeTimestamp
import me.xizzhu.android.rubridens.core.view.formatSenderUsername

class HomePresenter(private val application: Application) {
    fun buildFeedItems(
        statuses: List<Status>,
        openStatus: (status: Status) -> Unit,
        replyToStatus: (status: Status) -> Unit,
        reblogStatus: (status: Status) -> Unit,
        favoriteStatus: (status: Status) -> Unit,
        openUser: (user: User) -> Unit,
        openMedia: (media: Media) -> Unit,
        openTag: (tag: String) -> Unit,
        openUrl: (url: String) -> Unit,
    ): List<FeedItem<*>> {
        val items = ArrayList<FeedItem<*>>(statuses.size * 5)
        statuses.forEach { status ->
            items.add(status.toFeedStatusHeaderItem(openStatus = openStatus, openUser = openUser))
            items.add(status.toFeedStatusTextItem(openStatus = openStatus, openUrl = openUrl, openTag = openTag, openUser = openUser))
            status.toFeedStatusMediaItem(openStatus = openStatus, openMedia = openMedia)?.let { items.add(it) }
            status.toFeedStatusCardItem(openStatus = openStatus, openUrl = openUrl)?.let { items.add(it) }
            items.add(status.toFeedStatusFooterItem(openStatus = openStatus, replyToStatus = replyToStatus, reblogStatus = reblogStatus, favoriteStatus = favoriteStatus))
        }
        return items
    }

    private fun Status.toFeedStatusHeaderItem(
        openStatus: (status: Status) -> Unit,
        openUser: (user: User) -> Unit,
    ): FeedStatusHeaderItem = FeedStatusHeaderItem(
        status = this,
        blogger = sender,
        bloggerDisplayName = sender.formatDisplayName(),
        bloggerProfileImageUrl = sender.avatarUrl,
        rebloggedBy = reblogger?.formatDisplayName()?.let { application.resources.getString(R.string.feed_text_reblogged_by, it) },
        subtitle = "${formatSenderUsername()} • ${formatRelativeTimestamp()}",
        openStatus = openStatus,
        openBlogger = openUser,
    )

    private fun Status.toFeedStatusTextItem(
        openStatus: (status: Status) -> Unit,
        openUrl: (url: String) -> Unit,
        openTag: (tag: String) -> Unit,
        openUser: (user: User) -> Unit,
    ): FeedStatusTextItem = FeedStatusTextItem(
        status = this,
        openStatus = openStatus,
        openUrl = openUrl,
        openTag = openTag,
        openUser = openUser,
    )

    private fun Status.toFeedStatusMediaItem(openStatus: (status: Status) -> Unit, openMedia: (media: Media) -> Unit): FeedStatusMediaItem? =
        media.firstOrNull { media ->
            media.type == Media.Type.IMAGE || media.type == Media.Type.GIF || media.type == Media.Type.VIDEO
        }?.let { media ->
            FeedStatusMediaItem(
                status = this,
                media = media,
                imageUrl = media.previewUrl.takeIf { it.isNotEmpty() } ?: media.url,
                placeholder = BlurHashDecoder.decode(media.blurHash, 32, 18),
                isPlayable = media.type == Media.Type.GIF || media.type == Media.Type.VIDEO,
                openStatus = openStatus,
                openMedia = openMedia,
            )
        }

    private fun Status.toFeedStatusCardItem(openStatus: (status: Status) -> Unit, openUrl: (url: String) -> Unit): FeedStatusCardItem? = card?.let { card ->
        FeedStatusCardItem(
            status = this,
            title = card.title,
            description = card.description,
            author = card.author,
            imageUrl = card.previewUrl,
            placeholder = BlurHashDecoder.decode(card.blurHash, 16, 16),
            url = card.url,
            openStatus = openStatus,
            openUrl = openUrl,
        )
    }

    private fun Status.toFeedStatusFooterItem(
        openStatus: (status: Status) -> Unit,
        replyToStatus: (status: Status) -> Unit,
        reblogStatus: (status: Status) -> Unit,
        favoriteStatus: (status: Status) -> Unit,
    ): FeedStatusFooterItem = FeedStatusFooterItem(
        status = this,
        replies = repliesCount.formatCount(),
        reblogs = reblogsCount.formatCount(),
        reblogged = reblogged,
        favorites = favoritesCount.formatCount(),
        favorited = favorited,
        openStatus = openStatus,
        replyToStatus = replyToStatus,
        reblogStatus = reblogStatus,
        favoriteStatus = favoriteStatus,
    )
}
