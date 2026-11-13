package dev.typing.ui

/** Widths and heights are current available dimensions in dp. */
object AdaptiveLayoutPolicy {
    const val MIN_TWO_PANE_WIDTH_DP = 600
    const val MAX_TWO_PANE_FONT_SCALE = 1.3f
    const val COMPACT_PRACTICE_HEIGHT_DP = 260

    fun twoPane(widthDp: Int, fontScale: Float): Boolean =
        widthDp >= MIN_TWO_PANE_WIDTH_DP && fontScale <= MAX_TWO_PANE_FONT_SCALE

    fun sideBySidePractice(widthDp: Int, heightDp: Int, fontScale: Float): Boolean =
        twoPane(widthDp, fontScale) && heightDp < COMPACT_PRACTICE_HEIGHT_DP
}
