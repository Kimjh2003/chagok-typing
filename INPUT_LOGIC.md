# 입력 처리 구조 · 0.9

0.11: PracticeSession/TypingUnits/FinalScore/PromptRepository는 순수 JVM `practice/src/main/kotlin/dev/typing/practice/core/`로 이동했다. MainActivity와 MetricsFormatter는 앱에 남는다. 모듈 경계는 ARCHITECTURE.md가 최신 기준이다. 테스트는 practice의 PracticeLogicTest, app의 AppLogicTest로 나뉘며 Java 골든 결과는 앱 통합 검사에서 유지한다.

0.10 구현은 전부 Kotlin으로 이관했다. 여기서 정의한 작업 경로·계산 정책은 유지한다. 구현은 app/src/main/kotlin/dev/typing/practice/, 검증은 app/src/test/kotlin/dev/typing/practice/LogicTest.kt에 있다. 예문 배열은 PromptRepository에서 한 번만 초기화한다. Java 0.9에서 저장한 결과 해시와 비교하는 회귀 테스트를 추가했다.

목표: 화면 디자인과 계산 정책을 유지하면서 입력 경로 밖의 반복 작업을 줄이고, 입력·시간·표시·완료 처리를 구분한다. 0.8에서도 변경 범위 타수 집계는 이미 적용돼 있었으며 이번 변경에서 새로 도입한 것처럼 주장하지 않는다.

## 실행 경로

```text
사용자 IME → 네이티브 EditText의 편집/조합/표시
                 └ TextWatcher → PracticeSession의 변경 범위 집계

전경의 연습 화면에서만 200ms 간격
    시간/타수 숫자 → MetricsFormatter → 보이는 값이 바뀌면 TextView 갱신

완료 버튼
    입력 문자열 1회 확보 → 타이머 정지 → FinalScore 1회 생성 → 결과 표시
```

이 그림은 앱 로직의 책임 구분이다. Android의 실제 콜백/그리기 순서를 별도 스레드처럼 표현한 것은 아니다. 집계는 여전히 메인 스레드의 TextWatcher에서 동기 실행되므로 긴 붙여넣기의 변경 범위 처리는 그 길이에 비례한다. ‘어떤 입력에서도 지연 0’을 보장하지 않는다.

## 코드 역할

| 코드 | 책임 | 하지 않는 일 |
|---|---|---|
| `PracticeSession` | READY/RUNNING/PAUSED/FINISHED, 타수 차이 반영, 활성 시간, 완료 결과 보관 | View 접근, 입력 전체 보관/복사, 저장, 스케줄링 |
| `TypingUnits` | 자모 환산·분당 타수 산술 | IME 터치 수 측정, UI 갱신 |
| `MetricsFormatter` | 언어/유형 접두어와 표시 문자열 캐시 | 입력 텍스트 읽기, 원문 비교 |
| `FinalScore` | 완료 시 원문 일치율과 속도를 고정한 불변 결과 | 입력 중 채점, 타수 재집계 |
| `MainActivity` | IME·TextWatcher 연결, 수명주기·화면 조립, 표시 예약, 복원 | 입력 중 원문 비교/전체 화면 재생성 |
| `:ui` | 기존 컴포넌트 표시 | 세션·타이머·채점 소유 |

## 입력 한 번에서 수행하는 작업

1. 교체 전 범위의 환산 타수만 센다.
2. 교체 후 범위의 타수를 더하고 이전 범위의 타수를 뺀다.
3. 첫 비어 있지 않은 입력이면 측정 시작 시각을 기록한다.

이 경로에는 앱 코드의 전체 `toString`, 부분 문자열 생성, 원문 채점, 결과 객체 생성, 통계 문자열 구성, 디스크 I/O, 화면 재생성이 없다. 화면에서 이미 교체된 입력창의 늦은 콜백은 무시한다. IME 조합은 EditText가 보관하며 앱이 조합 문자열을 다시 쓰지 않는다.

## 0.8 대비 줄인 작업

- 홈·결과·백그라운드에서는 200ms 표시 작업을 예약하지 않는다. 화면 전환/일시 정지 때 예약을 제거한다.
- 이전에는 200ms마다 `String.format`으로 전체 문자열을 만든 뒤 비교했다. 이제 표시할 초/타수/반올림 속도가 달라진 때만 새 문자열을 구성한다. 언어/유형 접두어도 화면 생성 때 한 번 만든다.
- 완료 화면에서 전체 입력의 타수를 다시 세지 않고 집계된 값을 쓴다. 완료 결과는 한 번 생성하고 재사용한다. 프로세스 복원 시에는 저장된 입력으로 타수를 한 번 재구성한다.
- 입력을 모두 지웠더라도 ‘연습을 시작했다’는 상태는 별도로 유지한다. 빈 입력 상태로 일시 정지/복귀해도 시간을 정상 재개한다.
- 저장 시 선택 시작/끝을 모두 보관한다. 조합 span의 프로세스 복원은 이번 변경 범위 밖이다.

## 유지하는 계산 정책

- 현재 입력에 남은 자모 환산량 / 활성 경과 분. 삭제한 과거 타수 누적이나 물리적 키보드 터치 수가 아니다.
- 자모 가중치, 영어/공백/문장부호 정책은 0.8과 동일하다.
- 조합 중 글자도 표시와 환산량에 반영한다. 확정 글자만 별도 채점하는 기능은 아직 없다.
- 완료는 버튼 시점에 앱에 반영된 입력의 스냅샷이다. IME에 강제로 조합 완료를 요청하지 않는다.
- 정확도는 기존의 같은 위치 글자 비교를 유지한다. 편집 거리 기반 채점 개선은 별도 작업이다.
- 예문 공급 방식과 UI는 바꾸지 않는다.

## 검사

이전 Java 검사들은 Kotlin JUnit `LogicTest.kt`로 통합했다. `gradlew.bat :app:testDebugUnitTest`로 JVM에서 실행하며 실제 Android UI를 구동하지 않는다.

- 한글 조합 교체/삭제, 중간 삽입·교체·삭제 무작위 3,000건의 증분 집계와 전체 재계산 값 비교.
- 첫 입력 전 대기, 중복 일시 정지/재개, 백그라운드 시간 제외, 삭제 후 빈 입력 복원.
- 완료 후 시간·결과 동결 및 늦은 알림 무시, 0ms/빈 입력 결과.
- 100,000자 CharSequence의 한 글자 교체에서 변경된 두 글자만 읽고 전체 복사/부분 문자열을 만들지 않는지 확인. 이는 집계 함수의 작업 범위 검사이며 실제 ms 벤치마크가 아니다.
- 표시값이 같을 때 String 인스턴스 재사용 확인.
- 기존 현대 한글 11,172자의 분해 일관성 검사.

빌드와 Lint는 앱/UI 모듈 연결을 확인한다. 실제 ARM64 기기의 터치→표시 지연·GC·프레임 성능 개선 수치는 아직 측정하지 않았다. 구조상 제거한 작업과 체감 속도 개선의 실측은 구분한다.

## 0.17 — simplify changed-range unit counting
- Keep beforeChange/afterChange incremental counting, native IME composition, deferred cached metrics, completion-only scoring and background result storage unchanged.
- TypingUnits reads one UTF-16 code unit at a time. Every supported counted character is BMP; surrogate halves and supplementary characters contribute zero. This removes code-point decoding and prevents a high surrogate at the range boundary from reading the following code unit outside the changed range.
- ASCII has a direct branch. Compatibility consonant clusters use numeric branches instead of a string search. No per-character collections, snapshots or lookup-table initialization are added.
- Verify every BMP code unit against prior rules, split-surrogate edits, strict range access, existing all-Hangul/NFD and random edits, and the Java 0.9 golden fixture.
- This reduces operations structurally, not a measured claim of lower device latency. The 200ms cached statistics formatter and lifecycle routing are retained; changing visible cadence or adding native code requires measurement.

## 0.27.4 — current-text unit validation
Composition replacement, correction, deletion, paste, decomposed Hangul and completion are checked against explicit expected unit totals. The existing arithmetic and weighting policy are unchanged. Result screens call the quantity 환산 입력량; this is not a measurement of keyboard taps. Device IME behavior and performance remain to be measured.

## 0.27.6 — idle metrics scheduling
While the practice session is READY, no periodic metrics callback is scheduled. The first input transition to RUNNING schedules the existing 200ms ticker; pause, resume and screen teardown retain existing lifecycle controls. This removes idle wakeups; no latency measurement or database speedup is claimed.

## 0.28 — completion accuracy
New results use NFC-normalized Unicode code point Levenshtein distance divided by the longer text length (empty input yields zero). Over one million comparison cells, a positional fallback bounds completion work; this can score large pastes differently. Existing saved accuracy is never recalculated. Text-derived typing units and input callbacks remain unchanged.
