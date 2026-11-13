# 0.20 module update

Current boundaries and moved package paths are defined in MODULES.md at the project root. The six-module structure supersedes historical paths below.

# Motion — 0.18

Shared implementation: :ui UiMotion.kt and UiControls.kt. Native Android Views/Canvas/HWUI rendering is retained; no bundled Skia engine, native library or custom render loop is added. This follows the existing platform drawing path, not a promise of a particular GPU backend on every Android release/device.

- Action/choice buttons and option toggle: state-driven 0.985 scale press (90ms), return to 1.0 (150ms); existing ripple retained. StateListAnimator preserves click, keyboard activation, cancellation and accessibility semantics without touch interception or delayed callbacks.
- Button/choice surface state: 120ms enter fade. Selection state and check mark update immediately; only the drawable appearance transitions.
- Home/result/history scene entry: 8dp translation plus alpha 0.94→1 over 180ms, decelerating cubic curve. This is a destination entry effect, not a two-scene crossfade or predictive-back preview.
- Practice only animates the encouragement header on scene creation. Editor, prompt card, summary and pinned actions do not move/fade due to text edits or statistics updates. IME hide/show remains system-inset driven.
- No width/height animations, full-screen blur, particles, forced software layers or permanent hardware layers. Detached scene property animations are canceled/reset. Input/scoring/persistence paths remain unchanged.
- When system animators are disabled at view creation, custom effects are skipped. Property animators otherwise follow system duration scale. Drawable fades are configured at creation and do not independently observe later setting changes.

References: https://developer.android.com/reference/android/view/ViewPropertyAnimator and https://developer.android.com/topic/performance/views/hardware-accel-views . Frame time and rendering cost must still be measured on ARM64 devices; smoothness is a design target, not an established measurement.

## 0.18.1 — softer motion tuning
Supersedes the 0.18 numeric motion values: press scale 0.99 over 110ms; release 220ms; cubic (0.25, 0.1, 0.25, 1). Destination entry moves 6dp with alpha 0.96→1 over 240ms. Surface state fades enter over 180ms and exit over 120ms. Actions still execute immediately. Typing, IME/inset behavior and the scope of animated views are unchanged. Device smoothness has not been measured; this is visual timing/amplitude tuning.

## 0.19 — synchronized IME motion
- :ui ImeMotion.install(root, targets) synchronizes the practice scroll area (summary/card) and pinned action container with system keyboard animation on Android 11/API30+.
- Root applies final system/IME padding through its existing inset listener. WindowInsetsAnimation.Callback applies only translationY = final padding minus current animated bottom inset; system duration/interpolation controls the motion, not an independent 240ms timer. It handles show/hide, resets on end/detach, and skips custom translation when animators are disabled.
- No alpha on the editor, text rewriting, scoring changes, custom keyboard heights, per-frame requestLayout, or interception of keyboard control. Native visibility/insets remain authoritative, including zero-inset floating keyboards.
- Android 9/10 keep existing adjustResize behavior without the new synchronized effect. On API30+ behavior still depends on the system/IME dispatching animation callbacks. Device frame smoothness and interrupted transitions remain to be validated physically.
- Reference: https://developer.android.com/develop/ui/views/layout/sw-keyboard

## 0.19.1 — avoid clipping against the final IME boundary
The user recording shows the moving card/actions cut off before the keyboard finishes appearing. ImeMotion now disables clipToPadding on the practice root (API30+): final IME padding still controls layout, but translated children may draw through that padding during the transition. Window bounds and the system keyboard still clip/occlude normally; internal ScrollView clipping stays enabled. This addresses the final-padding clipping mechanism without moving the editor ahead of the keyboard or guessing a keyboard height. System animation timing, final 8dp gap and all input state remain unchanged. Build/JVM/lint verification does not establish physical-device visual behavior; another device recording should confirm the improvement.


## 0.24 — reduce gaze travel when dismissing the keyboard
- On API30+, remember only a docked IME bottom actually observed in this window. When hidden, reserve the former IME area minus 48dp, keeping the prompt/editor and action row together near their prior position. Keep the existing 8dp gap within the group; do not move actions independently.
- Clamp the retained reserve to leave at least 320dp of vertical window space (minus system top inset). Narrow/short windows retain scrolling and may move further to preserve usable space. Reset old IME memory on actual window geometry changes; floating IME with zero bottom inset clears docked reserve.
- The motion module accepts an optional effective-bottom provider so animation progress uses the same reserved-space policy as final layout. This avoids a jump at animation end. No per-frame layout or text mutations are added.
- Hide the small upper hint while the IME is visible; when hidden show: 입력칸을 누르면 이어서 연습할 수 있어. Keep the existing encouragement. No new cards/decorations.
- Persist observed keyboard geometry with other practice Bundle state. API28/29 retain native adjustResize and omit the hint; the new limited-travel behavior requires API30 inset visibility/animation.
- Timing continues while practice remains visible with keyboard hidden, as before. Back still dismisses keyboard first, then returns to selection. Palette, storage and scoring are unchanged.

0.24 verification: 19 JVM tests and app/UI/motion release lint passed. R8 release black-box test on Android36.1 with real Gboard verified keyboard appearance, first Back showing the resume hint while preserving text, and editor bounds moving downward no more than48dp (+2px tolerance). Completion/save/history navigation also passed. An emulator System UI ANR overlay was dismissed before the passing run. API28/29 and physical ARM64 review remain pending.

## 0.26.1 — store lifecycle and single scene entry
- Keep the Gemini updates (modern system bars/IME calls, predictive Back, haptics, catalog and logic refactors).
- MainActivity no longer closes the process-wide ResultStore when finishing. Closing an Activity and relaunching in the same process retains the live shared executor and database.
- Explicit close for independent stores is idempotent and serialized with task submission. Already accepted operations drain before SQLite closes on its worker. Requests after close report failure via the callback rather than throwing executor rejection on the caller thread. The singleton factory replaces an explicitly closed instance.
- Non-practice destinations invoke sceneTransition exactly once, after assembly. Remove the immediately-canceling second enter animation. Practice keeps only the encouragement entry animation; native editor/card/metrics are not animated by scene entry.
- Device regression coverage adds Activity finish/relaunch with the same shared store, save/read after relaunch, queued-write drain, double close and callback failure after close. Original input and history tests remain.

## 0.27.7 — gentler motion
Shared press scales to 0.99 over 140ms and releases over 240ms; entry transitions run 300ms with a more gradual easing curve. Choice fades run 220/180ms. Actions are not delayed; text and metrics stay unanimated. IME motion continues to follow system insets and timing. System animation-off is respected.

## 0.27.8 — contextual scene entry
Non-practice scenes use a short in-place fade; opening history detail adds 8dp logical horizontal movement and returning reverses it with RTL support. Same-screen refreshes do not animate. Practice encouragement fades in place; editor remains unanimated. IME motion follows the system. This is an incoming fade, not a crossfade or shared-element transition; Android system back handling remains unchanged.

## 0.27.9 — relaxed scene timing
Scene entry uses 380ms and the One UI basic cubic-bezier (0.22, 0.25, 0, 1), with initial opacity 0.84. The movement remains contextual; this is still an incoming fade, not a crossfade. Button timing, input and system-controlled IME motion are unchanged. Motion remains optional through the system animation setting.

## 0.27.10 — damped scene easing
Scene entry uses a normalized critically damped timing curve, inspired by spring settling without overshoot; this is a fixed-duration interpolator, not a physical SpringAnimation or iOS motion implementation. Home/history and history/detail entries use opposite 8dp horizontal offsets for forward/back with RTL support. Other scenes fade in place; practice input remains unanimated, and IME timing follows system insets. Actions remain immediate and system animation settings are respected.

## 0.27.11 — practice entry continuity
The practice content column uses the same in-place 380ms incoming fade when navigating from home or a result, including next practice. Its layout and IME-driven translation are unchanged; entry animation does not gate focus, input, actions or keyboard. Same-screen rerenders do not animate. The keyboard and action row continue to follow system insets. System animation-off remains supported.

## 0.27.12 — staggered practice entry and completion
For new practice, focus is immediate and the IME show request is delayed 100ms when animations are enabled. After completion, scoring and save are immediate; result content starts fading in after 100ms while the IME begins hiding. The actual IME duration is system-controlled; these are overlapping staged motions, not strict sequencing. System animation-off removes both delays. Delayed requests verify the current editor and foreground state. Real-device testing across keyboards remains necessary.

## 0.28.1 — layer handling
Remove manually forced hardware layers from IME and scene transitions. Animation properties and timings remain; hardware rendering remains enabled normally. This avoids leaving a forced layer after cancellation and avoids forcing a cached layer on a changing editor. Performance difference has not been benchmarked.

## Press state cleanup
The shared press effect resets scale when a control detaches, and clears its state animator and scale if motion is disabled when configured. System duration scaling still controls running property animations. Scene and IME effects keep their existing cancellation and inset behavior; device verification of interruptions and changes to animation settings is still needed.

## Asynchronous history content
History list results and the wide history detail pane reveal each loaded group with a single 180ms alpha transition from 0.88 to 1. Data, actions, and scroll restoration are not delayed. Empty and error messages update immediately. Animation-disabled settings skip the effect; detaching the group resets its properties. Typing, scoring, saving, and system IME timing are unaffected. Narrow history detail retains its existing scene transition. Device smoothness is not yet measured.

## 0.29.3 — calmer transitions
Scene entries take 520ms with a gentler normalized damped curve; async history content fades in over 260ms. Button press stays at 140ms for immediate feedback, release takes 300ms, and surface fades take 260/220ms. Predictive Back cancellation returns over 220ms. Existing 100ms IME sequencing and the system-controlled keyboard animation remain unchanged. Actions and data are not deferred by the longer effects; disabled system animation still skips custom effects. Device feel and frame timing require physical verification.

## 0.30.2 — keyboard request aligned to a frame
On entry to practice, request the IME on the next animation frame instead of imposing a fixed 100ms delay. When animations are disabled and when restoring a session, post the request normally. The existing editor identity, foreground and screen guards remain. The keyboard and system decide when the IME actually appears, and the result-entry stagger is unchanged. Real-device timing and motion continuity remain unmeasured.

## Unified motion tuning

Scene entry (460ms), async history reveal (260ms), button press/release (140/300ms), and predictive-back cancellation (240ms) share the same gentle cubic easing. Surface fades remain 260/220ms. These durations are design choices, not Apple's published timings. Contextual history navigation retains its small RTL-aware horizontal offset; other scenes fade in place. Results no longer wait 100ms before appearing. Actions, loading, and persistence are not delayed.

The practice editor and metrics do not animate on entry or while typing. Only its encouragement header uses the scene fade; keyboard and input-card movement continue to follow system IME insets and timing. Animation-disabled settings skip property animations; drawable state fade settings are evaluated when controls are created. This does not establish frame performance or visual comfort on physical devices.

Reference: https://developer.apple.com/design/human-interface-guidelines/motion
