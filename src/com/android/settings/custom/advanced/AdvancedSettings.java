package com.android.settings.custom.advanced;

import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class AdvancedSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.advanced_settings);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }
}
