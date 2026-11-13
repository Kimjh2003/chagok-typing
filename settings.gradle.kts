pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if (requested.id.id == "org.jetbrains.kotlin.jvm") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement { repositories { google(); mavenCentral() } }
rootProject.name = "TypingPractice"
include(":app", ":practice", ":ui", ":common", ":motion", ":history", ":smoke")
