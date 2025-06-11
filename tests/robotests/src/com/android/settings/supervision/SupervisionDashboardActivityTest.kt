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

import android.app.Application
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.settings.supervision.SupervisionDashboardActivity.Companion.FULL_SUPERVISION_REDIRECT_ACTION
import com.android.settings.supervision.SupervisionDashboardActivity.Companion.INSTALL_SUPERVISION_APP_ACTION
import com.android.settings.supervision.ipc.SupervisionMessengerClient
import com.android.settingslib.ipc.MessengerServiceRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowContextImpl
import org.robolectric.shadows.ShadowPackageManager

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
class SupervisionDashboardActivityTest {

    private lateinit var applicationContext: Context
    private lateinit var shadowPackageManager: ShadowPackageManager
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mockSupervisionManager = mock<RoleManager>()
    private val testSupervisionPackage = "com.android.settings.test"

    @get:Rule
    val serviceRule =
        MessengerServiceRule<SupervisionMessengerClient>(
            TestSupervisionMessengerService::class.java
        )

    @Before
    fun setup() {
        applicationContext = ApplicationProvider.getApplicationContext<Context>()
        shadowPackageManager = shadowOf(applicationContext.packageManager)

        Shadow.extract<ShadowContextImpl>((context as Application).baseContext).apply {
            setSystemService(Context.ROLE_SERVICE, mockSupervisionManager)
        }
    }

    @Test
    fun hasNecessaryComponent_enabled_loadInitialFragment() = runTest {
        // Setup necessary supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(eq(RoleManager.ROLE_SYSTEM_SUPERVISION)) } doReturn
                listOf(testSupervisionPackage)
        }
        setUpMessengerServiceComponent(disabled = false)

        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)

        activityScenario.onActivity { activity ->
            val fragmentName = activity.getInitialFragmentName(Intent())
            assertThat(fragmentName).isEqualTo(SupervisionDashboardFragment::class.java.name)
        }
    }

    @Test
    fun hasNecessaryComponent_disabled_startLoadingActivityAndFinishSelf() = runTest {
        // No supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(eq(RoleManager.ROLE_SYSTEM_SUPERVISION)) } doReturn
                listOf(testSupervisionPackage)
        }

        setUpMessengerServiceComponent(disabled = true)

        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)
        val nextActivityIntent = shadowOf(applicationContext as Application).nextStartedActivity

        // Check that the loading activity is started
        assertThat(nextActivityIntent.component?.className)
            .isEqualTo(SupervisionDashboardLoadingActivity::class.java.name)

        // Check that the activity is finished
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

    @Test
    fun hasNecessaryComponentEnabled_enabled_fullySupervised_intentResolved_redirect() = runTest {
        // Setup necessary supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(any()) } doReturn listOf(testSupervisionPackage)
        }
        setUpMessengerServiceComponent(disabled = false)
        setUpRedirectActivityComponent(FULL_SUPERVISION_REDIRECT_ACTION)

        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)
        val nextActivityIntent = shadowOf(applicationContext as Application).nextStartedActivity

        // Check that the redirect activity is started
        assertThat(nextActivityIntent.action).isEqualTo(FULL_SUPERVISION_REDIRECT_ACTION)

        // Check that the dashboard activity is finished
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

    @Test
    fun hasNecessaryComponent_enabled_notFullySupervised_doNotRedirect() = runTest {
        // Setup necessary supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(eq(RoleManager.ROLE_SYSTEM_SUPERVISION)) } doReturn
                listOf(testSupervisionPackage)
        }
        setUpMessengerServiceComponent(disabled = false)
        setUpRedirectActivityComponent(FULL_SUPERVISION_REDIRECT_ACTION)

        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)
        val nextActivityIntent = shadowOf(applicationContext as Application).nextStartedActivity

        // Check that the loading activity is not started
        assertThat(nextActivityIntent).isNull()

        // Check that the dashboard activity is not finished
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    fun hasNecessaryComponent_enabled_redirectIntentNotResolved_doNotRedirect() = runTest {
        // Setup necessary supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(any()) } doReturn listOf(testSupervisionPackage)
        }
        setUpMessengerServiceComponent(disabled = false)

        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)
        val nextActivityIntent = shadowOf(applicationContext as Application).nextStartedActivity

        // Check that the loading activity is not started
        assertThat(nextActivityIntent).isNull()

        // Check that the dashboard activity is not finished
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    fun noNecessaryComponent_appInstallIntentNotResolved_doNotRedirect() = runTest {
        // Setup necessary supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(any()) } doReturn listOf(testSupervisionPackage)
        }
        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)
        val nextActivityIntent = shadowOf(applicationContext as Application).nextStartedActivity

        // Check that the app install activity is not started
        assertThat(nextActivityIntent).isNull()

        // Check that the dashboard activity is finished
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

    @Test
    fun noNecessaryComponent_appInstallIntentResolved_redirectAppInstall() = runTest {
        // Setup necessary supervision component to be present
        mockSupervisionManager.stub {
            on { getRoleHolders(any()) } doReturn listOf(testSupervisionPackage)
        }
        setUpRedirectActivityComponent(INSTALL_SUPERVISION_APP_ACTION)

        val activityScenario = ActivityScenario.launch(SupervisionDashboardActivity::class.java)
        val nextActivityIntent = shadowOf(applicationContext as Application).nextStartedActivity

        // Check that the app install activity is started
        assertThat(nextActivityIntent.action).isEqualTo(INSTALL_SUPERVISION_APP_ACTION)

        // Check that the dashboard activity is finished
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

    private fun setUpMessengerServiceComponent(disabled: Boolean) {
        val serviceComponentName =
            ComponentName(testSupervisionPackage, "FakeSupervisionMessengerService")
        val intentFilter =
            IntentFilter(SupervisionMessengerClient.SUPERVISION_MESSENGER_SERVICE_BIND_ACTION)

        if (disabled) {
            applicationContext.packageManager.setComponentEnabledSetting(
                serviceComponentName,
                COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP,
            )
        }

        shadowPackageManager.addServiceIfNotPresent(serviceComponentName)
        shadowPackageManager.addIntentFilterForService(serviceComponentName, intentFilter)
    }

    private fun setUpRedirectActivityComponent(action: String) {
        val redirectComponentName =
            ComponentName(testSupervisionPackage, "com.example.FakeRedirectActivity")
        val intentFilter = IntentFilter(action)

        shadowPackageManager.addActivityIfNotPresent(redirectComponentName)
        shadowPackageManager.addIntentFilterForActivity(redirectComponentName, intentFilter)
    }
}
