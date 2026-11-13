package dev.typing.practice.core

import java.text.Normalizer
import kotlin.math.roundToInt

/** Completion-only text comparison; typing units and elapsed time remain independent. */
class FinalScore private constructor(
    val prompt: String, val input: String, val units: Int, val elapsedMillis: Long, val accuracy: Int,
) {
    val perMinute = TypingUnits.perMinute(units, elapsedMillis)
    companion object {
        fun calculate(prompt: String, input: String, units: Int, millis: Long): FinalScore {
            val left = Normalizer.normalize(prompt, Normalizer.Form.NFC).codePoints().toArray()
            val right = Normalizer.normalize(input, Normalizer.Form.NFC).codePoints().toArray()
            val length = maxOf(left.size, right.size)
            val accuracy = if (length == 0 || right.isEmpty()) 0 else {
                // Matching ends do not affect edit distance. Remove them before applying
                // the work limit so a small edit in a long paste is still measured exactly.
                var start = 0
                while (start < minOf(left.size, right.size) && left[start] == right[start]) start++
                var leftEnd = left.size
                var rightEnd = right.size
                while (leftEnd > start && rightEnd > start && left[leftEnd - 1] == right[rightEnd - 1]) {
                    leftEnd--; rightEnd--
                }
                val leftCount = leftEnd - start
                val rightCount = rightEnd - start
                val errors = if (leftCount == 0 || rightCount == 0) {
                    leftCount + rightCount
                } else if (leftCount.toLong() * rightCount <= 1_000_000L) {
                    distance(left.copyOfRange(start, leftEnd), right.copyOfRange(start, rightEnd))
                } else {
                    var mismatch = kotlin.math.abs(leftCount - rightCount)
                    for (i in 0 until minOf(leftCount, rightCount)) if (left[start + i] != right[start + i]) mismatch++
                    mismatch
                }
                (100.0 * (length - errors).coerceAtLeast(0) / length).roundToInt()
            }
            return FinalScore(prompt, input, units, millis, accuracy)
        }
        private fun distance(first: IntArray, second: IntArray): Int {
            val short = if (first.size <= second.size) first else second
            val long = if (first.size <= second.size) second else first
            var previous = IntArray(short.size + 1) { it }
            var current = IntArray(short.size + 1)
            for (i in long.indices) {
                current[0] = i + 1
                for (j in short.indices) {
                    current[j + 1] = minOf(previous[j + 1] + 1, current[j] + 1,
                        previous[j] + if (long[i] == short[j]) 0 else 1)
                }
                val swap = previous; previous = current; current = swap
            }
            return previous[short.size]
        }
    }
}
