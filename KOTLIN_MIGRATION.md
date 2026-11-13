# Kotlin 전환 · 0.10

0.11 후속 안내: 이 문서의 0.10 경로 기록 이후 연습 엔진 4개 파일은 :practice 모듈로 이동했다. 현재 경로와 테스트 구성은 ARCHITECTURE.md 및 practice/README.md 참조. Kotlin 전환 때 확보한 Java 기준 해시는 유지한다.

앱, 공통 UI, 회귀 검사, Gradle 스크립트를 Kotlin으로 전환했다. Compose/커스텀 렌더러로 UI 기술을 바꾼 작업은 아니다. 플랫폼 Views와 EditText, `:app → :ui` 의존성을 유지한다.

## 구조

```text
app/src/main/kotlin/dev/typing/practice/
  MainActivity.kt       화면/수명주기/입력 연결
  PracticeSession.kt    상태·시간·변경 범위 집계
  TypingUnits.kt        자모 환산
  FinalScore.kt         완료 시 한 번 채점
  MetricsFormatter.kt   표시값/문자열 캐시
  PromptRepository.kt   한 번 준비하는 예문 배열

ui/src/main/kotlin/dev/typing/ui/
  UiTokens.kt           공통 상수
  UiControls.kt         네이티브 컴포넌트 생성/스타일
  PracticeInputView.kt  제시문·입력 통합 카드
```

Gradle은 settings.gradle.kts, build.gradle.kts, app/build.gradle.kts, ui/build.gradle.kts로 구성한다. AGP의 내장 Kotlin 지원을 사용한다. 컴파일 설정 때 부족했던 Gradle 메타스페이스는 프로젝트 gradle.properties에서 조정하고 최대 작업자 수는 2로 제한했다.

## 간략화한 작업

- 타수 반복문은 `when` 분기와 정수 연산으로 구성. 자모 목록·부분 문자열·Sequence·Stream을 입력마다 만들지 않는다.
- 시간 공급은 primitive 반환 `LongSupplier`를 유지해 Kotlin 함수형 시계의 Long 박싱을 추가하지 않는다.
- nullable 상태와 불변 결과는 Kotlin 속성으로 표현하고 중복 getter/생성자 코드를 제거한다.
- 화면 너비 제한을 boundedColumn 한 곳으로 모으고, 공통 본문/버튼/결과 카드 생성은 UI 모듈을 사용한다.
- 예문 배열은 PromptRepository에서 한 번 준비한다. 순서와 문장은 0.9와 동일하다.
- 작은 버튼 두 개를 만들 때 임시 문구/콜백 배열 대신 공통 생성 함수를 사용한다.
- 통계 표시 문자열 캐시, 완료 결과 캐시, 전경 연습 화면에서만 통계를 갱신하는 정책을 유지한다.

Kotlin의 소스 길이와 기계 명령어 수는 같지 않다. 이번 작업은 중복/불필요한 준비 작업을 정리한 것이며, 언어 교체만으로 더 빨라졌다고 주장하지 않는다.

## 회귀 검증

전환 전에 Java 0.9의 JVM 검사들을 실행하고 다음 결과 스트림의 SHA-256을 저장했다.

1. 현대 한글 11,172자의 완성형/정규 분해 자모 수.
2. 고정 난수 편집 3,000건의 상태·타수·활성 시간·통계 문구. 매 11건마다 일시 정지/재개 포함.
3. 최종 타수·시간·속도·정확도.

기준 파일: app/src/test/resources/java09-golden.sha256

```text
256bdc09dc9022380cc80ee3796994ea76317e1aa96284aa6c1d6d7f75b2a929
```

Kotlin LogicTest는 같은 시나리오로 만든 결과와 기준 해시를 비교한다. 이외에 조합 교체/삭제/빈 입력 복귀/완료 동결, 변경 범위만 읽기, 표시 String 재사용, 예문 순서를 검사한다. UI PaletteTest는 기존 8개 색 대비 조합을 검사한다.

```powershell
.\gradlew.bat :app:assembleDebug :ui:assembleRelease :app:testDebugUnitTest :ui:testDebugUnitTest :app:lintDebug :ui:lintDebug
```

기존 수동 Java 검사 대신 Kotlin JUnit 테스트 8개(앱 7, UI 1)로 실행한다. Java 기준 파일은 이후 정책 변경 때 근거 없이 다시 만들지 않는다. 실제 ARM64 기기의 터치 지연·프레임·메모리 비교와 화면/IME 회귀 확인은 아직 별도 작업이다.
