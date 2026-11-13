plugins { alias(libs.plugins.android.library) }
android {
    namespace = "dev.typing.common"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
}
