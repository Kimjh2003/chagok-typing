# 과거 개발 기록 (보관용)

이 문서는 이전 README를 보관한 것으로, 현재 기능과 빌드 지침은 [README.md](README.md)를 참고해줘.

# 0.20 module update

Current boundaries and moved package paths are defined in MODULES.md at the project root. The six-module structure supersedes historical paths below.

# 차곡 타자 — Android 초안 0.12

0.12: 홈에 저장되는 ‘엔터로 완료’ 선택 추가(기본 꺼짐). 연습 카드 아래 버튼 줄을 제거해 입력 영역을 키보드 바로 위로 배치. ‘연습 선택으로’는 연습 묶음 위, 수동 ‘완료’는 카드의 입력 제목 오른쪽으로 이동. ENTER_COMPLETION.md 참조.

0.11: `:app → :practice`, `:app → :ui`의 3모듈 구성으로 분리. 연습 엔진은 순수 Kotlin/JVM `:practice`, 표시 문구(MetricsFormatter)와 화면/IME 연결은 앱, 공통 디자인은 UI에 유지. 테스트도 책임별로 분리. ARCHITECTURE.md 참조.

0.10: 앱·공통 UI·테스트를 전부 Kotlin으로 전환하고 빌드 설정도 Kotlin DSL로 변경. 타수·정확도·타이머 정책 유지. 예문 배열은 PromptRepository에서 한 번만 준비하고 화면 폭 제한/버튼/결과 표시의 중복을 정리했다. Kotlin 전환 자체의 실기기 속도 개선은 아직 측정하지 않았다.

0.9: 연습 상태/시간을 PracticeSession, 표시 문자열 캐시를 MetricsFormatter, 완료 채점을 FinalScore로 분리. 홈/결과의 통계 예약 중단, 변화 없는 통계 문자열 생성 생략, 완료 시 타수 재집계 제거. 입력·UI·계산 정책은 유지. 처리 흐름과 검증은 INPUT_LOGIC.md 참조.

0.8: 공통 UI를 Android Library `:ui`로 분리. `:app`이 `implementation project(':ui')`로 사용. 색상/치수, 버튼/선택/정보/격려 문구, 제시문/입력 통합 카드와 결과 비교 스타일을 모듈에서 제공. 계산·측정·화면 상태는 앱에 유지. 사용법은 ui/README.md 참조.

0.7.1: 연습 화면 좌상단 격려 문구를 15sp에서 18sp로 확대.

0.7: 청회색 정보 팔레트를 밝은 세이지 중립색으로 변경. 선택 배경과 주요 실행 녹색은 더 진하게, 눌림 Ripple은 더 또렷하게 조정. 공통 토큰으로 홈/연습/결과에 일괄 적용. 레이아웃과 타수 계산은 유지.

0.6.5: 결과 화면의 긴 타수 계산 설명 문구 제거. 계산 기준은 이 문서에 유지.

0.6.4: 연습 요약 항목바를 전체 너비 대신 글자+좌우 여백에 맞춘 너비로 변경. 좁은 공간에서는 기존처럼 줄바꿈.

0.6.3: 연습 화면의 우상단 브랜드 라벨을 제거하고 좌상단에 ‘서두르지 말고, 너만의 리듬으로.’ 문구를 테두리 없이 표시.

0.6.2: 연습 화면 오른쪽 상단에 청회색 브랜드 라벨 추가. 연습 묶음의 하단 정렬 유지.

0.6.1: 요약과 제시문/입력을 같은 영역으로 묶어 하단 버튼 위에 정렬. 남는 여백을 위로 보내 입력 중 키보드와 읽기 영역의 시선 거리를 줄임. 긴 내용은 스크롤하며 요약은 더 이상 상단 고정하지 않음.

0.6: 실기기 피드백에 따라 연습 화면 간결화. 상단 요약 1개, 제시문/내 입력 통합 카드, 하단 ‘완료’/‘연습 선택으로’ 나란히 배치. 연습 전용 버튼 최소 높이 48dp. 상세 규칙은 DESIGN_SYSTEM.md v1.1의 0.6 절 참조.

0.5: DESIGN_SYSTEM.md에 UI 규약 v1 정의. UiTokens.java에 색상·치수 토큰 분리. 입력창/제시문 공통 테두리·폰트 적용, 입력 라벨 연결, 연습 완료 버튼을 스크롤과 분리된 하단 영역으로 이동. 실시간 표시가 같으면 텍스트 갱신 생략.

0.4: 선택/실행은 녹색, 비대화형 정보는 연한 청회색 배경(#ECF1F6)·테두리(#A6B7C8)·진한 글자(#35495E)로 구분. 홈 라벨, 연습 모드/실시간 상태, 결과 지표/제시문/내 입력에 공통 정보 스타일 적용.

0.3.2: 연습 언어·연습 종류 제목에도 브랜드 라벨과 동일한 테두리·배경 적용.

0.3.1: 상단 브랜드 라벨에 버튼과 동일한 둥근 모서리·테두리 및 연한 녹색 배경 적용.

0.3: 완성 글자/분을 자음·모음 환산 타수로 변경. 제목을 조금 내리고 언어·연습 종류 선택을 하단 시작 버튼 가까이에 배치.

0.2: 언어/연습 종류를 공통 선택 버튼으로 변경. 실행 버튼에도 같은 크기·모서리·눌림·포커스 규약 적용. 상세 규약은 BUTTONS.md 참조.

한글/영어 × 단어/문장/문단을 선택하고 시스템 키보드로 연습하는 네이티브 앱 초안.

## 현재 기능
- 홈 → 연습 → 결과 → 다음 연습 흐름.
- 직접 작성한 오프라인 예문. 언어/모드마다 2~3개 샘플을 순환.
- 표준 EditText가 IME 조합을 처리. 입력을 채점 결과로 다시 덮어쓰지 않음.
- 입력 이벤트에서는 변경 범위의 자모 수 차이와 측정 시작 여부를 처리하고, 상태 표시는 200ms 간격으로 갱신. 조합 교체는 기존 범위를 빼고 새 범위를 더해 중복 집계 방지.
- 현재 창의 너비를 기준으로 콘텐츠 최대 너비 제한. 키보드/시스템 인셋 반영, 스크롤 지원.
- 화면 크기/방향 변경 시 Activity를 유지하여 편집 상태 유지. 일반 재생성 시 텍스트·커서 위치·측정 시간 복원.
- 백그라운드에서는 측정 일시 정지, 복귀 시 재개. 이는 초안 정책이며 최종 확정 전.

## 지표
속도 = 현재 입력에 남은 환산 타수 × 60,000 / 활성 연습 시간(ms). 최근 60초 이동 평균이 아닌 연습 시작부터의 평균 타/분이다. 시작 후 1초까지 실시간 속도는 측정 중으로 표시한다. 오타도 입력량에 포함하며 일치율은 별도로 표시한다.

한글은 초성·중성·종성으로 분해하고 복합 모음(ㅘㅙㅚㅝㅞㅟㅢ)과 겹받침은 각 2타, 쌍자음은 1타로 계산한다. 예: 한글=6타, 과=3타, 값=4타, 까=2타. 이는 앱의 초기 계산 정책이며 모든 타자 프로그램의 공통 표준이라고 주장하지 않는다. 영문 ASCII 문자·숫자·공백·문장부호는 각 1타. Shift·삭제 키, 줄바꿈·탭, 이모지는 제외한다.

실제 IME 터치 이벤트 수가 아니라 텍스트를 환산한 수치다. 삭제한 과거 입력은 누적하지 않으며 붙여넣기/자동 수정 결과는 텍스트 입력과 동일하게 계산하므로 경쟁용 기록으로 검증된 지표는 아니다. 일치율은 같은 위치의 일치 글자 수를 원문/입력 중 긴 길이로 나눈 값이다. 중간 삽입/삭제 이후 차이는 연쇄 불일치로 계산한다.

한글 분해 참고: https://www.unicode.org/versions/Unicode18.0.0/core-spec/chapter-3/ (Hangul Syllable Decomposition). 복합 자모 가중치는 위 앱 정책으로 별도 적용.

계산 검증: app/src/test/kotlin/dev/typing/practice/LogicTest.kt. 기존 예시·조합·속도·현대 한글 11,172자 및 편집 3,000건을 검사한다. 전환 전에 저장한 Java 0.9 결과 해시와 Kotlin 결과가 일치하는지 비교한다. 이는 실제 IME 호환성/지연 검증을 대체하지 않는다.

## 범위와 남은 작업
- minSdk 28, compileSdk/targetSdk 37. 네이티브 .so 없이 Kotlin + Android 플랫폼 Views로 구현되어 ARM64에서 실행 가능한 범용 APK이며 ARM64 전용 패키지는 아니다.
- 앱과 UI는 Kotlin, Gradle은 Kotlin DSL 사용. Compose로 화면 기술을 변경한 것은 아니다.
- 힌지 위치 감지·회피, 탁상 자세별 배치, 태블릿 2열 레이아웃은 미구현. 현재는 너비 제한 단일 열.
- Activity 재생성/프로세스 복구 시 한글 조합 span과 선택 범위 양끝의 완전 복원은 보장하지 않는다. 크기/방향 변경은 configChanges로 재생성을 피한다.
- 다크 테마, 앱 내 글자 크기 조절, 기록 저장, 영문 WPM 별도 표시, 오타 하이라이트는 후속 작업.
- 자동 수정 금지 플래그는 키보드에 대한 요청이며 모든 IME의 동작을 강제하지 않는다.
- ARM64 실기기·Android 9/17·한글 IME·폴더블 및 입력 지연 분포 검증은 아직 수행하지 않았다.

## 빌드
Android Studio에서 이 폴더를 열거나 JDK 17 이상과 Android SDK 37 환경에서 실행:

```powershell
.\gradlew.bat :app:assembleDebug
```

SDK 경로는 ANDROID_HOME 또는 로컬 local.properties의 sdk.dir로 지정한다. Gradle wrapper 포함. 인터넷 연결이 필요한 최초 의존성 설치가 있을 수 있다.

디버그 APK: app/build/outputs/apk/debug/app-debug.apk

디버그 서명 빌드로 개인 테스트용이다. 출시 서명 및 배포 설정은 포함하지 않는다.

## 0.13 — practice actions within reach
- Current Korean/English prompts, including paragraphs, contain no explicit newlines; visual wrapping does not require Enter.
- Keep the persisted Enter-to-complete option and native IME routing from 0.12 unchanged.
- Override 0.12 action placement: remove the input-header completion button and the return button above the summary.
- Pin equal-width 완료 / 연습 선택으로 actions below the scroll area, immediately above the actual IME/system inset with an 8dp bottom gap. Each action has a minimum height of 48dp.
- Keep summary and prompt/input card bottom-aligned just above the action row, with 8dp spacing. Long content scrolls independently of the actions.

## 0.14 — reachable Back and quieter buttons
- Back from practice or results returns to selection; home retains system exit behavior.
- Android 13+: register OnBackInvokedCallback at PRIORITY_OVERLAY only outside home so Back returns to selection even with the IME open. Android 9–12: tracked, uncanceled pre-IME Back on the editor routes to the same app callback; legacy Activity Back handles other focus states.
- Returning to selection abandons the current exercise, exactly like the existing selection button; it does not submit a score.
- Shared home/result actions and choices: minimum 48dp, 15sp, 8dp vertical padding. Practice actions: minimum 48dp, 14sp, 8dp padding. Heights remain flexible for larger fonts and wrapped labels.
- Keep the 0.13 pinned two-action row and existing Enter completion option.

## 0.15 — matched prompt/input space
- Prompt and input text bodies have equal heights, measured from the prompt's actual wrapping at the current width/font size; minimum body height 48dp.
- Use the same typeface, text size, line spacing and body padding for both. Remove the old fixed 64dp/two-line editor minimum.
- Input exceeding the prompt's space scrolls within the native editor. Long cards retain the outer scroll; pinned actions stay available. No text mutation, scoring or extra text copies are introduced.
- Re-measure after width/font changes; update editor height only when the measured prompt height changes.
- Practice encouragement increases from 18sp to 20sp.

## 0.16 — Local result history
Completed exercises are automatically saved on-device. Open 연습 기록 on home and tap a record to expand it. Uses existing shared UI; see RESULT_HISTORY.md for storage, error handling and verification.

## 0.16.1 — dismiss keyboard before leaving practice
- Overrides 0.14's immediate return with the IME open: first Back dismisses the system IME, keeping the same practice view, entered text, selection and session. Back after the keyboard is hidden returns to selection.
- Android 13+ uses PRIORITY_DEFAULT so the IME can consume Back first. Android 9–12 uses native editor/IME dispatch without a custom pre-IME interceptor; Activity Back handles navigation after IME dismissal.
- The existing bottom action row follows system/IME insets. Dismissing the IME moves 완료 / 연습 선택으로 to the screen bottom without re-rendering the practice scene. The explicit selection button still returns directly to selection.
- Result/history Back behavior and local result storage are unchanged. Timing continues while practice stays visible with the keyboard hidden.

## 0.17 — simplify changed-range unit counting
- Keep beforeChange/afterChange incremental counting, native IME composition, deferred cached metrics, completion-only scoring and background result storage unchanged.
- TypingUnits reads one UTF-16 code unit at a time. Every supported counted character is BMP; surrogate halves and supplementary characters contribute zero. This removes code-point decoding and prevents a high surrogate at the range boundary from reading the following code unit outside the changed range.
- ASCII has a direct branch. Compatibility consonant clusters use numeric branches instead of a string search. No per-character collections, snapshots or lookup-table initialization are added.
- Verify every BMP code unit against prior rules, split-surrogate edits, strict range access, existing all-Hangul/NFD and random edits, and the Java 0.9 golden fixture.
- This reduces operations structurally, not a measured claim of lower device latency. The 200ms cached statistics formatter and lifecycle routing are retained; changing visible cadence or adding native code requires measurement.

## 0.18 — Shared motion
Short press/selection and destination-entry animations live in :ui; practice typing stays unanimated. See MOTION.md.

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


## 0.21 — R8 release
Release code/resource shrinking and optimization are enabled in :app. See R8.md for opt-in local test signing, mapping files and verification.

## 0.22 — System dark theme
All screens follow system dark/light mode with shared :ui palettes. Existing input/session state and stored records are preserved. See DARK_MODE.md.

## 0.23 — language lists and separate record details
- Home → history has Korean/English choice tabs using the existing shared controls. Each language queries its own latest 100 records; filtering happens in SQLite before the limit.
- List rows contain only completion date/time, practice type, units/minute and accuracy. Prompt/input text is not loaded with the summary query.
- Tapping a UUID opens a separate in-app detail screen loaded by ID, including full prompt/input, units and active duration. Both system Back and 기록 목록으로 return to the originating language list.
- Each language retains its own scroll offset. Restore happens after asynchronous rows finish layout, not while loading. Language, record UUID and both list offsets survive Activity recreation via Bundle. Missing records and database read errors have explicit states; history content is read-only.
- Same SQLite schema/database and immutable scoring policy; no record migration or deletion. All screens use shared sage controls and context-selected light/dark palette.

## 0.23.1 — subdued dark actions
- User requested reducing the bright mint area first, deferring keyboard-hidden layout decisions. Layout/insets/scroll/navigation/motion timing are unchanged.
- Separate primary ACTION_FILL from the text/focus accent ACTION. Light fill remains the existing dark green. Dark primary fill is #223029 with soft text #D5DFD8 and a thin outline, replacing the large pale mint block.
- Dark text/focus accent is muted #A0B4A6, selected fill #26382D. Dark ripples use translucent black #33000000 rather than a pale/green overlay. Native dark cursor/accent is updated consistently.
- The panel has a dark tinted appearance using opaque solid fill; no blur, extra transparency layer or new offscreen rendering is introduced. Existing role contrast tests now check ON_ACTION against ACTION_FILL.
- Keyboard-hidden practice placement remains a pending design decision, not changed in this patch.

## 0.24 — reduce gaze travel when dismissing the keyboard
- On API30+, remember only a docked IME bottom actually observed in this window. When hidden, reserve the former IME area minus 48dp, keeping the prompt/editor and action row together near their prior position. Keep the existing 8dp gap within the group; do not move actions independently.
- Clamp the retained reserve to leave at least 320dp of vertical window space (minus system top inset). Narrow/short windows retain scrolling and may move further to preserve usable space. Reset old IME memory on actual window geometry changes; floating IME with zero bottom inset clears docked reserve.
- The motion module accepts an optional effective-bottom provider so animation progress uses the same reserved-space policy as final layout. This avoids a jump at animation end. No per-frame layout or text mutations are added.
- Hide the small upper hint while the IME is visible; when hidden show: 입력칸을 누르면 이어서 연습할 수 있어. Keep the existing encouragement. No new cards/decorations.
- Persist observed keyboard geometry with other practice Bundle state. API28/29 retain native adjustResize and omit the hint; the new limited-travel behavior requires API30 inset visibility/animation.
- Timing continues while practice remains visible with keyboard hidden, as before. Back still dismisses keyboard first, then returns to selection. Palette, storage and scoring are unchanged.

## 0.24.1 — compact practice typography and reachable long content
- Practice prompt/editor reduce from20sp to18sp, with shared3dp extra line spacing; encouragement reduces20sp→18sp. Labels, system font scaling, equal prompt/editor body heights, buttons and other screens are unchanged.
- Practice column now measures its natural content height (WRAP_CONTENT) while minimumHeight still fills the scroll viewport. Short exercises remain bottom-aligned. Long paragraphs can grow beyond the viewport and scroll from their true top, instead of bottom-aligning oversized children inside a forced viewport-height column and placing the summary above the scrollable origin.
- Actions remain outside the scroll area. The limited keyboard-hide travel policy and input/scoring state remain unchanged. Long content may still require scrolling; it is not truncated or made arbitrarily tiny to force-fit.

## 0.24.2 — truly equal, unclipped prompt/input areas
- Previously the input height was capped at the prompt's measured height, although TextView and EditText can use different line breaking and native cursor/font padding. Equal outer bounds did not guarantee enough space for the input's last line.
- Configure native multiline inputType before applying shared typography. Use identical simple line-breaking, no hyphenation, typeface, font padding, text size, spacing and horizontal padding. Disable the inner editor scrollbar; the shared outer scroll handles long cards.
- Measure both native text layouts at natural height, then assign both bodies the greater height. Extra/wrapped/composing input can grow both areas; deletion can shrink them again. No snapshots or scoring work are added to input callbacks. Larger system fonts remain respected.
- Result screen uses one shared compact action row: 다음 연습 / 연습 선택으로. Practice actions and history navigation are unchanged.

## 0.25 — separate equal practice cards
- Replace the shared outer border/divider with two independent rounded cards: prompt and input, separated by8dp. The outer PracticeInputView only groups them; it has no fill/border/padding.
- Both use the same width, shared card padding, native typography and measured body height. Measure natural bodies, share their maximum, and equalize total card heights when headers require extra space. Existing 48dp body minimum remains, with no fixed paragraph height.
- Input focus highlights only the input card. Prompt retains a quiet outline. Both grow together for longer/composing input and remain inside the shared outer scroll; no scoring, IME callback or stored-data changes.
- Result actions stay side-by-side from0.24.2. Keyboard-hide positioning, motion timing and light/dark palettes are unchanged.

## 0.26 — inline practice metrics and denser equal cards
- Remove the standalone summary pill. The prompt header contains 제시문 at start and right-aligned language/mode/elapsed/rate (11sp). Raw entered-unit count is omitted only from live compact metrics; result/history units and calculation are unchanged. Metadata wraps instead of ellipsizing if width/font scale requires it; both headers/cards retain equal heights.
- Practice cards use10dp horizontal/4dp vertical padding,6dp separation,2dp label vertical padding,4dp body bottom padding and40dp minimum body height. Retain18sp main text and native line wrapping. Both natural bodies and headers are equalized before total-card measurement.
- Practice actions use13sp and a visually inset40dp surface within a48dp minimum touch target. Results keep the existing adjacent action row.
- Cursor reveal expands to the full input card when the card fits inside its scroll viewport, helping avoid a clipped bottom border above pinned actions. Oversized content remains scrollable, with no unconditional forced full-card scrolling.
- Compact formatter caches only visible values; legacy default formatting is retained for the Java0.9 golden fixture. No scoring/storage/input-text changes or per-keystroke formatting are introduced.

## 0.26.1 — store lifecycle and single scene entry
- Keep the Gemini updates (modern system bars/IME calls, predictive Back, haptics, catalog and logic refactors).
- MainActivity no longer closes the process-wide ResultStore when finishing. Closing an Activity and relaunching in the same process retains the live shared executor and database.
- Explicit close for independent stores is idempotent and serialized with task submission. Already accepted operations drain before SQLite closes on its worker. Requests after close report failure via the callback rather than throwing executor rejection on the caller thread. The singleton factory replaces an explicitly closed instance.
- Non-practice destinations invoke sceneTransition exactly once, after assembly. Remove the immediately-canceling second enter animation. Practice keeps only the encouragement entry animation; native editor/card/metrics are not animated by scene entry.
- Device regression coverage adds Activity finish/relaunch with the same shared store, save/read after relaunch, queued-write drain, double close and callback failure after close. Original input and history tests remain.

0.26.1 device testing also exposed a modern system-bar startup crash: PhoneWindow's inset controller was read before DecorView initialization. updateSystemBars now initializes the DecorView first; cold launch and same-process relaunch are regression-tested.

0.26.1 validation: 20 JVM tests and release lint passed. All19 device checks passed, including same-process Activity finish/relaunch and subsequent save/read, queued-write drain and closed-store failure callbacks. Optimized release black-box smoke also passed launch/input/save/history and Activity finish/relaunch/history access. Initial cold-start crash was fixed by initializing DecorView; an emulator System UI ANR overlay was dismissed before UI smoke. Physical ARM64 review remains pending.

## 0.27 연습 구성
새 연습은 한국어·영어 단어와 문장만 제공한다. 언어별 단어 20개, 문장 15개를 순서대로 순환한다. 기존 문단 기록과 복원된 연습은 유지하며, 새 연습을 시작하면 문장으로 전환한다.
