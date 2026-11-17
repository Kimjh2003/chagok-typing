# Play 프로텍트와 릴리즈 보안 검토

이 문서는 2026년 10월 27일의 0.26.7 테스트 APK와 병합 매니페스트를 대상으로 한 로컬 검토 기록입니다. Google 인증이나 Play 프로텍트 경고가 나타나지 않는다는 보증은 아닙니다.

- 당시 병합된 릴리즈 매니페스트에는 권한이 선언되어 있지 않았습니다. 기록은 앱 전용 SQLite에 저장하고 앱 데이터 백업은 비활성화했습니다.
- 런처에서 앱을 열 수 있도록 `MainActivity`를 외부에 공개했습니다. 추후 추가하는 컴포넌트는 공개가 필요한지 검토해야 합니다.
- 릴리즈 코드·리소스 축소가 활성화되어 있었습니다. 로컬 테스트 APK는 APK 서명 방식 v2 검증을 통과했지만, Android Debug 인증서를 쓰므로 정식 배포용 서명은 아니었습니다.
- 특정 소스 검색에서 네트워크 사용, WebView, 프로세스 실행, 동적 코드 로딩을 발견하지 못했습니다. 이는 전체 보안 감사를 뜻하지 않습니다.
- Play 스토어 배포 전에는 업로드 키와 Play App Signing, 해당되는 계정 확인 및 데이터 보안 정보를 확인해야 합니다. 키스토어와 비밀번호는 소스 저장소에 포함하지 않습니다.
- 다른 키로 서명한 앱은 보통 기존 설치본에 덮어씌울 수 없습니다. 기존 앱을 삭제하면 내부 기록도 삭제되므로 먼저 이전 방법을 검토해야 합니다.
- 새로운 앱이나 드물게 배포된 앱에는 검사 안내가 나타날 수 있습니다. 실제로 잘못된 유해 판정을 받은 경우에는 Google의 재검토 절차를 이용할 수 있습니다.

## 참고 자료

- [Android 앱 서명](https://developer.android.com/studio/publish/app-signing)
- [Google Play 데이터 보안](https://support.google.com/googleplay/android-developer/answer/9888380)
- [Google Play 앱 검증](https://support.google.com/googleplay/android-developer/answer/2992033)
- [Android 외부 공개 컴포넌트](https://developer.android.com/privacy-and-security/risks/access-control-to-exported-components)
