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

package me.xizzhu.android.rubridens.core.repository.network.retrofit

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.Card
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Mention
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.repository.network.TimelinesService
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

internal class RetrofitTimelinesService : TimelinesService {
    override suspend fun fetchHome(
        instanceUrl: String,
        userOAuthToken: String,
        sinceId: String,
        maxId: String,
        minId: String,
        limit: Int,
        localOnly: Boolean
    ): List<Status> {
        if (instanceUrl.isEmpty()) {
            throw IllegalArgumentException("instanceUrl is empty")
        }
        if (userOAuthToken.isEmpty()) {
            throw IllegalArgumentException("userOAuthToken is empty")
        }
        if (limit <= 0) {
            throw IllegalArgumentException("limit ($limit) is not positive")
        }

        return request<MastodonTimelinesService, List<MastodonStatus>>(instanceUrl) {
            fetchHome(createAuthHeader(userOAuthToken), sinceId, maxId, minId, limit, localOnly)
        }.map { it.toStatus(instanceUrl) }
    }
}

/**
 * See https://docs.joinmastodon.org/methods/timelines/
 */
internal interface MastodonTimelinesService {
    @GET("api/v1/timelines/home")
    suspend fun fetchHome(
        @Header("Authorization") authorization: String,
        @Query("since_id") sinceId: String,
        @Query("max_id") maxId: String,
        @Query("min_id") minId: String,
        @Query("limit") limit: Int,
        @Query("local") localOnly: Boolean
    ): List<MastodonStatus>
}

/**
 * See https://docs.joinmastodon.org/entities/card/
 */
@JsonClass(generateAdapter = true)
internal class MastodonCard(
    @Json(name = "type") val type: String,
    @Json(name = "url") val url: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "provider_name") val providerName: String = "",
    @Json(name = "image") val image: String = "",
    @Json(name = "blurhash") val blurHash: String = "",
) {
    fun toCard(): Card? = type.toCardType()?.let { cardType ->
        Card(
            type = cardType,
            url = url,
            title = title,
            description = description,
            author = authorName.takeIf { it.isNotEmpty() } ?: providerName,
            previewUrl = image,
            blurHash = blurHash,
        )
    }
}

internal fun String.toCardType(): Card.Type? = when (this) {
    "link" -> Card.Type.LINK
    "photo" -> Card.Type.IMAGE
    "video" -> Card.Type.VIDEO
    "rich" -> Card.Type.RICH
    else -> null
}

/**
 * See https://docs.joinmastodon.org/entities/attachment/
 */
@JsonClass(generateAdapter = true)
internal class MastodonMediaAttachment(
    @Json(name = "type") val type: String,
    @Json(name = "url") val url: String,
    @Json(name = "preview_url") val previewUrl: String = "",
    @Json(name = "blurhash") val blurHash: String = "",
) {
    fun toMedia(): Media? = type.toMediaType()?.let { mediaType ->
        Media(
            type = mediaType,
            url = url,
            previewUrl = previewUrl,
            blurHash = blurHash,
        )
    }
}

internal fun String.toMediaType(): Media.Type? = when (this) {
    "image" -> Media.Type.IMAGE
    "gifv" -> Media.Type.GIF
    "video" -> Media.Type.VIDEO
    "audio" -> Media.Type.AUDIO
    else -> null
}

/**
 * See https://docs.joinmastodon.org/entities/mention/
 */
@JsonClass(generateAdapter = true)
internal class MastodonMention(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "acct") val accountName: String
) {
    fun toMention(instanceUrl: String): Mention = Mention(
        userId = EntityKey(
            instanceUrl = if (accountName.length > username.length) {
                accountName.substring(username.length + 1)
            } else {
                instanceUrl
            },
            id = id,
        ),
        username = username,
    )
}

/**
 * See https://docs.joinmastodon.org/entities/tag/
 */
@JsonClass(generateAdapter = true)
internal class MastodonTag(
    @Json(name = "name") val name: String,
)

/**
 * See https://docs.joinmastodon.org/entities/status/
 */
@JsonClass(generateAdapter = true)
internal class MastodonStatus(
    @Json(name = "id") val id: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "account") val account: MastodonAccount,
    @Json(name = "uri") val uri: String,
    @Json(name = "in_reply_to_id") val inReplyToId: String? = null,
    @Json(name = "in_reply_to_account_id") val inReplyToAccountId: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "replies_count") val repliesCount: Int,
    @Json(name = "reblogs_count") val reblogsCount: Int,
    @Json(name = "favourites_count") val favoritesCount: Int,
    @Json(name = "reblogged") val reblogged: Boolean,
    @Json(name = "favourited") val favorited: Boolean,
    @Json(name = "reblog") val reblog: MastodonStatus? = null,
    @Json(name = "media_attachments") val mediaAttachments: List<MastodonMediaAttachment> = emptyList(),
    @Json(name = "card") val card: MastodonCard? = null,
    @Json(name = "tags") val tags: List<MastodonTag> = emptyList(),
    @Json(name = "mentions") val mentions: List<MastodonMention> = emptyList(),
) {
    fun toStatus(instanceUrl: String): Status = reblog?.toStatusInternal(instanceUrl, account.toUser(instanceUrl), instanceUrl)
        ?: toStatusInternal(instanceUrl, null, null)

    private fun toStatusInternal(instanceUrl: String, reblogger: User?, rebloggedInstanceUrl: String?): Status = Status(
        id = EntityKey(instanceUrl, id),
        uri = uri,
        created = Instant.parse(createdAt),
        sender = account.toUser(instanceUrl),
        reblogger = reblogger,
        rebloggedInstanceUrl = rebloggedInstanceUrl,
        inReplyToStatusId = inReplyToId,
        inReplyToAccountId = inReplyToAccountId,
        content = content,
        tags = tags.map { it.name },
        mentions = mentions.map { it.toMention(instanceUrl) },
        media = mediaAttachments.mapNotNull { it.toMedia() },
        card = card?.toCard(),
        repliesCount = repliesCount,
        reblogsCount = reblogsCount,
        favoritesCount = favoritesCount,
        reblogged = reblogged,
        favorited = favorited,
    )
}
