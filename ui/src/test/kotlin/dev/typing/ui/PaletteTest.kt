package dev.typing.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class PaletteTest {
    private fun linear(value: Int): Double {
        val s = value / 255.0
        return if (s <= .04045) s / 12.92 else ((s + .055) / 1.055).pow(2.4)
    }
    private fun luminance(color: Int) = .2126 * linear(color shr 16 and 255) +
        .7152 * linear(color shr 8 and 255) + .0722 * linear(color and 255)
    private fun checkContrast(name: String, first: Int, second: Int, minimum: Double) {
        val a = luminance(first)
        val b = luminance(second)
        val ratio = (maxOf(a, b) + .05) / (minOf(a, b) + .05)
        assertTrue("$name $ratio", ratio >= minimum)
    }
    @Test fun sharedPaletteMeetsTargets() {
        checkContrast("Body", UiTokens.TEXT, UiTokens.CANVAS, 4.5)
        checkContrast("Input", UiTokens.TEXT, UiTokens.SURFACE, 4.5)
        checkContrast("Hint", UiTokens.SECONDARY_TEXT, UiTokens.SURFACE, 4.5)
        checkContrast("Information", UiTokens.INFO_TEXT, UiTokens.INFO_FILL, 4.5)
        checkContrast("Primary", UiTokens.SURFACE, UiTokens.ACTION, 4.5)
        checkContrast("Selection", UiTokens.ACTION, UiTokens.SELECTED, 4.5)
        checkContrast("Input boundary", UiTokens.INPUT_LINE, UiTokens.SURFACE, 3.0)
        checkContrast("Input focus", UiTokens.ACTION, UiTokens.SURFACE, 3.0)
    }

    @Test fun darkAndLightRoleContrast() {
        for (p in listOf(UiPalettes.light, UiPalettes.dark)) {
            checkContrast("Body", p.TEXT, p.CANVAS, 4.5)
            checkContrast("Input", p.TEXT, p.SURFACE, 4.5)
            checkContrast("Hint", p.SECONDARY_TEXT, p.SURFACE, 4.5)
            checkContrast("Information", p.INFO_TEXT, p.INFO_FILL, 4.5)
            checkContrast("Result score", p.INFO_TEXT, p.RESULT_SCORE_FILL, 4.5)
            checkContrast("Result metric", p.INFO_TEXT, p.RESULT_METRIC_FILL, 4.5)
            checkContrast("Result prompt", p.INFO_TEXT, p.RESULT_PROMPT_FILL, 4.5)
            checkContrast("Result input", p.INFO_TEXT, p.RESULT_INPUT_FILL, 4.5)
            checkContrast("Primary", p.ON_ACTION, p.ACTION_FILL, 4.5)
            checkContrast("Selection", p.ACTION, p.SELECTED, 4.5)
            checkContrast("Secondary action", p.ACTION, p.SURFACE, 4.5)
            checkContrast("Input boundary", p.INPUT_LINE, p.SURFACE, 3.0)
            checkContrast("Input focus", p.ACTION, p.SURFACE, 3.0)
        }
    }
}
