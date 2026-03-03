import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                implementation(libs.settings)
                implementation(libs.ktor.client.auth)

            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.compose)

                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.androidx.compose.ui.tooling.preview)

                implementation(libs.ktor.client.okhttp)

                implementation("io.coil-kt:coil-compose:2.6.0")
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
}

android {
    namespace = "com.travelguide"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.travelguide"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}