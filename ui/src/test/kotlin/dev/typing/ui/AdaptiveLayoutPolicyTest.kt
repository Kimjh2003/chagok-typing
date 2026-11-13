package dev.typing.ui

import org.junit.Assert.*
import org.junit.Test

class AdaptiveLayoutPolicyTest {
    @Test fun boundariesAndFontScale() {
        assertFalse(AdaptiveLayoutPolicy.twoPane(599, 1f))
        assertTrue(AdaptiveLayoutPolicy.twoPane(600, 1.3f))
        assertFalse(AdaptiveLayoutPolicy.twoPane(600, 1.31f))
        assertTrue(AdaptiveLayoutPolicy.sideBySidePractice(600, 259, 1f))
        assertFalse(AdaptiveLayoutPolicy.sideBySidePractice(600, 260, 1f))
        assertFalse(AdaptiveLayoutPolicy.sideBySidePractice(599, 100, 1f))
    }
}
