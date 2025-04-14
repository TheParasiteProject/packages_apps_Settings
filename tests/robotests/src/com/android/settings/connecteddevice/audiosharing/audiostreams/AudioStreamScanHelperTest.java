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

package com.android.settings.connecteddevice.audiosharing.audiostreams;

import static android.bluetooth.BluetoothStatusCodes.ERROR_ALREADY_IN_TARGET_STATE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.android.settingslib.bluetooth.LocalBluetoothLeBroadcastAssistant;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Consumer;

@RunWith(RobolectricTestRunner.class)
public class AudioStreamScanHelperTest {

    @Rule
    public final MockitoRule mMockitoRule = MockitoJUnit.rule();
    private static final int REASON_ERROR = 100;
    private final Context mContext = spy(ApplicationProvider.getApplicationContext());

    @Mock
    private LocalBluetoothLeBroadcastAssistant mLeBroadcastAssistant;
    @Mock
    private Consumer<Boolean> mScanStateChangedListener;

    private AudioStreamScanHelper mScanHelper;

    @Before
    public void setUp() {
        mScanHelper = new AudioStreamScanHelper(mContext.getMainExecutor(), mLeBroadcastAssistant,
                mScanStateChangedListener);
    }

    @Test
    public void startScanning_assistantNotNull_startsSearchingAndSetsTurningOn() {
        mScanHelper.startScanning();
        shadowOf(Looper.getMainLooper()).idle();

        verify(mLeBroadcastAssistant).startSearchingForSources(any());
        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener).accept(captor.capture());
        assertThat(captor.getValue()).isTrue();
    }

    @Test
    public void startScanning_assistantNull_doesNothing() {
        AudioStreamScanHelper scanHelperWithoutAssistant = new AudioStreamScanHelper(
                mContext.getMainExecutor(),
                null, mScanStateChangedListener);
        scanHelperWithoutAssistant.startScanning();
        shadowOf(Looper.getMainLooper()).idle();

        verify(mLeBroadcastAssistant, never()).startSearchingForSources(any());
        verify(mScanStateChangedListener, never()).accept(any());
    }

    @Test
    public void startScanning_alreadyOn_doesNothing() {
        AudioStreamScanHelper spiedHelper = spy(mScanHelper);
        spiedHelper.scanningStarted();
        spiedHelper.startScanning();
        shadowOf(Looper.getMainLooper()).idle();

        verify(mLeBroadcastAssistant, never()).startSearchingForSources(any());
        verify(mScanStateChangedListener, times(1)).accept(true);
    }

    @Test
    public void stopScanning_assistantNotNull_stopsSearchingAndSetsTurningOff() {
        mScanHelper.scanningStarted();
        mScanHelper.stopScanning();
        shadowOf(Looper.getMainLooper()).idle();

        verify(mLeBroadcastAssistant).stopSearchingForSources();
        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener, times(2)).accept(captor.capture());
        assertThat(captor.getValue()).isFalse();
    }

    @Test
    public void stopScanning_assistantNull_doesNothing() {
        AudioStreamScanHelper scanHelperWithoutAssistant = new AudioStreamScanHelper(
                mContext.getMainExecutor(),
                null, mScanStateChangedListener);
        scanHelperWithoutAssistant.stopScanning();
        shadowOf(Looper.getMainLooper()).idle();

        verify(mLeBroadcastAssistant, never()).stopSearchingForSources();
        verify(mScanStateChangedListener, never()).accept(any());
    }

    @Test
    public void stopScanning_alreadyOff_doesNothing() {
        mScanHelper.stopScanning();
        shadowOf(Looper.getMainLooper()).idle();

        verify(mLeBroadcastAssistant, never()).stopSearchingForSources();
        verify(mScanStateChangedListener, never()).accept(any());
    }

    @Test
    public void scanningStarted_setsStateOnAndNotifiesListener() {
        mScanHelper.scanningStarted();
        shadowOf(Looper.getMainLooper()).idle();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener).accept(captor.capture());
        assertThat(captor.getValue()).isTrue();
    }

    @Test
    public void scanningStartFailed_alreadyOn_setsStateOnAndNotifiesListener() {
        mScanHelper.scanningStartFailed(ERROR_ALREADY_IN_TARGET_STATE);
        shadowOf(Looper.getMainLooper()).idle();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener).accept(captor.capture());
        assertThat(captor.getValue()).isTrue();
    }

    @Test
    public void scanningStartFailed_otherReason_setsStateOffAndNotifiesListener() {
        mScanHelper.scanningStartFailed(REASON_ERROR);
        shadowOf(Looper.getMainLooper()).idle();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener).accept(captor.capture());
        assertThat(captor.getValue()).isFalse();
    }

    @Test
    public void scanningStopped_setsStateOffAndNotifiesListener() {
        mScanHelper.scanningStarted();
        mScanHelper.scanningStopped();
        shadowOf(Looper.getMainLooper()).idle();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener, times(2)).accept(captor.capture());
        assertThat(captor.getValue()).isFalse();
    }

    @Test
    public void scanningStopFailed_alreadyOff_setsStateOffAndNotifiesListener() {
        mScanHelper.scanningStopFailed(ERROR_ALREADY_IN_TARGET_STATE);
        shadowOf(Looper.getMainLooper()).idle();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener).accept(captor.capture());
        assertThat(captor.getValue()).isFalse();
    }

    @Test
    public void scanningStopFailed_otherReason_setsStateOnAndNotifiesListener() {
        mScanHelper.scanningStarted();
        mScanHelper.scanningStopFailed(REASON_ERROR);
        shadowOf(Looper.getMainLooper()).idle();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mScanStateChangedListener, times(2)).accept(captor.capture());
        assertThat(captor.getValue()).isTrue();
    }
}
