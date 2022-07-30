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

import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusTextBinding

data class FeedStatusTextItem(
    override val statusId: String,
    val text: CharSequence,
    val openStatus: (statusId: String) -> Unit,
) : FeedItem<FeedStatusTextItem>(TYPE_STATUS_TEXT, statusId)

internal class FeedStatusTextItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : FeedItemViewHolder<FeedStatusTextItem, ItemFeedStatusTextBinding>(ItemFeedStatusTextBinding.inflate(inflater, parent, false)) {
    init {
        viewBinding.root.setOnClickListener { item?.let { it.openStatus(it.statusId) } }
    }

    override fun bind(item: FeedStatusTextItem, payloads: List<Any>) = with(viewBinding) {
        text.text = item.text
    }
}
