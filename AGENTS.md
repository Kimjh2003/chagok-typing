# Android typing practice app

## User preferences
- Speak casually in Korean.

## Confirmed build requirements
- Version 0.11 has three modules: :app -> :practice and :app -> :ui, with no practice/UI dependency. :practice is plain Kotlin/JVM (package dev.typing.practice.core) for PracticeSession, TypingUnits, FinalScore and PromptRepository. MetricsFormatter, IME/lifecycle/navigation/persistence stay in :app. This supersedes earlier app-local engine paths. See ARCHITECTURE.md.
- Version 0.10 migrates all app/UI/test sources to Kotlin and Gradle scripts to Kotlin DSL. Keep the native Views/IME approach and :app -> :ui module boundary. App sources are under app/src/main/kotlin/dev/typing/practice; UI under ui/src/main/kotlin/dev/typing/ui. Run Kotlin JUnit tests through Gradle, including the captured Java 0.9 golden-result comparison.
- Build an Android typing practice app for ARM64 devices.
- Support Android 9 Pie through Android 17; do not infer an upper installation restriction.
- Use the user's selected system keyboard (IME), rather than an app-owned keyboard.
- User requests Korean consonant/vowel-based typing speed instead of completed characters per minute. Version 0.3 implements text-derived units per active elapsed minute; compound vowels/final clusters count 2, doubled consonants count 1. These edge-case weights are implementation defaults, not user-confirmed universal standards. Physical IME touch counts and deleted historical input are not collected.
- Confirmed practice modes: words, sentences, paragraphs; both Korean and English. Keyboard layout is delegated to the selected IME.

## Confirmed UI direction
- Version 0.12 replaces the bottom action row: keep the input card immediately above the IME inset (8dp gap), put manual completion in its input header, and place return-to-selection above the practice summary/card. Home has a persisted optional Enter-to-complete toggle (default off to preserve paragraph newlines). See ENTER_COMPLETION.md. Keep keyboard policy/UI routing out of :practice.
- Common UI is the Android Library module outputs/TypingPractice/ui (package dev.typing.ui), consumed by :app. Keep shared tokens/components there; keep scoring, timing, prompts and navigation in :app. See ui/README.md for API boundaries. This supersedes earlier app-local UI source paths.
- Use outputs/TypingPractice/DESIGN_SYSTEM.md as the UI specification and UiTokens.kt / UiControls.kt as shared implementation. It supersedes older button notes. The spec explicitly distinguishes implemented components from future adaptive/accessibility work.
- Practice UI should be compact (0.6): one pinned summary for language/mode/time/units/rate, one shared prompt/input card, and two adjacent bottom actions ('완료', '연습 선택으로'). Use small inline labels instead of separate title pills in practice. Practice action minimum height is 48dp; home controls retain 56dp.
- 0.6.1 overrides pinned-top summary: group the summary and prompt/input card near the keyboard, bottom-aligned above the persistent action row. Put spare space above this group; retain scrolling for long text.
- Prioritize comfortable one-handed operation. Put primary actions within reach, adapting their position to available space and device posture.
- Move the home headline slightly lower and place language/practice choices near the bottom start action, as requested after real-device review.
- Adapt to current window dimensions and usable space across slab phones, tablets, foldables, and multi-window use; do not select layouts solely by device name or orientation.
- Preserve the current exercise, entered text, cursor/selection, and timing/scoring state across folding, unfolding, rotation, and resizing.
- Respect the actual system keyboard area, including varying keyboard heights and floating keyboards. Do not assume the lower half of a tabletop device belongs to the keyboard.
- Account for folds, hinges, occluded areas, and system insets. Keep related content and controls together and avoid unnecessary large movements.
- Keep text clear with comfortable line spacing, adjustable text size, and a readable maximum line width.
- Use calm solid backgrounds, rounded cards, and one accent color. Keep motion restrained and purposeful.
- Keep a cohesive green palette (0.7): noninteractive labels/status/results use very pale sage neutrals; selected items use a distinctly stronger sage fill, dark green border and check. Primary actions use dark green. Earlier blue-gray palette is superseded. See DESIGN_SYSTEM.md and UiTokens.kt.
- Give home and results screens comfortable spacing; prioritize the prompt and text input on the practice screen, reducing large headings and excess whitespace while the keyboard is open.
- Use One UI reachability, HyperOS typography/transition ideas, HarmonyOS adaptive continuity, and Apple's iPhone Duo adaptive-layout principles as design references. Implement Android behavior through Android APIs and validate it on Android.

These are approved design requirements for this build, not a request to add unrelated features.

## Input and rendering performance
- Version 0.9 separates PracticeSession (state/time/incremental units), MetricsFormatter (cached visible statistics), and FinalScore (completion-only immutable score). MainActivity owns IME callbacks/lifecycle/scheduling. Consult outputs/TypingPractice/INPUT_LOGIC.md. Preserve the changed-range-only input path; do not add scoring, text snapshots, persistence or display formatting to each input callback.
- Use the shared button rules in outputs/TypingPractice/BUTTONS.md and UiControls.kt for action and choice controls.
- Keep the synchronous input-to-display path minimal; do not make visible text wait for scoring, persistence, or heavy analytics.
- Preserve IME composition and selection; optimize correctness and latency together.
- Minimize unnecessary allocations, full-text restyling, broad UI updates, redundant layout, and graphical overdraw.
- Measure release-build latency distributions and CPU/GPU frame costs before choosing low-level optimizations. Do not promise zero delay on every device.
- Consult outputs/input-performance-plan.md for the collected layer-by-layer design and measurement proposal. Numerical budgets and native-code optimization remain subject to evidence.

- Version 0.13 overrides 0.12 practice action placement: pin equal-width 48dp-minimum 완료 / 연습 선택으로 buttons below the scroll area and above the IME inset (8dp gap). Remove header completion and upper return buttons. Keep existing Enter-to-complete preference and routing. Current prompts contain no explicit newlines.

- Version 0.14: Back from practice/results returns to selection, including IME-open practice; home keeps system Back. Shared actions/choices now use 48dp minimum, 15sp and 8dp vertical padding; practice actions use 14sp. Supersedes older home 56dp requirement.

- Version 0.15: match prompt/input body heights to actual wrapped prompt height (48dp minimum), identical typography/padding; overflow scrolls in the native editor. Practice encouragement is now 20sp. Supersedes fixed editor minimum lines/64dp height.

- Version 0.16: completed results auto-save locally through :app ResultStore (private SQLite, background serial worker, idempotent completion UUID). Home history shows latest 100, tap to expand using shared UI; no input-path persistence. See RESULT_HISTORY.md.

- Version 0.16.1 overrides 0.14 Back routing: dismiss IME first, keep practice state and let actions follow insets to screen bottom; subsequent Back returns to selection. Use default-priority modern callback; remove pre-IME interception.

- Version 0.17 simplifies TypingUnits to BMP/UTF-16 changed-range reads and numeric consonant-cluster branches; preserve prior weights and incremental session flow. Supplementary characters count zero. See INPUT_LOGIC.md; no measured latency improvement claimed.

- Version 0.18: shared :ui UiMotion handles brief button press/selection and scene-entry effects on native Android rendering. Keep editor/card/metrics unanimated during practice. No bundled Skia renderer. See MOTION.md.

- Version 0.18.1 softens shared motion: 0.99 press/110ms, release220ms, scene6dp/240ms, state fades180/120ms. See MOTION.md. No input-path animation or delayed actions.

- Version 0.19: :ui ImeMotion synchronizes practice scroll/card and action row with system IME animation on API30+, using translation only. API28/29 retain adjustResize. See MOTION.md.

- Version 0.19.1: ImeMotion disables practice root clipToPadding on API30+ so final IME padding does not prematurely cut off translated card/actions during keyboard animation. Keep scroll/window clipping and system timing.

- Version 0.20 supersedes the three-module structure: :app -> :ui/:motion/:practice/:history; :ui -> :common/:motion; :motion -> :common. :practice stays pure Kotlin/JVM. :history owns SavedResult/ResultStore (package dev.typing.history) with no practice/UI dependency; app maps FinalScore. Motion moved to dev.typing.motion. Common only holds truly shared density conversion. Same DB/schema/app ID; see MODULES.md.

- Version 0.21: :app release enables R8 code optimization/minification and resource shrinking with proguard-android-optimize.txt. Debug remains unminified. Local test release signing requires -PlocalTestingSigning=true; archive exact mapping.txt per APK. Do not add broad keep rules. See R8.md.

- :smoke is a test-only standalone APK for black-box R8 release verification; it is outside the six-module production dependency graph and bundles its own test runtime.

- Version 0.22 follows system dark/light mode with :ui immutable UiPalettes selected per context; native values-night theme handles widget accents/system bars. Preserve session/text/selection through uiMode recreation and existing history. See DARK_MODE.md.

- Version 0.23: history has Korean/English tabs, SQL-filtered recent100 summaries (date/time, mode, rate, accuracy), separate UUID-loaded detail screen and per-language restored scroll offsets after list layout. Detail Back returns to history; preserve language/record/offsets through recreation. Same DB/schema and shared light/dark UI.

- Version 0.23.1: dark primary uses separate ACTION_FILL #223029 and ON_ACTION #D5DFD8 with thin outline; muted accent #A0B4A6, black translucent ripples. Light palette unchanged. Defer keyboard-hidden layout redesign; current placement remains. See DARK_MODE.md.

- Version 0.24: API30+ practice retains observed docked-IME space minus48dp when hidden, keeping card/actions together with limited gaze travel. Clamp for usable window space; reset on geometry changes/floating keyboard. Show small resume hint only when hidden. ImeMotion uses same effective-bottom policy. API28/29 retain native resizing. See MOTION.md.

- Version 0.24.1: practice prompt/editor and encouragement18sp; prompt/editor3dp line spacing. Practice content uses natural WRAP_CONTENT height plus viewport minimum so oversized paragraphs scroll from a reachable top. Keep equal body heights, system font scale, pinned actions and limited IME-hide travel.

- Version 0.24.2: prompt/editor share line-breaking and typography; measure both natural native layouts and use their maximum height for both bodies, including extra/composing text. Outer scroll handles long content. Results place 다음 연습 / 연습 선택으로 side by side using shared compactActions.

- Version 0.25: PracticeInputView groups two independent equal-sized prompt/input cards with8dp gap; share natural maximum body height and total card heights, same padding/width. Highlight only input card on focus. Supersedes shared-border/divider UI; retain outer scroll and native input behavior.

- Version 0.26: live metrics move into right-aligned prompt header (language/mode/time/rate only); separate pill removed. Practice cards denser but equal; actions visually40dp with48dp touch targets. Reveal whole input card if it fits viewport. See DESIGN_SYSTEM.md.

- Version 0.26.1: process ResultStore outlives Activity finish; explicit close drains queued work on its worker and reports post-close failures via callback. Only one non-practice scene entry effect; practice editor remains unanimated on entry. Preserve Gemini's other patches.

- 0.26.1 additionally initializes window.decorView before querying the window inset controller to prevent cold-start NPE on API30+.
