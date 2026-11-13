plugins { alias(libs.plugins.android.library) }
android {
    namespace = "dev.typing.history"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
}
dependencies { testImplementation(libs.junit) }
