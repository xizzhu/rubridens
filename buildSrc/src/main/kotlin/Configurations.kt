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
        const val target = 32
    }

    val java = JavaVersion.VERSION_11

    object Kotlin {
        val jvmTarget = java.toString()
        const val core = "1.7.10"
        const val coroutines = "1.6.4"
        const val datetime = "0.4.0"
        const val kover = "0.5.1"
    }

    object AndroidX {
        const val activity = "1.5.0"
        const val annotation = "1.4.0"
        const val appCompat = "1.4.2"
        const val core = "1.8.0"
        const val lifecycle = "2.5.0"
        const val room = "2.4.2"

        object View {
            const val constraintLayout = "2.1.4"
            const val recyclerView = "1.2.1"
            const val swipeRefreshLayout = "1.1.0"
        }

        object Test {
            const val core = "1.4.0"
        }
    }

    const val koin = "3.2.0"
    const val ksp = "1.7.10-1.0.6"
    const val materialComponent = "1.6.1"

    object Retrofit {
        const val moshi = "1.13.0"
        const val okhttp = "4.10.0"
        const val retrofit = "2.9.0"
    }

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
        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.Kotlin.datetime}"
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
        const val core = "androidx.core:core-ktx:${Versions.AndroidX.core}"

        object Lifecycle {
            const val common = "androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.lifecycle}"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
        }

        object Room {
            const val compiler = "androidx.room:room-compiler:${Versions.AndroidX.room}"
            const val runtime = "androidx.room:room-ktx:${Versions.AndroidX.room}"
        }

        object View {
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.View.constraintLayout}"
            const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.View.recyclerView}"
            const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.View.swipeRefreshLayout}"
        }

        object Test {
            const val core = "androidx.test:core:${Versions.AndroidX.Test.core}"
        }
    }

    object Koin {
        const val core = "io.insert-koin:koin-android:${Versions.koin}"
        const val test = "io.insert-koin:koin-test:${Versions.koin}"
    }

    const val materialComponent = "com.google.android.material:material:${Versions.materialComponent}"

    object Retrofit {
        object Moshi {
            const val core = "com.squareup.moshi:moshi:${Versions.Retrofit.moshi}"
            const val codegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.Retrofit.moshi}"
        }

        object OkHttp {
            const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.Retrofit.okhttp}"
            const val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.Retrofit.okhttp}"
        }

        object Retrofit {
            const val core = "com.squareup.retrofit2:retrofit:${Versions.Retrofit.retrofit}"
            const val moshi = "com.squareup.retrofit2:converter-moshi:${Versions.Retrofit.retrofit}"
        }
    }

    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
}
