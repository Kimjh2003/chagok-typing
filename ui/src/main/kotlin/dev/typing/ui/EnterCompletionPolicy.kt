package dev.typing.ui

/** A standalone Enter commit only; pasted text containing newlines is not a submit event. */
object EnterCompletionPolicy {
    fun isEnterCommit(enabled: Boolean, text: CharSequence?): Boolean = enabled && when {
        text == null -> false
        text.length == 1 -> text[0] == '\n' || text[0] == '\r'
        text.length == 2 -> text[0] == '\r' && text[1] == '\n'
        else -> false
    }
}
