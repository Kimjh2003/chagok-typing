package dev.typing.practice

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.TextView
import dev.typing.ui.SubmitEditText
import dev.typing.history.ResultStore
import dev.typing.history.SavedResult

/** Device-side native IME protocol regression checks; no external test runner required. */
class InputInstrumentation : Instrumentation() {
    private lateinit var activity: Activity
    private lateinit var editor: SubmitEditText
    private lateinit var connection: InputConnection
    private var submitted = 0
    private var snapshot = ""

    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    private fun setup(enabled: Boolean, value: String = "") {
        runOnMainSync {
            submitted = 0
            snapshot = ""
            editor = SubmitEditText(activity).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                completeOnEnter = enabled
                onSubmit = { submitted++; snapshot = text.toString() }
                setText(value)
                setSelection(length())
            }
            activity.setContentView(editor)
            editor.requestFocus()
            connection = requireNotNull(editor.onCreateInputConnection(EditorInfo()))
        }
        waitForIdleSync()
    }

    private fun expect(count: Int, text: String) {
        waitForIdleSync()
        runOnMainSync {
            check(submitted == count) { "submit count: $submitted != $count" }
            check(editor.text.toString() == text) { "text: ${editor.text} != $text" }
            if (count > 0) check(snapshot == text) { "composition snapshot lost" }
        }
    }

    override fun onStart() {
        var passed = 0
        try {
            activity = startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            runOnMainSync {
                val view = TextView(activity)
                activity.setContentView(view)
                dev.typing.motion.UiMotion.sceneTransition(view, 1, 100)
                dev.typing.motion.UiMotion.finishScene(view)
                check(view.alpha == 1f && view.translationX == 0f && view.animate().startDelay == 0L) {
                    "Interrupted scene left the view in an animated state"
                }
            }
            passed++
            runOnMainSync {
                val container = android.widget.LinearLayout(activity)
                var clicks = 0
                var longClicks = 0
                val row = dev.typing.ui.UiControls.recordAction(container, "테스트 기록", onLongClick = { longClicks++ }) { clicks++ }
                activity.setContentView(container)
                check(row.performLongClick() && longClicks == 1)
                dev.typing.ui.UiControls.updateRecordAction(row, "테스트 기록", true, true)
                check(row.isSelected && row.text.toString().startsWith("✓"))
                row.performClick()
                check(clicks == 1)
                dev.typing.ui.UiControls.updateRecordAction(row, "테스트 기록", false, true)
                check(!row.isSelected && row.text.toString() == "테스트 기록")
                dev.typing.ui.UiControls.updateRecordAction(row, "테스트 기록", false, false)
                check(row.text.toString().endsWith("›"))
            }
            passed++
            runOnMainSync {
                val prompt = "새로운 문장을 천천히 읽어 본다. 손끝의 움직임에 집중하며 글자를 이어 간다. 작은 변화가 쌓여 내일의 실력이 된다."
                val card = dev.typing.ui.PracticeInputView(activity, prompt, "제시문", "내 입력", "입력")
                card.metricsLabel.text = "한국어 · 문단 | 100초 · 999타/분"
                activity.setContentView(card)
                val promptCard = (card.getChildAt(0) as android.view.ViewGroup).getChildAt(1) as android.view.ViewGroup
                val inputCard = (card.getChildAt(1) as android.view.ViewGroup).getChildAt(1) as android.view.ViewGroup
                val shown = promptCard.getChildAt(0) as TextView
                val input = card.editor
                fun measureAndCheck() {
                    val width = (220 * activity.resources.displayMetrics.density).toInt()
                    card.measure(android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                        android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED))
                    card.layout(0, 0, width, card.measuredHeight)
                    check(shown.measuredHeight == input.measuredHeight) { "Unequal prompt/input heights" }
                    check(promptCard.measuredHeight == inputCard.measuredHeight) { "Unequal card heights" }
                    check(promptCard.measuredWidth == inputCard.measuredWidth) { "Unequal card widths" }
                    val layout = requireNotNull(input.layout)
                    val required = layout.getLineBottom(layout.lineCount - 1) + input.compoundPaddingTop + input.compoundPaddingBottom
                    check(input.measuredHeight >= required) { "Last input line clipped: ${input.measuredHeight} < $required" }
                    input.bringPointIntoView(input.length())
                    check(input.scrollY == 0) { "Input scrolled despite fitting its text: ${input.scrollY}, height=${input.height}, layout=${layout.height}" }
                }
                input.setText(prompt)
                measureAndCheck(); passed++
                input.setSelection(input.length())
                val ic = requireNotNull(input.onCreateInputConnection(EditorInfo()))
                ic.setComposingText(" 추가 입력으로 줄이 더 늘어나도 마지막 줄을 확인한다.", 1)
                measureAndCheck(); passed++
                input.setText("짧게")
                shown.textSize = 24f
                input.textSize = 24f
                measureAndCheck(); passed++
            }
            setup(false, "문단")
            runOnMainSync { connection.commitText("\n", 1) }
            expect(0, "문단\n"); passed++

            setup(true)
            runOnMainSync {
                connection.setComposingText("한", 1)
                connection.commitText("\n", 1)
            }
            expect(1, "한"); passed++

            if (Build.VERSION.SDK_INT >= 33) {
                setup(true)
                runOnMainSync {
                    connection.setComposingText("글", 1)
                    connection.commitText("\n", 1, null)
                }
                expect(1, "글"); passed++
            }

            setup(true, "완료")
            runOnMainSync { connection.performEditorAction(EditorInfo.IME_ACTION_DONE) }
            expect(1, "완료"); passed++

            setup(true, "키")
            runOnMainSync {
                editor.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                editor.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            expect(1, "키"); passed++

            setup(true)
            runOnMainSync { connection.commitText("문단\n연습", 1) }
            expect(0, "문단\n연습"); passed++

            setup(true, "중복")
            runOnMainSync {
                connection.commitText("\n", 1)
                connection.performEditorAction(EditorInfo.IME_ACTION_DONE)
            }
            expect(1, "중복"); passed++

            setup(true, "취소")
            runOnMainSync {
                connection.commitText("\n", 1)
                activity.setContentView(TextView(activity))
            }
            expect(0, "취소"); passed++

            val migrationName = "migration-test-${java.util.UUID.randomUUID()}.db"
            targetContext.openOrCreateDatabase(migrationName, 0, null).use { db ->
                db.execSQL("CREATE TABLE results (id TEXT PRIMARY KEY, completed_at INTEGER NOT NULL, language TEXT NOT NULL, mode TEXT NOT NULL, prompt TEXT NOT NULL, input TEXT NOT NULL, units INTEGER NOT NULL, elapsed_ms INTEGER NOT NULL, accuracy INTEGER NOT NULL, rate INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX results_date ON results(completed_at DESC)")
                db.execSQL("INSERT INTO results VALUES ('legacy', 1000, '한국어', '문장', '한글', '한글', 6, 1500, 100, 240)")
                db.version = 1
            }
            val migrated = ResultStore(targetContext, migrationName)
            val oldRecord = requireNotNull(awaitResult<SavedResult?> { migrated.find("legacy", it) })
            check(oldRecord.prompt == "한글" && oldRecord.accuracy == 100 && oldRecord.units == 6)
            check(awaitResult<List<dev.typing.history.HistorySummary>> { migrated.summaries("한국어", "문장", it) }.single().id == "legacy")
            migrated.close()
            passed++
            val databaseName = "history-test-${java.util.UUID.randomUUID()}.db"
            val store = ResultStore(targetContext, databaseName)
            check(awaitResult<List<SavedResult>> { store.list(it) }.isEmpty()); passed++
            val score = dev.typing.practice.core.FinalScore.calculate("한글!", "한글!", 7, 1500)
            val record = SavedResult("one", 1000, "한국어", "문장", score.prompt, score.input,
                score.units, score.elapsedMillis, score.accuracy, score.perMinute)
            awaitResult<Unit> { store.save(record, it) }
            awaitResult<Unit> { store.save(record, it) }
            val reopened = ResultStore(targetContext, databaseName)
            val first = awaitResult<List<SavedResult>> { reopened.list(it) }
            check(first.size == 1 && first.single().input == "한글!" && first.single().units == 7)
            check(first.single().elapsedMillis == 1500L && first.single().accuracy == 100 && first.single().perMinute == score.perMinute)
            passed++
            awaitResult<Unit> { store.save(record.copy(id = "two", completedAt = 2000, language = "English", mode = "단어"), it) }
            check(awaitResult<List<SavedResult>> { reopened.list(it) }.map { it.id } == listOf("two", "one")); passed++
            check(awaitResult<List<dev.typing.history.HistorySummary>> { reopened.summaries("한국어", it) }.map { it.id } == listOf("one"))
            check(awaitResult<List<dev.typing.history.HistorySummary>> { reopened.summaries("English", it) }.map { it.id } == listOf("two"))
            check(awaitResult<List<dev.typing.history.HistorySummary>> { reopened.summaries("missing", it) }.isEmpty()); passed++
            check(awaitResult<SavedResult?> { reopened.find("one", it) } == record)
            check(awaitResult<SavedResult?> { reopened.find("absent", it) } == null); passed++

            check(awaitResult<List<dev.typing.history.HistorySummary>> { reopened.summaries("한국어", "문장", it) }.map { it.id } == listOf("one"))
            check(awaitResult<List<dev.typing.history.HistorySummary>> { reopened.summaries("한국어", "단어", it) }.isEmpty())
            check(awaitResult<Boolean> { reopened.delete("two", it) })
            check(!awaitResult<Boolean> { reopened.delete("two", it) })
            check(awaitResult<SavedResult?> { reopened.find("one", it) } == record)
            check(awaitResult<SavedResult?> { reopened.find("two", it) } == null); passed++
            awaitResult<Unit> { store.save(record.copy(id = "batch-a"), it) }
            awaitResult<Unit> { store.save(record.copy(id = "batch-b", language = "English"), it) }
            check(awaitResult<Int> { reopened.deleteMany(listOf("batch-a", "batch-a", "missing"), it) } == 1)
            check(awaitResult<SavedResult?> { reopened.find("batch-a", it) } == null)
            check(awaitResult<SavedResult?> { reopened.find("batch-b", it) } != null)
            check(awaitResult<SavedResult?> { reopened.find("one", it) } == record)
            check(awaitResult<Int> { reopened.deleteMany(emptyList(), it) } == 0); passed++
            val shared = ResultStore.get(targetContext)
            awaitResult<List<SavedResult>> { shared.list(it) }
            runOnMainSync { activity.finish() }
            waitForIdleSync()
            activity = startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            check(shared === ResultStore.get(targetContext)) { "Activity must not close the process store" }
            // Verify that the process store still accepts reads without inserting test
            // records into the user's real history database.
            awaitResult<List<SavedResult>> { shared.list(it) }; passed++

            val drainingName = "close-test-${java.util.UUID.randomUUID()}.db"
            val draining = ResultStore(targetContext, drainingName)
            val committed = java.util.concurrent.CountDownLatch(1)
            var committedResult: Result<Unit>? = null
            draining.save(record) { committedResult = it; committed.countDown() }
            draining.close()
            draining.close()
            check(committed.await(30, java.util.concurrent.TimeUnit.SECONDS))
            requireNotNull(committedResult).getOrThrow()
            val readBack = ResultStore(targetContext, drainingName)
            check(awaitResult<SavedResult?> { readBack.find(record.id, it) } == record)
            readBack.close(); passed++
            val rejected = java.util.concurrent.CountDownLatch(1)
            var failureReported = false
            draining.list { failureReported = it.isFailure; rejected.countDown() }
            check(rejected.await(30, java.util.concurrent.TimeUnit.SECONDS) && failureReported); passed++
            store.close()
            reopened.close()

            finish(Activity.RESULT_OK, Bundle().apply { putString("stream", "PASS: $passed native IME/Enter and SQLite history checks\n") })
        } catch (error: Throwable) {
            finish(Activity.RESULT_CANCELED, Bundle().apply { putString("stream", "FAIL after $passed checks: $error\n") })
        } finally {
            if (::activity.isInitialized) runOnMainSync { activity.finish() }
        }
    }

    private fun <T> awaitResult(start: ((Result<T>) -> Unit) -> Unit): T {
        val latch = java.util.concurrent.CountDownLatch(1)
        var value: Result<T>? = null
        start { value = it; latch.countDown() }
        check(latch.await(30, java.util.concurrent.TimeUnit.SECONDS)) { "Database callback timed out" }
        return requireNotNull(value).getOrThrow()
    }
}





