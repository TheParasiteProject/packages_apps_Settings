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
package com.android.settings.supervision

import android.app.Activity
import android.app.role.RoleManager.ROLE_SYSTEM_SUPERVISION
import android.app.supervision.SupervisionManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowContextImpl
import org.robolectric.shadows.ShadowRoleManager

@RunWith(AndroidJUnit4::class)
class EnableSupervisionActivityTest {
    private val mockSupervisionManager = mock<SupervisionManager>()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val currentUser = context.user

    private lateinit var mActivity: EnableSupervisionActivity
    private lateinit var mActivityController: ActivityController<EnableSupervisionActivity>
    private lateinit var shadowActivity: ShadowActivity

    private val callingPackage = "com.example.caller"

    @Before
    fun setUp() {
        ShadowRoleManager.reset()

        // Note, we have to use ActivityController (instead of ActivityScenario) in order to access
        // the activity before it is created, so we can set up various mocked responses before they
        // are referenced in onCreate.
        mActivityController = Robolectric.buildActivity(EnableSupervisionActivity::class.java)
        mActivity = mActivityController.get()

        shadowActivity = shadowOf(mActivity)
        shadowActivity.setCallingPackage(callingPackage)
        Shadow.extract<ShadowContextImpl>(mActivity.baseContext).apply {
            setSystemService(Context.SUPERVISION_SERVICE, mockSupervisionManager)
        }
    }

    @Test
    fun onCreate_callerHasSupervisionRole_EnablesSupervision() {
        ShadowRoleManager.addRoleHolder(ROLE_SYSTEM_SUPERVISION, callingPackage, currentUser)

        mActivityController.setup()

        verify(mockSupervisionManager).setSupervisionEnabled(true)
        assertThat(shadowActivity.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(mActivity.isFinishing).isTrue()
    }

    @Test
    fun onCreate_callerWithoutSupervisionRole_doesNotEnableSupervision() {
        ShadowRoleManager.addRoleHolder(ROLE_SYSTEM_SUPERVISION, "com.other.package", currentUser)

        mActivityController.setup()

        verifyNoInteractions(mockSupervisionManager)
        assertThat(shadowActivity.resultCode).isEqualTo(Activity.RESULT_CANCELED)
        assertThat(mActivity.isFinishing).isTrue()
    }
}
