package dev.typing.practice

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.*
import android.text.*
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import dev.typing.ui.*
import dev.typing.motion.*
import dev.typing.history.*
import dev.typing.practice.core.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/** Scene assembly/lifecycle only; the native editor owns composition and selection. */
class MainActivity : Activity() {
    private var language = "한국어"
    private var mode = "문장"
    private var screen = "home"
    private var draft = ""
    private var prompt = ""
    private var selection = 0
    private var selectionEnd = 0
    private var sample = 0
    private var foreground = false
    private var restoreKeyboardOnResume = false
    private var deleteDialog: android.app.Dialog? = null
    private var enterCompletes = false
    private var backCallback: android.window.OnBackInvokedCallback? = null
    private val preferences by lazy { getSharedPreferences("typing_options", Context.MODE_PRIVATE) }
    private val session = PracticeSession(SystemClock::elapsedRealtime)
    private var finalScore: FinalScore? = null
    private val resultStore by lazy { ResultStore.get(this) }
    private var resultId = ""
    private var completedAt = 0L
    private var saveStatus: TextView? = null
    private var renderGeneration = 0
    private var renderedScreen: String? = null
    private var historyLanguage = "한국어"
    private var historyMode = "전체"
    private var historyId = ""
    private var historySelecting = false
    private val selectedHistoryIds = linkedSetOf<String>()
    private val historyPositions = mutableMapOf("한국어" to 0, "English" to 0)
    private var historyReady = false
    private var historyDetailPane: LinearLayout? = null
    private var historyDetailRequest = 0
    private lateinit var pageScroll: ScrollView
    private var metricsFormatter: MetricsFormatter? = null
    private var displayedMetrics: String? = null
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var editor: EditText? = null
    private var metrics: TextView? = null
    private var practiceHint: TextView? = null
    private var practiceHeader: View? = null
    private var practiceProgress: PracticeProgressView? = null
    private val progressUpdate = Runnable { updatePracticeProgress() }
    private val practiceViewport by lazy { PracticeViewport(dp(48), dp(320)) }
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            if (!foreground || screen != "practice" || metrics == null) return
            updateMetrics()
            updatePracticeProgress()
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(state: Bundle?) {
        val splashVisibleUntil = if (state == null) SystemClock.uptimeMillis() + SPLASH_MIN_VISIBLE_MS else 0L
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            SystemClock.uptimeMillis() < splashVisibleUntil
        }
        super.onCreate(state)
        enterCompletes = preferences.getBoolean("enter_completes", false)
        updateSystemBars()
        state?.let(::restore)
        render()
        restoreKeyboardOnResume = state != null && screen == "practice" && state.getBoolean("imeVisible")
        if (screen == "result" && resultId.isNotEmpty()) saveResult()
    }

    private companion object {
        // A slightly longer brand moment; never waits for storage or other background work.
        const val SPLASH_MIN_VISIBLE_MS = 650L
    }

    @Suppress("DEPRECATION")
    private fun updateSystemBars() {
        // PhoneWindow.getInsetsController requires its DecorView to exist.
        val decor = window.decorView
        val isDark = UiPalettes.isDark(this)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(UiPalettes.from(this).CANVAS))
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val appearance = if (isDark) 0 else (
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
            window.insetsController?.setSystemBarsAppearance(
                appearance,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            decor.systemUiVisibility = if (isDark) 0 else
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private fun showKeyboard(target: View, staged: Boolean = false) {
        target.requestFocus()
        val request = Runnable {
            if (editor === target && foreground && screen == "practice") {
                if (Build.VERSION.SDK_INT >= 30) {
                    window.insetsController?.show(WindowInsets.Type.ime())
                } else {
                    @Suppress("DEPRECATION")
                    inputMethod.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        if (staged && android.animation.ValueAnimator.areAnimatorsEnabled()) target.postOnAnimation(request)
        else target.post(request)
    }
    private fun restore(state: Bundle) {
        language = state.getString("language", "한국어") ?: "한국어"
        mode = state.getString("mode", "문장") ?: "문장"
        screen = state.getString("screen", "home") ?: "home"
        draft = state.getString("draft", "") ?: ""
        prompt = state.getString("prompt", "") ?: ""
        selection = state.getInt("selection")
        selectionEnd = state.getInt("selectionEnd", selection)
        sample = state.getInt("sample")
        resultId = state.getString("resultId", "") ?: ""
        completedAt = state.getLong("completedAt")
        historyLanguage = state.getString("historyLanguage", "한국어") ?: "한국어"
        historyId = state.getString("historyId", "") ?: ""
        historySelecting = state.getBoolean("historySelecting")
        selectedHistoryIds.addAll(state.getStringArrayList("selectedHistoryIds") ?: emptyList())
        historyMode = state.getString("historyMode", "전체") ?: "전체"
        historyPositions["한국어"] = state.getInt("historyKoreanY")
        historyPositions["English"] = state.getInt("historyEnglishY")
        practiceViewport.restore(state.getInt("practiceKeyboardBottom"),
            state.getInt("practiceWidth"), state.getInt("practiceHeight"))
        enterCompletes = state.getBoolean("enterCompletes", enterCompletes)
        val elapsed = state.getLong("elapsed")
        session.restore(draft, elapsed, state.getBoolean("hasStarted", draft.isNotEmpty() || elapsed > 0), screen == "result")
        if (screen == "result") finalScore = FinalScore.calculate(prompt, draft, session.units, elapsed)
    }

    private fun dp(value: Int) = UiControls.dp(this, value)
    private fun text(value: String, size: Int) = UiControls.text(this, value, size)
    private fun button(title: String, primary: Boolean, action: () -> Unit) = UiControls.action(content, title, primary, action)
    private fun LinearLayout.space(height: Int, weight: Float = 0f) =
        addView(Space(this@MainActivity), LinearLayout.LayoutParams(1, dp(height), weight))

    /** Shared width clamping, retaining local references across layout callbacks. */
    private fun boundedColumn(parent: ViewGroup, height: Int = -2, configure: LinearLayout.() -> Unit): LinearLayout {
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_HORIZONTAL }
        parent.addView(row, ViewGroup.LayoutParams(-1, -2))
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; configure() }
        row.addView(column, LinearLayout.LayoutParams(-1, height))
        row.addOnLayoutChangeListener { _, l, _, r, _, _, _, _, _ ->
            val width = minOf(r - l, dp(UiTokens.CONTENT_MAX_WIDTH))
            if (width > 0 && column.layoutParams.width != width) {
                column.layoutParams.width = width
                column.requestLayout()
            }
        }
        return column
    }

    private fun render() {
        val previousScreen = renderedScreen
        renderedScreen = screen
        val practicing = screen == "practice"
        historyDetailPane = null
        practiceHint = null
        practiceHeader = null
        practiceProgress = null
        renderGeneration++
        saveStatus = null
        updateBackNavigation()
        handler.removeCallbacks(ticker)
        editor = null
        metrics = null
        metricsFormatter = null
        displayedMetrics = null
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiPalettes.from(this@MainActivity).CANVAS)
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= 30) {
                    val bars = insets.getInsets(WindowInsets.Type.systemBars())
                    val ime = insets.getInsets(WindowInsets.Type.ime())
                    if (practicing) practiceViewport.update(view.width, view.height, ime.bottom, bars.bottom,
                        insets.isVisible(WindowInsets.Type.ime()))
                    val bottom = if (practicing) practiceViewport.bottom(ime.bottom, bars.bottom, bars.top)
                        else maxOf(bars.bottom, ime.bottom)
                    view.setPadding(bars.left, bars.top, bars.right, bottom)
                    if (practicing && view.height > 0) {
                        val usable = view.height - bars.top - bottom
                        val desired = if (usable < dp(320)) View.GONE else View.VISIBLE
                        practiceHeader?.let { if (it.visibility != desired) it.visibility = desired }
                    }
                    practiceHint?.visibility = if (insets.isVisible(WindowInsets.Type.ime())) View.GONE else View.VISIBLE
                } else {
                    practiceHint?.visibility = View.GONE
                    @Suppress("DEPRECATION")
                    view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                }
                insets
            }
        }
        root.addOnLayoutChangeListener { view, l, t, r, b, oldL, oldT, oldR, oldB ->
            if (r - l != oldR - oldL || b - t != oldB - oldT) view.requestApplyInsets()
            if (practicing) {
                val usableHeight = (b - t) - view.paddingTop - view.paddingBottom
                val desired = if (usableHeight < dp(320)) View.GONE else View.VISIBLE
                practiceHeader?.let { if (it.visibility != desired) it.visibility = desired }
            }
        }
        setContentView(root)
        root.requestApplyInsets()
        if (practicing) {
            val header = boundedColumn(root) {
                gravity = Gravity.START
                setPadding(dp(16), dp(8), dp(16), 0)
                addView(UiControls.encouragement(this@MainActivity, "서두르지 말고,\n너만의 리듬으로."), LinearLayout.LayoutParams(-2, -2))
                practiceHint = UiControls.inlineLabel(this@MainActivity, "입력칸을 누르면 이어서 연습할 수 있어").also {
                    it.setTypeface(null, Typeface.NORMAL)
                    addView(it, LinearLayout.LayoutParams(-1, -2))
                }
                UiMotion.enter(this)
            }
            practiceHeader = header.parent as View
            boundedColumn(root) {
                setPadding(dp(16), dp(8), dp(16), dp(8))
                practiceProgress = PracticeProgressView(this@MainActivity).also {
                    addView(it, LinearLayout.LayoutParams(dp(160), -2))
                }
            }
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        pageScroll = scroll
        root.addView(scroll, if (practicing) LinearLayout.LayoutParams(-1, 0, 1f) else LinearLayout.LayoutParams(-1, -1))
        if (screen == "history" && AdaptiveLayoutPolicy.twoPane(resources.configuration.screenWidthDp, resources.configuration.fontScale)) {
            root.removeView(scroll)
            val panes = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            root.addView(panes, LinearLayout.LayoutParams(-1, -1))
            panes.addView(scroll, LinearLayout.LayoutParams(0, -1, 1f))
            val detailsScroll = ScrollView(this).apply { isFillViewport = true }
            panes.addView(detailsScroll, LinearLayout.LayoutParams(0, -1, 1f))
            historyDetailPane = boundedColumn(detailsScroll) { setPadding(dp(12), dp(16), dp(16), dp(24)) }
            showHistoryDetailPane(historyId)
        }
        content = boundedColumn(scroll, if (practicing) -2 else -1) {
            if (practicing) {
                setPadding(dp(16), dp(8), dp(16), dp(8))
                gravity = Gravity.BOTTOM
            } else setPadding(dp(UiTokens.PAGE_GUTTER), dp(16), dp(UiTokens.PAGE_GUTTER), dp(24))
        }
        val column = content
        var practiceCardView: PracticeInputView? = null
        scroll.addOnLayoutChangeListener { _, _, t, _, b, _, _, _, _ ->
            if (column.minimumHeight != b - t) column.minimumHeight = b - t
            if (practicing) {
                val compact = b - t < dp(180)
                val padding = if (compact) 2 else 8
                if (column.paddingTop != dp(padding)) column.setPadding(dp(16), dp(padding), dp(16), dp(padding))
            }
            practiceCardView?.setCompactViewport(scroll.width - scroll.paddingLeft - scroll.paddingRight - dp(32), b - t)
        }
        when (screen) {
            "home" -> home()
            "practice" -> practiceCardView = practice()
            "history" -> history()
            "historyDetail" -> historyDetail()
            else -> results()
        }
        if (practicing) {
            val actions = boundedColumn(root) {
                setPadding(dp(16), 0, dp(16), dp(8))
                addView(LinearLayout(this@MainActivity).also {
                    UiControls.compactActions(it, "완료", ::finishExercise, "연습 선택으로", ::showHome, dense = true)
                }, LinearLayout.LayoutParams(-1, -2))
            }
            ImeMotion.install(root, scroll, actions.parent as View, bottomInset = { insets ->
                if (Build.VERSION.SDK_INT >= 30) {
                    val bars = insets.getInsets(WindowInsets.Type.systemBars())
                    practiceViewport.bottom(insets.getInsets(WindowInsets.Type.ime()).bottom, bars.bottom, bars.top)
                } else root.paddingBottom
            })
        }
        scheduleMetrics()
        if (previousScreen != screen && screen != "practice") {
            val direction = when {
                previousScreen == "home" && screen == "history" -> 1
                previousScreen == "history" && screen == "home" -> -1
                previousScreen == "history" && screen == "historyDetail" -> 1
                previousScreen == "historyDetail" && screen == "history" -> -1
                else -> 0
            }
            UiMotion.sceneTransition(content, direction)
        }
    }

    private fun home() {
        val wide = AdaptiveLayoutPolicy.twoPane(resources.configuration.screenWidthDp, resources.configuration.fontScale)
        val intro: LinearLayout
        val controls: LinearLayout
        if (wide) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            intro = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            controls = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            row.addView(intro, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(controls, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(16) })
            content.addView(row, LinearLayout.LayoutParams(-1, -2))
        } else {
            intro = content
            controls = content
        }
        val brandHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        brandHeader.addView(UiControls.brand(this, "차곡  /  TYPE AT YOUR PACE"),
            if (wide) LinearLayout.LayoutParams(-2, -2) else LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(8) })
        if (!wide) {
            brandHeader.addView(scoreHelpButton(), LinearLayout.LayoutParams(-2, -2))
        }
        intro.addView(brandHeader, LinearLayout.LayoutParams(-1, -2))
        intro.space(24, if (wide) 0f else 1f)
        intro.addView(text("손끝으로 쌓는\n오늘의 한 문장", UiTokens.TITLE_SP).apply { setTypeface(null, Typeface.BOLD) })
        intro.addView(text("평소 쓰던 키보드로, 편안하게 연습해봐.", 16))
        intro.space(24, if (wide) 0f else 2f)
        fun label(title: String) = controls.addView(UiControls.sectionTagAligned(this, title),
            LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(if (wide && controls.childCount == 0) 0 else UiTokens.LABEL_SECTION_GAP); bottomMargin = dp(UiTokens.LABEL_CONTENT_GAP) })
        if (wide) {
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            header.addView(UiControls.sectionTagAligned(this, "연습 언어"), LinearLayout.LayoutParams(-2, -2))
            header.addView(text("오프라인 · 로그인 없이", 13).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                setPadding(dp(8), 0, 0, 0)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            header.addView(scoreHelpButton(), LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(8) })
            controls.addView(header, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(UiTokens.LABEL_CONTENT_GAP) })
        } else label("연습 언어")
        UiControls.choices(controls, arrayOf("한국어", "English"), language) { language = it }
        if (mode == "문단") mode = "문장"
        label("연습 종류")
        UiControls.choices(controls, arrayOf("단어", "문장"), mode) { mode = it }
        UiControls.optionToggle(if (wide) intro else controls, "엔터로 완료", "켜면 엔터가 줄바꿈 대신 완료로 동작해", enterCompletes) {
            enterCompletes = it
            preferences.edit().putBoolean("enter_completes", it).apply()
        }
        controls.space(12)
        UiControls.action(controls, "연습 시작", true) { startExercise() }
        UiControls.action(controls, "연습 기록", false) { screen = "history"; render() }
        if (!wide) controls.addView(text("오프라인 · 로그인 없이", 13))
    }

    private fun sectionLabel(title: String) = content.addView(UiControls.sectionTagAligned(this, title),
        LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(UiTokens.LABEL_SECTION_GAP); bottomMargin = dp(UiTokens.LABEL_CONTENT_GAP) })

    private fun scoreHelpButton() = UiControls.helpButton(this, "타수 측정 기준",
        "현재 남은 입력을 한글 자모와 영문·숫자·공백·문장부호 기준으로 환산해. 복합 모음과 겹받침은 2타, 쌍자음은 1타로 계산해. 삭제한 입력은 포함하지 않으므로 실제 키보드 터치 횟수와 다를 수 있어. 타/분은 환산 입력량을 연습 시간으로 나눈 값이야.")

    private fun addInfo(view: TextView, compact: Boolean = false): TextView {
        content.addView(view, LinearLayout.LayoutParams(if (compact) -2 else -1, -2).apply {
            topMargin = dp(8); bottomMargin = dp(4)
        })
        return view
    }

    private fun info(value: String, size: Int, compact: Boolean = false) = addInfo(UiControls.info(this, value, size), compact)

    private fun startExercise(reusePrompt: Boolean = false) {
        session.reset()
        finalScore = null
        resultId = ""
        draft = ""
        selection = 0
        selectionEnd = 0
        if (!reusePrompt) prompt = PromptRepository.get(language, mode, sample)
        screen = "practice"
        render()
        editor?.let { showKeyboard(it, staged = true) }
    }

    private fun practice(): PracticeInputView {
        val card = PracticeInputView(this, prompt, "제시문", "내 입력", "여기에 입력해봐")
        metrics = card.metricsLabel
        content.addView(card, LinearLayout.LayoutParams(-1, -2))
        val input = card.editor
        editor = input
        input.completeOnEnter = enterCompletes
        input.onSubmit = { if (foreground && screen == "practice" && editor === input) finishExercise() }
        input.setText(draft)
        updatePracticeProgress()
        input.setSelection(selection.coerceIn(0, input.length()), selectionEnd.coerceIn(0, input.length()))
        metricsFormatter = MetricsFormatter(language, mode, compact = true)
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s != null && editor === input && screen == "practice") session.beforeChange(s, start, count)
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && editor === input && screen == "practice") {
                    val wasReady = session.state == PracticeSession.State.READY
                    session.afterChange(s, start, count)
                    if (wasReady && session.state == PracticeSession.State.RUNNING) scheduleMetrics()
                    updateMetrics()
                    practiceProgress?.let { progress ->
                        progress.removeCallbacks(progressUpdate)
                        progress.postOnAnimation(progressUpdate)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        updateMetrics()
        return card
    }

    private fun value() = editor?.text?.toString() ?: draft
    private fun updatePracticeProgress() {
        practiceProgress?.updateLength(editor?.length() ?: draft.length, prompt.length)
    }
    private fun updateMetrics() {
        val label = metrics ?: return
        val formatter = metricsFormatter ?: return
        val display = formatter.format(session.units, session.elapsedMillis())
        if (display != displayedMetrics) { label.text = display; displayedMetrics = display }
    }

    private fun scheduleMetrics() {
        handler.removeCallbacks(ticker)
        if (foreground && screen == "practice" && metrics != null && session.state == PracticeSession.State.RUNNING) handler.postDelayed(ticker, 200)
    }

    private val inputMethod get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private fun hideKeyboard() { editor?.let { inputMethod.hideSoftInputFromWindow(it.windowToken, 0) } }
    private fun showHome() {
        historySelecting = false
        selectedHistoryIds.clear()
        session.pause()
        hideKeyboard()
        screen = "home"
        render()
    }

    private fun updateBackNavigation() {
        if (Build.VERSION.SDK_INT >= 33) {
            backCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            backCallback = null
            if (screen != "home") {
                val callback = if (Build.VERSION.SDK_INT >= 34) {
                    object : android.window.OnBackAnimationCallback {
                        override fun onBackStarted(backEvent: android.window.BackEvent) {
                            if (::content.isInitialized) UiMotion.finishScene(content)
                        }
                        override fun onBackProgressed(backEvent: android.window.BackEvent) {
                            if (::content.isInitialized && android.animation.ValueAnimator.areAnimatorsEnabled()) {
                                val scale = 1f - (backEvent.progress * 0.05f)
                                content.scaleX = scale
                                content.scaleY = scale
                            }
                        }
                        override fun onBackInvoked() {
                            if (::content.isInitialized) {
                                content.scaleX = 1f
                                content.scaleY = 1f
                            }
                            navigateBack()
                        }
                        override fun onBackCancelled() {
                            if (::content.isInitialized) {
                                UiMotion.cancelBack(content)
                            }
                        }
                    }
                } else {
                    android.window.OnBackInvokedCallback { navigateBack() }
                }
                backCallback = callback
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
            }
        }
    }

    @Suppress("DEPRECATION")
    @android.annotation.SuppressLint("GestureBackNavigation") // Legacy only; API 33+ uses the dispatcher above.
    @Deprecated("Legacy navigation for Android 9–12")
    override fun onBackPressed() {
        if (screen != "home") navigateBack() else super.onBackPressed()
    }

    private fun navigateBack() {
        when {
            screen == "history" && historySelecting -> {
                rememberHistoryPosition()
                historySelecting = false
                selectedHistoryIds.clear()
                render()
            }
            screen == "historyDetail" -> showHistory()
            screen == "history" && historyDetailPane != null && historyId.isNotEmpty() -> {
                historyId = ""
                showHistoryDetailPane("")
            }
            else -> {
                if (screen == "history") rememberHistoryPosition()
                showHome()
            }
        }
    }
    private fun rememberHistoryPosition() {
        if (screen == "history" && historyReady) historyPositions[historyLanguage] = pageScroll.scrollY
    }

    private fun showHistory() { historyId = ""; screen = "history"; render() }

    override fun onDestroy() {
        deleteDialog?.dismiss()
        deleteDialog = null
        if (Build.VERSION.SDK_INT >= 33) {
            backCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
        }
        backCallback = null
        handler.removeCallbacks(ticker)
        // The shared store belongs to the application process, not this Activity.
        super.onDestroy()
    }

    private fun finishExercise() {
        val input = editor ?: return
        if (Build.VERSION.SDK_INT >= 30) {
            input.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            input.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        draft = value()
        selection = input.selectionStart
        selectionEnd = input.selectionEnd
        finalScore = session.finish(prompt, draft)
        resultId = java.util.UUID.randomUUID().toString()
        completedAt = System.currentTimeMillis()
        hideKeyboard()
        screen = "result"
        render()
        saveResult()
    }

    private fun saveResult() {
        val score = finalScore ?: return
        val id = resultId
        val label = saveStatus
        val record = SavedResult(id, completedAt, language, mode, score.prompt, score.input,
            score.units, score.elapsedMillis, score.accuracy, score.perMinute)
        resultStore.save(record) { result ->
            if (!isDestroyed && resultId == id && saveStatus === label) {
                label?.text = if (result.isSuccess) "기기에 저장했어" else "저장하지 못했어 · 눌러서 다시 시도"
                label?.setOnClickListener { if (result.isFailure) { label.text = "저장 중…"; saveResult() } }
            }
        }
    }

    private fun history() {
        historyReady = false
        info("연습 기록", 20, true)
        button("연습 선택으로", false) { rememberHistoryPosition(); showHome() }
        sectionLabel("연습 언어")
        UiControls.choices(content, arrayOf("한국어", "English"), historyLanguage) {
            if (it != historyLanguage) {
                rememberHistoryPosition()
                historyId = ""
                historySelecting = false
                selectedHistoryIds.clear()
                historyLanguage = it
                render()
            }
        }
        sectionLabel("연습 종류")
        UiControls.choices(content, arrayOf("전체", "단어", "문장", "문단"), historyMode) {
            if (it != historyMode) {
                historyMode = it
                historyId = ""
                historySelecting = false
                selectedHistoryIds.clear()
                historyPositions[historyLanguage] = 0
                render()
            }
        }
        content.addView(text("조건별 최근 100개 · 길게 눌러 여러 기록 선택", 13))
        val statusRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val status = text("기록을 불러오는 중…", 15)
        statusRow.addView(status, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(statusRow, LinearLayout.LayoutParams(-1, -2))
        val generation = renderGeneration
        resultStore.summaries(historyLanguage, historyMode.takeUnless { it == "전체" }) { result ->
            if (isDestroyed || screen != "history" || generation != renderGeneration) return@summaries
            val records = result.getOrNull()
            if (records != null) selectedHistoryIds.retainAll(records.map { it.id }.toSet())
            status.text = when {
                records == null -> "기록을 불러오지 못했어 · 눌러서 다시 시도"
                records.isEmpty() -> "아직 기록이 없어. 첫 연습을 시작해봐!"
                historySelecting -> "${records.size}개의 기록 · ${selectedHistoryIds.size}개 선택"
                else -> "${records.size}개의 기록"
            }
            status.setOnClickListener { if (records == null) { rememberHistoryPosition(); render() } }
            if (records != null) {
                val recordGroup = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                content.addView(recordGroup, LinearLayout.LayoutParams(-1, -2))
                recentAverages(records)?.let { avg ->
                    recordGroup.addView(UiControls.info(this, "현재 조건 최근 ${avg.count}개 평균 · ${avg.perMinute}타/분 · 일치율 ${avg.accuracy}%", 14),
                        LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.SECTION_GAP) })
                }
                val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                val delete = UiControls.compactButton(this, "삭제", false) { confirmDeleteSelected() }
                val rows = LinkedHashMap<String, Pair<Button, String>>()
                lateinit var selectHistoryRefresh: () -> Unit
                val select = UiControls.compactButton(this, "선택", false) {
                    historySelecting = true
                    historyId = ""
                    if (historyDetailPane != null) showHistoryDetailPane("")
                    // The selection controls and row states are refreshed below.
                    actions.visibility = View.VISIBLE
                    selectHistoryRefresh()
                }
                fun refreshSelection(changedId: String? = null) {
                    select.visibility = if (historySelecting || records.isEmpty()) View.GONE else View.VISIBLE
                    actions.visibility = if (historySelecting) View.VISIBLE else View.GONE
                    delete.isEnabled = selectedHistoryIds.isNotEmpty()
                    delete.text = "삭제 ${selectedHistoryIds.size}"
                    delete.contentDescription = "선택한 기록 ${selectedHistoryIds.size}개 삭제"
                    status.text = if (historySelecting) "${records.size}개의 기록 · ${selectedHistoryIds.size}개 선택"
                        else if (records.isEmpty()) "아직 기록이 없어. 첫 연습을 시작해봐!" else "${records.size}개의 기록"
                    if (changedId != null) {
                        rows[changedId]?.let { (button, title) ->
                            UiControls.updateRecordAction(button, title, changedId in selectedHistoryIds, historySelecting)
                        }
                    } else rows.forEach { (id, row) ->
                        UiControls.updateRecordAction(row.first, row.second, id in selectedHistoryIds, historySelecting)
                    }
                }
                selectHistoryRefresh = { refreshSelection() }
                val selectAll = UiControls.compactButton(this, "전체 선택", true) {
                    selectedHistoryIds.addAll(records.map { it.id })
                    refreshSelection()
                }.apply { contentDescription = "현재 표시된 기록 전체 선택" }
                val cancel = UiControls.compactButton(this, "취소", false) {
                    historySelecting = false
                    selectedHistoryIds.clear()
                    refreshSelection()
                }
                statusRow.addView(select, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(UiTokens.GAP) })
                listOf(selectAll, delete, cancel).forEachIndexed { index, control ->
                    actions.addView(control, LinearLayout.LayoutParams(0, -2, 1f).apply { if (index > 0) marginStart = dp(UiTokens.GAP) })
                }
                recordGroup.addView(actions, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.SECTION_GAP) })
                val date = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
                records.forEach { record ->
                    val title = "${date.format(java.util.Date(record.completedAt))} · ${record.mode}\n${record.perMinute} 타/분 · 정확도 ${record.accuracy}%"
                    val button = UiControls.recordAction(recordGroup, title,
                        selected = record.id in selectedHistoryIds, onLongClick = {
                            val wasSelecting = historySelecting
                            historySelecting = true
                            historyId = ""
                            if (historyDetailPane != null) showHistoryDetailPane("")
                            selectedHistoryIds.add(record.id)
                            refreshSelection(if (wasSelecting) record.id else null)
                        }) {
                        if (historySelecting) {
                            if (!selectedHistoryIds.add(record.id)) selectedHistoryIds.remove(record.id)
                            refreshSelection(record.id)
                        } else {
                            rememberHistoryPosition()
                            historyId = record.id
                            if (historyDetailPane != null) showHistoryDetailPane(historyId) else {
                                screen = "historyDetail"
                                render()
                            }
                        }
                    }
                    rows[record.id] = button to title
                }
                refreshSelection()
                if (records.isNotEmpty()) UiMotion.revealAsync(recordGroup)
            }
            val scroll = pageScroll
            scroll.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    scroll.viewTreeObserver.removeOnPreDrawListener(this)
                    if (generation == renderGeneration) {
                        scroll.scrollTo(0, historyPositions[historyLanguage] ?: 0)
                        historyReady = records != null
                    }
                    return true
                }
            })
        }
    }

    private fun confirmDeleteSelected() {
        if (deleteDialog?.isShowing == true || selectedHistoryIds.isEmpty()) return
        val ids = selectedHistoryIds.toSet()
        val dialog = UiControls.confirmDialog(this, "${ids.size}개 기록을 삭제할까?",
            "선택한 연습 기록 ${ids.size}개가 기기에서 삭제돼.") {
            resultStore.deleteMany(ids) { result ->
                if (isDestroyed) return@deleteMany
                if (result.isFailure) {
                    Toast.makeText(this, "기록을 삭제하지 못했어", Toast.LENGTH_SHORT).show()
                } else {
                    selectedHistoryIds.removeAll(ids)
                    if (historyId in ids) historyId = ""
                    if (screen == "history") {
                        rememberHistoryPosition()
                        if (selectedHistoryIds.isEmpty()) historySelecting = false
                        render()
                    }
                    Toast.makeText(this, "${result.getOrThrow()}개 기록을 삭제했어", Toast.LENGTH_SHORT).show()
                }
            }
        }
        deleteDialog = dialog
        dialog.setOnDismissListener { if (deleteDialog === dialog) deleteDialog = null }
        dialog.show()
    }

    private fun confirmDelete(id: String) {
        if (deleteDialog?.isShowing == true) return
        val dialog = UiControls.confirmDialog(
            this,
            title = "기록을 삭제할까?",
            message = "선택한 연습 기록이 기기에서 삭제돼.",
            confirmTitle = "삭제",
            cancelTitle = "취소"
        ) {
            resultStore.delete(id) { result ->
                if (isDestroyed) return@delete
                if (result.isFailure) {
                    Toast.makeText(this, "기록을 삭제하지 못했어", Toast.LENGTH_SHORT).show()
                } else if ((screen == "history" || screen == "historyDetail") && historyId == id) {
                    rememberHistoryPosition()
                    historyId = ""
                    screen = "history"
                    render()
                    Toast.makeText(this, if (result.getOrNull() == true) "기록을 삭제했어" else "이미 삭제된 기록이야", Toast.LENGTH_SHORT).show()
                } else if (screen == "history" && result.getOrNull() == true) {
                    // A filter or selection can change while the worker deletes the row.
                    // Refresh the currently visible list without clearing its selection.
                    rememberHistoryPosition()
                    render()
                }
            }
        }
        deleteDialog = dialog
        dialog.setOnDismissListener { if (deleteDialog === dialog) deleteDialog = null }
        dialog.show()
    }
    private fun showHistoryDetailPane(id: String) {
        val pane = historyDetailPane ?: return
        val request = ++historyDetailRequest
        pane.removeAllViews()
        pane.addView(UiControls.sectionTag(this, "기록 상세"))
        val status = text(if (id.isEmpty()) "기록을 선택하면 자세히 볼 수 있어" else "기록을 불러오는 중…", 15)
        pane.addView(status)
        if (id.isEmpty()) return
        resultStore.find(id) { result ->
            if (isDestroyed || screen != "history" || historyDetailPane !== pane || request != historyDetailRequest) return@find
            val record = result.getOrNull()
            if (record == null) {
                status.text = if (result.isFailure) "기록을 불러오지 못했어" else "기록을 찾을 수 없어"
                return@find
            }
            val date = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
            status.text = "${date.format(java.util.Date(record.completedAt))} · ${record.language} · ${record.mode}"
            val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            pane.addView(details, LinearLayout.LayoutParams(-1, -2))
            fun card(value: String, size: Int, role: ResultCardRole = ResultCardRole.METRIC) {
                details.addView(UiControls.resultInfo(this, value, size, role), LinearLayout.LayoutParams(-1, -2).apply {
                    topMargin = dp(UiTokens.SECTION_GAP)
                })
            }
            card("${record.perMinute} 타/분", UiTokens.SCORE_SP, ResultCardRole.SCORE)
            card("환산 입력량 ${record.units}타", 18)
            card("원문 일치율 ${record.accuracy}%  ·  ${record.elapsedMillis / 1000}초", 18)
            UiControls.labeledInfo(details, "제시문", record.prompt, ResultCardRole.PROMPT)
            UiControls.labeledInfo(details, "내 입력", record.input.ifEmpty { "입력 없음" }, ResultCardRole.INPUT)
            UiControls.action(details, "이 기록 삭제", false) { confirmDelete(record.id) }
            UiMotion.revealAsync(details)
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (screen == "history" || screen == "historyDetail" || screen == "result" || screen == "home") {
            rememberHistoryPosition()
            val wide = AdaptiveLayoutPolicy.twoPane(newConfig.screenWidthDp, newConfig.fontScale)
            screen = HistoryLayoutPolicy.screenAfterResize(screen, historyId, historyDetailPane != null, wide)
            render()
            if (screen == "result" && resultId.isNotEmpty()) saveResult()
        }
    }
    private fun historyDetail() {
        info("기록 상세", 20, true)
        button("기록 목록으로", false, ::showHistory)
        val status = text("기록을 불러오는 중…", 15)
        content.addView(status)
        val generation = renderGeneration
        resultStore.find(historyId) { result ->
            if (isDestroyed || screen != "historyDetail" || generation != renderGeneration) return@find
            val record = result.getOrNull()
            if (record == null) {
                status.text = if (result.isFailure) "기록을 불러오지 못했어 · 눌러서 다시 시도" else "기록을 찾을 수 없어"
                status.setOnClickListener { if (result.isFailure) render() }
                return@find
            }
            val date = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
            status.text = "${date.format(java.util.Date(record.completedAt))} · ${record.language} · ${record.mode}"
            addInfo(UiControls.resultInfo(this, "${record.perMinute} 타/분", UiTokens.SCORE_SP, ResultCardRole.SCORE)).setTypeface(null, Typeface.BOLD)
            addInfo(UiControls.resultInfo(this, "환산 입력량 ${record.units}타", 18, ResultCardRole.METRIC))
            addInfo(UiControls.resultInfo(this, "원문 일치율 ${record.accuracy}%  ·  ${record.elapsedMillis / 1000}초", 18, ResultCardRole.METRIC)).apply {
                (layoutParams as LinearLayout.LayoutParams).bottomMargin = 0
            }
            UiControls.labeledInfo(content, "제시문", record.prompt, ResultCardRole.PROMPT)
            UiControls.labeledInfo(content, "내 입력", record.input.ifEmpty { "입력 없음" }, ResultCardRole.INPUT)
            UiControls.action(content, "이 기록 삭제", false) { confirmDelete(record.id) }
        }
    }

    private fun results() {
        val result = requireNotNull(finalScore)
        info("연습 완료", 16, true)
        saveStatus = text("저장 중…", 13).also { content.addView(it) }
        content.addView(text("차곡차곡,\n한 번 더 성장했어.", 32))
        val wide = AdaptiveLayoutPolicy.twoPane(resources.configuration.screenWidthDp, resources.configuration.fontScale)
        if (wide) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.BOTTOM }
            val measurements = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            row.addView(measurements, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) })
            content.addView(row, LinearLayout.LayoutParams(-1, -2))
            fun metric(value: String, size: Int, bold: Boolean = false) {
                measurements.addView(UiControls.resultInfo(this, value, size, if (bold) ResultCardRole.SCORE else ResultCardRole.METRIC).apply {
                    if (bold) setTypeface(null, Typeface.BOLD)
                }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8); bottomMargin = dp(4) })
            }
            metric("${result.perMinute} 타/분", UiTokens.SCORE_SP, true)
            metric("환산 입력량 ${result.units}타", 18)
            metric("원문 일치율 ${result.accuracy}%  ·  ${result.elapsedMillis / 1000}초", 18)
            (measurements.getChildAt(measurements.childCount - 1).layoutParams as LinearLayout.LayoutParams).bottomMargin = 0
            UiControls.labeledDifference(texts, "제시문", result.prompt, result.input, ResultCardRole.PROMPT)
            UiControls.labeledDifference(texts, "내 입력", result.input, result.prompt, ResultCardRole.INPUT)
        } else {
            addInfo(UiControls.resultInfo(this, "${result.perMinute} 타/분", UiTokens.SCORE_SP, ResultCardRole.SCORE)).setTypeface(null, Typeface.BOLD)
            addInfo(UiControls.resultInfo(this, "환산 입력량 ${result.units}타", 18, ResultCardRole.METRIC))
            addInfo(UiControls.resultInfo(this, "원문 일치율 ${result.accuracy}%  ·  ${result.elapsedMillis / 1000}초", 18, ResultCardRole.METRIC)).apply {
                (layoutParams as LinearLayout.LayoutParams).bottomMargin = 0
            }
            UiControls.labeledDifference(content, "제시문", result.prompt, result.input, ResultCardRole.PROMPT)
            UiControls.labeledDifference(content, "내 입력", result.input, result.prompt, ResultCardRole.INPUT)
        }
        if (java.text.Normalizer.normalize(result.prompt, java.text.Normalizer.Form.NFC) !=
            java.text.Normalizer.normalize(result.input, java.text.Normalizer.Form.NFC))
            content.addView(text("밑줄은 두 입력이 달라지는 구간이야. 빠진 글자는 반대쪽에서 확인해줘.", 13))
        content.addView(LinearLayout(this).also {
            UiControls.compactActions(it, "다음 연습", { sample++; startExercise() }, "연습 선택으로", ::showHome)
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(UiTokens.SECTION_GAP) })
        UiControls.action(content, "같은 제시문 다시 연습", false) { startExercise(reusePrompt = true) }
    }

    override fun onResume() {
        super.onResume()
        foreground = true
        if (screen == "practice") session.resume()
        if (restoreKeyboardOnResume) {
            restoreKeyboardOnResume = false
            if (screen == "practice") editor?.let { showKeyboard(it) }
        }
        scheduleMetrics()
    }

    override fun onPause() {
        foreground = false
        handler.removeCallbacks(ticker)
        session.pause()
        super.onPause()
    }

    override fun onSaveInstanceState(out: Bundle) {
        rememberHistoryPosition()
        editor?.let { draft = value(); selection = it.selectionStart; selectionEnd = it.selectionEnd }
        with(out) {
            putString("language", language); putString("mode", mode); putString("screen", screen)
            putString("draft", draft); putString("prompt", prompt)
            putLong("elapsed", session.elapsedMillis()); putBoolean("hasStarted", session.hasStarted)
            putInt("selection", selection); putInt("selectionEnd", selectionEnd); putInt("sample", sample)
            putString("resultId", resultId); putLong("completedAt", completedAt)
            putString("historyLanguage", historyLanguage); putString("historyId", historyId)
            putString("historyMode", historyMode)
            putBoolean("historySelecting", historySelecting)
            putStringArrayList("selectedHistoryIds", ArrayList(selectedHistoryIds))
            putInt("practiceKeyboardBottom", practiceViewport.keyboardBottom)
            putInt("practiceWidth", practiceViewport.width); putInt("practiceHeight", practiceViewport.height)
            putInt("historyKoreanY", historyPositions["한국어"] ?: 0)
            putInt("historyEnglishY", historyPositions["English"] ?: 0)
            if (Build.VERSION.SDK_INT >= 30) putBoolean("imeVisible",
                root.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == true)
            putBoolean("enterCompletes", enterCompletes)
        }
        super.onSaveInstanceState(out)
    }
}

































