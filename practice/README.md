# 연습 로직 모듈 :practice

순수 Kotlin/JVM 라이브러리 · package `dev.typing.practice.core` · JVM bytecode target 1.8.

Android 플러그인·Context·View·Handler·IME에 의존하지 않습니다. 시간은 앱이 `LongSupplier`로 주입합니다. Android 앱은 minSdk 28을 유지하며 JVM 1.8 설정은 Android 버전 제한을 바꾸는 설정이 아닙니다.

| 코드 | 책임 |
|---|---|
| PracticeSession | 연습 상태, 활성 시간, 변경 범위 타수 집계, 완료 결과 보관 |
| TypingUnits | 자모 환산과 분당 타수 산술 |
| FinalScore | 완료 스냅샷과 위치 기반 일치율 |
| PromptRepository | 오프라인 샘플 예문 공급 |

UI 문구를 만드는 `MetricsFormatter`는 앱의 표시 계층에 남아 있습니다. 이 모듈은 숫자와 결과만 제공하며 문자열 상태 표시나 화면 이동을 하지 않습니다. 기본 예문 데이터는 제품 콘텐츠이므로 여기 유지합니다.

## 연결

```kotlin
// app/build.gradle.kts
dependencies { implementation(project(":practice")) }

// 앱에서
val session = PracticeSession(SystemClock::elapsedRealtime)
// TextWatcher: 교체 전/후 범위 전달
session.beforeChange(oldText, start, removedLength)
session.afterChange(newText, start, insertedLength)
// 전체 입력을 매번 복사하거나 결과 객체를 만들지 않는다.
```

`beforeChange`와 `afterChange`는 같은 편집의 전후로 호출합니다. 네이티브 입력창은 앱/UI 모듈이 관리합니다. 세션은 원문 편집 버퍼를 보관하지 않고 변경 범위를 그 자리에서 읽는다. 클래스는 메인 스레드에서 순차 호출하는 계약이며 내부 락이나 스레드 전환을 추가하지 않습니다.

## 상태·정책

- READY → 첫 비어 있지 않은 입력 → RUNNING.
- pause/resume은 PAUSED를 거쳐 활성 시간만 측정합니다.
- finish는 FINISHED로 고정하고 같은 결과를 재사용합니다.
- 타수/일치율 정책은 0.10과 동일합니다. 물리 키보드 터치 횟수나 삭제한 과거 입력은 집계하지 않습니다.

## 검사와 빌드

```powershell
.\gradlew.bat :practice:test :practice:jar
```

모듈 자체는 Android SDK 없이 컴파일·검사할 수 있는 소스/의존성으로 구성되어 있습니다. 전체 멀티프로젝트 구성의 환경 설정은 별개다. 첫 빌드에는 Kotlin JVM 컴파일러 의존성 설치가 필요할 수 있습니다.

0.11에서 Android 프로젝트를 포함하지 않는 별도 설정으로 SDK 환경변수를 비운 빌드/6개 테스트/JAR 생성을 확인했습니다.

결과 JAR: practice/build/libs/practice.jar. Kotlin 표준 라이브러리는 Gradle 의존성으로 전달되며 JAR 안에 합치지 않습니다.

연습 모듈의 단위 검사는 `src/test/kotlin/dev/typing/practice/core/PracticeLogicTest.kt`에 있습니다. 표시 캐시와 Java 0.9 골든 비교처럼 앱 표시 계층을 함께 사용하는 검사는 app/src/test/kotlin/dev/typing/practice/AppLogicTest.kt에 남깁니다. 비교 기준 해시는 변경하지 않습니다.
