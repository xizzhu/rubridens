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

package me.xizzhu.android.rubridens.core.view.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.view.BlurHashDecoder
import me.xizzhu.android.rubridens.core.view.ImageLoadingCancellable
import me.xizzhu.android.rubridens.core.view.R
import me.xizzhu.android.rubridens.core.view.cancelImageLoading
import me.xizzhu.android.rubridens.core.view.databinding.ItemMediaPreviewBinding
import me.xizzhu.android.rubridens.core.view.loadImage

class MediaPreviewView : RecyclerView {
    private lateinit var adapter: MediaPreviewAdapter

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
                super.getItemOffsets(outRect, view, parent, state)

                val itemCount = adapter.itemCount
                outRect.right = if (itemCount > 0 && parent.getChildAdapterPosition(view) < itemCount - 1) {
                    resources.getDimensionPixelSize(R.dimen.media_preview_left_margin)
                } else {
                    0
                }
            }
        })
    }

    fun init(openMedia: (media: Media) -> Unit) {
        adapter = MediaPreviewAdapter(
            context = context,
            openMedia = openMedia,
        )
        setAdapter(adapter)
    }

    fun setMedia(media: List<Media>) {
        media.mapNotNull { media ->
            if (media.type == Media.Type.IMAGE || media.type == Media.Type.GIF || media.type == Media.Type.VIDEO) {
                MediaPreview(
                    media = media,
                    imageUrl = media.previewUrl.takeIf { it.isNotEmpty() } ?: media.url,
                    placeholder = BlurHashDecoder.decode(media.blurHash, 32, 18),
                    isPlayable = media.type == Media.Type.GIF || media.type == Media.Type.VIDEO,
                )
            } else {
                null
            }
        }.let {
            adapter.submitList(it)
        }
    }
}

private data class MediaPreview(
    val media: Media,
    val imageUrl: String,
    val placeholder: Bitmap?,
    val isPlayable: Boolean,
)

private class MediaPreviewAdapter(
    context: Context,
    private val openMedia: (media: Media) -> Unit,
) : ListAdapter<MediaPreview, MediaPreviewViewHolder>(
    AsyncDifferConfig.Builder(MediaPreviewDiffCallback()).setBackgroundThreadExecutor(Dispatchers.Default.limitedParallelism(1).asExecutor()).build()
) {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPreviewViewHolder = MediaPreviewViewHolder(inflater, parent, openMedia)

    override fun onBindViewHolder(holder: MediaPreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: MediaPreviewViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelImageLoading()
    }
}

private class MediaPreviewDiffCallback : DiffUtil.ItemCallback<MediaPreview>() {
    override fun areItemsTheSame(oldItem: MediaPreview, newItem: MediaPreview): Boolean = oldItem.media.url == newItem.media.url

    override fun areContentsTheSame(oldItem: MediaPreview, newItem: MediaPreview): Boolean = oldItem == newItem
}

private class MediaPreviewViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    openMedia: (media: Media) -> Unit,
) : RecyclerView.ViewHolder(ItemMediaPreviewBinding.inflate(inflater, parent, false).root), ImageLoadingCancellable {
    private val viewBinding = ItemMediaPreviewBinding.bind(itemView)
    private var mediaPreview: MediaPreview? = null

    init {
        viewBinding.root.setOnClickListener { mediaPreview?.let { openMedia(it.media) } }
    }

    fun bind(mediaPreview: MediaPreview) {
        this.mediaPreview = mediaPreview

        viewBinding.image.loadImage(mediaPreview.imageUrl, placeholder = mediaPreview.placeholder)
        viewBinding.play.isVisible = mediaPreview.isPlayable
    }

    override fun cancelImageLoading() {
        viewBinding.image.cancelImageLoading()
    }
}
