import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "net.bonysoft.bonusstage.raapi"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withHostTestBuilder {}
    }
    // Plain JVM target for the daily contract tests (ADR 0003), so CI can run them without Android.
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        // The live API check (ADR 0003) runs only through `contractTest`, never with the unit tests.
        testRuns.named("test") {
            executionTask.configure {
                filter { excludeTestsMatching("*ApiContractTest") }
            }
        }
        testRuns.create("contract") {
            executionTask.configure {
                filter { includeTestsMatching("*ApiContractTest") }
                outputs.upToDateWhen { false }
                testLogging { showStandardStreams = true }
            }
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
