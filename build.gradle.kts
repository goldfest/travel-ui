plugins {
    // Этот плагин должен быть только в корневом build.gradle.kts
    kotlin("multiplatform").version("1.9.22").apply(false)
    id("com.android.application").version("8.2.1").apply(false)
    id("com.android.library").version("8.2.1").apply(false)
    id("org.jetbrains.compose").version("1.6.0").apply(false)
}