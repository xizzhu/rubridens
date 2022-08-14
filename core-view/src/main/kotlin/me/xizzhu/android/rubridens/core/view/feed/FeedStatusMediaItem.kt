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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.google.android.material.imageview.ShapeableImageView
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.view.ImageLoadingCancellable
import me.xizzhu.android.rubridens.core.view.cancelImageLoading
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusMediaFourBinding
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusMediaOneBinding
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusMediaThreeBinding
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusMediaTwoBinding
import me.xizzhu.android.rubridens.core.view.loadImage
import me.xizzhu.android.rubridens.core.view.widget.AspectRatioImageView

data class FeedStatusMediaInfo(
    val media: Media,
    val imageUrl: String,
    val placeholder: Bitmap?,
    val isPlayable: Boolean,
)

data class FeedStatusMediaItem(
    override val status: Status,
    val mediaInfo: List<FeedStatusMediaInfo>,
) : FeedStatusItem<FeedStatusMediaItem>(
    viewType = when (mediaInfo.size) {
        0 -> throw IllegalArgumentException("mediaInfo is empty")
        1 -> TYPE_STATUS_ONE_MEDIA
        2 -> TYPE_STATUS_TWO_MEDIA
        3 -> TYPE_STATUS_THREE_MEDIA
        else -> TYPE_STATUS_FOUR_MEDIA
    },
    status = status,
)

internal abstract class FeedStatusMediaItemViewHolder<VB : ViewBinding>(
    viewBinding: VB,
    private val imageViews: Array<AspectRatioImageView>,
    private val playButtons: Array<ShapeableImageView>,
    openStatus: (status: Status) -> Unit,
    openMedia: (media: Media) -> Unit,
) : FeedItemViewHolder<FeedStatusMediaItem, VB>(viewBinding), ImageLoadingCancellable {
    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup,
            openStatus: (status: Status) -> Unit,
            openMedia: (media: Media) -> Unit,
            @FeedItem.Companion.ViewType viewType: Int,
        ): FeedStatusMediaItemViewHolder<*> {
            val viewBinding: ViewBinding
            val imageViews: Array<AspectRatioImageView>
            val playButtons: Array<ShapeableImageView>
            when (viewType) {
                FeedItem.TYPE_STATUS_ONE_MEDIA -> {
                    viewBinding = ItemFeedStatusMediaOneBinding.inflate(inflater, parent, false)
                    imageViews = arrayOf(viewBinding.image)
                    playButtons = arrayOf(viewBinding.play)
                }
                FeedItem.TYPE_STATUS_TWO_MEDIA -> {
                    viewBinding = ItemFeedStatusMediaTwoBinding.inflate(inflater, parent, false)
                    imageViews = arrayOf(viewBinding.image1, viewBinding.image2)
                    playButtons = arrayOf(viewBinding.play1, viewBinding.play2)
                }
                FeedItem.TYPE_STATUS_THREE_MEDIA -> {
                    viewBinding = ItemFeedStatusMediaThreeBinding.inflate(inflater, parent, false)
                    imageViews = arrayOf(viewBinding.image1, viewBinding.image2, viewBinding.image3)
                    playButtons = arrayOf(viewBinding.play1, viewBinding.play2, viewBinding.play3)
                }
                FeedItem.TYPE_STATUS_FOUR_MEDIA -> {
                    viewBinding = ItemFeedStatusMediaFourBinding.inflate(inflater, parent, false)
                    imageViews = arrayOf(viewBinding.image1, viewBinding.image2, viewBinding.image3, viewBinding.image4)
                    playButtons = arrayOf(viewBinding.play1, viewBinding.play2, viewBinding.play3, viewBinding.play4)
                }
                else -> throw IllegalStateException("Unsupported view type: $viewType")
            }
            return object : FeedStatusMediaItemViewHolder<ViewBinding>(viewBinding, imageViews, playButtons, openStatus, openMedia) {}
        }
    }

    init {
        viewBinding.root.setOnClickListener { item?.let { openStatus(it.status) } }
        imageViews.forEachIndexed { i, imageView ->
            imageView.setOnClickListener { item?.let { openMedia(it.mediaInfo[i].media) } }
        }
    }

    override fun bind(item: FeedStatusMediaItem, payloads: List<Any>) {
        item.mediaInfo.forEachIndexed { i, mediaInfo ->
            imageViews[i].loadImage(mediaInfo.imageUrl, placeholder = mediaInfo.placeholder)
            playButtons[i].isVisible = mediaInfo.isPlayable
        }
    }

    override fun cancelImageLoading() {
        imageViews.forEach { it.cancelImageLoading() }
    }
}
