# 차곡 UI 규약 v1.1

## 둥근 모서리 정렬

홈과 기록의 선택 항목 제목, 연습 화면의 제시문·내 입력 제목은 아래 버튼 또는 카드의 시작 곡선이 끝나는 지점과 제목 알약의 시작 곡선이 끝나는 지점을 맞춘다. 알약 높이에 따라 실제 반지름이 달라지므로 표시된 높이와 공통 16dp 반경을 기준으로 위치를 계산한다. 연습 현황 텍스트의 끝은 제시문 카드의 끝 곡선이 시작되는 지점에 맞춘다. RTL에서는 시작·끝을 반대로 적용한다.

## 런처 아이콘

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`의 Android 적응형 아이콘을 사용한다. 배경과 전경은 분리하고, OEM 런처가 제공하는 원형·스퀘어클·라운드 사각형 마스크를 그대로 따른다. 전경에 모서리 마스크, 외곽 테두리, 바깥 그림자를 직접 그리지 않는다.
- 전경·단색 레이어는 108dp 벡터 캔버스를 사용하며 중요 심볼은 중앙 66×66dp 안전 영역 안에 둔다. 사용자가 테마 아이콘을 켜거나 지원 런처를 쓰면 전용 단색 실루엣을 제공한다.
- 컬러 적응형 아이콘 배경과 전경은 `values/` 및 `values-night/` 팔레트를 사용해 시스템 라이트/다크 모드에 대응한다. 단색 레이어는 배경화면 기반 테마 색을 적용하는 Android/런처에 맡긴다.
- 앱은 Android 9(API 28) 이상을 지원하므로 Android 8 이전용 레거시 아이콘은 필요하지 않다. One UI의 부드러운 사각형 배경은 시각 참고로 삼되, 기기별 모양은 OEM 런처가 처리한다.

0.12는 하단 버튼 줄 규칙을 대체한다. 연습 입력 카드를 키보드 인셋 바로 위(8dp 간격)에 배치하고, 수동 완료는 ‘내 입력’ 제목 오른쪽, ‘연습 선택으로’는 연습 묶음 위쪽 오른편에 둔다. 홈에 공통 스위치 스타일의 ‘엔터로 완료’를 추가하며 기본 꺼짐, 선택 저장. Enter 처리와 접근 경로는 ENTER_COMPLETION.md가 최신 기준이다.

0.10: 공통 UI는 `ui/src/main/kotlin/dev/typing/ui/`의 UiTokens.kt, UiControls.kt, PracticeInputView.kt로 이관했다. 팔레트 테스트는 ui/src/test/kotlin/dev/typing/ui/PaletteTest.kt. 시각 규칙은 유지하며 아래 과거 Java 경로보다 이 안내가 우선한다.

0.8: 공통 UI 구현 위치를 `ui/src/main/java/dev/typing/ui/`로 이동했다. `UiTokens`, `UiControls`, `PracticeInputView`가 라이브러리 `:ui`의 API이며 `:app`에서 사용한다. 본문의 이전 경로/버전 기록보다 이 모듈 경계가 우선한다. 시각 규칙은 0.7.1을 유지하며 계산·타이머·예문·화면 이동은 앱 책임이다. 사용법/빌드는 ui/README.md 참조. 팔레트 검사는 ui/checks/UiPaletteCheck.java로 이동.

0.7.1: 좌상단 격려 문구는 기존 15sp 대신 18sp. 보조 글자색·보통 굵기·두 줄 구성 유지.

## 0.7 팔레트 통합 (아래 과거 청회색 규칙보다 우선)

별도 청회색 계열을 제거하고 원래 녹색 기조로 통일한다. 정보와 선택은 채움의 밝기·테두리·체크로 구분한다.

| 역할 | 새 색 |
|---|---|
| 주요 실행/선택 글자·테두리/입력 포커스 | #285B46 |
| 선택된 항목 배경 | #C8DCCD |
| 정보 항목 배경 | #F0F4EE |
| 정보 항목 테두리 | #B3C2B5 |
| 정보 항목 글자 | #45594A |
| 입력 기본 테두리 | #738676 |

정보 항목은 아주 옅은 세이지 바탕으로 표시하고 체크나 눌림 효과는 추가하지 않는다. 선택된 항목은 더 짙은 세이지 채움과 진한 녹색 테두리·체크를 유지한다. 주요 실행은 진한 녹색 채움이다. 눌림은 주요 버튼 흰색 25%, 밝은 표면 녹색 약 19% Ripple로 강화한다. 입력 글자/흰색 표면/화면 바탕과 레이아웃은 유지한다. 색 대비는 새 토큰으로 재검사하며 이전 색 수치를 현재 결과로 사용하지 않는다.

0.6.5: 결과 화면의 긴 자모 환산·삭제·일치율 계산 설명을 제거한다. 결과 지표/비교/다음 행동에 집중하며 상세 계산 기준은 README에 유지한다. 이전 규칙의 ‘결과 설명 유지’ 문구보다 우선한다.

0.6.4: 연습 요약 항목바는 내용 너비(wrap_content)를 사용한다. 좌우 12dp 여백은 유지하고 텍스트가 없는 오른쪽 공간까지 배경을 늘리지 않는다. 사용 가능 너비가 부족하면 줄바꿈한다.

0.6.3은 아래 0.6.2 브랜드 규칙을 대체한다. 연습 화면 좌상단에 ‘서두르지 말고, / 너만의 리듬으로.’를 15sp 보조 글자색으로 표시한다. 배경·테두리·클릭 기능은 없으며 홈 브랜드를 반복하지 않는다. 하단 연습 묶음은 유지한다.

0.6.2: 연습 화면의 시스템 상단 인셋 아래 오른쪽에 ‘차곡 / TYPE AT YOUR PACE’ 정보 라벨을 배치한다. 클릭 기능 없이 공통 청회색 스타일 사용. 하단 연습 묶음과 행동 영역은 기존 정렬을 유지하며 좁은 높이에서는 중앙 스크롤 영역을 사용한다.

## 0.6.1 시선 이동 개선 (0.6 상단 고정 규칙보다 우선)

- 요약과 제시문/입력을 같은 스크롤 영역으로 묶고 사용 가능한 높이의 하단에 정렬한다. 빈 공간은 묶음 위로 보내 키보드와 읽기/입력 영역의 거리를 줄인다.
- 하단 행동 두 개는 고정 유지. 통합 카드와 행동 사이 간격은 8dp.
- 실제 IME 인셋과 창 크기로 유효 영역을 결정하며 기기별 픽셀 오프셋으로 내리지 않는다.
- 내용이 길면 중앙 영역이 확장/스크롤되므로 요약 정보는 화면 밖으로 이동할 수 있다. 커서 접근과 입력 유지가 우선이다.
- 키보드가 닫혀 있어도 연습 묶음은 버튼 위 하단에 정렬한다. 홈/결과 배치는 변경하지 않는다.

## 0.6 연습 화면 간결화 (아래 v1 연습 화면 규칙보다 우선)

실기기에서 분리된 라벨·카드·큰 버튼이 입력 공간을 차지한다는 피드백을 반영했다. 홈/결과의 기존 역할별 색상은 유지한다.

- **상단 요약 한 개:** `한국어 · 문장 | 12초 · 47타 · 235타/분`. 청회색 13sp, 내부 여백 좌우 12dp/상하 8dp. 스크롤 밖 상단에 두어 입력 중 유지한다. 좁은 창/큰 글꼴에서는 글자를 강제로 축소하거나 말줄임하지 않고 자연스럽게 줄바꿈한다.
- **제시문·입력 통합 카드:** 흰색 표면과 단일 16dp 테두리. 내부에 작은 ‘제시문’ 라벨 → 원문 → 구분선 → 작은 ‘내 입력’ 라벨 → 네이티브 입력창 순서. 라벨은 별도 알약/테두리 없이 표시한다. 입력 포커스는 카드 외곽 녹색 2dp로 표시한다.
- **글자·공간:** 원문/입력은 같은 20sp, 추가 행간 4dp. 카드 내부 좌우 12dp, 텍스트 추가 여백 4dp. 연습 화면 좌우 여백 16dp, 입력 최소 높이 64dp. 단어 최소 1행, 문장/문단 최소 2행; 긴 내용은 잘라내지 않고 스크롤한다.
- **하단 행동 한 줄:** ‘완료’ 녹색과 ‘연습 선택으로’ 흰색을 동일 너비로 나란히 배치. 최소 높이 48dp, 글자 15sp, 간격 8dp. 고정 높이가 아닌 최소 높이여서 큰 글꼴은 줄바꿈/높이 확장한다. 두 행동 모두 IME 인셋을 제외한 하단 영역에 둔다.
- **중복 제거:** 모드/측정 카드를 따로 쌓지 않고, 큰 제시문/입력 라벨과 연습 중 긴 사용 설명은 제거한다. 결과의 타수 설명은 유지한다.
- **입력 경로 유지:** 표준 EditText, labelFor, 한글 조합·선택 처리 및 변경 범위 집계를 유지한다. 카드 활성 상태만 포커스에 따라 변경하며 입력 내용을 다시 쓰지 않는다.
- **한계:** 긴 문단·큰 글꼴·짧은 가로 창에서는 원문/입력 전체의 동시 노출을 보장하지 않는다. 요약·행동은 고정하고 중앙 카드만 스크롤한다. 키보드별 인셋과 폴더블 검증은 계속 필요하다.

아래 0.5 검증 기록은 당시 버전에 관한 것이며 0.6 실행 QA 통과를 뜻하지 않는다.

상태: 2026-10-26 작성한 제품 설계 기준. 0.5에서 공통 색상·컴포넌트와 입력창·주요 행동 영역을 먼저 적용했다. 아래 ‘구현 범위’에서 설계와 실제 적용을 구분한다. 이후 화면은 이 문서를 기준으로 확장하며 수치는 실기기 검증에 따라 조정한다. 과거 BUTTONS.md와 충돌하면 이 문서가 우선한다.

## 1. 기본 원칙

1. **읽는 곳과 누르는 곳을 구분한다.** 녹색은 행동/선택, 청회색은 정보, 흰색은 읽기/편집 표면.
2. **손의 위치를 고려한다.** 홈 선택은 하단, 연습의 주요 완료 행동은 키보드 위 유효 영역.
3. **입력이 우선이다.** 키보드, 조합, 커서를 보존하고 장식이나 채점이 표시를 기다리게 하지 않는다.
4. **공간이 바뀌어도 문맥을 유지한다.** 창 크기/키보드/접힘에 맞춰 배치하되 문장과 입력 상태를 새로 시작하지 않는다.
5. **같은 역할에는 같은 표현을 쓴다.** 새로운 색과 컴포넌트를 화면마다 임의로 추가하지 않는다.

## 2. 색상 토큰 (현재 라이트 팔레트)

구현 원본: `UiTokens.java`. 팔레트는 OS 브랜드를 그대로 복제한 것이 아닌 이 앱의 제품 선택이다.

| 역할 | 값 | 용도 |
|---|---|---|
| Canvas | #F6F8F4 | 화면 바탕 |
| Surface | #FFFFFF | 제시문·입력창·미선택 버튼 |
| Text | #1D2923 | 제목·본문·입력 글자 |
| Secondary text | #536258 | 힌트·보조 설명 |
| Action | #306451 | 주요 실행·선택 테두리·입력 포커스 |
| Selected | #E5EDE7 | 선택된 항목의 옅은 채움 |
| Action outline | #A1B3A6 | 보조 실행·미선택 테두리 |
| Info fill | #ECF1F6 | 라벨·상태·결과 배경 |
| Info line | #A6B7C8 | 정보·제시문 테두리 |
| Info text | #35495E | 정보 글자 |
| Input line | #708496 | 편집 가능한 영역의 기본 테두리 |
| Disabled | #E9ECE8 / #6F7973 | 비활성 배경/글자 |
| Error | #963B3B / #FFF3F1 | 향후 입력 오류 안내 글자/배경 |

정보 영역에는 선택 녹색 채움이나 Ripple을 쓰지 않는다. 입력창은 흰색이며 포커스 때만 녹색 테두리로 바뀐다. 오타와 시스템 오류는 의미가 다르다. 한글 조합 중인 글자를 시스템 오류처럼 빨갛게 표시하지 않는다.

## 3. 형태·간격·글자

| 항목 | 기본값 | 확장 규칙 |
|---|---|---|
| 모서리 | 16dp | 버튼·입력·카드·정보 라벨 공통 |
| 테두리 | 기본 1dp, 선택/입력 포커스 2dp | 키보드 포커스 버튼은 3dp |
| 화면 좌우 여백 | 24dp | 좁은 창에서는 추후 16dp까지 축소 검토 |
| 내부 여백 | 좌우 16dp, 위아래 12dp | 내용 줄바꿈에 따라 높이 증가 |
| 간격 | 4/8/12/16/24/32dp | 관련 요소 8dp, 섹션 구분 12~24dp |
| 조작 높이 | 최소 56dp | 고정 높이로 큰 글꼴을 자르지 않음 |
| 콘텐츠 최대 너비 | 680dp | 화면 크기가 아니라 현재 창 기준 |
| 라벨·설명 | 13sp | 시스템 글꼴·크기 존중 |
| 본문·버튼 | 16sp | 버튼은 굵게 |
| 부가 결과 | 18sp | 긴 값은 줄바꿈 |
| 제시문·입력 | 22sp | 동일 크기로 시선 왕복 부담 완화 |
| 홈 제목 | 34sp, 굵게 | 작은 창은 스크롤로 기능 접근 보존 |
| 핵심 결과 | 40sp, 굵게 | 큰 글꼴에서도 단위와 값이 잘리지 않게 확인 |

문장/입력은 왼쪽 시작 정렬, 버튼은 가운데 정렬. 시스템 글꼴을 우선하며 글자 폭을 강제로 줄이거나 이미지로 글자를 그리지 않는다. 글자 크기·행간 변경은 입력 중 매번 수행하지 않는다.

## 4. 공통 컴포넌트

### 주요/보조 실행 버튼
- 한 화면에서 가장 중요한 행동 하나를 녹색 채움으로 강조한다.
- 보조 행동은 흰색 배경·테두리·녹색 글자.
- 기본/눌림/포커스/비활성을 구분. 눌림은 시스템 Ripple, 그림자는 기본 제거.
- 같은 화면에서 주요 버튼을 여러 개 나열하지 않는다. 단어는 ‘연습 시작’, ‘완료하고 결과 보기’, ‘다음 연습’처럼 결과를 알 수 있게 쓴다.

### 단일 선택 그룹
- 언어·종류처럼 하나만 고르는 항목에 사용. 동일 너비, 8dp 간격.
- 선택은 옅은 녹색 배경 + 2dp 테두리 + 체크 표시. 색만으로 선택을 전달하지 않는다.
- 네이티브 RadioGroup/RadioButton의 checked 의미를 유지한다.
- 긴 번역/큰 글꼴에서 높이를 늘린다. 폭이 부족한 경우 향후 세로 그룹으로 전환하며 가로 스크롤에 필수 선택을 숨기지 않는다.

### 정보 라벨/상태/결과 카드
- 브랜드와 섹션 제목은 내용 너비, 상태와 결과는 내용 영역 너비.
- 모두 청회색 팔레트. 클릭·Ripple·버튼 접근성 역할을 주지 않는다.
- 실시간 숫자는 제자리에서 갱신. 텍스트가 커질 때 다른 요소가 움직이는지 검증한다.
- 결과 비교는 ‘제시문’, ‘내 입력’ 순서로 제목과 본문을 함께 묶는다.

### 제시문
- 흰색 표면·청회색 1dp 테두리·16dp 모서리.
- ‘제시문’ 라벨을 위에 둔다. 입력창과 같은 22sp, 추가 행간 4dp.
- 긴 문단은 전체 페이지 스크롤로 접근하며 가로 잘림이나 말줄임으로 원문을 숨기지 않는다.

### 입력창
- 흰색 표면·청회색 기본 테두리, 포커스 시 녹색 2dp. 기존 플랫폼 밑줄은 제거한다.
- ‘내 입력’ 라벨을 항상 표시하고 labelFor로 편집 영역에 연결한다. 힌트만으로 이름을 대신하지 않는다.
- 최소 높이 104dp. 단어/문장 최소 2행, 문단 최소 3행. 내용이 늘면 높이 확장.
- 기본 선택/커서/복사/붙여넣기와 IME 조합은 네이티브 EditText가 처리한다.
- 상태별로 표시: 빈 상태=힌트, 입력 중=실제 글자, 포커스=테두리, 비활성=회색. 오류 팔레트는 정의했지만 현재 타자 오류 검증 UI는 미구현.
- 오타 검증을 추가하더라도 조합 중인 글자를 확정 오타로 판정하지 않고, 입력 문자열 전체를 다시 쓰지 않는다.

## 5. 화면과 키보드

### 홈
브랜드 → 여유 공간 → 제목/설명 → 유연한 여백 → 언어/종류 선택 → 시작 → 짧은 안내. 넓고 높은 화면의 여백은 가변적이다. 좁거나 큰 글꼴이면 여백을 줄이고 스크롤로 모든 기능에 접근한다.

### 연습
모드 → 실시간 상태 → 제시문 → 내 입력 → 보조 행동/설명은 스크롤 영역. 주요 완료 버튼은 별도 하단 영역에 배치해 스크롤과 분리한다. 하단 영역 너비도 680dp로 제한한다.

키보드 높이는 추정하지 않고 시스템 인셋을 따른다. 하단 버튼은 IME 인셋을 제외한 유효 영역에 둔다. 짧은 화면에서는 모든 정보를 한 번에 보장하는 대신 입력·커서와 주요 행동에 우선순위를 두고 나머지는 스크롤로 접근한다. 플로팅 키보드는 일부 영역을 덮을 수 있으므로 별도 실기기 검증이 필요하다.

### 결과
완료 라벨 → 짧은 제목 → 핵심 타수 → 입력량/일치율/시간 → 원문/입력 비교 → 계산 기준 → 다음 연습/선택으로. 결과 정보는 청회색, 행동만 녹색. 긴 설명과 문단은 스크롤한다. 현재 주요 결과 행동은 스크롤 하단이며 고정 배치는 후속 검토한다.

## 6. 화면 크기·폴더블 정책 (설계 목표)

- 현재는 최대 너비를 제한한 단일 열. 기기 모델이나 세로/가로 플래그로 폰/태블릿을 판별하지 않는다.
- 넓은 창에서는 향후 연습 선택/기록 또는 제시문/보조 정보를 두 영역으로 나눈다. 큰 글꼴에서는 열을 줄인다.
- 접힘·가림 영역을 얻은 뒤 그곳에 입력창·중요 버튼이 걸리지 않도록 배치한다. 각도마다 UI가 튀는 애니메이션은 넣지 않는다.
- 키보드가 열리면 실제 남은 너비·높이로 재판단. 폴더블 아래 절반을 앱 소유 키보드로 가정하지 않는다.
- 크기 변경은 상태 변경이 아니다. 입력·조합·선택·타이머·스크롤 맥락을 유지하는 것을 목표로 한다. 프로세스 재생성의 조합 span/선택 범위 복원은 아직 한계가 있다.

## 7. 접근성·움직임·성능

- 색 외에 체크·라벨·테두리로 역할을 전달하고 실제 버튼 의미를 유지.
- 일반 글자 대비 목표 4.5:1, 입력창 경계/포커스 목표 3:1. 정보 카드의 옅은 외곽선은 장식이며 정보 구분을 색선 하나에 의존하지 않는다. 모든 상태가 접근성 인증을 통과했다고 주장하지 않는다.
- TalkBack으로 선택값·입력 라벨을 확인. 실시간 타수를 매번 읽는 live region은 사용하지 않는다.
- 100/130/200% 글꼴, 좁은 창, 가로, 키보드 열림/닫힘을 확인. 조작 영역은 최소 56dp 유지.
- 문자 입력에 페이드/확대/애니메이션을 넣지 않는다. 전환 모션 추가 시 짧고 중단 가능하게 하고 시스템 모션 설정을 존중한다. 현재 커스텀 전환 모션은 없음.
- 상태별 배경은 생성 때 준비하고 포커스 변화는 drawable state로 처리. 입력마다 배경/카드/전체 화면을 재생성하지 않는다.
- 입력 이벤트는 변경 범위 집계만 수행. 통계 표시는 200ms 주기, 같은 표시 문자열이면 setText 생략.
- 비동기 계산·디스크 작업은 입력 반영을 막지 않으며 실제 성능은 릴리스 빌드로 검증한다.

## 8. 구현 범위와 확인 기준

| 항목 | 0.5 상태 |
|---|---|
| 역할별 팔레트·형태·공통 토큰 | 적용 |
| 공통 버튼/선택/정보/제시문/입력 컴포넌트 | 적용 |
| 입력 기본/포커스/비활성 스타일·외부 라벨 | 적용 |
| 연습 주요 완료 버튼의 별도 하단 영역 | 적용, 실기기/IME별 확인 필요 |
| 홈 선택 하단 배치·창 너비 제한 | 적용 |
| 한글 IME 조합 보존·상태 연속성 | 네이티브 입력 사용, 기기별 검증 계속 필요 |
| 오류/오타 안내, 앱 내 글자 크기 조절 | 설계만 정의, 기능 미구현 |
| 태블릿 2열·힌지 회피·다크 팔레트 | 후속 작업 |
| UI 규약 전체의 접근성/성능 통과 | 미인증, 별도 검증 필요 |

새 화면 검토 순서: 역할에 맞는 컴포넌트 → 필수 행동 접근 → 작은 창/큰 글꼴/키보드 → 한글 조합 → 프레임·할당·지연. 새 컴포넌트는 먼저 역할·상태·토큰·접근성·갱신 범위를 이 문서에 정의한다.

## 9. 이번 변경의 검증 기록

- 0.5 디버그 APK 빌드와 Android Lint 통과. 기존 백업 설정/앱 아이콘 관련 경고는 남아 있다.
- checks/UiPaletteCheck.java의 8개 색 대비 검사 통과: 본문/바탕 14.10:1, 입력 글자 15.07:1, 힌트 6.45:1, 정보 8.16:1, 주요 버튼 6.85:1, 선택 글자 5.74:1, 입력 경계 3.87:1, 입력 포커스 6.85:1.
- 이 수치는 지정한 불투명 sRGB 토큰 조합의 계산 결과다. OEM 글꼴 렌더링, 모든 접근성 상태, 실제 표시 품질을 보증하지 않는다.
- 에뮬레이터 부팅/서비스 지연으로 이번 0.5 화면의 실행 QA는 완료하지 못했다. 스크린샷은 이전 0.4 실기기 피드백이며 0.5 결과 이미지로 사용하지 않는다.
- 다음 실기기 확인: 키보드가 열린 상태에서 입력창/커서와 완료 버튼 접근 → 문단 스크롤 → 글꼴 확대/가로 창 → 한글 조합/선택/삭제 → 결과 화면. 다크 테마와 힌지 회피는 미구현으로 따로 취급한다.

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

## 0.15 — matched prompt/input space
- Prompt and input text bodies have equal heights, measured from the prompt's actual wrapping at the current width/font size; minimum body height 48dp.
- Use the same typeface, text size, line spacing and body padding for both. Remove the old fixed 64dp/two-line editor minimum.
- Input exceeding the prompt's space scrolls within the native editor. Long cards retain the outer scroll; pinned actions stay available. No text mutation, scoring or extra text copies are introduced.
- Re-measure after width/font changes; update editor height only when the measured prompt height changes.
- Practice encouragement increases from 18sp to 20sp.

## 0.16.1 — dismiss keyboard before leaving practice
- Overrides 0.14's immediate return with the IME open: first Back dismisses the system IME, keeping the same practice view, entered text, selection and session. Back after the keyboard is hidden returns to selection.
- Android 13+ uses PRIORITY_DEFAULT so the IME can consume Back first. Android 9–12 uses native editor/IME dispatch without a custom pre-IME interceptor; Activity Back handles navigation after IME dismissal.
- The existing bottom action row follows system/IME insets. Dismissing the IME moves 완료 / 연습 선택으로 to the screen bottom without re-rendering the practice scene. The explicit selection button still returns directly to selection.
- Result/history Back behavior and local result storage are unchanged. Timing continues while practice stays visible with the keyboard hidden.

## 0.18 motion
Use UiMotion for short press and scene entry effects; selection surface fade is shared in UiControls. See MOTION.md. No animation on typed text or metric updates.

## 0.18.1 — softer motion tuning
Supersedes the 0.18 numeric motion values: press scale 0.99 over 110ms; release 220ms; cubic (0.25, 0.1, 0.25, 1). Destination entry moves 6dp with alpha 0.96→1 over 240ms. Surface state fades enter over 180ms and exit over 120ms. Actions still execute immediately. Typing, IME/inset behavior and the scope of animated views are unchanged. Device smoothness has not been measured; this is visual timing/amplitude tuning.

## 0.19 — synchronized IME motion
- :ui ImeMotion.install(root, targets) synchronizes the practice scroll area (summary/card) and pinned action container with system keyboard animation on Android 11/API30+.
- Root applies final system/IME padding through its existing inset listener. WindowInsetsAnimation.Callback applies only translationY = final padding minus current animated bottom inset; system duration/interpolation controls the motion, not an independent 240ms timer. It handles show/hide, resets on end/detach, and skips custom translation when animators are disabled.
- No alpha on the editor, text rewriting, scoring changes, custom keyboard heights, per-frame requestLayout, or interception of keyboard control. Native visibility/insets remain authoritative, including zero-inset floating keyboards.
- Android 9/10 keep existing adjustResize behavior without the new synchronized effect. On API30+ behavior still depends on the system/IME dispatching animation callbacks. Device frame smoothness and interrupted transitions remain to be validated physically.
- Reference: https://developer.android.com/develop/ui/views/layout/sw-keyboard

## 0.22 system dark theme
Use context-selected UiPalettes.from(context) for rendered colors; preserve UiTokens dimensions. Dark green/sage role colors, native values-night widgets and system bars are specified in DARK_MODE.md.

## 0.23 — language lists and separate record details
- Home → history has Korean/English choice tabs using the existing shared controls. Each language queries its own latest 100 records; filtering happens in SQLite before the limit.
- List rows contain only completion date/time, practice type, units/minute and accuracy. Prompt/input text is not loaded with the summary query.
- Tapping a UUID opens a separate in-app detail screen loaded by ID, including full prompt/input, units and active duration. Both system Back and 기록 목록으로 return to the originating language list.
- Each language retains its own scroll offset. Restore happens after asynchronous rows finish layout, not while loading. Language, record UUID and both list offsets survive Activity recreation via Bundle. Missing records and database read errors have explicit states; history content is read-only.
- Same SQLite schema/database and immutable scoring policy; no record migration or deletion. All screens use shared sage controls and context-selected light/dark palette.

## 0.23.1 — subdued dark actions
- User requested reducing the bright mint area first, deferring keyboard-hidden layout decisions. Layout/insets/scroll/navigation/motion timing are unchanged.
- Separate primary ACTION_FILL from the text/focus accent ACTION. Light fill remains the existing dark green. Dark primary fill is #223029 with soft text #D5DFD8 and a thin outline, replacing the large pale mint block.
- Dark text/focus accent is muted #A0B4A6, selected fill #26382D. Dark ripples use translucent black #33000000 rather than a pale/green overlay. Native dark cursor/accent is updated consistently.
- The panel has a dark tinted appearance using opaque solid fill; no blur, extra transparency layer or new offscreen rendering is introduced. Existing role contrast tests now check ON_ACTION against ACTION_FILL.
- Keyboard-hidden practice placement remains a pending design decision, not changed in this patch.

## 0.24 — reduce gaze travel when dismissing the keyboard
- On API30+, remember only a docked IME bottom actually observed in this window. When hidden, reserve the former IME area minus 48dp, keeping the prompt/editor and action row together near their prior position. Keep the existing 8dp gap within the group; do not move actions independently.
- Clamp the retained reserve to leave at least 320dp of vertical window space (minus system top inset). Narrow/short windows retain scrolling and may move further to preserve usable space. Reset old IME memory on actual window geometry changes; floating IME with zero bottom inset clears docked reserve.
- The motion module accepts an optional effective-bottom provider so animation progress uses the same reserved-space policy as final layout. This avoids a jump at animation end. No per-frame layout or text mutations are added.
- Hide the small upper hint while the IME is visible; when hidden show: 입력칸을 누르면 이어서 연습할 수 있어. Keep the existing encouragement. No new cards/decorations.
- Persist observed keyboard geometry with other practice Bundle state. API28/29 retain native adjustResize and omit the hint; the new limited-travel behavior requires API30 inset visibility/animation.
- Timing continues while practice remains visible with keyboard hidden, as before. Back still dismisses keyboard first, then returns to selection. Palette, storage and scoring are unchanged.

## 0.24.1 — compact practice typography and reachable long content
- Practice prompt/editor reduce from20sp to18sp, with shared3dp extra line spacing; encouragement reduces20sp→18sp. Labels, system font scaling, equal prompt/editor body heights, buttons and other screens are unchanged.
- Practice column now measures its natural content height (WRAP_CONTENT) while minimumHeight still fills the scroll viewport. Short exercises remain bottom-aligned. Long paragraphs can grow beyond the viewport and scroll from their true top, instead of bottom-aligning oversized children inside a forced viewport-height column and placing the summary above the scrollable origin.
- Actions remain outside the scroll area. The limited keyboard-hide travel policy and input/scoring state remain unchanged. Long content may still require scrolling; it is not truncated or made arbitrarily tiny to force-fit.

0.24.1 validation: 19 JVM tests and app/UI release lint passed. Optimized release paragraph smoke test passed in a 1080x1400 emulator window: after scrolling to the top, the language/mode/time/rate summary was within the scroll viewport; completion/save/history flow also passed. System UI's startup ANR overlay was dismissed before the passing run. Physical-device font comfort and scrolling remain subject to review.

## 0.24.2 — truly equal, unclipped prompt/input areas
- Previously the input height was capped at the prompt's measured height, although TextView and EditText can use different line breaking and native cursor/font padding. Equal outer bounds did not guarantee enough space for the input's last line.
- Configure native multiline inputType before applying shared typography. Use identical simple line-breaking, no hyphenation, typeface, font padding, text size, spacing and horizontal padding. Disable the inner editor scrollbar; the shared outer scroll handles long cards.
- Measure both native text layouts at natural height, then assign both bodies the greater height. Extra/wrapped/composing input can grow both areas; deletion can shrink them again. No snapshots or scoring work are added to input callbacks. Larger system fonts remain respected.
- Result screen uses one shared compact action row: 다음 연습 / 연습 선택으로. Practice actions and history navigation are unchanged.

0.24.2 validation: 19 JVM tests and release lint passed; 16 device checks passed, including equal body heights and full last-line capacity for matching paragraphs, longer composing input and24sp text. Optimized release paragraph completion/save/history smoke also passed after dismissing emulator launcher/System UI ANR overlays. Physical Samsung IME composition and font comfort remain to be reviewed on device.

## 0.25 — separate equal practice cards
- Replace the shared outer border/divider with two independent rounded cards: prompt and input, separated by8dp. The outer PracticeInputView only groups them; it has no fill/border/padding.
- Both use the same width, shared card padding, native typography and measured body height. Measure natural bodies, share their maximum, and equalize total card heights when headers require extra space. Existing 48dp body minimum remains, with no fixed paragraph height.
- Input focus highlights only the input card. Prompt retains a quiet outline. Both grow together for longer/composing input and remain inside the shared outer scroll; no scoring, IME callback or stored-data changes.
- Result actions stay side-by-side from0.24.2. Keyboard-hide positioning, motion timing and light/dark palettes are unchanged.

0.25 verification: 19 JVM tests, R8 release build and app/UI release lint passed. All16 device checks passed; matching paragraph, longer composing input and large-font cases assert equal body heights, equal total card widths/heights, and sufficient last-line capacity. Device assertions ran against the debug app; the new optimized release was built but not separately smoke-tested this patch. Physical visual review remains pending.

## 0.26 — inline practice metrics and denser equal cards
- Remove the standalone summary pill. The prompt header contains 제시문 at start and right-aligned language/mode/elapsed/rate (11sp). Raw entered-unit count is omitted only from live compact metrics; result/history units and calculation are unchanged. Metadata wraps instead of ellipsizing if width/font scale requires it; both headers/cards retain equal heights.
- Practice cards use10dp horizontal/4dp vertical padding,6dp separation,2dp label vertical padding,4dp body bottom padding and40dp minimum body height. Retain18sp main text and native line wrapping. Both natural bodies and headers are equalized before total-card measurement.
- Practice actions use13sp and a visually inset40dp surface within a48dp minimum touch target. Results keep the existing adjacent action row.
- Cursor reveal expands to the full input card when the card fits inside its scroll viewport, helping avoid a clipped bottom border above pinned actions. Oversized content remains scrollable, with no unconditional forced full-card scrolling.
- Compact formatter caches only visible values; legacy default formatting is retained for the Java0.9 golden fixture. No scoring/storage/input-text changes or per-keystroke formatting are introduced.

0.26 validation: 20 JVM tests and release lint passed. All16 device checks passed with wrapped metadata, equal card/body sizing and last-line capacity. Optimized release paragraph smoke passed with an observed Gboard IME window and full prompt text entered. Screenshot typing-compact-paragraph-0.26.png was visually inspected: both complete card outlines, all three text lines, inline metadata and actions are visible above the keyboard. Longer paragraphs/larger font scales may still need outer scrolling. Initial emulator System UI ANR overlay was dismissed before tests.

## 0.26.3 practice labels
Prompt and input labels sit above their respective equal-sized content cards; metrics remain beside the prompt label. Long content continues to use the outer scroll viewport.

## 0.26.5 compact section tags
Home section labels and practice prompt/input labels share a compact noninteractive outlined tag, with text retained for clarity and accessibility.

## 0.27.14 — compact practice viewport (initial step)
When usable practice height is below 320dp, the decorative encouragement header is hidden to free vertical space. The native scroll view and actions remain. This is not yet a two-pane adaptive layout or fold/hinge detection; real-device landscape, IME and resize testing remain outstanding.

## 0.27.15 — compact-height practice layout
When practice viewport is under 260dp high and usable content width is at least 600dp, show prompt and editor columns side by side, unless system font scale exceeds 1.3. Layout changes reuse the same editor instance and native IME. Otherwise retain stacked layout and scrolling. Fold/hinge-aware layout and landscape variants for other screens are not implemented. Device validation remains necessary.

## 0.27.17 — wide history preview
For history screens at configuration width 600dp or greater (font scale <=1.3), show the history list alongside an independently scrollable selected record. Narrow windows keep separate detail navigation. Re-layout on configuration change preserves stored list positions. Initial implementation uses a width heuristic and does not yet detect folds or hinges; validate actual window resizing, foldables and RTL on devices.

## 0.28.2 — launch splash
- The launcher activity uses AndroidX Core SplashScreen so Android 9–11 and Android 12+ share the platform launch flow; no separate splash Activity or duplicate scene is added.
- The centered splash mark is a static vector composition of the launcher key motif, “차곡 타자”, and “서두르지 말고, / 너만의 리듬으로.” All content stays within the Android splash icon safe area. Colors follow the light/dark sage palettes.
- On a fresh activity launch, keep the native splash visible for at least 650ms so the brand can register; this is a bounded presentation interval, not a wait for storage or network. Restored Activity instances add no extra hold. Android controls the normal launch and exit motion.
## 0.30.0 — compact history selection

- Place the record count and selection entry together; selection mode shows select all, delete and cancel on one 48dp-minimum control row.
- Use the shared 12dp section gap between the average card, selection row and record cards. Select all applies to the currently displayed filtered records (up to 100).
- Keep existing language/type filters, detail navigation, long press, confirmation and restored history scroll positions. On small windows and enlarged text, controls can wrap; physical-device layout verification is pending.

# Section labels over rounded cards

Use the shared `sectionTagAligned` for section pills directly above rounded cards or choices, including practice, results, and history detail. Its start tangent aligns with the card's corner tangent using the measured pill height, card radius, and layout direction. Standalone tags without an associated card may use `sectionTag`.

Use `LABEL_SECTION_GAP` (12dp) before a labeled section and `LABEL_CONTENT_GAP` (8dp) between the label and its card or choices. In side-by-side practice, the latter is 2dp to preserve keyboard viewport space. Avoid adding an extra bottom margin to the preceding element. The first section in a pane can omit its leading gap where its layout requires it.

