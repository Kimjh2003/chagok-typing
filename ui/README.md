# 공통 UI 모듈

`:ui`는 Android 기본 View를 사용하는 공통 디자인 요소를 제공합니다. `UiControls`는 버튼·선택·라벨·결과 카드·확인 창을, `PracticeInputView`와 `SubmitEditText`는 제시문과 입력 UI 및 엔터 요청을 제공합니다. `UiTokens`와 `UiPalettes`는 크기와 색상, `AdaptiveLayoutPolicy`는 창 크기 기반 배치 기준을 관리합니다.

입력 내용·점수·저장·내비게이션은 앱과 기능 모듈에서 소유합니다. 공통 모션은 `:motion`, 밀도 변환은 `:common`을 사용합니다. 폴더블 힌지의 실제 위치를 감지하지는 않습니다.

자세한 규칙은 [디자인 시스템](../DESIGN_SYSTEM.md)과 [엔터 완료](../ENTER_COMPLETION.md)를 참고해 주세요.

```powershell
.\gradlew.bat :ui:testDebugUnitTest
```
