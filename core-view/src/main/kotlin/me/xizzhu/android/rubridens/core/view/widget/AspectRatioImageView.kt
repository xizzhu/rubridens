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
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import me.xizzhu.android.rubridens.core.view.R
import kotlin.math.roundToInt

class AspectRatioImageView : ShapeableImageView {
    private var aspectRatio: Double = Double.NaN

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return

        val a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView)
        try {
            val ratio = a.getString(R.styleable.AspectRatioImageView_aspectRatio)
                ?: throw IllegalArgumentException("'aspectRatio' is required by AspectRatioImageView")
            val (width, height) = ratio.split(":")
            aspectRatio = height.toDouble() / width.toDouble()
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        // Keep the aspect ratio to be 16:9
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = (measuredWidth * aspectRatio).roundToInt()
        setMeasuredDimension(measuredWidth, measuredHeight)
    }
}
