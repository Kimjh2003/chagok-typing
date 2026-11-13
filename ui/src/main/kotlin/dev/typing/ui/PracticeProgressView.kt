package dev.typing.ui

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar

/** Compact length indicator. The caller supplies counts; no input text is retained. */
class PracticeProgressView(context: Context) : LinearLayout(context) {
    private val bar: ProgressBar

    init {
        orientation = VERTICAL
        val palette = UiPalettes.from(context)
        addView(UiControls.inlineLabel(context, "입력 진행 · 길이 기준").apply {
            setPadding(0, 0, 0, 0)
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }, LayoutParams(-2, -2))
        fun line(color: Int) = GradientDrawable().apply {
            setColor(color)
            cornerRadius = UiControls.dp(context, 2).toFloat()
        }
        val layers = LayerDrawable(arrayOf(
            line(palette.INFO_LINE),
            ClipDrawable(line(palette.ACTION), Gravity.START, ClipDrawable.HORIZONTAL)
        )).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
        }
        bar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressDrawable = layers
            progressTintList = null
            progressBackgroundTintList = null
            contentDescription = "입력 진행, 제시문 길이 기준"
            accessibilityLiveRegion = android.view.View.ACCESSIBILITY_LIVE_REGION_NONE
        }
        addView(bar, LayoutParams(-1, UiControls.dp(context, 4)).apply {
            topMargin = UiControls.dp(context, 6)
        })
    }

    fun updateLength(inputLength: Int, promptLength: Int) {
        val percent = if (promptLength <= 0) 0 else
            (inputLength.coerceAtLeast(0).toLong() * 100 / promptLength).coerceIn(0, 100).toInt()
        if (bar.progress != percent) bar.progress = percent
    }
}
