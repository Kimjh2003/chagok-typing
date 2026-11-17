# 엔터 완료와 키보드 인접 입력 · 0.12

## 사용자 동작

- 홈의 ‘엔터로 완료’ 스위치로 선택합니다. 기본값은 꺼짐이며 선택은 기기에 저장합니다.
- 꺼짐: 기존처럼 엔터는 줄바꿈. 카드 안의 ‘완료’ 버튼으로 종료합니다.
- 켜짐: 키보드 완료 액션 또는 일반 엔터로 현재 입력을 확정해 결과로 이동합니다. 문단에서도 엔터가 완료이므로 여러 줄을 직접 입력하려면 설정을 끈다.
- 수동 완료 버튼은 항상 남겨 키보드별 동작 차이에도 사용할 수 있게 합니다.

## 화면 배치

- 입력 카드 아래의 고정 버튼 줄 제거.
- ‘연습 선택으로’는 연습 묶음 위쪽 오른편의 48dp 버튼으로 이동.
- 요약 → 제시문/입력 카드 순서를 유지. 작은 ‘완료’ 버튼은 카드의 ‘내 입력’ 제목 오른쪽에 배치.
- 실제 시스템/IME 인셋을 제외한 화면의 하단에 카드를 정렬해, 입력 영역이 키보드 바로 위에 오게 합니다. 하단 간격은 8dp.
- 긴 문단이나 큰 글꼴에서는 중앙 영역을 스크롤합니다. 플로팅 키보드의 실제 겹침은 기기별 확인이 필요합니다.

## 모듈 책임

- app: 설정 저장/복원, 스위치 연결, 완료 시점과 화면 이동. 설정 쓰기는 홈 스위치를 바꿀 때만 수행하며 입력 경로에서 저장하지 않습니다.
- ui: 공통 스위치/작은 버튼, SubmitEditText의 엔터 이벤트 전달, 네이티브 입력 카드.
- practice: 기존 타수·시간·채점 유지. IME나 설정/UI에 의존하지 않습니다.

## 입력 보호

SubmitEditText는 Android EditText의 하위 클래스다. 기본 텍스트 편집·표시·조합 처리를 유지합니다.

- IME_ACTION_DONE, 일반 물리 Enter/숫자패드 Enter, 단독 줄바꿈 commitText를 처리합니다.
- Android 33 이상 commitText의 TextAttribute 오버로드도 지원합니다. 구형 API 경로는 별도 기본 래퍼를 사용합니다.
- 완료 시 finishComposingText로 현재 표시된 조합을 유지하고, 현재 IME 콜백이 반환된 뒤 한 번만 완료 콜백을 전달합니다.
- 연속 알림은 예약 중에 합쳐 중복 완료를 방지하고, 입력창이 제거되면 예약을 취소합니다. 앱도 현재 화면/입력창을 확인합니다.
- 줄바꿈이 포함된 일반 문자열/여러 줄 붙여넣기는 엔터 이벤트로 간주하지 않습니다. 단독 줄바꿈만 전달되면 실제 Enter와 구분할 수 없으므로 켜짐 상태에서는 완료로 취급합니다.
- Shift가 포함된 물리 Enter는 가로채지 않고 플랫폼에 위임합니다. 모든 모바일 키보드의 Shift+Enter 조작을 보장하는 별도 기능은 아닙니다.
- 입력 문자열 전체를 매번 검사·복사하지 않습니다. 켜짐 상태의 commitText 분기에서는 길이와 줄바꿈 문자만 확인합니다.

## 검사

- Kotlin JUnit: 단독 LF/CR/CRLF, 비활성, 붙여넣기/일반 문자열 구분과 비활성 상태의 불필요한 읽기 방지.
- 기존 연습/표시/색상 회귀 검사 유지.
- app/src/androidTest/kotlin/dev/typing/practice/InputInstrumentation.kt: 네이티브 InputConnection에서 조합 중 마지막 글자, API 33 오버로드, 완료 액션, 물리 Enter, 여러 줄 붙여넣기, 중복 요청, 분리된 입력창의 취소를 검사합니다.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :practice:test :app:testDebugUnitTest :ui:testDebugUnitTest :app:lintDebug :ui:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w dev.typing.practice.test/dev.typing.practice.InputInstrumentation
```

기기 측 프로토콜 검사가 모든 키보드/OEM 조합의 호환성을 보장하지는 않습니다. 구현 중 실제 실행 결과와 화면 확인은 릴리스 검증 기록에 남깁니다.

0.12 검증: APK/테스트 APK 빌드와 앱/UI Lint 통과. JVM 테스트 12개 통과. Android 36.1 x86_64 에뮬레이터에서 네이티브 입력 프로토콜 검사 8건 통과. Gboard 실제 완료 키로 결과 화면 진입, 키보드 위 카드 배치, 앱 강제 종료 후 옵션 유지까지 확인했습니다. ARM64 실기기 및 삼성 키보드 등 다른 IME는 별도 확인 대상입니다.

참고: [Android InputConnection](https://developer.android.com/reference/android/view/inputmethod/InputConnection), [Editor action listener](https://developer.android.com/reference/android/widget/TextView.OnEditorActionListener).

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

## 0.16.1 — dismiss keyboard before leaving practice
- Overrides 0.14's immediate return with the IME open: first Back dismisses the system IME, keeping the same practice view, entered text, selection and session. Back after the keyboard is hidden returns to selection.
- Android 13+ uses PRIORITY_DEFAULT so the IME can consume Back first. Android 9–12 uses native editor/IME dispatch without a custom pre-IME interceptor; Activity Back handles navigation after IME dismissal.
- The existing bottom action row follows system/IME insets. Dismissing the IME moves 완료 / 연습 선택으로 to the screen bottom without re-rendering the practice scene. The explicit selection button still returns directly to selection.
- Result/history Back behavior and local result storage are unchanged. Timing continues while practice stays visible with the keyboard hidden.
