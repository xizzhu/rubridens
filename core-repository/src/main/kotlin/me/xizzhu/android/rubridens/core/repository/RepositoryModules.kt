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

package me.xizzhu.android.rubridens.core.repository

import androidx.room.Room
import com.squareup.moshi.Moshi
import me.xizzhu.android.rubridens.core.repository.local.ApplicationCredentialCache
import me.xizzhu.android.rubridens.core.repository.local.StatusCache
import me.xizzhu.android.rubridens.core.repository.local.UserCache
import me.xizzhu.android.rubridens.core.repository.local.UserCredentialCache
import me.xizzhu.android.rubridens.core.repository.local.room.AppDatabase
import me.xizzhu.android.rubridens.core.repository.local.room.RoomApplicationCredentialCache
import me.xizzhu.android.rubridens.core.repository.local.room.RoomStatusCache
import me.xizzhu.android.rubridens.core.repository.local.room.RoomUserCache
import me.xizzhu.android.rubridens.core.repository.local.room.RoomUserCredentialCache
import me.xizzhu.android.rubridens.core.repository.network.AccountsService
import me.xizzhu.android.rubridens.core.repository.network.AppsService
import me.xizzhu.android.rubridens.core.repository.network.InstanceService
import me.xizzhu.android.rubridens.core.repository.network.OAuthService
import me.xizzhu.android.rubridens.core.repository.network.StatusesService
import me.xizzhu.android.rubridens.core.repository.network.TimelinesService
import me.xizzhu.android.rubridens.core.repository.network.retrofit.RetrofitAccountsService
import me.xizzhu.android.rubridens.core.repository.network.retrofit.RetrofitAppsService
import me.xizzhu.android.rubridens.core.repository.network.retrofit.RetrofitInstanceService
import me.xizzhu.android.rubridens.core.repository.network.retrofit.RetrofitOAuthService
import me.xizzhu.android.rubridens.core.repository.network.retrofit.RetrofitStatusesService
import me.xizzhu.android.rubridens.core.repository.network.retrofit.RetrofitTimelinesService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val repositoryModule = module {
    single { Room.databaseBuilder(get(), AppDatabase::class.java, "app_database").build() }
    single<ApplicationCredentialCache> { RoomApplicationCredentialCache(get()) }
    single<StatusCache> { RoomStatusCache(get()) }
    single<UserCache> { RoomUserCache(get()) }
    single<UserCredentialCache> { RoomUserCredentialCache(get()) }

    single { Moshi.Builder().build() }
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    factory { (instanceUrl: String) ->
        Retrofit.Builder()
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .baseUrl("https://$instanceUrl")
            .build()
    }
    single<AccountsService> { RetrofitAccountsService() }
    single<AppsService> { RetrofitAppsService() }
    single<InstanceService> { RetrofitInstanceService() }
    single<OAuthService> { RetrofitOAuthService() }
    single<StatusesService> { RetrofitStatusesService() }
    single<TimelinesService> { RetrofitTimelinesService() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
    single<InstanceRepository> { InstanceRepositoryImpl(get()) }
    single<StatusRepository> { StatusRepositoryImpl(get(), get(), get()) }
}
