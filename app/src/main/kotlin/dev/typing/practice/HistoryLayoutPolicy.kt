package dev.typing.practice

/** Navigation state when history changes between one and two panes. */
object HistoryLayoutPolicy {
    fun screenAfterResize(screen: String, selectedId: String, wasTwoPane: Boolean, isTwoPane: Boolean): String = when {
        screen == "historyDetail" && isTwoPane -> "history"
        screen == "history" && wasTwoPane && !isTwoPane && selectedId.isNotEmpty() -> "historyDetail"
        else -> screen
    }
}
