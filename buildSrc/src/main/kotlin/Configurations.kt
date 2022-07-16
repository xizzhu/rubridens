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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.gradle.api.JavaVersion

object Configurations {
    const val applicationId = "me.xizzhu.android.rubridens"
    val supportedLocales = listOf("en")
}

object Versions {
    object App {
        const val code = 100
        const val name: String = "${code / 10000}.${(code % 10000) / 100}.${code % 100}"
    }

    object Sdk {
        const val classpath = "7.2.1"
        const val buildTools = "33.0.0"
        const val compile = 33
        const val min = 21
        const val target = 33
    }

    val java = JavaVersion.VERSION_11

    object Kotlin {
        val jvmTarget = java.toString()
        const val core = "1.7.10"
        const val coroutines = "1.6.4"
        const val kover = "0.5.1"
    }

    object AndroidX {
        const val activity = "1.5.0"
        const val annotation = "1.4.0"
        const val appCompat = "1.4.2"
        const val fragment = "1.5.0"

        object Test {
            const val core = "1.4.0"
        }
    }

    const val materialComponent = "1.6.1"

    const val mockk = "1.12.4"
    const val robolectric = "4.8.1"
}

object Dependencies {
    object Sdk {
        const val classpath = "com.android.tools.build:gradle:${Versions.Sdk.classpath}"
    }

    object Kotlin {
        const val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.core}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
        const val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.Kotlin.core}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.coroutines}"

        object Kover {
            const val classpath = "org.jetbrains.kotlinx:kover:${Versions.Kotlin.kover}"
        }
    }

    object AndroidX {
        const val activity = "androidx.activity:activity-ktx:${Versions.AndroidX.activity}"
        const val annotation = "androidx.annotation:annotation:${Versions.AndroidX.annotation}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val fragment = "androidx.fragment:fragment-ktx:${Versions.AndroidX.fragment}"

        object Test {
            const val core = "androidx.test:core:${Versions.AndroidX.Test.core}"
        }
    }

    const val materialComponent = "com.google.android.material:material:${Versions.materialComponent}"

    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
}
