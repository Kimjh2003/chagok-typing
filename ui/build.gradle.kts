plugins { alias(libs.plugins.android.library) }
android {
    namespace = "dev.typing.ui"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
}
dependencies {
    implementation(project(":common"))
    implementation(project(":motion"))
    testImplementation(libs.junit)
}
