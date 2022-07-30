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
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import me.xizzhu.android.rubridens.core.view.R
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusFooterBinding

data class FeedStatusFooterItem(
    override val statusId: String,
    val replies: String,
    val reblogs: String,
    val reblogged: Boolean,
    val favorites: String,
    val favorited: Boolean,
    val openStatus: (statusId: String) -> Unit,
    val replyToStatus: (statusId: String) -> Unit,
    val reblogStatus: (statusId: String) -> Unit,
    val favoriteStatus: (statusId: String) -> Unit,
) : FeedItem<FeedStatusFooterItem>(TYPE_STATUS_FOOTER, statusId)

internal class FeedStatusFooterItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : FeedItemViewHolder<FeedStatusFooterItem, ItemFeedStatusFooterBinding>(ItemFeedStatusFooterBinding.inflate(inflater, parent, false)) {
    init {
        viewBinding.root.setOnClickListener { item?.let { it.openStatus(it.statusId) } }

        setDrawable(viewBinding.replies, R.drawable.ic_reply_24, false)
        viewBinding.replies.setOnClickListener { item?.let { it.replyToStatus(it.statusId) } }
        viewBinding.reblogs.setOnClickListener { item?.let { it.reblogStatus(it.statusId) } }
        viewBinding.favorites.setOnClickListener { item?.let { it.favoriteStatus(it.statusId) } }
    }

    override fun bind(item: FeedStatusFooterItem, payloads: List<Any>) = with(viewBinding) {
        replies.text = item.replies

        reblogs.text = item.reblogs
        setDrawable(reblogs, R.drawable.ic_reblog_24, item.reblogged)

        favorites.text = item.favorites
        setDrawable(favorites, R.drawable.ic_favorite_24, item.favorited)
    }

    private fun setDrawable(textView: TextView, @DrawableRes drawableRes: Int, highlight: Boolean) {
        val drawable = ResourcesCompat.getDrawable(textView.resources, drawableRes, null)!!
        drawable.setTint(if (highlight) Color.RED else Color.GRAY)
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
    }
}
