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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.UiThread
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.core.view.ImageLoadingCancellable

abstract class FeedItem<T : FeedItem<T>>(@ViewType internal val viewType: Int, open val status: Status) {
    companion object {
        internal const val TYPE_STATUS_HEADER = 1
        internal const val TYPE_STATUS_FOOTER = 2
        internal const val TYPE_STATUS_TEXT = 3
        internal const val TYPE_STATUS_ONE_MEDIA = 4
        internal const val TYPE_STATUS_TWO_MEDIA = 5
        internal const val TYPE_STATUS_THREE_MEDIA = 6
        internal const val TYPE_STATUS_FOUR_MEDIA = 7
        internal const val TYPE_STATUS_CARD = 8
        internal const val TYPE_STATUS_THREAD = 9

        @IntDef(
            TYPE_STATUS_HEADER, TYPE_STATUS_FOOTER, TYPE_STATUS_TEXT,
            TYPE_STATUS_ONE_MEDIA, TYPE_STATUS_TWO_MEDIA, TYPE_STATUS_THREE_MEDIA, TYPE_STATUS_FOUR_MEDIA,
            TYPE_STATUS_CARD, TYPE_STATUS_THREAD
        )
        @Retention(AnnotationRetention.SOURCE)
        internal annotation class ViewType
    }

    internal fun isSameItem(other: FeedItem<*>): Boolean = viewType == other.viewType && status.id == other.status.id

    internal fun isContentTheSame(other: FeedItem<*>): Boolean = this == other

    @Suppress("UNCHECKED_CAST")
    internal fun getChangePayload(other: FeedItem<*>): Any? = calculateDiff(other as T)

    protected open fun calculateDiff(other: T): Any? = null
}

class FeedRecyclerView : RecyclerView {
    private lateinit var adapter: FeedItemAdapter

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    @UiThread
    fun init(
        openStatus: (status: Status) -> Unit,
        replyToStatus: (status: Status) -> Unit,
        reblogStatus: (status: Status) -> Unit,
        favoriteStatus: (status: Status) -> Unit,
        openUser: (user: User) -> Unit,
        openMedia: (media: Media) -> Unit,
        openTag: (tag: String) -> Unit,
        openUrl: (url: String) -> Unit,
    ) {
        adapter = FeedItemAdapter(
            context = context,
            openStatus = openStatus,
            replyToStatus = replyToStatus,
            reblogStatus = reblogStatus,
            favoriteStatus = favoriteStatus,
            openUser = openUser,
            openMedia = openMedia,
            openTag = openTag,
            openUrl = openUrl,
        )
        setAdapter(adapter)
    }

    @UiThread
    fun setItems(items: List<FeedItem<*>>, scrollToPosition: Int = NO_POSITION) {
        val commitCallback = if (scrollToPosition >= 0) {
            Runnable {
                when (val lm = layoutManager) {
                    is LinearLayoutManager -> lm.scrollToPositionWithOffset(scrollToPosition, 0)
                    else -> throw IllegalStateException("Unsupported layout manager: $lm")
                }
            }
        } else {
            null
        }
        adapter.submitList(items, commitCallback)
    }

    @UiThread
    fun itemCount(): Int = adapter.itemCount

    @UiThread
    fun firstVisibleItemPosition(): Int = when (val lm = layoutManager) {
        is LinearLayoutManager -> lm.findFirstVisibleItemPosition()
        else -> throw IllegalStateException("Unsupported layout manager: $lm")
    }

    @UiThread
    fun lastVisibleItemPosition(): Int = when (val lm = layoutManager) {
        is LinearLayoutManager -> lm.findLastVisibleItemPosition()
        else -> throw IllegalStateException("Unsupported layout manager: $lm")
    }
}

internal abstract class FeedItemViewHolder<I : FeedItem<*>, VB : ViewBinding>(protected val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root) {
    protected var item: I? = null
        private set

    fun bindData(item: I, payloads: List<Any>) {
        this.item = item
        bind(item, payloads)
    }

    protected abstract fun bind(item: I, payloads: List<Any>)
}

private class FeedItemAdapter(
    context: Context,
    private val openStatus: (status: Status) -> Unit,
    private val replyToStatus: (status: Status) -> Unit,
    private val reblogStatus: (status: Status) -> Unit,
    private val favoriteStatus: (status: Status) -> Unit,
    private val openUser: (user: User) -> Unit,
    private val openMedia: (media: Media) -> Unit,
    private val openTag: (tag: String) -> Unit,
    private val openUrl: (url: String) -> Unit,
) : ListAdapter<FeedItem<*>, FeedItemViewHolder<FeedItem<*>, *>>(
    AsyncDifferConfig.Builder(FeedItemDiffCallback()).setBackgroundThreadExecutor(Dispatchers.Default.limitedParallelism(1).asExecutor()).build()
) {
    private val inflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int = getItem(position).viewType

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedItemViewHolder<FeedItem<*>, *> =
        when (viewType) {
            FeedItem.TYPE_STATUS_HEADER -> {
                FeedStatusHeaderItemViewHolder(
                    inflater = inflater,
                    parent = parent,
                    openStatus = openStatus,
                    openBlogger = openUser,
                )
            }
            FeedItem.TYPE_STATUS_FOOTER -> {
                FeedStatusFooterItemViewHolder(
                    inflater = inflater,
                    parent = parent,
                    openStatus = openStatus,
                    replyToStatus = replyToStatus,
                    reblogStatus = reblogStatus,
                    favoriteStatus = favoriteStatus,
                )
            }
            FeedItem.TYPE_STATUS_TEXT -> {
                FeedStatusTextItemViewHolder(
                    inflater = inflater,
                    parent = parent,
                    openStatus = openStatus,
                    openUrl = openUrl,
                    openTag = openTag,
                    openUser = openUser,
                )
            }
            FeedItem.TYPE_STATUS_ONE_MEDIA,
            FeedItem.TYPE_STATUS_TWO_MEDIA,
            FeedItem.TYPE_STATUS_THREE_MEDIA,
            FeedItem.TYPE_STATUS_FOUR_MEDIA -> {
                FeedStatusMediaItemViewHolder.create(
                    inflater = inflater,
                    parent = parent,
                    openStatus = openStatus,
                    openMedia = openMedia,
                    viewType = viewType,
                )
            }
            FeedItem.TYPE_STATUS_CARD -> {
                FeedStatusCardItemViewHolder(
                    inflater = inflater,
                    parent = parent,
                    openStatus = openStatus,
                    openUrl = openUrl,
                )
            }
            FeedItem.TYPE_STATUS_THREAD -> {
                FeedStatusThreadViewHolder(
                    inflater = inflater,
                    parent = parent,
                    openStatus = openStatus,
                )
            }
            else -> throw IllegalStateException("Unsupported view type: $viewType")
        } as FeedItemViewHolder<FeedItem<*>, *>

    override fun onBindViewHolder(holder: FeedItemViewHolder<FeedItem<*>, *>, position: Int) {
        holder.bindData(getItem(position), emptyList())
    }

    override fun onBindViewHolder(holder: FeedItemViewHolder<FeedItem<*>, *>, position: Int, payloads: List<Any>) {
        holder.bindData(getItem(position), payloads)
    }

    override fun onViewRecycled(holder: FeedItemViewHolder<FeedItem<*>, *>) {
        super.onViewRecycled(holder)

        if (holder is ImageLoadingCancellable) {
            holder.cancelImageLoading()
        }
    }
}

private class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem<*>>() {
    override fun areItemsTheSame(oldItem: FeedItem<*>, newItem: FeedItem<*>): Boolean = oldItem.isSameItem(newItem)

    override fun areContentsTheSame(oldItem: FeedItem<*>, newItem: FeedItem<*>): Boolean = oldItem.isContentTheSame(newItem)

    override fun getChangePayload(oldItem: FeedItem<*>, newItem: FeedItem<*>): Any? = oldItem.getChangePayload(newItem)
}
