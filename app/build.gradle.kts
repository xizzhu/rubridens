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

import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
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
        applicationId = Configurations.applicationId

        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        versionCode = Versions.App.code
        versionName = Versions.App.name

        resourceConfigurations.addAll(Configurations.supportedLocales)
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

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = File("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())

                storeFile = File(rootDir, keystoreProperties.getProperty("KEYSTORE_FILE"))
                storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " debug"
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
    implementation(project(":core-repository"))
    implementation(project(":feature-auth"))

    implementation(Dependencies.Kotlin.coroutines)

    implementation(Dependencies.AndroidX.activity)
    implementation(Dependencies.AndroidX.annotation)
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.AndroidX.Lifecycle.common)

    implementation(Dependencies.Koin.core)

    implementation(Dependencies.materialComponent)

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
    testImplementation(Dependencies.AndroidX.Test.core)
    testImplementation(Dependencies.Koin.test)
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.Retrofit.Retrofit.core)
}
