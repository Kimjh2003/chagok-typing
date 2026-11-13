package dev.typing.smoke

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo

/** Black-box release smoke test: no references to classes renamed/removed by R8. */
class ReleaseSmokeInstrumentation : Instrumentation() {
    private var themeSwitch = false
    private var viewport = false
    private var paragraph = false
    override fun onCreate(arguments: Bundle?) {
        themeSwitch = arguments?.getString("themeSwitch") == "true"
        viewport = arguments?.getString("viewport") == "true"
        paragraph = arguments?.getString("paragraph") == "true"
        super.onCreate(arguments); start()
    }

    private fun shell(command: String) {
        uiAutomation.executeShellCommand(command).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
    }

    private fun awaitCanvas(color: Int) {
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (SystemClock.uptimeMillis() < deadline) {
            val bitmap = uiAutomation.takeScreenshot()
            if (bitmap != null) {
                val matches = bitmap.getPixel(5, bitmap.height / 3) == color
                bitmap.recycle()
                if (matches) return
            }
            SystemClock.sleep(300)
        }
        error("Theme canvas did not update")
    }

    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        fun find(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (predicate(node)) return node
            for (i in 0 until node.childCount) find(node.getChild(i))?.let { return it }
            return null
        }
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (SystemClock.uptimeMillis() < deadline) {
            find(uiAutomation.rootInActiveWindow)?.let { return it }
            SystemClock.sleep(200)
        }
        error("Expected UI node missing")
    }

    private fun click(text: String) {
        check(awaitNode { it.text?.toString() == text && it.isClickable }
            .performAction(AccessibilityNodeInfo.ACTION_CLICK)) { "Click failed: $text" }
    }

    override fun onStart() {
        try {
            val info = uiAutomation.serviceInfo
            info.flags = info.flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            uiAutomation.serviceInfo = info
            val activity = startActivitySync(Intent().setClassName(targetContext.packageName,
                "dev.typing.practice.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            if (paragraph) click("문단")
            click("연습 시작")
            val input = awaitNode { it.isEditable }
            check(input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "한글 연습!")
            }))
            if (paragraph) {
                val keyboardDeadline = SystemClock.uptimeMillis() + 20_000
                while (uiAutomation.windows.none { it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD }) {
                    check(SystemClock.uptimeMillis() < keyboardDeadline) { "Paragraph keyboard did not appear" }
                    SystemClock.sleep(200)
                }
                SystemClock.sleep(400)
                val promptText = awaitNode {
                    it.text?.toString()?.startsWith("아침 햇살이 창문 사이로 들어온다.") == true
                }.text.toString()
                check(awaitNode { it.isEditable }.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, promptText)
                }))
                val scroller = awaitNode { it.className?.toString() == "android.widget.ScrollView" }
                repeat(4) { scroller.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) }
                SystemClock.sleep(400)
                val viewportBounds = android.graphics.Rect().also { scroller.getBoundsInScreen(it) }
                val summaryBounds = android.graphics.Rect().also { targetRect ->
                    awaitNode { node -> node.text?.toString()?.startsWith("한국어 · 문단 | ") == true }.getBoundsInScreen(targetRect)
                }
                check(summaryBounds.top >= viewportBounds.top && summaryBounds.bottom <= viewportBounds.bottom) {
                    "Paragraph summary is clipped above scrollable area: $summaryBounds / $viewportBounds"
                }
                uiAutomation.takeScreenshot()?.let { bitmap ->
                    java.io.File(requireNotNull(targetContext.getExternalFilesDir(null)), "paragraph-practice.png").outputStream().use {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
            }
            if (viewport) {
                val deadline = SystemClock.uptimeMillis() + 20_000
                while (uiAutomation.windows.none { it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD }) {
                    check(SystemClock.uptimeMillis() < deadline) { "Keyboard did not appear" }
                    SystemClock.sleep(200)
                }
                SystemClock.sleep(500)
                val before = android.graphics.Rect().also { rect ->
                    awaitNode { node -> node.isEditable && node.text?.toString() == "한글 연습!" }.getBoundsInScreen(rect)
                }
                shell("input keyevent 4")
                awaitNode { it.isVisibleToUser && it.text?.toString() == "입력칸을 누르면 이어서 연습할 수 있어" }
                SystemClock.sleep(500)
                val after = android.graphics.Rect().also { rect ->
                    awaitNode { node -> node.isEditable && node.text?.toString() == "한글 연습!" }.getBoundsInScreen(rect)
                }
                val limit = (48 * targetContext.resources.displayMetrics.density).toInt() + 2
                check(after.top - before.top in 0..limit) { "Excessive input movement: $before -> $after" }
            }
            if (themeSwitch) {
                val selection = awaitNode { it.isEditable && it.text?.toString() == "한글 연습!" }.textSelectionEnd
                shell("cmd uimode night yes")
                awaitCanvas(0xff101813.toInt())
                val restored = awaitNode { it.isEditable && it.text?.toString() == "한글 연습!" }
                check(restored.textSelectionEnd == selection) { "Selection lost after theme change" }
                uiAutomation.takeScreenshot()?.let { bitmap ->
                    java.io.File(requireNotNull(targetContext.getExternalFilesDir(null)), "dark-practice.png").outputStream().use {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
            }
            click("완료")
            awaitNode { it.text?.toString() == "기기에 저장했어" }
            click("연습 선택으로")
            click("연습 기록")
            val listScroll = awaitNode { it.className?.toString() == "android.widget.ScrollView" }
            listScroll.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            SystemClock.sleep(400)
            val record = awaitNode { it.isClickable && it.isVisibleToUser && it.text?.contains("타/분 · 정확도") == true }
            val recordTitle = record.text.toString()
            val beforeBounds = android.graphics.Rect().also { record.getBoundsInScreen(it) }
            check(record.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            awaitNode { it.text?.toString() == "기록 상세" }
            awaitNode { it.text?.toString()?.startsWith("내 입력\n") == true }
            click("기록 목록으로")
            SystemClock.sleep(400)
            val restoredRow = awaitNode { it.text?.toString() == recordTitle }
            val afterBounds = android.graphics.Rect().also { restoredRow.getBoundsInScreen(it) }
            check(beforeBounds == afterBounds) { "History scroll position changed: $beforeBounds -> $afterBounds" }
            click("English")
            awaitNode { it.text?.toString() == "아직 기록이 없어. 첫 연습을 시작해봐!" || it.text?.toString()?.endsWith("개의 기록") == true }
            click("한국어")
            awaitNode { it.text?.toString() == recordTitle }
            if (themeSwitch) {
                SystemClock.sleep(300)
                uiAutomation.takeScreenshot()?.let { bitmap ->
                    java.io.File(requireNotNull(targetContext.getExternalFilesDir(null)), "dark-history.png").outputStream().use {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
                shell("cmd uimode night no")
                awaitCanvas(0xfff6f8f4.toInt())
                awaitNode { it.text?.toString() == "연습 기록" }
            }
            runOnMainSync { activity.finish() }
            waitForIdleSync()
            val relaunched = startActivitySync(Intent().setClassName(targetContext.packageName,
                "dev.typing.practice.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            click("연습 기록")
            awaitNode { it.text?.toString() == recordTitle }
            runOnMainSync { relaunched.finish() }
            finish(Activity.RESULT_OK, Bundle().apply { putString("stream", "PASS: R8 release launch/input/complete/save/history smoke\n") })
        } catch (error: Throwable) {
            finish(Activity.RESULT_CANCELED, Bundle().apply { putString("stream", "FAIL: $error\n") })
        }
    }
}

