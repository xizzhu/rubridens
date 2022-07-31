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

import android.text.format.DateUtils
import androidx.core.text.HtmlCompat
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

fun Status.formatSenderUsername(): String = if (instanceUrl == sender.instanceUrl) {
    "@${sender.username}"
} else {
    "@${sender.username}@${sender.instanceUrl}"
}

fun Status.formatRelativeTimestamp(): CharSequence = DateUtils.getRelativeTimeSpanString(min(created.toEpochMilliseconds(), System.currentTimeMillis()))

fun Status.formatTextContent(): CharSequence = HtmlCompat.fromHtml(content, 0).trim().toString()
