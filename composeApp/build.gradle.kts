import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "net.bonysoft.bonusstage.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources {
            enable = true
        }
        withHostTestBuilder {}
    }

    // rcheevos rhash for the ROM scan; on Android it is built with CMake in androidApp. On iOS it
    // is built into a static library with Xcode's clang (macOS only, like every Apple cinterop).
    val rcheevos = rootProject.file("third_party/rcheevos")
    val interopDir = file("src/nativeInterop/cinterop")
    val rhashSources = listOf(
        "src/rc_compat.c", "src/rhash/aes.c", "src/rhash/cdreader.c", "src/rhash/hash.c",
        "src/rhash/hash_disc.c", "src/rhash/hash_encrypted.c", "src/rhash/hash_rom.c",
        "src/rhash/hash_zip.c", "src/rhash/md5.c",
    ).map { rcheevos.resolve(it) } + interopDir.resolve("bs_rhash.c")

    // Each iOS target with its Xcode SDK and clang target; the minimum version matches the Xcode project.
    val iosTargets = mapOf(
        iosArm64() to ("iphoneos" to "arm64-apple-ios16.0"),
        iosSimulatorArm64() to ("iphonesimulator" to "arm64-apple-ios16.0-simulator"),
    )
    iosTargets.forEach { (iosTarget, sdkAndTriple) ->
        val (sdk, triple) = sdkAndTriple
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        val libDir = layout.buildDirectory.dir("rhash/${iosTarget.name}")
        val buildRhash = tasks.register<Exec>("buildRhash${iosTarget.name.replaceFirstChar(Char::uppercase)}") {
            inputs.files(rhashSources, interopDir.resolve("bs_rhash.h"))
            outputs.dir(libDir)
            val out = libDir.get().asFile
            val compile = rhashSources.joinToString(" && ") { source ->
                "xcrun --sdk $sdk clang -target $triple -O2 -I${rcheevos.resolve("include")} " +
                    "-I${rcheevos.resolve("src")} -I$interopDir -c ${source.path} -o $out/obj/${source.nameWithoutExtension}.o"
            }
            commandLine(
                "sh", "-c",
                "rm -rf $out && mkdir -p $out/obj && $compile && " +
                    "xcrun --sdk $sdk libtool -static -o $out/librhash.a $out/obj/*.o",
            )
        }
        iosTarget.compilations.getByName("main").cinterops.create("rhash") {
            includeDirs(interopDir)
            extraOpts("-libraryPath", libDir.get().asFile.path)
            tasks.named(interopProcessingTaskName) { dependsOn(buildRhash) }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.raApi)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.navigation3.ui)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            // Serves the bundled demo account (DemoEngine).
            implementation(libs.ktor.client.mock)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.work.runtime)
            implementation(libs.glance.appwidget)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.activity.compose)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.compose.ui.tooling)
}

sqldelight {
    databases {
        create("BonusStageDatabase") {
            packageName.set("net.bonysoft.bonusstage.db")
        }
    }
}

compose.resources {
    packageOfResClass = "net.bonysoft.bonusstage.resources"
}
