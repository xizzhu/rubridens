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
import android.text.format.DateUtils
import androidx.core.text.HtmlCompat
import me.xizzhu.android.rubridens.core.repository.model.Status
import me.xizzhu.android.rubridens.core.repository.model.User
import me.xizzhu.android.rubridens.core.view.feed.FeedItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusFooterItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusHeaderItem
import me.xizzhu.android.rubridens.core.view.feed.FeedStatusTextItem
import kotlin.math.min

class HomePresenter(private val application: Application) {
    fun buildFeedItems(
        statuses: List<Status>,
        openStatus: (statusId: String) -> Unit,
        replyToStatus: (statusId: String) -> Unit,
        reblogStatus: (statusId: String) -> Unit,
        favoriteStatus: (statusId: String) -> Unit,
        openUser: (userId: String) -> Unit,
    ): List<FeedItem<*>> {
        val items = ArrayList<FeedItem<*>>(statuses.size * 3)
        statuses.forEach { status ->
            items.add(status.toFeedStatusHeaderItem(openStatus = openStatus, openUser = openUser))
            items.add(status.toFeedStatusTextItem(openStatus = openStatus))
            items.add(status.toFeedStatusFooterItem(openStatus = openStatus, replyToStatus = replyToStatus, reblogStatus = reblogStatus, favoriteStatus = favoriteStatus))
        }
        return items
    }

    private fun Status.toFeedStatusHeaderItem(
        openStatus: (statusId: String) -> Unit,
        openUser: (userId: String) -> Unit,
    ): FeedStatusHeaderItem = FeedStatusHeaderItem(
        statusId = createUniqueStatusId(this),
        bloggerId = createUniqueUserId(sender),
        bloggerDisplayName = sender.formatDisplayName(),
        bloggerProfileImageUrl = sender.avatarUrl,
        rebloggedBy = reblogger?.formatDisplayName()?.let { application.resources.getString(R.string.feed_text_reblogged_by, it) },
        subtitle = "${formatSenderUsername()} • ${formatRelativeTimestamp()}",
        openStatus = openStatus,
        openBlogger = openUser,
    )

    private fun Status.toFeedStatusTextItem(openStatus: (statusId: String) -> Unit): FeedStatusTextItem = FeedStatusTextItem(
        statusId = createUniqueStatusId(this),
        text = formatTextContent(),
        openStatus = openStatus,
    )

    private fun Status.toFeedStatusFooterItem(
        openStatus: (statusId: String) -> Unit,
        replyToStatus: (statusId: String) -> Unit,
        reblogStatus: (statusId: String) -> Unit,
        favoriteStatus: (statusId: String) -> Unit,
    ): FeedStatusFooterItem = FeedStatusFooterItem(
        statusId = createUniqueStatusId(this),
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

    private fun Int.formatCount(): String = when {
        this >= 1_000_000 -> "${this / 1_000_000}M+"
        this >= 1_000 -> "${this / 1_000}K+"
        this <= 0 -> ""
        else -> toString()
    }

    private fun User.formatDisplayName(): String = displayName.takeIf { it.isNotEmpty() } ?: username

    private fun Status.formatSenderUsername(): String = if (instanceUrl == sender.instanceUrl) {
        "@${sender.username}"
    } else {
        "@${sender.username}@${sender.instanceUrl}"
    }

    private fun Status.formatRelativeTimestamp(): CharSequence =
        DateUtils.getRelativeTimeSpanString(min(created.toEpochMilliseconds(), System.currentTimeMillis())) ?: ""

    private fun Status.formatTextContent(): CharSequence = HtmlCompat.fromHtml(content, 0).trim().toString()

    fun createUniqueStatusId(status: Status): String = "${status.instanceUrl}:${status.id}"

    fun createUniqueUserId(user: User): String = "${user.instanceUrl}:${user.id}"
}
