/*
 * Copyright (C) 2022 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp").version(Versions.ksp)
}

android {
    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }
    kotlinOptions {
        jvmTarget = Versions.Kotlin.jvmTarget
    }

    buildToolsVersion = Versions.Sdk.buildTools
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        ksp {
            arg("room.schemaLocation", "$projectDir/src/schemas")
            arg("room.incremental", "true")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")

        getByName("test").java.srcDirs("src/test/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true

        unitTests.all { test ->
            test.maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
        }
    }

    packagingOptions {
        resources.excludes.addAll(listOf(
                "META-INF/atomicfu.kotlin_module", "META-INF/AL2.0", "META-INF/LGPL2.1", "META-INF/licenses/*",

                // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-debug#debug-agent-and-android
                "win32-x86/attach_hotspot_windows.dll", "win32-x86-64/attach_hotspot_windows.dll"
        ))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = Versions.Kotlin.jvmTarget
    }
}

dependencies {
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.Kotlin.datetime)

    ksp(Dependencies.AndroidX.Room.compiler)
    implementation(Dependencies.AndroidX.Room.runtime)

    implementation(Dependencies.Koin.core)

    implementation(Dependencies.Retrofit.Moshi.core)
    ksp(Dependencies.Retrofit.Moshi.codegen)
    implementation(Dependencies.Retrofit.OkHttp.okhttp)
    implementation(Dependencies.Retrofit.Retrofit.core)
    implementation(Dependencies.Retrofit.Retrofit.moshi)

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
    testImplementation(Dependencies.AndroidX.Test.core)
    testImplementation(Dependencies.Koin.test)
    testImplementation(Dependencies.Retrofit.OkHttp.mockWebServer)
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.robolectric)
}
