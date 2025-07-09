plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.codingfeline.buildkonfig") version "0.17.1" apply false
//    classpath("c:latest_version")
//    alias(libs.plugins.) apply false

}