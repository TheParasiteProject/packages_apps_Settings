/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display.darkmode

import android.content.Context
import android.content.res.Configuration
import android.view.ThreadedRenderer
import com.android.settings.R
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.SwitchPreference
import lineageos.datastore.SystemPropertyStore

class HwuiForceDarkSwitchPreference(private val hwuiForceDarkDataStore: KeyValueStore) :
    SwitchPreference(KEY, R.string.hwui_force_dark_title, R.string.hwui_force_dark_summary),
    PreferenceAvailabilityProvider {

    override fun storage(context: Context) = hwuiForceDarkDataStore

    override fun isAvailable(context: Context) = context.isDarkMode()

    private fun Context.isDarkMode() =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

    override fun getReadPermissions(context: Context) = SystemPropertyStore.getReadPermissions()

    override fun getWritePermissions(context: Context) = SystemPropertyStore.getWritePermissions()

    override fun getReadPermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override fun getWritePermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    companion object {
        const val KEY = "persist.sys." + ThreadedRenderer.DEBUG_FORCE_DARK
        const val DEFAULT_VALUE = false

        val hwuiForceDarkPropertyDataStore: KeyValueStore
            get() = SystemPropertyStore.get().apply { setDefaultValue(KEY, DEFAULT_VALUE) }
    }
}
