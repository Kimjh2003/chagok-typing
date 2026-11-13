# 0.20 module update

Current boundaries and moved package paths are defined in MODULES.md at the project root. The six-module structure supersedes historical paths below.

# 차곡 공통 UI 모듈

0.12: optionToggle/compactButton, PracticeInputView.setCompletionAction, SubmitEditText/EnterCompletionPolicy 추가. PracticeInputView.editor는 EditText 하위 타입 SubmitEditText이며 completeOnEnter/onSubmit으로 동작만 주입한다. 설정 저장·채점·화면 이동은 앱 책임이다. ENTER_COMPLETION.md 참조.

0.11부터 전체 앱은 :app/:practice/:ui로 구성한다. 이 UI 모듈은 :practice를 참조하지 않는다. 전체 의존성과 로직 모듈 설명은 프로젝트 루트 ARCHITECTURE.md 참조.

Gradle Android Library `:ui` · namespace/package `dev.typing.ui` · minSdk 28 · compileSdk 37.

0.10부터 구현과 테스트는 모두 Kotlin이다. 구현 경로는 `src/main/kotlin/dev/typing/ui/`, 테스트 경로는 `src/test/kotlin/dev/typing/ui/`다. AGP 9.4.1의 내장 Kotlin 지원을 사용한다.

## 역할과 의존성

```text
:app ── implementation(project(":ui")) ──> :ui ──> Android 플랫폼 Views
```

공통 UI는 이 모듈에서 관리한다. 앱에 역으로 의존하지 않으며 타수 계산, 시간 측정, 예문 선택, 저장, 화면 이동, 네트워크 기능은 포함하지 않는다. 새 디자인 토큰/컴포넌트는 여기 추가하고 화면마다 복제하지 않는다. 실제 창/키보드 인셋, 스크롤과 하단 정렬은 앱의 화면 조립 책임이다.

## 공개 구성

`UiControls.disclosure`는 펼침 버튼과 본문을 제공하며 상태는 호출자가 소유한다. 닫힌 본문은 포커스 대상에서 제외되고 버튼 문구와 접근성 상태가 현재 열림 상태를 나타낸다. 삭제 확인 창은 접근성 창 제목과 취소 버튼의 초기 키보드 포커스를 제공한다.

`UiControls.helpButton`은 48dp 터치 영역과 스크롤 가능한 팝업을 제공한다. 도움말 내용과 배치는 앱이 정하며, 바깥 터치와 뒤로 가기로 닫는다.

`ResultCardRole`과 `UiControls.resultInfo`는 결과와 기록 상세에서 타수·보조 측정치·제시문·내 입력의 라이트/다크 배경색을 구분한다. 글자 대비는 팔레트 테스트로 검증하며 화면 배치와 계산은 앱이 담당한다.

`PracticeProgressView`는 제시문과 입력의 UTF-16 길이 비율을 나타내는 정적 진행 막대다. 실제 정확도나 완료 상태를 나타내지 않고 입력이 길어지면 100%에서 멈춘다. 호출자는 숫자만 전달하며 표시 여부는 사용 가능한 창 높이에 따라 앱이 정한다.

`UiControls.optionToggle`은 네이티브 Switch의 터치·상태·접근성과 시스템 손잡이 전환을 유지한다. 25dp 트랙 높이와 3dp 여백을 픽셀로 변환한 뒤 그 값에서 손잡이 지름과 트랙 너비를 도출해, 계산상 원 둘레의 끝·상하 여백을 일치시킨다. OEM별 실제 렌더링은 기기에서 확인해야 한다.

| API | 역할 |
|---|---|
| `UiTokens` | 색상, 모서리, 간격, 글자 크기, 조작 최소 높이 |
| `UiControls.text`, `encouragement` | 본문과 좌상단 격려 문구 스타일 |
| `UiControls.brand`, `inlineLabel`, `info`, `summary` | 정보 라벨·카드·요약 바 |
| `UiControls.action` | 주요/보조 버튼을 부모에 추가 |
| `UiControls.compactActions` | 문구/콜백을 전달받는 나란한 버튼 두 개 |
| `UiControls.choices` | 네이티브 단일 선택 그룹 |
| `UiControls.comparison` | 제목과 내용을 함께 표시하는 결과 카드 |
| `UiControls.editor`, `prompt`, `practiceCard` | 독립 편집/제시문/카드 기반 스타일 |
| `PracticeInputView` | 현재 사용하는 제시문·네이티브 입력 통합 카드 |

`PracticeInputView.editor`는 실제 `EditText`다. 앱이 여기에 입력 유형·초기 텍스트·선택 위치·TextWatcher를 설정한다. 카드가 입력 텍스트를 별도 상태로 복사하거나 IME를 감싸지 않는다. 입력 포커스 리스너는 카드 강조를 위해 내부에서 사용하므로 앱에서 덮어쓰지 않는다.

## 사용 예

```kotlin
// app/build.gradle.kts
dependencies { implementation(project(":ui")) }
```

```kotlin
import dev.typing.ui.PracticeInputView
import dev.typing.ui.UiControls

val card = PracticeInputView(this, prompt, "제시문", "내 입력", "여기에 입력해봐", 2)
parent.addView(card)
val editor = card.editor
// 앱에서 입력/조합 처리와 TextWatcher 연결
UiControls.compactActions(actions, "완료", ::finishExercise, "연습 선택으로", ::showHome)
UiControls.action(parent, "다음 연습", primary = true) { startExercise() }
```

모든 View 생성/상태 갱신은 Android 메인 스레드에서 수행한다. `action`, `compactActions`, `choices`는 전달한 부모에 자식을 추가하므로 부모를 다시 사용할 때 중복 추가하지 않는다. 다른 메서드는 View를 반환한다. 기존 모양을 유지하는 추출이며 다크 테마·힌지 회피·새 화면 기능을 추가한 것은 아니다.

## 검증·빌드

프로젝트 루트에서:

```powershell
.\gradlew.bat :ui:assembleRelease :app:assembleDebug :ui:testDebugUnitTest :app:testDebugUnitTest :ui:lintDebug :app:lintDebug
```

라이브러리: `ui/build/outputs/aar/ui-release.aar`.

색 대비 검사는 Kotlin JUnit 테스트로 이관했다:

```powershell
.\gradlew.bat :ui:testDebugUnitTest
```

검사 결과는 팔레트의 계산상 대비를 뜻하며 실제 기기에서 조합·포커스·창 크기 전환을 확인하는 UI 검증을 대체하지 않는다.

전체 제품 규약: 프로젝트 루트의 `DESIGN_SYSTEM.md`. 모듈은 로컬 프로젝트용 API이며 외부 Maven 배포나 API 안정성 보장 설정은 포함하지 않는다.

## 0.15 — matched prompt/input space
- Prompt and input text bodies have equal heights, measured from the prompt's actual wrapping at the current width/font size; minimum body height 48dp.
- Use the same typeface, text size, line spacing and body padding for both. Remove the old fixed 64dp/two-line editor minimum.
- Input exceeding the prompt's space scrolls within the native editor. Long cards retain the outer scroll; pinned actions stay available. No text mutation, scoring or extra text copies are introduced.
- Re-measure after width/font changes; update editor height only when the measured prompt height changes.
- Practice encouragement increases from 18sp to 20sp.

0.18 adds UiMotion.press(view) and UiMotion.enter(view), using native property animators and the shared motion specification in MOTION.md.

0.19 adds ImeMotion.install(root, targets) for API30+ system IME progress synchronization; see MOTION.md.


0.22 adds UiPalette and UiPalettes.from(context) for system dark/light role colors; UiTokens retains shared dimensions and original light defaults. See DARK_MODE.md.

## 0.25 — separate equal practice cards
- Replace the shared outer border/divider with two independent rounded cards: prompt and input, separated by8dp. The outer PracticeInputView only groups them; it has no fill/border/padding.
- Both use the same width, shared card padding, native typography and measured body height. Measure natural bodies, share their maximum, and equalize total card heights when headers require extra space. Existing 48dp body minimum remains, with no fixed paragraph height.
- Input focus highlights only the input card. Prompt retains a quiet outline. Both grow together for longer/composing input and remain inside the shared outer scroll; no scoring, IME callback or stored-data changes.
- Result actions stay side-by-side from0.24.2. Keyboard-hide positioning, motion timing and light/dark palettes are unchanged.
