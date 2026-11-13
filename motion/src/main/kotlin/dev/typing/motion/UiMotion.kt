package dev.typing.motion

import dev.typing.common.Dimensions

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.PathInterpolator

/** Short native View animations; never delays an action or animates typed text. */
object UiMotion {
    private const val BACK_CANCEL_MS = 240L
    const val SURFACE_ENTER_MS = 260
    const val SURFACE_EXIT_MS = 220
    private const val SCENE_MS = 460L
    private const val ASYNC_REVEAL_MS = 260L
    private val settle = PathInterpolator(0.25f, 0.1f, 0.25f, 1f)
    private val detachReset = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) = Unit
        override fun onViewDetachedFromWindow(view: View) {
            view.animate().cancel()
            view.alpha = 1f
            view.translationY = 0f
            view.translationX = 0f
            view.removeOnAttachStateChangeListener(this)
        }
    }

    fun press(view: View) {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            view.stateListAnimator = null
            view.scaleX = 1f
            view.scaleY = 1f
            return
        }
        fun scale(value: Float, millis: Long) = ObjectAnimator.ofPropertyValuesHolder(view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, value),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, value)).apply {
            duration = millis
            interpolator = settle
        }
        view.stateListAnimator = StateListAnimator().apply {
            addState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed), scale(0.99f, 140))
            addState(intArrayOf(), scale(1f, 300))
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit
            override fun onViewDetachedFromWindow(v: View) {
                v.stateListAnimator?.jumpToCurrentState()
                v.scaleX = 1f
                v.scaleY = 1f
            }
        })
    }

    fun enter(view: View) = sceneTransition(view)

    fun cancelBack(view: View) {
        view.animate().cancel()
        if (!ValueAnimator.areAnimatorsEnabled()) {
            view.scaleX = 1f
            view.scaleY = 1f
            return
        }
        view.animate().scaleX(1f).scaleY(1f).setStartDelay(0)
            .setDuration(BACK_CANCEL_MS).setInterpolator(settle).start()
    }

    /** Reveal a single async result container without delaying its data or interaction. */
    fun revealAsync(view: View) {
        view.animate().cancel()
        view.alpha = 1f
        if (!ValueAnimator.areAnimatorsEnabled()) return
        view.alpha = 0.88f
        view.addOnAttachStateChangeListener(detachReset)
        view.animate().alpha(1f).setStartDelay(0).setDuration(ASYNC_REVEAL_MS).setInterpolator(settle)
            .withEndAction { view.removeOnAttachStateChangeListener(detachReset) }.start()
    }

    /** Finish an interrupted scene before another gesture takes control of the view. */
    fun finishScene(view: View) {
        view.animate().cancel()
        view.animate().setStartDelay(0)
        view.alpha = 1f
        view.translationX = 0f
        view.removeOnAttachStateChangeListener(detachReset)
    }

    /** Direction is logical: 1 opens a detail, -1 returns to its list, 0 fades in place. */
    fun sceneTransition(view: View, direction: Int = 0) {
        finishScene(view)
        if (!ValueAnimator.areAnimatorsEnabled()) return
        view.alpha = 0.84f
        val layoutSign = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) -1 else 1
        view.translationX = Dimensions.dp(view.context, 8).toFloat() * direction * layoutSign
        view.addOnAttachStateChangeListener(detachReset)
        view.animate().alpha(1f).translationX(0f).setStartDelay(0).setDuration(SCENE_MS).setInterpolator(settle)
            .withEndAction {
                view.animate().setStartDelay(0)
                view.removeOnAttachStateChangeListener(detachReset)
            }.start()
    }
}


