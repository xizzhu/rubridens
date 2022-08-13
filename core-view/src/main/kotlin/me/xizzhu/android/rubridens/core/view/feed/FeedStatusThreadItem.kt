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
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusThreadBinding

data class FeedStatusThreadItem(
    override val status: Status,
) : FeedItem<FeedStatusThreadItem>(TYPE_STATUS_THREAD, status)

internal class FeedStatusThreadViewHolder(inflater: LayoutInflater, parent: ViewGroup, openStatus: (status: Status) -> Unit)
    : FeedItemViewHolder<FeedStatusThreadItem, ItemFeedStatusThreadBinding>(ItemFeedStatusThreadBinding.inflate(inflater, parent, false)) {
    init {
        viewBinding.root.setOnClickListener { item?.let { openStatus(it.status) } }
    }

    override fun bind(item: FeedStatusThreadItem, payloads: List<Any>) = Unit
}
