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

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.withTransaction
import kotlinx.datetime.Instant
import me.xizzhu.android.rubridens.core.model.Card
import me.xizzhu.android.rubridens.core.model.EntityKey
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Mention
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.repository.local.StatusCache

internal class RoomStatusCache(private val appDatabase: AppDatabase) : StatusCache {
    override suspend fun readLatest(instanceUrl: String, olderThan: Long, limit: Int): List<Status> = appDatabase.withTransaction {
        buildStatuses(appDatabase.statusDao().readLatest(instanceUrl, olderThan, limit))
    }

    override suspend fun readOldest(instanceUrl: String, newerThan: Long, limit: Int): List<Status> = appDatabase.withTransaction {
        buildStatuses(appDatabase.statusDao().readOldest(instanceUrl, newerThan, limit))
    }

    override suspend fun read(statusId: EntityKey): Status? = appDatabase.withTransaction {
        appDatabase.statusDao().readByStatusId(
            instanceUrl = statusId.instanceUrl,
            statusId = statusId.id,
        )?.let { statusEntity ->
            buildStatuses(listOf(statusEntity)).firstOrNull()
        }
    }

    private suspend fun buildStatuses(statusEntities: List<StatusEntity>): List<Status> {
        val statusIds = arrayListOf<String>()
        val userIds = hashSetOf<String>()
        statusEntities.forEach { statusEntity ->
            statusIds.add(statusEntity.id)
            userIds.add(statusEntity.senderId)
            statusEntity.rebloggerId?.let { userIds.add(it) }
        }

        val users = appDatabase.userDao().readById(userIds)
            .associate { userEntity -> EntityKey(userEntity.instanceUrl, userEntity.id) to userEntity.toUser() }
        val cards = appDatabase.cardDao().readByStatusId(statusIds)
            .associate { cardEntity -> EntityKey(cardEntity.statusInstanceUrl, cardEntity.statusId) to cardEntity.toCard() }

        val media = hashMapOf<EntityKey, ArrayList<Media>>()
        appDatabase.mediaDao().readByStatusId(statusIds).forEach { mediaEntity ->
            media.getOrPut(EntityKey(mediaEntity.statusInstanceUrl, mediaEntity.statusId)) { arrayListOf() }.add(mediaEntity.toMedia())
        }

        return statusEntities.mapNotNull { statusEntity ->
            val sender = users[EntityKey(statusEntity.senderInstanceUrl, statusEntity.senderId)] ?: return@mapNotNull null
            val reblogger = if (statusEntity.rebloggerId != null && statusEntity.rebloggerInstanceUrl != null) {
                users[EntityKey(statusEntity.rebloggerInstanceUrl, statusEntity.rebloggerId)]
            } else {
                null
            }
            val statusId = EntityKey(statusEntity.instanceUrl, statusEntity.id)
            statusEntity.toStatus(
                sender = sender,
                reblogger = reblogger,
                media = media[statusId] ?: emptyList(),
                card = cards[statusId],
            )
        }.sortedByDescending { it.created }
    }

    override suspend fun save(statuses: List<Status>) {
        appDatabase.withTransaction {
            val usersToSave = arrayListOf<UserEntity>()
            val cardsToSave = arrayListOf<CardEntity>()
            val mediaToSave = arrayListOf<MediaEntity>()
            val statusesToSave = arrayListOf<StatusEntity>()
            statuses.forEach { status ->
                usersToSave.add(UserEntity(status.sender))
                status.reblogger?.let { usersToSave.add(UserEntity(it)) }
                status.card?.let { cardsToSave.add(CardEntity(it, status)) }
                status.media.forEach { mediaToSave.add(MediaEntity(it, status)) }
                statusesToSave.add(StatusEntity(status))
            }
            appDatabase.userDao().save(usersToSave)
            appDatabase.cardDao().save(cardsToSave)
            appDatabase.mediaDao().save(mediaToSave)
            appDatabase.statusDao().save(statusesToSave)
        }
    }
}

@Dao
internal interface CardDao {
    @Query("SELECT * FROM ${CardEntity.TABLE_NAME} WHERE ${CardEntity.COLUMN_NAME_STATUS_ID} IN (:statusIds)")
    suspend fun readByStatusId(statusIds: Collection<String>): List<CardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(cards: Collection<CardEntity>)
}

@Entity(tableName = CardEntity.TABLE_NAME)
internal class CardEntity(
    @ColumnInfo(name = COLUMN_NAME_URL) @PrimaryKey val url: String,
    @ColumnInfo(name = COLUMN_NAME_STATUS_INSTANCE_URL) val statusInstanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_STATUS_ID) val statusId: String,
    @ColumnInfo(name = COLUMN_NAME_TYPE) val type: String,
    @ColumnInfo(name = COLUMN_NAME_TITLE) val title: String,
    @ColumnInfo(name = COLUMN_NAME_DESCRIPTION) val description: String,
    @ColumnInfo(name = COLUMN_NAME_AUTHOR) val author: String,
    @ColumnInfo(name = COLUMN_NAME_PREVIEW_URL) val previewUrl: String,
    @ColumnInfo(name = COLUMN_NAME_BLUR_HASH) val blurHash: String,
) {
    companion object {
        const val TABLE_NAME = "card"
        const val COLUMN_NAME_URL = "url"
        const val COLUMN_NAME_STATUS_INSTANCE_URL = "status_instance_url"
        const val COLUMN_NAME_STATUS_ID = "status_id"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_DESCRIPTION = "description"
        const val COLUMN_NAME_AUTHOR = "author"
        const val COLUMN_NAME_PREVIEW_URL = "previewUrl"
        const val COLUMN_NAME_BLUR_HASH = "blurHash"
    }

    constructor(card: Card, status: Status) : this(
        url = card.url,
        statusInstanceUrl = status.id.instanceUrl,
        statusId = status.id.id,
        type = when (card.type) {
            Card.Type.LINK -> "link"
            Card.Type.IMAGE -> "image"
            Card.Type.VIDEO -> "video"
            Card.Type.RICH -> "rich"
        },
        title = card.title,
        description = card.description,
        author = card.author,
        previewUrl = card.previewUrl,
        blurHash = card.blurHash,
    )

    fun toCard(): Card = Card(
        url = url,
        type = when (type) {
            "link" -> Card.Type.LINK
            "image" -> Card.Type.IMAGE
            "video" -> Card.Type.VIDEO
            "rich" -> Card.Type.RICH
            else -> throw IllegalArgumentException("Unsupported card type - '$type'")
        },
        title = title,
        description = description,
        author = author,
        previewUrl = previewUrl,
        blurHash = blurHash,
    )
}

@Dao
internal interface MediaDao {
    @Query("SELECT * FROM ${MediaEntity.TABLE_NAME} WHERE ${MediaEntity.COLUMN_NAME_STATUS_ID} IN (:statusIds)")
    suspend fun readByStatusId(statusIds: Collection<String>): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(cards: Collection<MediaEntity>)
}

@Entity(tableName = MediaEntity.TABLE_NAME)
internal class MediaEntity(
    @ColumnInfo(name = COLUMN_NAME_URL) @PrimaryKey val url: String,
    @ColumnInfo(name = COLUMN_NAME_STATUS_INSTANCE_URL) val statusInstanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_STATUS_ID) val statusId: String,
    @ColumnInfo(name = COLUMN_NAME_TYPE) val type: String,
    @ColumnInfo(name = COLUMN_NAME_PREVIEW_URL) val previewUrl: String,
    @ColumnInfo(name = COLUMN_NAME_BLUR_HASH) val blurHash: String,
) {
    companion object {
        const val TABLE_NAME = "media"
        const val COLUMN_NAME_URL = "url"
        const val COLUMN_NAME_STATUS_INSTANCE_URL = "status_instance_url"
        const val COLUMN_NAME_STATUS_ID = "status_id"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_PREVIEW_URL = "previewUrl"
        const val COLUMN_NAME_BLUR_HASH = "blurHash"
    }

    constructor(media: Media, status: Status) : this(
        url = media.url,
        statusInstanceUrl = status.id.instanceUrl,
        statusId = status.id.id,
        type = when (media.type) {
            Media.Type.IMAGE -> "image"
            Media.Type.GIF -> "gif"
            Media.Type.VIDEO -> "video"
            Media.Type.AUDIO -> "audio"
        },
        previewUrl = media.previewUrl,
        blurHash = media.blurHash,
    )

    fun toMedia(): Media = Media(
        url = url,
        type = when (type) {
            "image" -> Media.Type.IMAGE
            "gif" -> Media.Type.GIF
            "video" -> Media.Type.VIDEO
            "audio" -> Media.Type.AUDIO
            else -> throw IllegalArgumentException("Unsupported media type - '$type'")
        },
        previewUrl = previewUrl,
        blurHash = blurHash,
    )
}

@Dao
internal interface StatusDao {
    @Query("""
        SELECT *
        FROM ${StatusEntity.TABLE_NAME}
        WHERE
            (${StatusEntity.COLUMN_NAME_INSTANCE_URL} = :instanceUrl OR ${StatusEntity.COLUMN_NAME_REBLOGGED_INSTANCE_URL} = :instanceUrl)
            AND
            ${StatusEntity.COLUMN_NAME_CREATED} < :olderThan
        ORDER BY ${StatusEntity.COLUMN_NAME_CREATED} DESC
        LIMIT :limit
    """)
    suspend fun readLatest(instanceUrl: String, olderThan: Long, limit: Int): List<StatusEntity>

    @Query("""
        SELECT *
        FROM ${StatusEntity.TABLE_NAME}
        WHERE
            (${StatusEntity.COLUMN_NAME_INSTANCE_URL} = :instanceUrl OR ${StatusEntity.COLUMN_NAME_REBLOGGED_INSTANCE_URL} = :instanceUrl)
            AND
            ${StatusEntity.COLUMN_NAME_CREATED} > :newerThan
        ORDER BY ${StatusEntity.COLUMN_NAME_CREATED} ASC
        LIMIT :limit
    """)
    suspend fun readOldest(instanceUrl: String, newerThan: Long, limit: Int): List<StatusEntity>

    @Query("""
        SELECT *
        FROM ${StatusEntity.TABLE_NAME}
        WHERE
            ${StatusEntity.COLUMN_NAME_INSTANCE_URL} = :instanceUrl
            AND
            ${StatusEntity.COLUMN_NAME_ID} = :statusId
    """)
    suspend fun readByStatusId(instanceUrl: String, statusId: String): StatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(statuses: Collection<StatusEntity>)
}

@Entity(
    tableName = StatusEntity.TABLE_NAME,
    primaryKeys = [StatusEntity.COLUMN_NAME_INSTANCE_URL, StatusEntity.COLUMN_NAME_ID],
    indices = [
        Index(value = [StatusEntity.COLUMN_NAME_INSTANCE_URL]),
        Index(value = [StatusEntity.COLUMN_NAME_REBLOGGED_INSTANCE_URL]),
        Index(value = [StatusEntity.COLUMN_NAME_CREATED]),
    ],
)
internal class StatusEntity(
    @ColumnInfo(name = COLUMN_NAME_INSTANCE_URL) val instanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_ID) val id: String,
    @ColumnInfo(name = COLUMN_NAME_URI) val uri: String,
    @ColumnInfo(name = COLUMN_NAME_CREATED) val created: Long,
    @ColumnInfo(name = COLUMN_NAME_SENDER_INSTANCE_URL) val senderInstanceUrl: String,
    @ColumnInfo(name = COLUMN_NAME_SENDER_ID) val senderId: String,
    @ColumnInfo(name = COLUMN_NAME_REBLOGGER_INSTANCE_URL) val rebloggerInstanceUrl: String?,
    @ColumnInfo(name = COLUMN_NAME_REBLOGGER_ID) val rebloggerId: String?,
    @ColumnInfo(name = COLUMN_NAME_REBLOGGED_INSTANCE_URL) val rebloggedInstanceUrl: String?,
    @ColumnInfo(name = COLUMN_NAME_IN_REPLY_TO_STATUS_ID) val inReplyToStatusId: String?,
    @ColumnInfo(name = COLUMN_NAME_IN_REPLY_TO_ACCOUNT_ID) val inReplyToAccountId: String?,
    @ColumnInfo(name = COLUMN_NAME_CONTENT) val content: String,
    @ColumnInfo(name = COLUMN_NAME_TAG) val tag: String,
    @ColumnInfo(name = COLUMN_NAME_MENTIONS) val mentions: String,
    @ColumnInfo(name = COLUMN_NAME_REPLIES_COUNT) val repliesCount: Int,
    @ColumnInfo(name = COLUMN_NAME_REBLOGS_COUNT) val reblogsCount: Int,
    @ColumnInfo(name = COLUMN_NAME_FAVORITES_COUNT) val favoritesCount: Int,
    @ColumnInfo(name = COLUMN_NAME_REBLOGGED) val reblogged: Boolean,
    @ColumnInfo(name = COLUMN_NAME_FAVORITED) val favorited: Boolean,
) {
    companion object {
        const val TABLE_NAME = "status"
        const val COLUMN_NAME_INSTANCE_URL = "instance_url"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_URI = "uri"
        const val COLUMN_NAME_CREATED = "created"
        const val COLUMN_NAME_SENDER_INSTANCE_URL = "sender_instance_url"
        const val COLUMN_NAME_SENDER_ID = "sender_id"
        const val COLUMN_NAME_REBLOGGER_INSTANCE_URL = "reblogger_instance_url"
        const val COLUMN_NAME_REBLOGGER_ID = "reblogger_id"
        const val COLUMN_NAME_REBLOGGED_INSTANCE_URL = "reblogged_instance_url"
        const val COLUMN_NAME_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id"
        const val COLUMN_NAME_IN_REPLY_TO_ACCOUNT_ID = "in_reply_to_account_id"
        const val COLUMN_NAME_CONTENT = "content"
        const val COLUMN_NAME_TAG = "tag"
        const val COLUMN_NAME_MENTIONS = "mentions"
        const val COLUMN_NAME_REPLIES_COUNT = "replies_count"
        const val COLUMN_NAME_REBLOGS_COUNT = "reblogs_count"
        const val COLUMN_NAME_FAVORITES_COUNT = "favorites_count"
        const val COLUMN_NAME_REBLOGGED = "reblogged"
        const val COLUMN_NAME_FAVORITED = "favorited"
    }

    constructor(status: Status) : this(
        instanceUrl = status.id.instanceUrl,
        id = status.id.id,
        uri = status.uri,
        created = status.created.toEpochMilliseconds(),
        senderInstanceUrl = status.sender.id.instanceUrl,
        senderId = status.sender.id.id,
        rebloggerInstanceUrl = status.reblogger?.id?.instanceUrl,
        rebloggerId = status.reblogger?.id?.id,
        rebloggedInstanceUrl = status.rebloggedInstanceUrl,
        inReplyToStatusId = status.inReplyToStatusId,
        inReplyToAccountId = status.inReplyToAccountId,
        content = status.content,
        tag = status.tags.joinToString(separator = ":"),
        mentions = status.mentions.joinToString(separator = ":") { mention ->
            "${mention.userId.instanceUrl}@${mention.userId.id}@${mention.username}"
        },
        repliesCount = status.repliesCount,
        reblogsCount = status.reblogsCount,
        favoritesCount = status.favoritesCount,
        reblogged = status.reblogged,
        favorited = status.favorited,
    )

    fun toStatus(sender: User, reblogger: User?, media: List<Media>, card: Card?): Status = Status(
        id = EntityKey(instanceUrl, id),
        uri = uri,
        created = Instant.fromEpochMilliseconds(created),
        sender = sender,
        reblogger = reblogger,
        rebloggedInstanceUrl = rebloggedInstanceUrl,
        inReplyToStatusId = inReplyToStatusId,
        inReplyToAccountId = inReplyToAccountId,
        content = content,
        tags = tag.takeIf { it.isNotEmpty() }?.split(":") ?: emptyList(),
        mentions = mentions.takeIf { it.isNotEmpty() }?.split(":")?.mapNotNull { mention ->
            mention.split("@").takeIf { it.size == 3 }?.let { Mention(EntityKey(it[0], it[1]), it[2]) }
        } ?: emptyList(),
        media = media,
        card = card,
        repliesCount = repliesCount,
        reblogsCount = reblogsCount,
        favoritesCount = favoritesCount,
        reblogged = reblogged,
        favorited = favorited,
    )
}
