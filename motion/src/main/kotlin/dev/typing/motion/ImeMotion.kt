package dev.typing.motion

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsAnimation

/** Follow the system IME clock, without mutating text or re-laying out on each frame. */
object ImeMotion {
    fun install(root: View, vararg targets: View, bottomInset: ((WindowInsets) -> Int)? = null) {
        if (Build.VERSION.SDK_INT >= 30) {
            // Final IME padding is applied before animation starts. Do not clip
            // translated children to that final boundary while the IME is moving.
            if (root is ViewGroup) root.clipToPadding = false
            Api30.install(root, targets, bottomInset)
        }
    }

    @TargetApi(30)
    private object Api30 {
        fun install(root: View, targets: Array<out View>, bottomInset: ((WindowInsets) -> Int)?) {
            root.setWindowInsetsAnimationCallback(object : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                private var active: WindowInsetsAnimation? = null

                override fun onPrepare(animation: WindowInsetsAnimation) {
                    if ((animation.typeMask and WindowInsets.Type.ime()) != 0) {
                        active = animation
                        targets.forEach {
                            it.animate().cancel()
                        }
                    }
                }

                override fun onProgress(insets: WindowInsets, runningAnimations: MutableList<WindowInsetsAnimation>): WindowInsets {
                    if (active != null) {
                        val bottom = bottomInset?.invoke(insets) ?: maxOf(insets.getInsets(WindowInsets.Type.ime()).bottom,
                            insets.getInsets(WindowInsets.Type.systemBars()).bottom)
                        // onApplyWindowInsets already laid out the final position.
                        val offset = if (ValueAnimator.areAnimatorsEnabled()) (root.paddingBottom - bottom).toFloat() else 0f
                        targets.forEach { it.translationY = offset }
                    }
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimation) {
                    if (active === animation) {
                        active = null
                        targets.forEach {
                            it.translationY = 0f
                        }
                    }
                }
            })
            root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) = Unit
                override fun onViewDetachedFromWindow(view: View) {
                    targets.forEach {
                        it.translationY = 0f
                    }
                    view.setWindowInsetsAnimationCallback(null)
                    view.removeOnAttachStateChangeListener(this)
                }
            })
        }
    }
}

