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

package me.xizzhu.android.rubridens.core.view.feed

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.SystemClock
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.view.BlurHashDecoder
import me.xizzhu.android.rubridens.core.view.R
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusDetailBinding
import me.xizzhu.android.rubridens.core.view.formatAbsoluteTimestamp
import me.xizzhu.android.rubridens.core.view.formatDisplayName
import me.xizzhu.android.rubridens.core.view.formatSenderUsername
import me.xizzhu.android.rubridens.core.view.formatTextContent
import me.xizzhu.android.rubridens.core.view.loadImage

data class FeedStatusDetailItem(
    override val status: Status,
    val hasAncestor: Boolean,
    val hasDescendant: Boolean,
) : FeedStatusItem<FeedStatusDetailItem>(TYPE_STATUS_DETAIL, status) {
    val profileImageUrl: String = status.sender.avatarUrl
    val displayName: String by lazy(mode = LazyThreadSafetyMode.NONE) { status.sender.formatDisplayName() }
    val accountName: String by lazy(mode = LazyThreadSafetyMode.NONE) { status.formatSenderUsername() }
    val postTime: CharSequence by lazy(mode = LazyThreadSafetyMode.NONE) { status.formatAbsoluteTimestamp() }

    internal val cardTitle: String? = status.card?.title
    internal val cardDescription: String? = status.card?.description
    internal val cardAuthor: String? = status.card?.author
    internal val cardImageUrl: String? = status.card?.previewUrl
    internal val cardPlaceholder: Bitmap? = BlurHashDecoder.decode(status.card?.blurHash, 16, 16)
    internal val cardUrl: String? = status.card?.url

    internal val reblogged: Boolean = status.reblogged
    internal val favorited: Boolean = status.favorited

    internal var clickableSpanLastClicked: Long = 0L

    private var text: CharSequence? = null

    internal fun getText(
        openUrl: (url: String) -> Unit,
        openTag: (tag: String) -> Unit,
        openUser: (user: User) -> Unit,
    ): CharSequence {
        if (text == null) {
            text = status.formatTextContent(
                openUrl = { url ->
                    clickableSpanLastClicked = SystemClock.elapsedRealtime()
                    openUrl(url)
                },
                openTag = { tag ->
                    clickableSpanLastClicked = SystemClock.elapsedRealtime()
                    openTag(tag)
                },
                openUser = { user ->
                    clickableSpanLastClicked = SystemClock.elapsedRealtime()
                    openUser(user)
                }
            )
        }
        return text!!
    }
}

internal class FeedStatusDetailItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    openStatus: (status: Status) -> Unit,
    replyToStatus: (status: Status) -> Unit,
    reblogStatus: (status: Status) -> Unit,
    favoriteStatus: (status: Status) -> Unit,
    shareStatus: (status: Status) -> Unit,
    private val openUser: (user: User) -> Unit,
    openMedia: (media: Media) -> Unit,
    private val openTag: (tag: String) -> Unit,
    private val openUrl: (url: String) -> Unit,
) : FeedItemViewHolder<FeedStatusDetailItem, ItemFeedStatusDetailBinding>(ItemFeedStatusDetailBinding.inflate(inflater, parent, false)) {
    companion object {
        private val IMAGE_FILTER_ON = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        private val IMAGE_FILTER_OFF = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    init {
        val openUserListener = View.OnClickListener { item?.let { openUser(it.status.sender) } }
        viewBinding.profileImage.setOnClickListener(openUserListener)
        viewBinding.displayName.setOnClickListener(openUserListener)

        // ClickableSpan prevents the click event from being propagated to parent, so have to set the listener on the same TextView
        viewBinding.text.setOnClickListener {
            item?.let { item ->
                // The text view can still receive the click event even when the ClickableSpan is clicked.
                if (SystemClock.elapsedRealtime() - item.clickableSpanLastClicked > 250L) {
                    openStatus(item.status)
                }
            }
        }
        viewBinding.text.movementMethod = LinkMovementMethod.getInstance()

        viewBinding.mediaPreview.init(openMedia)
        viewBinding.cardBorder.setOnClickListener { item?.cardUrl?.let { openUrl(it) } }

        viewBinding.reply.colorFilter = IMAGE_FILTER_OFF
        viewBinding.share.colorFilter = IMAGE_FILTER_OFF
        viewBinding.reply.setOnClickListener { item?.let { replyToStatus(it.status) } }
        viewBinding.reblog.setOnClickListener { item?.let { reblogStatus(it.status) } }
        viewBinding.favorite.setOnClickListener { item?.let { favoriteStatus(it.status) } }
        viewBinding.share.setOnClickListener { item?.let { shareStatus(it.status) } }
    }

    override fun bind(item: FeedStatusDetailItem, payloads: List<Any>) = with(viewBinding) {
        ancestorIndicator.isVisible = item.hasAncestor
        profileImage.loadImage(item.profileImageUrl, R.drawable.img_person_placeholder)
        displayName.text = item.displayName
        accountName.text = item.accountName
        text.text = item.getText(openUrl = openUrl, openTag = openTag, openUser = openUser)

        mediaPreview.isVisible = item.status.media.isNotEmpty()
        mediaPreview.setMedia(item.status.media)

        if (item.status.card == null) {
            cardPreview.isVisible = false
            cardTitle.isVisible = false
            cardDescription.isVisible = false
            cardAuthor.isVisible = false
            cardBorder.isVisible = false
        } else {
            cardPreview.isVisible = true
            cardTitle.isVisible = true
            cardDescription.isVisible = true
            cardAuthor.isVisible = true
            cardBorder.isVisible = true
            cardPreview.loadImage(item.cardImageUrl!!, R.drawable.img_card_placeholder, item.cardPlaceholder)
            cardTitle.text = item.cardTitle
            cardDescription.text = item.cardDescription
            cardAuthor.text = item.cardAuthor
        }

        dateTime.text = item.postTime

        val reblogsFavoritesBuilder = StringBuilder()
        if (item.status.reblogsCount > 0) {
            reblogsFavoritesBuilder.append(itemView.resources.getQuantityString(R.plurals.text_x_reblogs, item.status.reblogsCount, item.status.reblogsCount)).append("  ")
        }
        if (item.status.favoritesCount > 0) {
            reblogsFavoritesBuilder.append(itemView.resources.getQuantityString(R.plurals.text_x_favorites, item.status.favoritesCount, item.status.favoritesCount))
        }
        if (reblogsFavoritesBuilder.isNotEmpty()) {
            reblogsFavorites.text = reblogsFavoritesBuilder.toString()
            reblogsFavorites.visibility = View.VISIBLE
            reblogsFavoritesDivider.visibility = View.VISIBLE
        } else {
            reblogsFavorites.visibility = View.GONE
            reblogsFavoritesDivider.visibility = View.GONE
        }

        reblog.colorFilter = if (item.reblogged) IMAGE_FILTER_ON else IMAGE_FILTER_OFF
        favorite.colorFilter = if (item.favorited) IMAGE_FILTER_ON else IMAGE_FILTER_OFF
    }
}
