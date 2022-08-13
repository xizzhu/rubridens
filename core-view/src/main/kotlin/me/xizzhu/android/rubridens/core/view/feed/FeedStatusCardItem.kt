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
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.view.ImageLoadingCancellable
import me.xizzhu.android.rubridens.core.view.R
import me.xizzhu.android.rubridens.core.view.cancelImageLoading
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusCardBinding
import me.xizzhu.android.rubridens.core.view.loadImage

data class FeedStatusCardItem(
    override val status: Status,
    val title: String,
    val description: String,
    val author: String,
    val imageUrl: String,
    val placeholder: Bitmap?,
    val url: String,
) : FeedItem<FeedStatusCardItem>(TYPE_STATUS_CARD, status)

internal class FeedStatusCardItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    openStatus: (status: Status) -> Unit,
    openUrl: (url: String) -> Unit,
) : FeedItemViewHolder<FeedStatusCardItem, ItemFeedStatusCardBinding>(ItemFeedStatusCardBinding.inflate(inflater, parent, false)), ImageLoadingCancellable {
    init {
        viewBinding.root.setOnClickListener { item?.let { openStatus(it.status) } }
        viewBinding.card.setOnClickListener { item?.let { openUrl(it.url) } }
    }

    override fun bind(item: FeedStatusCardItem, payloads: List<Any>) = with(viewBinding) {
        image.loadImage(item.imageUrl, R.drawable.img_card_placeholder, item.placeholder)
        title.text = item.title
        description.text = item.description
        author.text = item.author
    }

    override fun cancelImageLoading() {
        viewBinding.image.cancelImageLoading()
    }
}
