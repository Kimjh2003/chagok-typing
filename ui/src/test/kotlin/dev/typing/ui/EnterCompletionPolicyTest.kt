package dev.typing.ui

import org.junit.Assert.*
import org.junit.Test

class EnterCompletionPolicyTest {
    @Test fun enabledAcceptsOnlyStandaloneEnterCommits() {
        for (text in arrayOf("\n", "\r", "\r\n")) assertTrue(text, EnterCompletionPolicy.isEnterCommit(true, text))
        for (text in arrayOf(null, "", "한", "한\n", "\n한", "a\nb", "\n\n", "  ", "😀")) {
            assertFalse(text, EnterCompletionPolicy.isEnterCommit(true, text))
        }
    }

    @Test fun disabledDoesNotInterpretTextAsSubmit() {
        for (text in arrayOf(null, "", "\n", "\r\n", "문단\n입력")) {
            assertFalse(EnterCompletionPolicy.isEnterCommit(false, text))
        }
    }

    @Test fun disabledDoesNotReadTheIncomingBuffer() {
        val text = object : CharSequence {
            override val length: Int get() = error("unnecessary buffer read")
            override fun get(index: Int): Char = error("unnecessary buffer read")
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("copy")
        }
        assertFalse(EnterCompletionPolicy.isEnterCommit(false, text))
    }
}
