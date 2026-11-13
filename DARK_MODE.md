# System dark theme — 0.22

The app follows system UI_MODE_NIGHT. Shared :ui UiPalette/UiPalettes contains immutable context-selected light and dark role colors; there is no mutable global theme and no per-input theme work. UiTokens retains dimensions and original light color defaults for compatibility. All rendered controls use the selected palette, including primary on-color, secondary buttons, choices, hints, card outlines, dividers, statistics and history.

Dark theme: deep green canvas #101813, surface #19251D, main text #E4EDE6 and sage accent #A9D7BD with dark button text #103525. Light theme preserves the prior palette. The ripple mask remains white because it defines alpha/shape, not a visible surface.

App values/values-night themes select native light/dark widgets, cursor/accent, launch background and system-bar appearance. System keyboards remain user-controlled; their own settings determine keyboard colors. No app-owned keyboard or forced keyboard recoloring is added. Android 9 follows its available night configuration; Android 10+ exposes the usual system dark-theme setting.

uiMode changes use normal Activity recreation, not a manual scene rebuild that could discard state. Existing Bundle saves prompt, draft, selection, language/mode, timing, result identity and options. Restored practice requests input focus and, on API30+, restores known keyboard visibility. API28/29 rely on native system keyboard visibility restoration. Result resaving stays idempotent and stored history is unchanged.

Verification: text role contrast >=4.5:1, editor outlines/focus >=3:1 in light and dark palettes. Existing typing/golden tests remain. Standalone R8 smoke harness can run with `-e themeSwitch true` to switch dark during input, check text/selection survival, save/view history and return to light; screenshots are written to its external files folder. Theme screenshots validate layout/color appearance separately from JVM tests. ARM64 physical-device and Samsung IME appearance require user/device review.

Verified: 15 JVM tests and release lint passed. On Android 36.1 emulator, optimized release switched from light to visibly dark during practice, retained text/selection, completed/saved and expanded history, then returned to visibly light. Captured dark practice/history screenshots were visually inspected. A System UI ANR dialog and a test screenshot-path issue were resolved before the final passing run; no app behavior workaround was added.

## 0.23.1 — subdued dark actions
- User requested reducing the bright mint area first, deferring keyboard-hidden layout decisions. Layout/insets/scroll/navigation/motion timing are unchanged.
- Separate primary ACTION_FILL from the text/focus accent ACTION. Light fill remains the existing dark green. Dark primary fill is #223029 with soft text #D5DFD8 and a thin outline, replacing the large pale mint block.
- Dark text/focus accent is muted #A0B4A6, selected fill #26382D. Dark ripples use translucent black #33000000 rather than a pale/green overlay. Native dark cursor/accent is updated consistently.
- The panel has a dark tinted appearance using opaque solid fill; no blur, extra transparency layer or new offscreen rendering is introduced. Existing role contrast tests now check ON_ACTION against ACTION_FILL.
- Keyboard-hidden practice placement remains a pending design decision, not changed in this patch.

## 0.26.2 — clearer dark hierarchy
Primary actions use a muted green #355B47 with light text; informational panels use #1B2920, and the brighter muted accent #C3D4C8 clarifies selection and secondary actions. Light colors and layouts are unchanged. Solid fills and black ripples retain the subdued dark appearance.
