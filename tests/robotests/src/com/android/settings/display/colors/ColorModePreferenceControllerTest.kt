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
package com.android.settings.display

import android.content.Context
import android.hardware.display.ColorDisplayManager

import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider

import com.android.settingslib.testutils.shadow.ShadowColorDisplayManager
import com.android.settings.R
import com.google.common.truth.Truth.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowColorDisplayManager::class])
class ColorModePreferenceControllerTest {
    private lateinit var context: Context
    private lateinit var preference: Preference
    private lateinit var controller: ColorModePreferenceController
    private lateinit var shadowColorDisplayManager: ShadowColorDisplayManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        controller = ColorModePreferenceController(context, "test")
        preference = Preference(context)
        shadowColorDisplayManager = Shadow.extract(
            context.getSystemService(ColorDisplayManager::class.java));
    }

    @Test
    fun updateState_colorModeAutomatic_shouldSetSummaryToAutomatic() {
        shadowColorDisplayManager.setColorMode(ColorDisplayManager.COLOR_MODE_AUTOMATIC)
        controller.updateState(preference)
        val automaticColorModeName = context.getString(R.string.color_mode_option_automatic)
        assertThat(preference.summary.toString()).isEqualTo(automaticColorModeName)
    }

    @Test
    fun updateState_colorModeSaturated_shouldSetSummaryToSaturated() {
        shadowColorDisplayManager.setColorMode(ColorDisplayManager.COLOR_MODE_SATURATED)
        controller.updateState(preference)
        val saturatedColorModeName = context.getString(R.string.color_mode_option_saturated)
        assertThat(preference.summary.toString()).isEqualTo(saturatedColorModeName)
    }

    @Test
    fun updateState_colorModeBoosted_shouldSetSummaryToBoosted() {
        shadowColorDisplayManager.setColorMode(ColorDisplayManager.COLOR_MODE_BOOSTED)
        controller.updateState(preference)
        val boostedColorModeName = context.getString(R.string.color_mode_option_boosted)
        assertThat(preference.summary.toString()).isEqualTo(boostedColorModeName)
    }

    @Test
    fun updateState_colorModeNatural_shouldSetSummaryToNatural() {
        shadowColorDisplayManager.setColorMode(ColorDisplayManager.COLOR_MODE_NATURAL)
        controller.updateState(preference)
        val naturalColorModeName = context.getString(R.string.color_mode_option_natural)
        assertThat(preference.summary.toString()).isEqualTo(naturalColorModeName)
    }
}