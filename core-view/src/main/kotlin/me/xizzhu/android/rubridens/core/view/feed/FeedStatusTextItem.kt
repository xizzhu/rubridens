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

import android.os.SystemClock
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.view.databinding.ItemFeedStatusTextBinding
import me.xizzhu.android.rubridens.core.view.formatTextContent

data class FeedStatusTextItem(
    override val status: Status,
    val hasAncestor: Boolean,
    val hasDescendant: Boolean,
) : FeedStatusItem<FeedStatusTextItem>(TYPE_STATUS_TEXT, status) {
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

internal class FeedStatusTextItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    openStatus: (status: Status) -> Unit,
    private val openUrl: (url: String) -> Unit,
    private val openTag: (tag: String) -> Unit,
    private val openUser: (user: User) -> Unit,
) : FeedItemViewHolder<FeedStatusTextItem, ItemFeedStatusTextBinding>(ItemFeedStatusTextBinding.inflate(inflater, parent, false)) {
    init {
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
    }

    override fun bind(item: FeedStatusTextItem, payloads: List<Any>) = with(viewBinding) {
        text.text = item.getText(openUrl = openUrl, openTag = openTag, openUser = openUser)
    }
}
