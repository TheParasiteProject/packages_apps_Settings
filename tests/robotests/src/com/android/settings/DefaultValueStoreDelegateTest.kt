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
package com.android.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.settingslib.datastore.KeyValueStore
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class DefaultValueStoreDelegateTest {
    private val keyValueStore: KeyValueStore = mock()

    @Test
    fun setDefaultValueTrue_shouldReturnTrue() {
        val storeDelegate = DefaultValueStoreDelegate(keyValueStore, true)

        assertThat(storeDelegate.getBoolean(KEY)).isTrue()
    }

    @Test
    fun setDefaultValueFalse_shouldReturnFalse() {
        val storeDelegate = DefaultValueStoreDelegate(keyValueStore, false)

        assertThat(storeDelegate.getBoolean(KEY)).isFalse()
    }

    @Test
    fun setDefaultValueInt_shouldReturnExactInt() {
        val value = 10
        val storeDelegate = DefaultValueStoreDelegate(keyValueStore, value)

        assertThat(storeDelegate.getInt(KEY)).isEqualTo(value)
    }

    @Test
    fun setDefaultValueFloat_shouldReturnExactFloat() {
        val value = 1.1f
        val storeDelegate = DefaultValueStoreDelegate(keyValueStore, value)

        assertThat(storeDelegate.getFloat(KEY)).isEqualTo(value)
    }

    @Test
    fun setDefaultValueLong_shouldReturnExactLong() {
        val value = 10L
        val storeDelegate = DefaultValueStoreDelegate(keyValueStore, value)

        assertThat(storeDelegate.getLong(KEY)).isEqualTo(value)
    }

    @Test
    fun setDefaultValueString_shouldReturnExactString() {
        val value = "abcdef"
        val storeDelegate = DefaultValueStoreDelegate(keyValueStore, value)

        assertThat(storeDelegate.getString(KEY)).isEqualTo(value)
    }

    companion object {
        const val KEY = "test_key"
    }
}
