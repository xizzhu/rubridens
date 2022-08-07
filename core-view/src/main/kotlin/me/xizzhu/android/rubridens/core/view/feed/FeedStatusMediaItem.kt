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
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.view.ImageLoadingCancellable
import me.xizzhu.android.rubridens.core.view.cancelImageLoading
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusMediaBinding
import me.xizzhu.android.rubridens.core.view.loadImage

data class FeedStatusMediaItem(
    override val status: Status,
    val media: Media,
    val imageUrl: String,
    val placeholder: Bitmap?,
    val isPlayable: Boolean,
    val openStatus: (status: Status) -> Unit,
    val openMedia: (media: Media) -> Unit,
) : FeedItem<FeedStatusMediaItem>(TYPE_STATUS_MEDIA, status)

internal class FeedStatusMediaItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : FeedItemViewHolder<FeedStatusMediaItem, ItemFeedStatusMediaBinding>(ItemFeedStatusMediaBinding.inflate(inflater, parent, false)), ImageLoadingCancellable {
    init {
        viewBinding.root.setOnClickListener { item?.let { it.openStatus(it.status) } }
        viewBinding.image.setOnClickListener { item?.let { it.openMedia(it.media) } }
    }

    override fun bind(item: FeedStatusMediaItem, payloads: List<Any>) = with(viewBinding) {
        image.loadImage(item.imageUrl, placeholder = item.placeholder)
        play.isVisible = item.isPlayable
    }

    override fun cancelImageLoading() {
        viewBinding.image.cancelImageLoading()
    }
}
