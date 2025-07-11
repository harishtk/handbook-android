// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://repo1.maven.org/maven2")
    }

    dependencies {
        classpath(libs.android.gradlePlugin)
        classpath(libs.google.services.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.ksp.gradlePlugin)
        classpath(libs.hilt.android.gradlePlugin)
        classpath(libs.androidx.navigation.safeargs.gradlePlugin)
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.dokka.gradlePlugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply(false)
    alias(libs.plugins.android.library) apply(false)
    alias(libs.plugins.android.nav.safeargs) apply(false)
    alias(libs.plugins.android.test) apply(false)
    alias(libs.plugins.firebase.crashlytics) apply(false)
    alias(libs.plugins.google.services) apply(false)
    alias(libs.plugins.protobuf) apply(false)
    alias(libs.plugins.hilt) apply(false)
    alias(libs.plugins.ksp) apply(false)
    alias(libs.plugins.kotlin) apply(false)
    alias(libs.plugins.compose) apply(false)
    alias(libs.plugins.dokka) apply(false)
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}