# 배포와 서명 준비

## 현재 상태

- 패키지 이름은 `dev.typing.practice`다. 현재 로컬 실기기 APK는 `-PlocalTestingSigning=true`로 디버그 키에 서명한 테스트용 R8 릴리스다.
- 앱의 병합 매니페스트에는 네트워크나 민감 권한이 선언되어 있지 않고, 런처 Activity만 내보낸다. 배포 전에 최종 빌드의 병합 매니페스트를 다시 확인한다.
- Google Play 프로텍트의 검사 권고와 기기의 외부 출처 경고는 앱이 끌 수 있는 옵션이 아니다. 서명 변경이나 코드 수정으로 경고가 사라진다고 보장할 수 없다.

## 별도 배포 서명

키스토어를 저장소 밖에 두고 안전하게 백업한 다음, 현재 PowerShell 세션에 `CHAGOK_RELEASE_STORE_FILE`, `CHAGOK_RELEASE_STORE_PASSWORD`, `CHAGOK_RELEASE_KEY_ALIAS`, `CHAGOK_RELEASE_KEY_PASSWORD` 환경 변수를 직접 설정한다. 비밀번호를 문서, 소스, 채팅, 명령 기록에 남기지 않는다. 절대 경로를 사용한다.

설정 후 `gradlew :app:assembleRelease :app:bundleRelease -PdistributionSigning=true --offline`으로 서명된 APK와 AAB를 만들 수 있다. `-PlocalTestingSigning=true`와 함께 지정하면 빌드가 거부된다. 두 옵션이 모두 없으면 릴리스는 서명되지 않는다. 정확한 R8 `mapping.txt`를 각 APK/AAB와 함께 보관한다.

이 PC에는 `%LOCALAPPDATA%\ChagokSigning\chagok-release.p12` 배포 키와 `password.dpapi`가 만들어져 있다. 후자는 현재 Windows 사용자 계정에 묶여 있으므로 다른 PC나 Windows 재설치 후에는 복호화되지 않을 수 있다. `powershell -ExecutionPolicy Bypass -File .\release-signing.ps1 build`로 이 계정에서 릴리스 APK와 AAB를 빌드할 수 있다. `init`은 기존 파일을 덮어쓰지 않는다.

키스토어와 복구 가능한 비밀번호를 별도의 안전한 곳에 백업해야 한다. 비밀번호는 현재 계정의 PowerShell에서 `(Get-Content "$env:LOCALAPPDATA\ChagokSigning\password.dpapi" -Raw).Trim() | ConvertTo-SecureString`으로 복원한 SecureString으로 접근할 수 있다. 비밀번호 관리자 등 안전한 수단으로 옮기고 채팅, 소스 저장소, 공개 파일에 노출하지 않는다. 암호화된 파일만 복사하는 것은 충분한 백업이 아니다. Play 배포라면 Play App Signing의 앱 서명 키와 업로드 키 역할을 먼저 결정한다. 키스토어, 비밀번호와 개인키는 공개하거나 저장소에 추가하지 않는다.

**설치 기록 보존:** 기존 테스트 APK는 디버그 키로 서명돼 있다. 다른 키로 서명한 앱은 기존 설치본에 일반적인 방식으로 덮어씌울 수 없다. 제거하면 앱 내부의 연습 기록도 함께 삭제되므로, 서명 전환 전에 기존 설치와 기록을 보존할 방법을 먼저 결정해야 한다. Google Play에서 제공하는 앱 서명 키와 외부 APK 키도 각각 확인한다.

## 유통 경로

Google Play 내부 테스트를 사용하면 승인된 테스터에게 Play 스토어를 통해 배포할 수 있다. 계정 설정과 검토, 설치에 사용될 앱 서명 키 확인은 개발자가 Play Console에서 직접 진행한다. 자동 배포나 등록은 수행하지 않았다. Play 밖에서 배포한다면 Android Developer Console의 개발자 확인 및 패키지 등록 경로를 확인한다. 개발자 확인, 삼성의 설치 출처 판단, Play 프로텍트 검사는 서로 별개이므로 한 절차의 완료가 다른 경고의 부재를 보장하지 않는다.

관련 공식 자료:

- https://developer.android.com/studio/publish/app-signing
- https://support.google.com/googleplay/android-developer/answer/9845334
- https://support.google.com/googleplay/answer/2812853
- https://developer.android.com/developer-verification/guides
- https://www.samsung.com/us/support/answer/ANS10003636/
