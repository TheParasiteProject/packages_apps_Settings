/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.settings.connecteddevice.audiosharing;

import android.content.Context;

import com.android.settings.core.BasePreferenceController;

public class AudioSharingPreferenceController extends BasePreferenceController {
    private static final String TAG = "AudioSharingPreferenceController";

    private Context mContext;

    public AudioSharingPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return AudioSharingUtils.isFeatureEnabled() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }
}

/*
 * Copyright (C) 2019 The PixelExperience Project
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

package com.android.settings.sound;

import android.content.Context;
import android.os.Vibrator;

import com.android.settings.core.BasePreferenceController;

public class CustomHapticPreferenceController extends BasePreferenceController {

    private final boolean mHasVibrator;

    public CustomHapticPreferenceController(Context context, String key) {
        super(context, key);
        mHasVibrator = context.getSystemService(Vibrator.class).hasVibrator();
    }

    @Override
    public int getAvailabilityStatus() {
        return mHasVibrator ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

}
