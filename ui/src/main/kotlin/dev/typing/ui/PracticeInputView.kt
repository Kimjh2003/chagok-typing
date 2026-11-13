package dev.typing.ui

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** The app owns input state/IME policy/scoring; this view only assembles native UI. */
class PracticeInputView(
    context: Context, prompt: CharSequence, promptLabel: String,
    inputLabel: String, hint: String,
) : LinearLayout(context) {
    val editor: SubmitEditText
    private val promptView: TextView
    private val promptCard = UiControls.practiceCard(context)
    private val inputCard = UiControls.practiceCard(context)
    private var scrollViewport: android.widget.ScrollView? = null
    private val promptColumn = LinearLayout(context).apply { orientation = VERTICAL }
    private val inputColumn = LinearLayout(context).apply { orientation = VERTICAL }
    private var sideBySide = false
    val metricsLabel = UiControls.inlineLabel(context, "").apply {
        textSize = 11f
        gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
        setPaddingRelative(dp(6), dp(2), dp(UiTokens.RADIUS), dp(2))
        setTypeface(null, android.graphics.Typeface.NORMAL)
    }
    private val promptHeader = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
    }
    private val inputHeader = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

    init {
        orientation = VERTICAL
        addView(promptColumn, LayoutParams(-1, -2))
        addView(inputColumn, LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.LABEL_SECTION_GAP) })
        promptColumn.addView(promptHeader, LayoutParams(-1, -2))
        promptColumn.addView(promptCard, LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.LABEL_CONTENT_GAP) })
        inputColumn.addView(inputHeader, LayoutParams(-1, -2))
        inputColumn.addView(inputCard, LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.LABEL_CONTENT_GAP) })
        for (card in arrayOf(promptCard, inputCard)) card.setPadding(dp(10), dp(4), dp(10), dp(4))
        promptHeader.addView(UiControls.sectionTagAligned(context, promptLabel), LayoutParams(-2, -2))
        promptHeader.addView(metricsLabel, LayoutParams(0, -2, 1f))

        promptView = UiControls.text(context, prompt.toString(), UiTokens.COMPACT_PRACTICE_SP).apply {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                breakStrategy = android.graphics.text.LineBreaker.BREAK_STRATEGY_SIMPLE
            } else {
                @Suppress("WrongConstant")
                breakStrategy = android.text.Layout.BREAK_STRATEGY_SIMPLE
            }
            hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NONE
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(dp(4), dp(4), dp(4), dp(4))
            minimumHeight = dp(40)
        }
        promptCard.addView(promptView, LayoutParams(-1, -2))
        val label = UiControls.sectionTagAligned(context, inputLabel)
        inputHeader.addView(label, LayoutParams(-2, -2))

        editor = SubmitEditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            id = View.generateViewId()
            label.labelFor = id
            UiControls.editor(this)
            background = null
            textSize = UiTokens.COMPACT_PRACTICE_SP.toFloat()
            setLineSpacing(dp(3).toFloat(), 1f)
            typeface = promptView.typeface
            breakStrategy = promptView.breakStrategy
            hyphenationFrequency = promptView.hyphenationFrequency
            includeFontPadding = promptView.includeFontPadding
            isVerticalScrollBarEnabled = false
            setPadding(dp(4), dp(4), dp(4), dp(4))
            minHeight = 0
            minimumHeight = 0
            minLines = 1
            this.hint = hint
            setOnFocusChangeListener { _, focused -> inputCard.isActivated = focused }
        }
        inputCard.addView(editor, LayoutParams(-1, -2))
    }

    /** Switch arrangement without replacing the editor or touching its composing text. */
    fun setCompactViewport(availableWidth: Int, availableHeight: Int) {
        val horizontal = AdaptiveLayoutPolicy.sideBySidePractice(
            (availableWidth / resources.displayMetrics.density).toInt(),
            (availableHeight / resources.displayMetrics.density).toInt(), resources.configuration.fontScale)
        if (horizontal == sideBySide) return
        sideBySide = horizontal
        orientation = if (horizontal) HORIZONTAL else VERTICAL
        (promptCard.layoutParams as LayoutParams).topMargin = dp(if (horizontal) UiTokens.LABEL_CONTENT_GAP_COMPACT else UiTokens.LABEL_CONTENT_GAP)
        (inputCard.layoutParams as LayoutParams).topMargin = dp(if (horizontal) UiTokens.LABEL_CONTENT_GAP_COMPACT else UiTokens.LABEL_CONTENT_GAP)
        for (card in arrayOf(promptCard, inputCard)) card.setPadding(dp(10), dp(if (horizontal) 2 else 4), dp(10), dp(if (horizontal) 2 else 4))
        promptView.minimumHeight = dp(if (horizontal) 32 else 40)
        promptView.setPadding(dp(4), dp(if (horizontal) 2 else 4), dp(4), dp(if (horizontal) 2 else 4))
        editor.setPadding(dp(4), dp(if (horizontal) 2 else 4), dp(4), dp(if (horizontal) 2 else 4))
        promptColumn.layoutParams = if (horizontal) LayoutParams(0, -2, 1f) else LayoutParams(-1, -2)
        inputColumn.layoutParams = if (horizontal) LayoutParams(0, -2, 1f).apply { marginStart = dp(8) }
            else LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.LABEL_SECTION_GAP) }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Measure both native text layouts naturally, including IME composition,
        // cursor/font padding and any extra wrapped input lines. No text snapshots.
        promptView.layoutParams.height = LayoutParams.WRAP_CONTENT
        editor.layoutParams.height = LayoutParams.WRAP_CONTENT
        promptCard.layoutParams.height = LayoutParams.WRAP_CONTENT
        inputCard.layoutParams.height = LayoutParams.WRAP_CONTENT
        promptHeader.layoutParams.height = LayoutParams.WRAP_CONTENT
        inputHeader.layoutParams.height = LayoutParams.WRAP_CONTENT
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val bodyHeight = maxOf(promptView.measuredHeight, editor.measuredHeight)
        val headerHeight = maxOf(promptHeader.measuredHeight, inputHeader.measuredHeight)
        if (promptView.measuredHeight != bodyHeight || editor.measuredHeight != bodyHeight ||
            promptHeader.measuredHeight != headerHeight || inputHeader.measuredHeight != headerHeight) {
            promptView.layoutParams.height = bodyHeight
            editor.layoutParams.height = bodyHeight
            promptHeader.layoutParams.height = headerHeight
            inputHeader.layoutParams.height = headerHeight
            promptColumn.forceLayout(); inputColumn.forceLayout()
            promptCard.forceLayout(); inputCard.forceLayout()
            promptHeader.forceLayout(); inputHeader.forceLayout()
            promptView.forceLayout(); editor.forceLayout()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        val cardHeight = maxOf(promptCard.measuredHeight, inputCard.measuredHeight)
        if (promptCard.measuredHeight != inputCard.measuredHeight) {
            promptCard.layoutParams.height = cardHeight
            inputCard.layoutParams.height = cardHeight
            promptColumn.forceLayout(); inputColumn.forceLayout()
            promptCard.forceLayout(); inputCard.forceLayout()
            promptHeader.forceLayout(); inputHeader.forceLayout()
            promptView.forceLayout(); editor.forceLayout()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var ancestor = parent
        while (ancestor != null && ancestor !is android.widget.ScrollView) ancestor = ancestor.parent
        scrollViewport = ancestor as? android.widget.ScrollView
    }

    override fun onDetachedFromWindow() {
        scrollViewport = null
        super.onDetachedFromWindow()
    }

    override fun requestChildRectangleOnScreen(child: View, rectangle: android.graphics.Rect, immediate: Boolean): Boolean {
        val viewport = scrollViewport
        if (child === inputColumn && viewport != null &&
            inputColumn.height <= viewport.height - viewport.paddingTop - viewport.paddingBottom) {
            // Reveal the entire input card, rather than just the cursor above the buttons.
            rectangle.top = 0
            rectangle.bottom = inputColumn.height
        }
        return super.requestChildRectangleOnScreen(child, rectangle, immediate)
    }

    private fun dp(value: Int) = UiControls.dp(context, value)
}








