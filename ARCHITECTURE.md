# 0.20 module update

현재 경계와 이동된 패키지 경로는 프로젝트 루트의 MODULES.md에 정의됩니다. 6개 모듈 구조는 아래의 역사적 경로를 대체합니다.

# 모듈 구조 · 0.11

```text
:app ──→ :practice (순수 Kotlin/JVM)
  └────→ :ui       (Android Library)
```

`:practice`와 `:ui` 사이에는 의존성이 없습니다. `:app`이 두 모듈을 조립합니다.

| 모듈 | 담당 | 포함하지 않는 책임 |
|---|---|---|
| app | Activity, IME/TextWatcher 연결, 수명주기, 화면 전환, 상태 저장/복원, MetricsFormatter | 자모 알고리즘·연습 시간 상태를 중복 구현하지 않음 |
| practice | PracticeSession, TypingUnits, FinalScore, PromptRepository | Android/UI/화면 표시 문자열/저장/스케줄러 |
| ui | UiTokens, UiControls, PracticeInputView | 채점/타이머/예문 선택/앱 화면 전환 |

## 입력과 표시의 연결

```text
시스템 IME → ui의 네이티브 EditText
               ↓ app의 TextWatcher
               ↓ practice에 변경 범위 전달
                 타수·상태만 반영

app의 200ms 표시 예약 → practice의 숫자 읽기
                      → app의 MetricsFormatter
                      → ui의 TextView 표시

app의 완료 행동 → practice.finish → 고정 결과 → ui로 표시
```

모듈 경계는 같은 프로세스의 직접 메서드 호출입니다. 입력마다 직렬화·전체 문자열 복사·스레드 전환·새 상태 객체를 추가하지 않습니다.

## 디렉터리

- 앱/src/main/kotlin/dev/typing/practice/: MainActivity, MetricsFormatter.
- practice/src/main/kotlin/dev/typing/practice/core/: 연습 엔진 4개 클래스/객체.
- ui/src/main/kotlin/dev/typing/ui/: 공통 UI 3개 컴포넌트/토큰.

상세 API는 practice/README.md와 ui/README.md 참조. 과거 문서에서 계산 코드가 app 아래에 있다고 설명한 경로보다 이 안내가 우선합니다.

## 검증

- practice: 한글 11,172자·시간/상태·조합·편집 3,000건·변경 범위 작업량·예문 순서.
- app: 표시 문자열 캐시와 Java 0.9 결과 해시 비교.
- ui: 팔레트 대비.

```powershell
.\gradlew.bat :practice:test :practice:jar :app:testDebugUnitTest :ui:testDebugUnitTest :app:assembleDebug :app:lintDebug :ui:lintDebug
```

이번 동작은 책임과 의존성 분리이며 UI/타수/정확도 정책 변경이나 실기기 속도 개선을 의미하지 않습니다. 실제 IME·기기·화면 전환 성능 검증은 계속 필요합니다. 기록 저장·독립 기능 화면 모듈은 해당 기능을 구현할 때 추가합니다.

0.11 검증 결과: 전체 앱 빌드·앱/UI Lint 통과. practice 6개 + app 2개 + ui 1개, 총 9개 테스트 통과. Java 0.9 기준 해시 비교도 통과했습니다. 추가로 app/ui를 포함하지 않는 별도 Gradle 설정에서 ANDROID_HOME/ANDROID_SDK_ROOT를 비우고 :practice:test 및 :practice:jar를 실행해 성공했습니다. 이는 로직 모듈의 Android SDK 독립성을 확인한 검사이며 실기기 UI/성능 검증은 아닙니다.

0.16: ResultStore in :app owns private SQLite persistence on a serial worker. :practice and :ui remain storage-independent. See RESULT_HISTORY.md.

