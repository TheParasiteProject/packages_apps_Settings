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

package com.android.settings.spa.search

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.settingslib.spa.framework.common.SettingsPageProvider
import com.android.settingslib.spa.search.SpaSearchLanding.SpaSearchLandingKey
import com.android.settingslib.spa.search.SpaSearchLanding.SpaSearchLandingSpaPage
import com.android.settingslib.spa.search.decodeToSpaSearchLandingKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsSpaSearchRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun createSearchIndexableRaw() {
        val spaSearchLandingKey =
            SpaSearchLandingKey.newBuilder()
                .setSpaPage(SpaSearchLandingSpaPage.newBuilder().setDestination(PAGE_NAME))
                .build()
        val pageProvider =
            object : SettingsPageProvider {
                override val name = PAGE_NAME
            }

        val searchIndexableRaw =
            SettingsSpaSearchRepository.createSearchIndexableRaw(
                context = context,
                spaSearchLandingKey = spaSearchLandingKey,
                itemTitle = ITEM_TITLE,
                indexableClass = pageProvider::class.java,
                pageTitle = PAGE_TITLE,
            )

        assertThat(decodeToSpaSearchLandingKey(searchIndexableRaw.key))
            .isEqualTo(spaSearchLandingKey)
        assertThat(searchIndexableRaw.title).isEqualTo(ITEM_TITLE)
        assertThat(searchIndexableRaw.className).isEqualTo(pageProvider::class.java.name)
        assertThat(searchIndexableRaw.screenTitle).isEqualTo(PAGE_TITLE)
        assertThat(searchIndexableRaw.intentAction).isEqualTo("android.settings.SPA_SEARCH_LANDING")
        assertThat(searchIndexableRaw.intentTargetClass)
            .isEqualTo(SettingsSpaSearchLandingActivity::class.qualifiedName)
    }

    private companion object {
        const val PAGE_NAME = "PageName"
        const val PAGE_TITLE = "Page Title"
        const val ITEM_TITLE = "Item Title"
    }
}
