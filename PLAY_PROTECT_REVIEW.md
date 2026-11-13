# Play Protect and release security review

Reviewed 2026-10-27 against the 0.26.7 test APK and its merged release manifest. This is a local review, not Google certification or a guarantee that Play Protect will not warn.

- No permissions are declared in the merged release manifest. The app stores history in its private SQLite database; backup is disabled.
- MainActivity is exported for the launcher; review any future exported components and keep private components non-exported.
- Release minification/resource shrinking is enabled. The local test APK passes apksigner verification using APK signature scheme v2, but its certificate is Android Debug and is not a production signing identity.
- No network use, WebView, process execution, or dynamic code loading was found by a targeted source search; this is not a complete security audit.
- For Google Play, configure a release upload key and Play App Signing, complete the applicable Play Console identity verification and accurate Data safety declarations before publication. Do not include keystores or passwords in source archives.
- A differently signed release cannot normally update the existing debug-signed installation. Removing the old installation deletes its private history, so plan record migration first.
- New or uncommon apps may be flagged even when benign. If Google incorrectly flags an app, request review through Google Play's app verification appeal process. Do not disable Play Protect or attempt to evade its checks.

References:
https://developer.android.com/studio/publish/app-signing
https://support.google.com/googleplay/android-developer/answer/9888380
https://support.google.com/googleplay/android-developer/answer/2992033
https://developer.android.com/privacy-and-security/risks/access-control-to-exported-components
