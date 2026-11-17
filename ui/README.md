# 0.20 module update

현재 경계와 이동된 패키지 경로는 프로젝트 루트의 MODULES.md에 정의됩니다. 6개 모듈 구조는 아래의 역사적 경로를 대체합니다.

# 차곡 공통 UI 모듈

0.12: optionToggle/compactButton, PracticeInputView.setCompletionAction, SubmitEditText/EnterCompletionPolicy 추가. PracticeInputView.editor는 EditText 하위 타입 SubmitEditText이며 completeOnEnter/onSubmit으로 동작만 주입합니다. 설정 저장·채점·화면 이동은 앱 책임입니다. ENTER_COMPLETION.md 참조.

0.11부터 전체 앱은 :app/:practice/:ui로 구성합니다. 이 UI 모듈은 :practice를 참조하지 않습니다. 전체 의존성과 로직 모듈 설명은 프로젝트 루트 ARCHITECTURE.md 참조.

Gradle Android 라이브러리 `:ui` · 네임스페이스/패키지 `dev.typing.ui` · minSdk 28 · compileSdk 37.

0.10부터 구현과 테스트는 모두 Kotlin입니다. 구현 경로는 `src/main/kotlin/dev/typing/ui/`, 테스트 경로는 `src/test/kotlin/dev/typing/ui/`입니다. AGP 9.4.1의 내장 Kotlin 지원을 사용합니다.

## 역할과 의존성

```text
:app ── implementation(project(":ui")) ──> :ui ──> Android 플랫폼 Views
```

공통 UI는 이 모듈에서 관리합니다. 앱에 역으로 의존하지 않으며 타수 계산, 시간 측정, 예문 선택, 저장, 화면 이동, 네트워크 기능은 포함하지 않습니다. 새 디자인 토큰/컴포넌트는 여기 추가하고 화면마다 복제하지 않습니다. 실제 창/키보드 인셋, 스크롤과 하단 정렬은 앱의 화면 조립 책임입니다.

## 공개 구성

`UiControls.disclosure`는 펼침 버튼과 본문을 제공하며 상태는 호출자가 소유합니다. 닫힌 본문은 포커스 대상에서 제외되고 버튼 문구와 접근성 상태가 현재 열림 상태를 나타낸다. 삭제 확인 창은 접근성 창 제목과 취소 버튼의 초기 키보드 포커스를 제공합니다.

`UiControls.helpButton`은 48dp 터치 영역과 스크롤 가능한 팝업을 제공합니다. 도움말 내용과 배치는 앱이 정하며, 바깥 터치와 뒤로 가기로 닫는다.

`ResultCardRole`과 `UiControls.resultInfo`는 결과와 기록 상세에서 타수·보조 측정치·제시문·내 입력의 라이트/다크 배경색을 구분합니다. 글자 대비는 팔레트 테스트로 검증하며 화면 배치와 계산은 앱이 담당합니다.

`PracticeProgressView`는 제시문과 입력의 UTF-16 길이 비율을 나타내는 정적 진행 막대다. 실제 정확도나 완료 상태를 나타내지 않고 입력이 길어지면 100%에서 멈춘다. 호출자는 숫자만 전달하며 표시 여부는 사용 가능한 창 높이에 따라 앱이 정합니다.

`UiControls.optionToggle`은 네이티브 Switch의 터치·상태·접근성과 시스템 손잡이 전환을 유지합니다. 25dp 트랙 높이와 3dp 여백을 픽셀로 변환한 뒤 그 값에서 손잡이 지름과 트랙 너비를 도출해, 계산상 원 둘레의 끝·상하 여백을 일치시킨다. OEM별 실제 렌더링은 기기에서 확인해야 합니다.

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

`PracticeInputView.editor`는 실제 `EditText`입니다. 앱이 여기에 입력 유형·초기 텍스트·선택 위치·TextWatcher를 설정합니다. 카드가 입력 텍스트를 별도 상태로 복사하거나 IME를 감싸지 않습니다. 입력 포커스 리스너는 카드 강조를 위해 내부에서 사용하므로 앱에서 덮어쓰지 않습니다.

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

모든 View 생성/상태 갱신은 Android 메인 스레드에서 수행합니다. `action`, `compactActions`, `choices`는 전달한 부모에 자식을 추가하므로 부모를 다시 사용할 때 중복 추가하지 않습니다. 다른 메서드는 View를 반환합니다. 기존 모양을 유지하는 추출이며 다크 테마·힌지 회피·새 화면 기능을 추가한 것은 아닙니다.

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

검사 결과는 팔레트의 계산상 대비를 뜻하며 실제 기기에서 조합·포커스·창 크기 전환을 확인하는 UI 검증을 대체하지 않습니다.

전체 제품 규약: 프로젝트 루트의 `DESIGN_SYSTEM.md`. 모듈은 로컬 프로젝트용 API이며 외부 Maven 배포나 API 안정성 보장 설정은 포함하지 않습니다.

## 0.15 — 제시문과 입력 공간 높이 맞춤
- 제시문와 입력 텍스트 본문의 높이는 현재 너비/글꼴 크기에서 제시문의 실제 줄 바꿈을 기준으로 측정되어 동일합니다. 최소 본문 높이 48dp.
- 두 가지 모두에 동일한 서체, 텍스트 크기, 줄 간격 및 본문 패딩을 사용합니다. 기존의 고정 64dp/2줄 편집기 최소값을 제거합니다.
- 제시문 공간을 초과하는 입력은 기본 편집기 내에서 스크롤됩니다. 긴 카드는 바깥쪽 스크롤을 유지합니다. 고정된 동작은 계속 사용할 수 있습니다. 텍스트 변형, 채점 또는 추가 텍스트 사본이 도입되지 않습니다.
- 너비/글꼴 변경 후 다시 측정합니다. 측정된 제시문 높이가 변경될 때만 편집기 높이를 업데이트합니다.
- 연습 격려가 18sp에서 20sp로 증가합니다.

0.18 adds UiMotion.press(view) and UiMotion.enter(view), using native property animators and the shared motion specification in MOTION.md.

0.19 adds ImeMotion.install(root, targets) for API30+ system IME progress synchronization; see MOTION.md.


0.22 adds UiPalette and UiPalettes.from(context) for system dark/light role colors; UiTokens retains shared dimensions and original light defaults. See DARK_MODE.md.

## 0.25 — 높이가 같은 독립 연습 카드
- 공유된 외부 테두리/분할선을 두 개의 독립적인 둥근 카드(8dp로 구분된 제시문 및 입력)로 바꿉니다. 외부 PracticeInputView는 그룹화만 합니다. 채우기/테두리/패딩이 없습니다.
- 둘 다 동일한 너비, 공유 카드 패딩, 기본 타이포그래피 및 측정된 본문 높이를 사용합니다. 헤더에 추가 공간이 필요한 경우 자연스러운 본문를 측정하고 최대값을 공유하며 총 카드 높이를 균등화합니다. 고정된 단락 높이 없이 기존 48dp 본문 최소 크기가 그대로 유지됩니다.
- 입력 포커스는 입력 카드만 강조 표시합니다. 제시문는 조용한 개요를 유지합니다. 둘 다 더 긴/구성 입력을 위해 함께 성장하고 공유된 외부 스크롤 내부에 유지됩니다. 채점, IME 콜백 또는 저장된 데이터 변경이 없습니다.
- 결과 동작은 0.24.2에서 나란히 유지됩니다. 키보드 숨기기 위치 지정, 모션 타이밍 및 밝은/어두운 팔레트는 변경되지 않았습니다.
