# 연습 로직 모듈

`:practice`는 Android에 의존하지 않는 Kotlin/JVM 모듈입니다. `PracticeSession`이 상태와 시간을 관리하고, `TypingUnitPolicy`가 언어별 타수를 계산합니다. `FinalScore`는 완료 결과와 일치율을, `PromptRepository`는 오프라인 제시문을 제공합니다.

앱은 세션을 생성할 때 시간을 공급하고 입력 변경 전후 범위를 전달합니다. 키보드, 화면 표시, 설정, 저장과 내비게이션은 이 모듈의 책임이 아닙니다. 정책과 계산 방식은 [입력 처리 설명](../INPUT_LOGIC.md)을 참고해 주세요.

```powershell
.\gradlew.bat :practice:test
```
