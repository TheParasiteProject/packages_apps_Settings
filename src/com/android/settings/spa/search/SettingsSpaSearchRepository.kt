/*
 * Copyright (C) 2024 The Android Open Source Project
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
import com.android.settings.network.telephony.MobileNetworkSettingsSearchIndex
import com.android.settingslib.spa.framework.common.SpaEnvironment
import com.android.settingslib.spa.framework.common.SpaEnvironmentFactory
import com.android.settingslib.spa.search.SpaSearchLanding.SpaSearchLandingKey
import com.android.settingslib.spa.search.SpaSearchRepository

class SettingsSpaSearchRepository(spaEnvironment: SpaEnvironment = SpaEnvironmentFactory.instance) {
    private val spaSearchRepository = SpaSearchRepository(spaEnvironment)

    fun getSearchIndexableDataList() =
        spaSearchRepository.getSearchIndexableDataList(
            intentAction = SEARCH_LANDING_ACTION,
            intentTargetClass = SettingsSpaSearchLandingActivity::class.qualifiedName!!,
        ) + MobileNetworkSettingsSearchIndex().createSearchIndexableData()

    companion object {
        fun createSearchIndexableRaw(
            context: Context,
            spaSearchLandingKey: SpaSearchLandingKey,
            itemTitle: String,
            indexableClass: Class<*>,
            pageTitle: String,
            keywords: String? = null,
        ) =
            SpaSearchRepository.createSearchIndexableRaw(
                context = context,
                spaSearchLandingKey = spaSearchLandingKey,
                itemTitle = itemTitle,
                indexableClass = indexableClass,
                pageTitle = pageTitle,
                intentAction = SEARCH_LANDING_ACTION,
                intentTargetClass = SettingsSpaSearchLandingActivity::class.qualifiedName!!,
                keywords = keywords,
            )

        private const val SEARCH_LANDING_ACTION = "android.settings.SPA_SEARCH_LANDING"
    }
}
