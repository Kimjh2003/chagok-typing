# 화면 전환과 모션

모션 공통 코드는 `:motion` 모듈의 `UiMotion.kt`와 `ImeMotion.kt`에 있으며, 모듈 경계는 [MODULES.md](MODULES.md)를 참고해 주세요.

## 현재 동작

- 화면 진입은 460ms, 비동기 기록 표시에는 260ms를 사용합니다. 버튼을 누를 때는 140ms, 놓을 때는 300ms이며, 예측 뒤로 가기 취소는 240ms입니다. 이 동작들은 같은 완만한 곡선을 사용합니다.
- 선택 상태 배경은 들어올 때 260ms, 나갈 때 220ms로 바뀝니다. 위 시간은 앱에서 정한 값이며 Apple이 정한 공식 수치는 아닙니다.
- 기록 목록과 상세를 오갈 때는 방향을 알려주는 작은 가로 이동을 사용하고, RTL 언어에서는 방향을 반대로 적용합니다. 그 외 화면은 제자리에서 서서히 나타납니다. 결과 화면 진입은 별도의 시작 대기 없이 진행됩니다.
- 연습 화면에서 입력창과 실시간 수치는 진입이나 타이핑 중에 애니메이션을 적용하지 않습니다. 격려 문구만 서서히 나타나며, 데이터 로딩·저장·버튼 동작을 애니메이션이 지연시키지 않습니다.
- Android 11(API 30) 이상에서는 키보드의 시스템 애니메이션에 맞춰 연습 카드와 버튼을 이동합니다. Android 9·10은 기존 `adjustResize` 동작을 사용합니다. 키보드 높이를 임의로 정하지 않습니다.
- 시스템 애니메이션이 꺼져 있으면 속성 애니메이션을 생략합니다. 배경 상태 전환은 컨트롤을 만들 때의 설정을 기준으로 합니다.
- 기록 목록은 항목을 화면에 붙이기 전에 한 그룹으로 구성합니다. 완료 버튼은 공통 버튼 진동을 한 번 사용하고, 키보드의 완료 동작은 별도 확인 진동을 사용합니다.

이 동작은 Android 기본 View 렌더링을 이용하며 별도의 렌더링 엔진을 포함하지 않습니다. 실제 기기별 프레임 시간이나 시각적 편안함은 아직 측정하지 않았습니다.

## 참고 자료

- [Android ViewPropertyAnimator](https://developer.android.com/reference/android/view/ViewPropertyAnimator)
- [Android 키보드 애니메이션](https://developer.android.com/develop/ui/views/layout/sw-keyboard)
- [Apple 모션 디자인 가이드](https://developer.apple.com/design/human-interface-guidelines/motion)
