plugins { alias(libs.plugins.android.application) }

val localTestingSigning = providers.gradleProperty("localTestingSigning").orNull == "true"
val distributionSigning = providers.gradleProperty("distributionSigning").orNull == "true"
require(!(localTestingSigning && distributionSigning)) {
    "Choose either local testing or distribution signing, not both."
}

android {
    namespace = "dev.typing.practice"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.typing.practice"
        minSdk = 28
        targetSdk = 37
        versionCode = 92
        versionName = "0.30.3"
        testInstrumentationRunner = "dev.typing.practice.InputInstrumentation"
    }
    signingConfigs {
        if (distributionSigning) {
            fun requiredEnv(name: String) = providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }
                ?: throw GradleException("Missing required signing environment variable: $name")
            create("distribution") {
                storeFile = file(requiredEnv("CHAGOK_RELEASE_STORE_FILE"))
                require(storeFile!!.isFile) { "Distribution keystore file does not exist." }
                storePassword = requiredEnv("CHAGOK_RELEASE_STORE_PASSWORD")
                keyAlias = requiredEnv("CHAGOK_RELEASE_KEY_ALIAS")
                keyPassword = requiredEnv("CHAGOK_RELEASE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = when {
                localTestingSigning -> signingConfigs.getByName("debug")
                distributionSigning -> signingConfigs.getByName("distribution")
                else -> null
            }
        }
    }
}
dependencies {
    implementation(project(":ui"))
    implementation(project(":practice"))
    implementation(project(":motion"))
    implementation(project(":history"))
    implementation(libs.core.splashscreen)
    testImplementation(libs.junit)
}




































