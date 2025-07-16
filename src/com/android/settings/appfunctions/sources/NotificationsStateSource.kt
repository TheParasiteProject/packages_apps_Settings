/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.settings.appfunctions.sources

import android.app.INotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.ServiceManager
import com.android.settings.appfunctions.DeviceStateCategory
import com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateItem
import com.google.android.appfunctions.schema.common.v1.devicestate.PerScreenDeviceStates

class NotificationsStateSource : DeviceStateSource {
    override val category: DeviceStateCategory = DeviceStateCategory.UNCATEGORIZED

    // TODO: prototype implementation, to be replaced in b/426005115
    override fun get(context: Context): PerScreenDeviceStates {
        val packageManager = context.packageManager
        val notificationManager =
            INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE)
            )

        val installedApplications =
            packageManager.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS)

        val deviceStateItems = mutableListOf<DeviceStateItem>()
        for (info in installedApplications) {
            val packageName = info.packageName
            val appName = packageManager.getApplicationLabel(info.applicationInfo)
            val uid = info.uid
            val areNotificationsEnabled =
                notificationManager?.areNotificationsEnabledForPackage(packageName, uid) ?: false

            deviceStateItems.add(
                DeviceStateItem(
                    key = "notifications_enabled_package_$packageName",
                    jsonValue = areNotificationsEnabled.toString(),
                    hintText = "App: $appName",
                )
            )
        }

        return PerScreenDeviceStates(
            description =
                "Notifications Settings Screen. Note that to get to the notification settings for a given package, the intent uri is intent:#Intent;action=android.settings.APP_NOTIFICATION_SETTINGS;S.android.provider.extra.APP_PACKAGE=\$packageName;end",
            deviceStateItems = deviceStateItems,
        )
    }
}
