package dev.typing.practice

import dev.typing.practice.core.TypingUnits

/** Creates text only when the visible numbers change; never reads the editor. */
class MetricsFormatter(language: String, mode: String, private val compact: Boolean = false) {
    private val prefix = "$language · $mode | "
    private var seconds = Long.MIN_VALUE
    private var units = -1
    private var rate = Int.MIN_VALUE
    private var displayed = ""
    private val buffer = StringBuilder(64)

    fun format(currentUnits: Int, millis: Long): String {
        val currentSeconds = millis / 1000
        val currentRate = if (millis < 1000) -1 else TypingUnits.perMinute(currentUnits, millis)
        if (seconds != currentSeconds || (!compact && units != currentUnits) || rate != currentRate) {
            seconds = currentSeconds
            units = currentUnits
            rate = currentRate
            buffer.setLength(0)
            buffer.append(prefix).append(seconds).append("초 · ")
            if (!compact) {
                buffer.append(units).append("타 · ")
            }
            if (rate < 0) {
                buffer.append("측정 중")
            } else {
                buffer.append(rate).append("타/분")
            }
            displayed = buffer.toString()
        }
        return displayed
    }
}
