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

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import me.xizzhu.android.rubridens.core.view.R
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusHeaderBinding

data class FeedStatusHeaderItem(
    override val statusInstanceUrl: String,
    override val statusId: String,
    val bloggerDisplayName: String,
    val bloggerProfileImageUrl: String,
    val rebloggedBy: String?,
    val subtitle: String,
) : FeedItem<FeedStatusHeaderItem>(TYPE_STATUS_HEADER, statusInstanceUrl, statusId)

internal class FeedStatusHeaderItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : FeedItemViewHolder<FeedStatusHeaderItem, ItemFeedStatusHeaderBinding>(ItemFeedStatusHeaderBinding.inflate(inflater, parent, false)) {
    init {
        val reblog = DrawableCompat.wrap(ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_reblog_16, null)!!)
            .apply { DrawableCompat.setTint(this, Color.GRAY) }
        viewBinding.rebloggedBy.setCompoundDrawablesRelativeWithIntrinsicBounds(reblog, null, null, null)
    }

    override fun bind(item: FeedStatusHeaderItem, payloads: List<Any>) = with(viewBinding) {
        if (item.rebloggedBy.isNullOrEmpty()) {
            rebloggedBy.isVisible = false
        } else {
            rebloggedBy.text = item.rebloggedBy
            rebloggedBy.isVisible = true
        }

        displayName.text = item.bloggerDisplayName
        subtitle.text = item.subtitle
    }
}
