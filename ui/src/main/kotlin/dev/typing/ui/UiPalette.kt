package dev.typing.ui

import android.content.Context
import android.content.res.Configuration

/** Immutable role colors, chosen per context; never a mutable global theme. */
data class UiPalette(
    val CANVAS: Int,
    val SURFACE: Int,
    val TEXT: Int,
    val SECONDARY_TEXT: Int,
    val ACTION: Int,
    val SELECTED: Int,
    val ACTION_OUTLINE: Int,
    val INFO_FILL: Int,
    val INFO_LINE: Int,
    val INFO_TEXT: Int,
    val INPUT_LINE: Int,
    val RIPPLE_ON_ACTION: Int,
    val RIPPLE_ON_SURFACE: Int,
    val DISABLED_FILL: Int,
    val DISABLED_TEXT: Int,
    val ERROR: Int,
    val ERROR_FILL: Int,
    val ON_ACTION: Int,
    val ACTION_FILL: Int,
    val RESULT_SCORE_FILL: Int,
    val RESULT_METRIC_FILL: Int,
    val RESULT_PROMPT_FILL: Int,
    val RESULT_INPUT_FILL: Int,
)

object UiPalettes {
    val light = UiPalette(
        CANVAS = UiTokens.CANVAS,
        SURFACE = UiTokens.SURFACE,
        TEXT = UiTokens.TEXT,
        SECONDARY_TEXT = UiTokens.SECONDARY_TEXT,
        ACTION = UiTokens.ACTION,
        SELECTED = UiTokens.SELECTED,
        ACTION_OUTLINE = UiTokens.ACTION_OUTLINE,
        INFO_FILL = UiTokens.INFO_FILL,
        INFO_LINE = UiTokens.INFO_LINE,
        INFO_TEXT = UiTokens.INFO_TEXT,
        INPUT_LINE = UiTokens.INPUT_LINE,
        RIPPLE_ON_ACTION = UiTokens.RIPPLE_ON_ACTION,
        RIPPLE_ON_SURFACE = UiTokens.RIPPLE_ON_SURFACE,
        DISABLED_FILL = UiTokens.DISABLED_FILL,
        DISABLED_TEXT = UiTokens.DISABLED_TEXT,
        ERROR = UiTokens.ERROR,
        ERROR_FILL = UiTokens.ERROR_FILL,
        ON_ACTION = 0xffffffff.toInt(),
        ACTION_FILL = UiTokens.ACTION,
        RESULT_SCORE_FILL = 0xffdbeade.toInt(),
        RESULT_METRIC_FILL = 0xffedf3ec.toInt(),
        RESULT_PROMPT_FILL = 0xfff8faf6.toInt(),
        RESULT_INPUT_FILL = 0xffe8f1e7.toInt()
    )
    val dark = UiPalette(
        CANVAS = 0xff101813.toInt(),
        SURFACE = 0xff19251D.toInt(),
        TEXT = 0xffE4EDE6.toInt(),
        SECONDARY_TEXT = 0xffB3C2B7.toInt(),
        ACTION = 0xffC3D4C8.toInt(),
        SELECTED = 0xff26382D.toInt(),
        ACTION_OUTLINE = 0xff718A78.toInt(),
        INFO_FILL = 0xff1B2920.toInt(),
        INFO_LINE = 0xff617568.toInt(),
        INFO_TEXT = 0xffC5D7C9.toInt(),
        INPUT_LINE = 0xff819989.toInt(),
        RIPPLE_ON_ACTION = 0x33000000.toInt(),
        RIPPLE_ON_SURFACE = 0x33000000.toInt(),
        DISABLED_FILL = 0xff29332C.toInt(),
        DISABLED_TEXT = 0xff8C9B90.toInt(),
        ERROR = 0xffFFB4AB.toInt(),
        ERROR_FILL = 0xff462922.toInt(),
        ON_ACTION = 0xffF0F6F1.toInt(),
        ACTION_FILL = 0xff355B47.toInt(),
        RESULT_SCORE_FILL = 0xff2c4434.toInt(),
        RESULT_METRIC_FILL = 0xff202f24.toInt(),
        RESULT_PROMPT_FILL = 0xff19251d.toInt(),
        RESULT_INPUT_FILL = 0xff24372a.toInt()
    )
    fun isDark(context: Context) = context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    fun from(context: Context) = if (isDark(context)) dark else light
}

