import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.konan.properties.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    id("com.google.osdetector") version "1.7.3"
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("com.google.gms.google-services")
    id("com.codingfeline.buildkonfig")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate{
        common {
            group("nonJs"){
                withAndroidTarget()
                withJvm()
                group("ios"){
                    withIos()
                }
            }
        }
    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        outputModuleName = "composeApp"
//        browser {
//            val rootDirPath = project.rootDir.path
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                outputFileName = "composeApp.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(rootDirPath)
//                        add(projectDirPath)
//                    }
//                }
//            }
//        }
//        binaries.executable()
//    }

    sourceSets {
        val desktopMain by getting

        val nonJsMain by getting{
            dependencies {
                implementation("androidx.paging:paging-common:3.3.6")
                implementation("io.github.huarangmeng:uuid:1.0.0")
                implementation(libs.room.paging)
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundeled)
            }
        }
        androidMain.configure {
            dependencies{
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)
                implementation("io.ktor:ktor-client-cio:3.1.0")
                implementation("androidx.core:core-splashscreen:1.0.1")
                implementation(libs.firebase.cloud.messaging)
                implementation(libs.work.runtime)
                implementation(libs.accompanist.android)
            }
        }


        commonMain.dependencies {
            implementation(libs.ksoup)
            implementation(libs.ksoup.ktor)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("io.coil-kt.coil3:coil-compose:3.1.0")
            implementation("io.coil-kt.coil3:coil-svg:3.1.0")
            implementation("io.ktor:ktor-client-core:3.1.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.1.0")
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.storage)
            implementation(libs.uri.kmp)

            implementation(libs.mediaplayer.kmp)
            implementation(libs.kermit.logger)

            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.material3.adaptive.navigation.suite)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.serialization.json)
        }
        desktopMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            val fxSuffix = when (osdetector.classifier) {
                "linux-x86_64" -> "linux"
                "linux-aarch_64" -> "linux-aarch64"
                "windows-x86_64" -> "win"
                "osx-x86_64" -> "mac"
                "osx-aarch_64" -> "mac-aarch64"
                else -> throw IllegalStateException("Unknown OS: ${osdetector.classifier}")
            }

            implementation("org.openjfx:javafx-base:19:${fxSuffix}")
            implementation("org.openjfx:javafx-controls:19:${fxSuffix}")
            implementation("org.openjfx:javafx-graphics:19:${fxSuffix}")
            implementation("org.openjfx:javafx-media:19:${fxSuffix}")
            implementation("org.openjfx:javafx-web:19:${fxSuffix}")
            implementation(libs.quartz)
            implementation("io.ktor:ktor-client-cio:3.1.0")
        }
        appleMain.configure {
            dependencies{
                implementation("io.ktor:ktor-client-darwin:3.1.0")
            }
        }
//        wasmJsMain.dependencies {
//            implementation("io.ktor:ktor-client-js:3.1.0")
//        }
    }
}

buildkonfig {
    packageName = "com.example.app"
    // objectName = "YourAwesomeConfig"
    // exposeObjectWithName = "YourAwesomePublicConfig"

    defaultConfigs {
        val keystore = project.rootProject.file("apikeys.properties")
        val properties= Properties().apply {
            load(keystore.inputStream())
        }
        val supabaseUrl = properties.getProperty("SUPABASE_URL")?:""
        val supabaseKey =  properties.getProperty("SUPABASE_KEY")?:""
        val webClientId =  properties.getProperty("WEB_CLIENT_ID")?:""
        buildConfigField(STRING,"SUPABASE_URL",supabaseUrl)
        buildConfigField(STRING,"SUPABASE_KEY",supabaseKey)
        buildConfigField(STRING,"WEB_CLIENT_ID",webClientId)
    }
}

android {
    namespace = "devoid.secure.dm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
//    android.buildFeatures.buildConfig =  true
    defaultConfig {
        applicationId = "devoid.secure.dm"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
//        val keystore = project.rootProject.file("apikeys.properties")
//        val properties= Properties().apply {
//            load(keystore.inputStream())
//        }
//        val supabaseUrl = properties.getProperty("SUPABASE_URL")?:""
//        val supabaseKey =  properties.getProperty("SUPABASE_KEY")?:""
//        val webClientId =  properties.getProperty("WEB_CLIENT_ID")?:""
//        buildConfigField("String","SUPABASE_URL",supabaseUrl)
//        buildConfigField("String","SUPABASE_KEY",supabaseKey)
//        buildConfigField("String","WEB_CLIENT_ID",webClientId)

    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room{
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    debugImplementation(compose.uiTooling)
//    ksp(libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

compose.desktop {
    application {
        mainClass = "devoid.secure.dm.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "devoid.secure.dm"
            packageVersion = "1.0.0"
        }
    }
}
