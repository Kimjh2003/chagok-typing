plugins { alias(libs.plugins.android.library) }
android {
    namespace = "dev.typing.motion"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
}
dependencies { implementation(project(":common")) }
