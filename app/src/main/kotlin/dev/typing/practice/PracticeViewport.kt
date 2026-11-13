package dev.typing.practice

/** Layout-only memory of an observed docked IME, invalidated when window geometry changes. */
class PracticeViewport(private val travel: Int, private val minimumVisible: Int) {
    var keyboardBottom = 0
        private set
    var width = 0
        private set
    var height = 0
        private set

    fun restore(bottom: Int, oldWidth: Int, oldHeight: Int) {
        keyboardBottom = bottom.coerceAtLeast(0)
        width = oldWidth
        height = oldHeight
    }

    fun update(newWidth: Int, newHeight: Int, ime: Int, bar: Int, visible: Boolean) {
        if (newWidth > 0 && newHeight > 0) {
            if (width != newWidth || height != newHeight) keyboardBottom = 0
            width = newWidth
            height = newHeight
        }
        if (visible) keyboardBottom = if (ime > bar) ime else 0 // Floating IME reserves no guessed area.
    }

    fun bottom(ime: Int, bar: Int, top: Int): Int {
        val reserve = (keyboardBottom - travel).coerceIn(bar, (height - top - minimumVisible).coerceAtLeast(bar))
        return maxOf(ime, bar, reserve)
    }
}
