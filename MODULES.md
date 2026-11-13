# Module boundaries — 0.20

This version supersedes the earlier three-module structure. Behavior and scoring are unchanged.

| Module | Responsibility | Project dependencies |
|---|---|---|
| :app | Scene assembly, navigation, native IME/lifecycle binding, option preferences, metrics formatting, mapping FinalScore to SavedResult | :ui, :motion, :practice, :history |
| :ui | Tokens, reusable native controls, prompt/editor card and Enter routing view | :common, :motion |
| :motion | UiMotion and ImeMotion; native property/insets animation only | :common |
| :common | Density conversion shared by UI and motion; no theme, app state or feature logic | none |
| :practice | Pure Kotlin/JVM session, text units, final score, prompt repository | none |
| :history | SavedResult DTO and private SQLite store, serial background I/O and main-thread callbacks | none |

No feature depends on :app. :practice is still Android-free. :history has no UI/motion/practice dependency: it stores completed SavedResult values supplied by the app; it never calculates scores. :motion does not import :ui, preventing a dependency cycle. Common is deliberately small; feature-specific code stays out of it.

Moved packages:
- dev.typing.ui.UiMotion / ImeMotion → dev.typing.motion
- dev.typing.practice.ResultStore / SavedResult → dev.typing.history
- shared density conversion → dev.typing.common.Dimensions (UiControls.dp remains a delegating facade)

At 0.20 history used the same application context, results.db filename, schema version 1, column names, UUID keys and ordering. Later 0.28 upgrades the schema to version 2 by adding an index; it retains the same records, database name and application ID.

Screen builders, MetricsFormatter and IME/lifecycle orchestration remain in :app. They are app-specific composition, not generic common utilities. Native editor Enter routing stays in :ui and emits a callback; it has no scoring/navigation dependency. Creating a module for every button or callback is intentionally avoided.

Build all Android lint checks with `gradlew lintDebug`; compile app and device tests with `:app:assembleDebug :app:assembleDebugAndroidTest`; run `:practice:test :app:testDebugUnitTest :ui:testDebugUnitTest`. Existing device history tests now use SavedResult, exercising the new storage boundary when run. Old architectural notes in other documents describe earlier versions; this document is authoritative for 0.20.

0.21 adds :smoke as a separate test-only Android APK. The production app still uses the same six-module graph; :smoke has no project dependencies and is never packaged into :app.

## 0.27.5 — recent feature placement
Shared section tags, labeled information cards, record controls and dark palettes belong to :ui. :app supplies screen content, navigation, history mapping and lifecycle; :practice owns prompts, text-derived units, session and JVM regression tests; :history owns saved results and storage; :motion owns effects. The UI module knows no score or database model. Existing signing and database remain unchanged.

## 0.27.13 — motion policy and computation
:motion owns the shared stagger duration and precomputed easing normalization. :app owns lifecycle, IME requests and navigation. R8 remains enabled with optimized defaults; no speculative unsafe rules added. Runtime benefit is not benchmarked.

## 0.27.27 — adaptive layout ownership
:ui owns AdaptiveLayoutPolicy and practice card layout. :app owns window/configuration observation, history selection/navigation and screen assembly; :history remains storage-only. Width thresholds are current heuristics, not fold/hinge detection. Tests cover threshold boundaries; device validation remains necessary.

## Recent history selection and stability changes
- :app owns selection state, filtering, confirmation, lifecycle and navigation. A single toggle updates only its row and selection controls; selecting all or changing modes updates visible rows.
- :ui owns reusable row styling and selection feedback. :history deletes a snapshot of IDs atomically on its worker using a reused bound statement; no feature or UI dependency is added.
- :practice owns bounded completion scoring and shared-prefix/suffix trimming. :motion owns cancellation of interrupted scene transitions. No change is made to the keyboard input hot path.
- These changes do not change the SQLite schema or rewrite old scores. Performance improvement is not benchmarked.
