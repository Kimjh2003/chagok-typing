# 0.20 module update

Current boundaries and moved package paths are defined in MODULES.md at the project root. The six-module structure supersedes historical paths below.

# Local result history — 0.16

Completing an exercise automatically saves an immutable result in app-private SQLite storage. Returning to selection without completion does not save an exercise. No network or account is required. Uninstalling/clearing app data removes history; existing backup policy remains disabled.

Stored fields: completion UUID, device wall-clock timestamp, language, practice mode, prompt, entered text, text-derived units, active elapsed milliseconds, positional accuracy, and calculated units/minute. Existing scoring policy is unchanged.

`:app/ResultStore.kt` owns SQLite and a process-wide serial worker; callbacks return on the main thread. Input callbacks and the `:practice` and `:ui` modules do not perform persistence. SQL uses bound values. UUID primary keys make retries and restored result screens idempotent. Errors are surfaced on the result screen with a retry action. The result view itself does not trigger saves on every render.

Home → 연습 기록 shows the newest 100 saved exercises (all records remain in the database). Tap a record to expand/collapse units, elapsed time, prompt and entered text. Existing UiControls actions/info/comparison components and tokens are reused. History handles empty/loading/error states; stale callbacks are ignored after navigation. Back returns to selection.

A result-screen Bundle retains its UUID and completion timestamp, allowing safe retry after activity/process recreation. SQLite writes are asynchronous: a forced process kill before the queued transaction commits can lose the newest result. App relaunch retains committed records. Old app versions have no stored history to import.

Verification includes JVM scoring regression tests, Android lint, and device-side SQLite checks in InputInstrumentation: empty store, save/reopen with Korean text and score fields, duplicate suppression, and newest-first order, alongside existing native IME checks. Device tests use a separate randomly named database, never the real results database.

Validated on Android 36.1 emulator: 11 native IME/SQLite checks passed (8 IME + 3 history); 12 JVM tests and app lint passed. ARM64 physical-device history UI remains to be checked.


## 0.23 — language lists and separate record details
- Home → history has Korean/English choice tabs using the existing shared controls. Each language queries its own latest 100 records; filtering happens in SQLite before the limit.
- List rows contain only completion date/time, practice type, units/minute and accuracy. Prompt/input text is not loaded with the summary query.
- Tapping a UUID opens a separate in-app detail screen loaded by ID, including full prompt/input, units and active duration. Both system Back and 기록 목록으로 return to the originating language list.
- Each language retains its own scroll offset. Restore happens after asynchronous rows finish layout, not while loading. Language, record UUID and both list offsets survive Activity recreation via Bundle. Missing records and database read errors have explicit states; history content is read-only.
- Same SQLite schema/database and immutable scoring policy; no record migration or deletion. All screens use shared sage controls and context-selected light/dark palette.

0.23 validation: 15 JVM tests and release lint passed; 13 device IME/SQLite checks passed, including language-filtered summaries, exact ID lookup and missing ID. Optimized release black-box test passed on a smaller 1080x1400 emulator window: scroll list, open separate detail, return via list button with identical row bounds, switch language tabs. Existing data retained. A System UI ANR overlay was dismissed before the passing UI test; ARM64 physical review remains pending.

## 0.26.1 — store lifecycle and single scene entry
- Keep the Gemini updates (modern system bars/IME calls, predictive Back, haptics, catalog and logic refactors).
- MainActivity no longer closes the process-wide ResultStore when finishing. Closing an Activity and relaunching in the same process retains the live shared executor and database.
- Explicit close for independent stores is idempotent and serialized with task submission. Already accepted operations drain before SQLite closes on its worker. Requests after close report failure via the callback rather than throwing executor rejection on the caller thread. The singleton factory replaces an explicitly closed instance.
- Non-practice destinations invoke sceneTransition exactly once, after assembly. Remove the immediately-canceling second enter animation. Practice keeps only the encouragement entry animation; native editor/card/metrics are not animated by scene entry.
- Device regression coverage adds Activity finish/relaunch with the same shared store, save/read after relaunch, queued-write drain, double close and callback failure after close. Original input and history tests remain.

## 0.28 — filter and delete
History can filter by language and practice type before the 100-record limit, including older paragraph records. A selected record can be deleted only after an explicit confirmation. Storage remains private SQLite and existing records are unchanged. Accuracy rules for new results differ from older stored results.

## 0.28.1 — upgrade regression
An isolated device test creates a version-1 database with an existing record, upgrades through ResultStore and verifies the record and filtered query. This test requires an Android device or emulator; it has not been validated until actually run.

## Multi-selection
Long-press a row to enter selection mode, tap rows to toggle them, or select all currently displayed rows (at most 100 matching the active language and type). Confirmation shows the count; only the captured IDs are deleted together in a single transaction. Back or cancel exits selection. Changing filters clears selection; state survives Activity recreation. Selection updates do not reload the database. Device tests use isolated databases and cover duplicate/missing IDs and preservation of unselected records.

2026-10-29 verification: the native instrumentation completed 24 checks on the Pixel_10_Pro x86_64 Android emulator, including the v1-to-v2 migration, isolated batch-delete checks, row long-click/selection presentation, interrupted scene cleanup and simulated native input connections. This is not a physical ARM64 device or a third-party keyboard test. The release build was compiled with R8; this emulator run used the debug build.
