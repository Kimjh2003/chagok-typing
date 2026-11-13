plugins { alias(libs.plugins.android.application) }
android {
    namespace = "dev.typing.smoke"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.typing.smoke"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1"
    }
}
