package dev.typing.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import dev.typing.common.Dimensions
import dev.typing.motion.UiMotion

enum class ResultCardRole { SCORE, METRIC, PROMPT, INPUT }

/** Shared native controls; callbacks and content are supplied by the app. */
object UiControls {
    private val disabled = intArrayOf(-android.R.attr.state_enabled)
    private val checked = intArrayOf(android.R.attr.state_checked)
    private val focused = intArrayOf(android.R.attr.state_focused)
    private val normal = intArrayOf()

    fun dp(c: Context, value: Int) = Dimensions.dp(c, value)

    private class SwitchThumb(private val size: Int, private val inset: Float, color: Int) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        override fun draw(canvas: Canvas) {
            val radius = (minOf(bounds.width(), bounds.height()) / 2f - inset).coerceAtLeast(0f)
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), radius, paint)
        }
        override fun getIntrinsicWidth() = size
        override fun getIntrinsicHeight() = size
        override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
        override fun setColorFilter(filter: ColorFilter?) { paint.colorFilter = filter; invalidateSelf() }
        @Deprecated("Deprecated by Android")
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }

    private fun shape(c: Context, fill: Int, stroke: Int = 0, width: Int = 0) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(c, UiTokens.RADIUS).toFloat()
        if (width > 0) setStroke(dp(c, width), stroke)
    }

    private fun surface(c: Context, primary: Boolean, choice: Boolean = false): RippleDrawable {
        val states = StateListDrawable().apply {
            if (android.animation.ValueAnimator.areAnimatorsEnabled()) {
                setEnterFadeDuration(UiMotion.SURFACE_ENTER_MS)
                setExitFadeDuration(UiMotion.SURFACE_EXIT_MS)
            }
            addState(disabled, shape(c, UiPalettes.from(c).DISABLED_FILL))
            addState(focused, shape(c, if (primary) UiPalettes.from(c).ACTION_FILL else UiPalettes.from(c).SELECTED, UiPalettes.from(c).ACTION, 3))
            if (choice) addState(checked, shape(c, UiPalettes.from(c).SELECTED, UiPalettes.from(c).ACTION, 2))
            addState(normal, shape(c, if (primary) UiPalettes.from(c).ACTION_FILL else UiPalettes.from(c).SURFACE,
                UiPalettes.from(c).ACTION_OUTLINE, if (primary && !UiPalettes.isDark(c)) 0 else 1))
        }
        val ripple = if (primary) UiPalettes.from(c).RIPPLE_ON_ACTION else UiPalettes.from(c).RIPPLE_ON_SURFACE
        return RippleDrawable(ColorStateList.valueOf(ripple), states, shape(c, Color.WHITE))
    }

    private fun Button.configure() {
        isAllCaps = false
        textSize = UiTokens.CONTROL_SP.toFloat()
        setTypeface(null, Typeface.BOLD)
        gravity = Gravity.CENTER
        minHeight = dp(context, UiTokens.CONTROL_MIN_HEIGHT)
        minimumHeight = minHeight
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(context, 12), dp(context, UiTokens.CONTROL_PADDING_Y), dp(context, 12), dp(context, UiTokens.CONTROL_PADDING_Y))
        stateListAnimator = null
        UiMotion.press(this)
        elevation = 0f
    }

    private fun actionButton(c: Context, title: String, primary: Boolean, action: () -> Unit) = Button(c).apply {
        configure()
        text = title
        setTextColor(ColorStateList(arrayOf(disabled, normal),
            intArrayOf(UiPalettes.from(c).DISABLED_TEXT, if (primary) UiPalettes.from(c).ON_ACTION else UiPalettes.from(c).ACTION)))
        background = surface(c, primary)
        setOnClickListener {
            performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            action()
        }
    }

    fun compactButton(c: Context, title: String, primary: Boolean = false, action: () -> Unit) =
        actionButton(c, title, primary, action).apply {
            textSize = 14f
            minHeight = dp(c, 48)
            minimumHeight = minHeight
            setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8))
        }

    /** Compact anchored help, with a scrollable body for large font settings. */
    fun helpButton(c: Context, title: String, message: String): Button {
        lateinit var button: Button
        button = compactButton(c, title) {
            val body = text(c, message, 14).apply {
                setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12))
                accessibilityPaneTitle = title
            }
            val scroll = ScrollView(c).apply {
                addView(body)
                background = shape(c, UiPalettes.from(c).SURFACE, UiPalettes.from(c).ACTION_OUTLINE, 1)
            }
            val width = minOf(dp(c, 320), (c.resources.displayMetrics.widthPixels - dp(c, 32)).coerceAtLeast(dp(c, 48)))
            val popup = PopupWindow(scroll, width, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                isOutsideTouchable = true
                elevation = dp(c, 4).toFloat()
            }
            val available = popup.getMaxAvailableHeight(button).coerceAtLeast(dp(c, 48))
            scroll.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(available, View.MeasureSpec.AT_MOST))
            popup.height = scroll.measuredHeight.coerceAtMost(available)
            popup.showAsDropDown(button, 0, dp(c, 4), Gravity.END)
        }
        button.contentDescription = "$title 도움말"
        button.textSize = 12f
        button.minWidth = dp(c, 48)
        button.minimumWidth = dp(c, 48)
        return button
    }

    /** The caller owns expanded state so it survives screen recreation. */
    fun disclosure(parent: LinearLayout, title: String, message: String, expanded: Boolean,
                   onExpandedChange: (Boolean) -> Unit) {
        val c = parent.context
        val body = text(c, message, 14)
        var open = expanded
        lateinit var trigger: Button
        fun update() {
            body.visibility = if (open) View.VISIBLE else View.GONE
            trigger.text = if (open) "$title 접기" else "$title 보기"
            if (android.os.Build.VERSION.SDK_INT >= 30) trigger.stateDescription = if (open) "펼쳐짐" else "접힘"
        }
        trigger = compactButton(c, title) {
            open = !open
            update()
            onExpandedChange(open)
        }
        parent.addView(trigger, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, UiTokens.SECTION_GAP) })
        parent.addView(body, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, 8) })
        update()
    }

    fun optionToggle(parent: LinearLayout, title: String, description: String, checked: Boolean,
                     onChanged: (Boolean) -> Unit) {
        val c = parent.context
        val palette = UiPalettes.from(c)
        // Derive every dimension from the same rounded pixel values so the end
        // clearance equals the top/bottom clearance at either checked position.
        val trackHeight = dp(c, 25)
        val gap = dp(c, 3)
        val thumbSize = trackHeight - 2 * gap
        val trackWidth = 2 * thumbSize + 2 * gap
        fun switchTrack(fill: Int, stroke: Int) = GradientDrawable().apply {
            setColor(fill)
            setStroke(dp(c, 1), stroke)
            cornerRadius = trackHeight / 2f
            setSize(trackWidth, trackHeight)
        }
        val track = object : StateListDrawable() {
            override fun getPadding(padding: Rect): Boolean {
                padding.set(gap, 0, gap, 0)
                return true
            }
        }.apply {
            addState(this@UiControls.checked, switchTrack(palette.ACTION_FILL, palette.ACTION_OUTLINE))
            addState(normal, switchTrack(palette.INFO_LINE, palette.ACTION_OUTLINE))
        }
        val thumb = SwitchThumb(thumbSize, 0f,
            if (UiPalettes.isDark(c)) 0xffe0e9e2.toInt() else Color.WHITE)
        val toggle = Switch(c).apply {
            text = "$title\n$description"
            textSize = 14f
            setTextColor(UiPalettes.from(c).TEXT)
            setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10))
            minimumHeight = dp(c, 64)
            switchPadding = dp(c, 12)
            background = shape(c, UiPalettes.from(c).SURFACE, UiPalettes.from(c).ACTION_OUTLINE, 1)
            thumbTintList = null
            trackTintList = null
            thumbDrawable = thumb
            trackDrawable = track
            showText = false
            thumbTextPadding = 0
            switchMinWidth = trackWidth
            splitTrack = false
            isChecked = checked
            UiMotion.press(this)
            setOnCheckedChangeListener { _, enabled ->
                performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                onChanged(enabled)
            }
        }
        parent.addView(toggle, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, 12) })
    }

    fun action(parent: LinearLayout, title: String, primary: Boolean, action: () -> Unit) {
        val params = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(parent.context, UiTokens.SECTION_GAP) }
        parent.addView(actionButton(parent.context, title, primary, action), params)
    }

    fun recordAction(parent: LinearLayout, title: String, selected: Boolean = false,
                     onLongClick: (() -> Unit)? = null, action: () -> Unit): Button {
        val c = parent.context
        val button = actionButton(c, title, false, action).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setTextColor(UiPalettes.from(c).INFO_TEXT)
            if (onLongClick != null) setOnLongClickListener { onLongClick(); true }
            setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10))
        }
        parent.addView(button, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, UiTokens.SECTION_GAP) })
        updateRecordAction(button, title, selected, false)
        return button
    }
    fun updateRecordAction(button: Button, title: String, selected: Boolean, selecting: Boolean) {
        val state = (if (selected) 1 else 0) + (if (selecting) 2 else 0)
        if (button.tag == state) return
        button.tag = state
        val c = button.context
        val palette = UiPalettes.from(c)
        button.isSelected = selected
        button.text = if (selected) "✓  $title" else if (selecting) title else "$title  ›"
        button.contentDescription = if (selecting) "$title, ${if (selected) "선택됨" else "선택되지 않음"}" else "$title, 기록 상세 보기, 길게 눌러 선택"
        button.background = RippleDrawable(ColorStateList.valueOf(palette.RIPPLE_ON_SURFACE),
            shape(c, if (selected) palette.SELECTED else palette.INFO_FILL,
                if (selected) palette.ACTION else palette.INFO_LINE, if (selected) 2 else 1), shape(c, Color.WHITE))
    }
    fun compactActions(parent: LinearLayout, primaryTitle: String, done: () -> Unit,
                       secondaryTitle: String, choose: () -> Unit, dense: Boolean = false) {
        parent.orientation = LinearLayout.HORIZONTAL
        fun add(title: String, primary: Boolean, action: () -> Unit) {
            val c = parent.context
            val button = actionButton(c, title, primary, action).apply {
                textSize = UiTokens.COMPACT_CONTROL_SP.toFloat()
                minHeight = dp(c, 48)
                minimumHeight = minHeight
                setPadding(dp(c, 12), dp(c, UiTokens.CONTROL_PADDING_Y), dp(c, 12), dp(c, UiTokens.CONTROL_PADDING_Y))
                if (dense) {
                    background = android.graphics.drawable.InsetDrawable(background, 0, dp(c, 4), 0, dp(c, 4))
                    textSize = 13f
                    setPadding(dp(c, 10), dp(c, 4), dp(c, 10), dp(c, 4))
                }
            }
            parent.addView(button, LinearLayout.LayoutParams(0, -2, 1f).apply {
                if (!primary) marginStart = dp(c, 8)
            })
        }
        add(primaryTitle, true, done)
        add(secondaryTitle, false, choose)
    }

    fun practiceCard(c: Context) = LinearLayout(c).apply { stylePracticeCard(this) }

    internal fun stylePracticeCard(card: LinearLayout) = with(card) {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8))
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_activated), shape(context, UiPalettes.from(context).SURFACE, UiPalettes.from(context).ACTION, 2))
            addState(normal, shape(context, UiPalettes.from(context).SURFACE, UiPalettes.from(context).INPUT_LINE, 1))
        }
    }

    fun text(c: Context, value: String, size: Int) = TextView(c).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(UiPalettes.from(c).TEXT)
        setPadding(0, dp(c, 8), 0, dp(c, 8))
    }

    fun encouragement(c: Context, value: String) = text(c, value, UiTokens.ENCOURAGEMENT_SP).apply {
        setTextColor(UiPalettes.from(c).SECONDARY_TEXT)
        setLineSpacing(dp(c, 3).toFloat(), 1f)
    }

    fun comparison(c: Context, heading: String, body: String) = info(c, "", 18).apply {
        text = SpannableString("$heading\n$body").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, heading.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun inlineLabel(c: Context, title: String) = TextView(c).apply {
        text = title
        textSize = UiTokens.LABEL_SP.toFloat()
        setTypeface(null, Typeface.BOLD)
        setTextColor(UiPalettes.from(c).INFO_TEXT)
        setPadding(dp(c, 4), dp(c, 4), dp(c, 4), dp(c, 4))
    }

    fun sectionTag(c: Context, title: String) = inlineLabel(c, title).apply {
        textSize = 12f
        gravity = Gravity.CENTER
        setPadding(dp(c, 8), dp(c, 3), dp(c, 8), dp(c, 3))
        background = shape(c, UiPalettes.from(c).INFO_FILL, UiPalettes.from(c).INFO_LINE, 1)
    }

    /** Align the start tangent of a short pill with the 16dp card/button corner. */
    fun sectionTagAligned(c: Context, title: String) = sectionTag(c, title).apply {
        addOnLayoutChangeListener { view, _, top, _, bottom, _, _, _, _ ->
            val radius = dp(c, UiTokens.RADIUS).toFloat()
            val pillRadius = minOf(radius, (bottom - top) / 2f)
            val inset = radius - pillRadius
            view.translationX = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) -inset else inset
        }
    }

    /** Shared labeled information section; caller supplies content and semantics. */
    fun labeledInfo(parent: LinearLayout, label: String, value: String, role: ResultCardRole? = null) {
        val c = parent.context
        parent.addView(sectionTagAligned(c, label), LinearLayout.LayoutParams(-2, -2).apply {
            topMargin = dp(c, UiTokens.LABEL_SECTION_GAP)
        })
        parent.addView(if (role == null) info(c, value, 18) else resultInfo(c, value, 18, role), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(c, UiTokens.LABEL_CONTENT_GAP)
        })
    }

    /** Highlight the differing middle region without modifying either input string. */
    fun labeledDifference(parent: LinearLayout, label: String, value: String, other: String, role: ResultCardRole) {
        val c = parent.context
        val a = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFC)
        val b = java.text.Normalizer.normalize(other, java.text.Normalizer.Form.NFC)
        val left = a.codePoints().toArray()
        val right = b.codePoints().toArray()
        var start = 0
        while (start < minOf(left.size, right.size) && left[start] == right[start]) start++
        var end = left.size
        var otherEnd = right.size
        while (end > start && otherEnd > start && left[end - 1] == right[otherEnd - 1]) { end--; otherEnd-- }
        parent.addView(sectionTagAligned(c, label), LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(c, UiTokens.LABEL_SECTION_GAP) })
        val card = resultInfo(c, if (a.isEmpty()) "입력 없음" else a, 18, role)
        if (start < end) {
            val from = a.offsetByCodePoints(0, start)
            val to = a.offsetByCodePoints(0, end)
            card.text = SpannableString(a).apply { setSpan(UnderlineSpan(), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, UiTokens.LABEL_CONTENT_GAP) })
    }
    fun brand(c: Context, title: String) = info(c, title, UiTokens.LABEL_SP).apply {
        setTypeface(null, Typeface.BOLD)
        gravity = Gravity.CENTER
    }

    fun info(c: Context, value: String, size: Int) = TextView(c).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(UiPalettes.from(c).INFO_TEXT)
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12))
        setLineSpacing(dp(c, 3).toFloat(), 1f)
        background = shape(c, UiPalettes.from(c).INFO_FILL, UiPalettes.from(c).INFO_LINE, 1)
    }

    fun resultInfo(c: Context, value: String, size: Int, role: ResultCardRole) = info(c, value, size).apply {
        val palette = UiPalettes.from(c)
        val fill = when (role) {
            ResultCardRole.SCORE -> palette.RESULT_SCORE_FILL
            ResultCardRole.METRIC -> palette.RESULT_METRIC_FILL
            ResultCardRole.PROMPT -> palette.RESULT_PROMPT_FILL
            ResultCardRole.INPUT -> palette.RESULT_INPUT_FILL
        }
        background = shape(c, fill, palette.INFO_LINE, 1)
    }

    fun editor(field: EditText) = with(field) {
        background = StateListDrawable().apply {
            addState(disabled, shape(context, UiPalettes.from(context).DISABLED_FILL, UiPalettes.from(context).INFO_LINE, 1))
            addState(focused, shape(context, UiPalettes.from(context).SURFACE, UiPalettes.from(context).ACTION, 2))
            addState(normal, shape(context, UiPalettes.from(context).SURFACE, UiPalettes.from(context).INPUT_LINE, 1))
        }
        backgroundTintList = null
        textSize = UiTokens.PRACTICE_SP.toFloat()
        setTextColor(UiPalettes.from(context).TEXT)
        setHintTextColor(UiPalettes.from(context).SECONDARY_TEXT)
        highlightColor = (UiPalettes.from(context).ACTION and 0x00ffffff) or 0x33000000
        gravity = Gravity.TOP or Gravity.START
        setPadding(dp(context, UiTokens.CARD_PADDING), dp(context, 12), dp(context, UiTokens.CARD_PADDING), dp(context, 12))
        setLineSpacing(dp(context, 4).toFloat(), 1f)
        minimumHeight = dp(context, 104)
        elevation = 0f
    }

    fun prompt(c: Context, value: String) = text(c, value, UiTokens.PRACTICE_SP).apply {
        setLineSpacing(dp(c, 4).toFloat(), 1f)
        setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12))
        background = shape(c, UiPalettes.from(c).SURFACE, UiPalettes.from(c).INFO_LINE, 1)
    }

    fun confirmDialog(
        c: Context,
        title: String,
        message: String,
        confirmTitle: String = "삭제",
        cancelTitle: String = "취소",
        onConfirm: () -> Unit
    ): android.app.Dialog {
        val dialog = android.app.Dialog(c)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        val palette = UiPalettes.from(c)
        val container = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            accessibilityPaneTitle = title
            setPadding(dp(c, 20), dp(c, 20), dp(c, 20), dp(c, 20))
            background = shape(c, palette.SURFACE, palette.ACTION_OUTLINE, 1)
        }
        container.addView(TextView(c).apply {
            text = title
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(palette.TEXT)
        }, LinearLayout.LayoutParams(-1, -2))
        container.addView(TextView(c).apply {
            text = message
            textSize = 14f
            setTextColor(palette.SECONDARY_TEXT)
            setPadding(0, dp(c, 8), 0, dp(c, 16))
        }, LinearLayout.LayoutParams(-1, -2))
        val actions = LinearLayout(c)
        compactActions(actions, confirmTitle, {
            dialog.dismiss()
            onConfirm()
        }, cancelTitle, {
            dialog.dismiss()
        }, dense = true)
        // Start keyboard focus on the non-destructive action.
        actions.getChildAt(1).requestFocus()
        container.addView(actions, LinearLayout.LayoutParams(-1, -2))
        dialog.setContentView(container)
        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            val width = (dp(c, 320)).coerceAtMost((c.resources.displayMetrics.widthPixels * 0.85).toInt())
            setLayout(width, -2)
        }
        return dialog
    }

    fun choices(parent: LinearLayout, options: Array<String>, selected: String, onSelected: (String) -> Unit) {
        val c = parent.context
        val group = RadioGroup(c).apply { orientation = LinearLayout.HORIZONTAL }
        for ((index, title) in options.withIndex()) {
            val button = RadioButton(c).apply {
                configure()
                id = View.generateViewId()
                buttonDrawable = null
                text = if (title == selected) "✓ $title" else title
                tag = title
                setTextColor(ColorStateList(arrayOf(disabled, checked, normal),
                    intArrayOf(UiPalettes.from(c).DISABLED_TEXT, UiPalettes.from(c).ACTION, UiPalettes.from(c).TEXT)))
                background = surface(c, false, true)
            }
            group.addView(button, RadioGroup.LayoutParams(0, -1, 1f).apply {
                if (index > 0) marginStart = dp(c, 8)
            })
            button.isChecked = title == selected
        }
        group.setOnCheckedChangeListener { g, id ->
            for (i in 0 until g.childCount) {
                val button = g.getChildAt(i) as RadioButton
                val title = button.tag as String
                button.text = if (button.isChecked) "✓ $title" else title
                if (button.id == id) onSelected(title)
            }
        }
        parent.addView(group, LinearLayout.LayoutParams(-1, -2))
    }
}




