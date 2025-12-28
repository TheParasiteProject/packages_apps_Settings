/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display.darkmode

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import com.android.settings.R
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.SwitchPreference
import lineageos.datastore.LineageSettingsSecureStore
import lineageos.providers.LineageSettings

class BerryBlackThemeSwitchPreference(private val berryBlackThemeDataStore: KeyValueStore) :
    SwitchPreference(KEY, R.string.berry_black_theme_title, R.string.berry_black_theme_summary),
    PreferenceAvailabilityProvider {

    override fun storage(context: Context) = berryBlackThemeDataStore

    override fun isAvailable(context: Context) =
        isPackageInstalled(context, LINEAGE_BLACK_THEME) && context.isDarkMode()

    private fun Context.isDarkMode() =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

    override fun getReadPermissions(context: Context) =
        LineageSettingsSecureStore.getReadPermissions()

    override fun getWritePermissions(context: Context) =
        LineageSettingsSecureStore.getWritePermissions()

    override fun getReadPermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override fun getWritePermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    companion object {
        const val KEY = LineageSettings.Secure.BERRY_BLACK_THEME
        const val DEFAULT_VALUE = false

        const val LINEAGE_BLACK_THEME = "org.lineageos.overlay.customization.blacktheme"

        val Context.berryBlackThemeDataStore: KeyValueStore
            get() =
                LineageSettingsSecureStore.get(this).apply { setDefaultValue(KEY, DEFAULT_VALUE) }

        fun isPackageInstalled(context: Context, pkg: String): Boolean =
            try {
                val pi = context.packageManager.getPackageInfo(pkg, 0)
                if (pi?.applicationInfo?.enabled == true) {
                    true
                } else {
                    false
                }
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
    }
}
