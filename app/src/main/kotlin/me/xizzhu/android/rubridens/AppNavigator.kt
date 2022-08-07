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

package me.xizzhu.android.rubridens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.xizzhu.android.rubridens.auth.AuthActivity
import me.xizzhu.android.rubridens.core.infra.Navigator
import me.xizzhu.android.rubridens.core.model.Media
import me.xizzhu.android.rubridens.core.model.Status
import me.xizzhu.android.rubridens.core.model.User
import me.xizzhu.android.rubridens.home.HomeActivity

class AppNavigator : Navigator {
    override fun goToAuthentication(activity: Activity) {
        activity.startActivity(AuthActivity.newStartIntent(activity))
    }

    override fun goToHome(activity: Activity) {
        activity.startActivity(HomeActivity.newStartIntent(activity))
    }

    override fun gotoMedia(activity: Activity, media: Media) {
        // TODO
    }

    override fun goToStatus(activity: Activity, status: Status) {
        // TODO
    }

    override fun goToTag(activity: Activity, tag: String) {
        // TODO
    }

    override fun gotoUrl(activity: Activity, url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
    }

    override fun gotoUser(activity: Activity, user: User) {
        // TODO
    }
}
