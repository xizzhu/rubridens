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
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import me.xizzhu.android.rubridens.core.view.R
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusFooterBinding

data class FeedStatusFooterItem(
    override val statusInstanceUrl: String,
    override val statusId: String,
    val replies: String,
    val reblogs: String,
    val reblogged: Boolean,
    val favorites: String,
    val favorited: Boolean,
) : FeedItem<FeedStatusFooterItem>(TYPE_STATUS_FOOTER, statusInstanceUrl, statusId)

internal class FeedStatusFooterItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : FeedItemViewHolder<FeedStatusFooterItem, ItemFeedStatusFooterBinding>(ItemFeedStatusFooterBinding.inflate(inflater, parent, false)) {
    private val reply = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_reply_24, null)!!
    private val reblog = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_reblog_24, null)!!
    private val favorite = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_favorite_24, null)!!

    init {
        setDrawable(viewBinding.replies, reply, false)
    }

    override fun bind(item: FeedStatusFooterItem, payloads: List<Any>) = with(viewBinding) {
        replies.text = item.replies

        reblogs.text = item.reblogs
        setDrawable(reblogs, reblog, item.reblogged)

        favorites.text = item.favorites
        setDrawable(favorites, favorite, item.favorited)
    }

    private fun setDrawable(textView: TextView, originalDrawable: Drawable, highlight: Boolean) {
        DrawableCompat.wrap(originalDrawable).let { drawable ->
            DrawableCompat.setTint(drawable, if (highlight) Color.RED else Color.GRAY)
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
        }
    }
}
