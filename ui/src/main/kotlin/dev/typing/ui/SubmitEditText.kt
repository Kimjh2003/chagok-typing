package dev.typing.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.KeyEvent
import android.view.inputmethod.*
import android.widget.EditText

/** Native composition/rendering, with optional Enter action routing only. */
class SubmitEditText(context: Context) : EditText(context) {
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val textLayout = layout ?: return
        val available = height - compoundPaddingTop - compoundPaddingBottom
        if (textLayout.height <= available && scrollY != 0) scrollTo(scrollX, 0)
    }

    var completeOnEnter = false
        set(value) {
            field = value
            imeOptions = if (value) EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                else EditorInfo.IME_ACTION_NONE or EditorInfo.IME_FLAG_NO_ENTER_ACTION or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        }
    var onSubmit: (() -> Unit)? = null
    private var connection: InputConnection? = null
    private var pending = false
    private val submit = Runnable {
        pending = false
        onSubmit?.invoke()
    }

    private fun requestSubmit(): Boolean {
        if (!completeOnEnter || onSubmit == null) return false
        if (!pending) {
            pending = true
            connection?.finishComposingText() // Keep the visible final syllable; do not replace the buffer.
            post(submit) // Complete after the current IME/batch-edit callback returns.
        }
        return true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val base = super.onCreateInputConnection(outAttrs) ?: return null
        connection = base
        if (!completeOnEnter) return base
        outAttrs.imeOptions = (outAttrs.imeOptions and EditorInfo.IME_MASK_ACTION.inv()
            and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()) or EditorInfo.IME_ACTION_DONE
        return if (Build.VERSION.SDK_INT >= 33) Api33Connection(base) else EnterConnection(base)
    }

    override fun onEditorAction(actionCode: Int) {
        if (actionCode != EditorInfo.IME_ACTION_DONE || !requestSubmit()) super.onEditorAction(actionCode)
    }

    private fun handlesEnter(keyCode: Int, event: KeyEvent) = completeOnEnter && onSubmit != null &&
        !event.isShiftPressed && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
        handlesEnter(keyCode, event) || super.onKeyDown(keyCode, event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (!handlesEnter(keyCode, event)) return super.onKeyUp(keyCode, event)
        if (!event.isCanceled) requestSubmit()
        return true
    }

    private open inner class EnterConnection(base: InputConnection) : InputConnectionWrapper(base, false) {
        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean =
            (EnterCompletionPolicy.isEnterCommit(completeOnEnter, text) && requestSubmit()) ||
                super.commitText(text, newCursorPosition)
    }

    @TargetApi(33)
    private inner class Api33Connection(base: InputConnection) : EnterConnection(base) {
        override fun commitText(text: CharSequence, newCursorPosition: Int, textAttribute: TextAttribute?): Boolean =
            (EnterCompletionPolicy.isEnterCommit(completeOnEnter, text) && requestSubmit()) ||
                super.commitText(text, newCursorPosition, textAttribute)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(submit)
        pending = false
        connection = null
        super.onDetachedFromWindow()
    }
}

