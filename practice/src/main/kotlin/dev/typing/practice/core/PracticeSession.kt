package dev.typing.practice.core

import java.util.function.LongSupplier

/** Main-thread state only. LongSupplier keeps clock reads primitive on the JVM. */
class PracticeSession(private val clock: LongSupplier, private var unitPolicy: TypingUnitPolicy = KoreanTypingUnitPolicy) {
    enum class State { READY, RUNNING, PAUSED, FINISHED }
    var state = State.READY
        private set
    var units = 0
        private set
    val hasStarted get() = state != State.READY
    private var accumulated = 0L
    private var startedAt = 0L
    private var removedUnits = 0
    private var result: FinalScore? = null

    /** Select a policy only between exercises or before restoring a saved session. */
    fun setUnitPolicy(policy: TypingUnitPolicy) {
        check(state == State.READY) { "Cannot change the unit policy during an exercise" }
        unitPolicy = policy
    }

    fun reset() {
        state = State.READY
        accumulated = 0
        startedAt = 0
        units = 0
        removedUnits = 0
        result = null
    }

    fun restore(text: CharSequence, elapsed: Long, hasStarted: Boolean, finished: Boolean) {
        units = unitPolicy.count(text, 0, text.length)
        removedUnits = 0
        result = null
        accumulated = elapsed.coerceAtLeast(0)
        state = when {
            finished -> State.FINISHED
            hasStarted -> State.PAUSED
            else -> State.READY
        }
    }

    fun beforeChange(text: CharSequence, start: Int, count: Int) {
        if (state != State.FINISHED) removedUnits = unitPolicy.count(text, start, start + count)
    }

    fun afterChange(text: CharSequence, start: Int, count: Int) {
        if (state == State.FINISHED) return
        units = (units + unitPolicy.count(text, start, start + count) - removedUnits).coerceAtLeast(0)
        removedUnits = 0
        if (state == State.READY && text.isNotEmpty()) {
            startedAt = clock.asLong
            state = State.RUNNING
        }
    }

    fun pause() {
        if (state != State.RUNNING) return
        accumulated += (clock.asLong - startedAt).coerceAtLeast(0)
        state = State.PAUSED
    }

    fun resume() {
        if (state != State.PAUSED) return
        startedAt = clock.asLong
        state = State.RUNNING
    }

    fun elapsedMillis() = accumulated +
        if (state == State.RUNNING) (clock.asLong - startedAt).coerceAtLeast(0) else 0

    fun finish(prompt: String, input: String): FinalScore {
        result?.let { return it }
        pause()
        state = State.FINISHED
        return FinalScore.calculate(prompt, input, units, accumulated).also { result = it }
    }
}
