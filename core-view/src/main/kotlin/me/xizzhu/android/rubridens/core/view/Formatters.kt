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

package me.xizzhu.android.rubridens.core.view

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.util.PatternsCompat
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import kotlin.math.min

fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M+"
    this >= 1_000 -> "${this / 1_000}K+"
    this <= 0 -> ""
    else -> toString()
}

fun User.formatDisplayName(): String = displayName.takeIf { it.isNotEmpty() } ?: username

fun Status.formatSenderUsername(): String = if (id.instanceUrl == sender.id.instanceUrl) {
    "@${sender.username}"
} else {
    "@${sender.username}@${sender.id.instanceUrl}"
}

fun Status.formatRelativeTimestamp(): CharSequence = DateUtils.getRelativeTimeSpanString(min(created.toEpochMilliseconds(), System.currentTimeMillis()))

fun Status.formatTextContent(openUrl: (url: String) -> Unit, openTag: (tag: String) -> Unit, openUser: (user: User) -> Unit): CharSequence {
    val rawContent = HtmlCompat.fromHtml(content, 0).trim().toString()
    val builder = SpannableStringBuilder(rawContent)

    // 1. Find all web links.
    with(PatternsCompat.WEB_URL.matcher(rawContent)) {
        while (find()) {
            val url = group()
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(v: View) {
                    openUrl(url)
                }
            }
            builder.setSpan(clickableSpan, start(), end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    val rawContentLowercase = rawContent.lowercase()
    // 2. Find all tags.
    tags.forEach { tag ->
        val tagLowercase = tag.lowercase()
        val start = rawContentLowercase.indexOf("#$tagLowercase", 0, true)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(v: View) {
                openTag(tagLowercase)
            }
        }
        builder.setSpan(clickableSpan, start, start + tagLowercase.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // 3. Find all mentions.
    mentions.forEach { mention ->
        val usernameLowercase = mention.username.lowercase()
        val start = rawContentLowercase.indexOf("@$usernameLowercase", 0, true)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(v: View) {
                openUser(User(
                    id = mention.userId,
                    username = mention.username,
                    displayName = "",
                    avatarUrl = ""
                ))
            }
        }
        builder.setSpan(clickableSpan, start, start + usernameLowercase.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return builder
}
