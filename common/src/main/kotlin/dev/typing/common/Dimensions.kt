package dev.typing.common

import android.content.Context
import kotlin.math.roundToInt

/** Shared density conversion; no app state, theme or feature dependencies. */
object Dimensions {
    fun dp(context: Context, value: Int) = (value * context.resources.displayMetrics.density).roundToInt()
}
