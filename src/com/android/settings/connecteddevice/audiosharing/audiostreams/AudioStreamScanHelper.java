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

import static com.android.settings.connecteddevice.audiosharing.audiostreams.AudioStreamScanHelper.State.STATE_OFF;
import static com.android.settings.connecteddevice.audiosharing.audiostreams.AudioStreamScanHelper.State.STATE_ON;
import static com.android.settings.connecteddevice.audiosharing.audiostreams.AudioStreamScanHelper.State.STATE_TURNING_OFF;
import static com.android.settings.connecteddevice.audiosharing.audiostreams.AudioStreamScanHelper.State.STATE_TURNING_ON;

import static java.util.Collections.emptyList;

import android.annotation.NonNull;
import android.util.Log;

import com.android.settingslib.bluetooth.LocalBluetoothLeBroadcastAssistant;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper class for managing the scanning. It utilizes the
 * {@link LocalBluetoothLeBroadcastAssistant} to initiate and stop scanning and provides callbacks
 * to inform listeners about the scan state.
 */
public class AudioStreamScanHelper implements
        AudioStreamsProgressCategoryCallback.ScanStateListener {
    enum State {
        STATE_OFF,
        STATE_TURNING_ON,
        STATE_ON,
        STATE_TURNING_OFF
    }

    private static final String TAG = "AudioStreamScanHelper";
    private final @Nullable LocalBluetoothLeBroadcastAssistant mLeBroadcastAssistant;
    private final Consumer<Boolean> mScanStateChangedListener;
    private final @NonNull Executor mExecutor;
    private State mState = STATE_OFF;

    AudioStreamScanHelper(@NonNull Executor executor,
            @Nullable LocalBluetoothLeBroadcastAssistant leBroadcastAssistant,
            @Nonnull Consumer<Boolean> scanStateChangedListener) {
        mExecutor = executor;
        mLeBroadcastAssistant = leBroadcastAssistant;
        mScanStateChangedListener = scanStateChangedListener;
    }

    /**
     * Starts the scanning process for available audio stream sources.
     * This method will do nothing if scanning is already active or in the process of starting.
     */
    public void startScanning() {
        mExecutor.execute(() -> {
            if (mState == STATE_ON || mState == STATE_TURNING_ON) {
                Log.d(TAG, "startScanning() : do nothing, state = " + mState);
                return;
            }
            if (mLeBroadcastAssistant != null) {
                Log.d(TAG, "startScanning()");
                mLeBroadcastAssistant.startSearchingForSources(emptyList());
                setState(STATE_TURNING_ON);
            }
        });
    }

    /**
     * Stops the ongoing scanning process for audio stream sources.
     * This method will do nothing if scanning is already off or in the process of stopping.
     */
    public void stopScanning() {
        mExecutor.execute(() -> {
            if (mState == STATE_OFF || mState == STATE_TURNING_OFF) {
                Log.d(TAG, "stopScanning() : do nothing, state = " + mState);
                return;
            }
            if (mLeBroadcastAssistant != null) {
                Log.d(TAG, "stopScanning()");
                mLeBroadcastAssistant.stopSearchingForSources();
                setState(STATE_TURNING_OFF);
            }
        });
    }

    @Override
    public void scanningStarted() {
        mExecutor.execute(() -> {
            Log.d(TAG, "scanningStarted()");
            setState(STATE_ON);
        });
    }

    @Override
    public void scanningStartFailed(int reason) {
        mExecutor.execute(() -> {
            Log.d(TAG, "scanningStartFailed() : reason = " + reason);
            setState(reason == ERROR_ALREADY_IN_TARGET_STATE ? STATE_ON : STATE_OFF);
        });
    }

    @Override
    public void scanningStopped() {
        mExecutor.execute(() -> {
            Log.d(TAG, "scanningStopped()");
            setState(STATE_OFF);
        });
    }

    @Override
    public void scanningStopFailed(int reason) {
        mExecutor.execute(() -> {
            Log.d(TAG, "scanningStopFailed() : reason = " + reason);
            setState(reason == ERROR_ALREADY_IN_TARGET_STATE ? STATE_OFF : STATE_ON);
        });
    }

    private void setState(State newState) {
        mScanStateChangedListener.accept(newState == STATE_ON || newState == STATE_TURNING_ON);
        Log.d(TAG, "setState: from " + mState + " to " + newState);
        mState = newState;
    }
}
