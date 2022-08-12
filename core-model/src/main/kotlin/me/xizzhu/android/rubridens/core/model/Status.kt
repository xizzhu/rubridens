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

package me.xizzhu.android.rubridens.core.model

import kotlinx.datetime.Instant

data class Card(
    val type: Type,
    val url: String,
    val title: String,
    val description: String,
    val author: String,
    val previewUrl: String,
    val blurHash: String,
) {
    enum class Type {
        LINK,
        IMAGE,
        VIDEO,
        RICH,
    }
}

data class Media(
    val type: Type,
    val url: String,
    val previewUrl: String,
    val blurHash: String,
) {
    enum class Type {
        IMAGE,
        GIF,
        VIDEO,
        AUDIO,
    }
}

data class Mention(
    val userInstanceUrl: String,
    val userId: String,
    val username: String,
)

data class Status(
    val id: String,
    val instanceUrl: String,
    val uri: String,
    val created: Instant,
    val sender: User,
    val reblogger: User?,
    val rebloggedInstanceUrl: String?,
    val inReplyToStatusId: String?,
    val inReplyToAccountId: String?,
    val content: String,
    val tags: List<String>,
    val mentions: List<Mention>,
    val media: List<Media>,
    val card: Card?,
    val repliesCount: Int,
    val reblogsCount: Int,
    val favoritesCount: Int,
    val reblogged: Boolean,
    val favorited: Boolean,
)
