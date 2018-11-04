package com.kircherelectronics.accelerationexplorer.activity.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;

import com.kircherelectronics.accelerationexplorer.R;

/*
 * AccelerationExplorer
 * Copyright 2018 Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Preferences for the smoothing and linear acceleration filters.
 *
 * @author Kaleb
 */
public class FilterConfigActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    private static final String tag = FilterConfigActivity.class
            .getSimpleName();

    public static final String AXIS_INVERSION_ENABLED_KEY = "axis_inversion_enabled_preference";

    public final static String SENSOR_FREQUENCY_KEY = "sensor_frequency_preference";

    // Preference keys for smoothing filters
    public static final String MEAN_FILTER_SMOOTHING_ENABLED_KEY = "mean_filter_smoothing_enabled_preference";
    public static final String MEDIAN_FILTER_SMOOTHING_ENABLED_KEY = "median_filter_smoothing_enabled_preference";
    public static final String LPF_SMOOTHING_ENABLED_KEY = "lpf_smoothing_enabled_preference";

    public static final String MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "mean_filter_smoothing_time_constant_preference";
    public static final String MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "median_filter_smoothing_time_constant_preference";
    public static final String LPF_SMOOTHING_TIME_CONSTANT_KEY = "lpf_smoothing_time_constant_preference";

    // Preference keys for linear acceleration filters
    public static final String FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY = "lpf_linear_accel_enabled_preference";
    public static final String FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY = "complimentary_fusion_enabled_preference";
    public static final String FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY = "kalman_fusion_enabled_preference";
    public static final String ANDROID_LINEAR_ACCEL_ENABLED_KEY = "android_linear_accel_filter_enabled_preference";

    public static final String FSENSOR_LPF_LINEAR_ACCEL_TIME_CONSTANT_KEY = "lpf_linear_accel_time_constant_preference";
    public static final String FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_TIME_CONSTANT_KEY = "complimentary_fusion_time_constant_preference";

    private SwitchPreference fSensorLpfLinearAccel;
    private SwitchPreference fSensorComplimentaryLinearAccel;
    private SwitchPreference fSensorKalmanLinearAccel;
    private SwitchPreference androidLinearAccel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_filter);

        fSensorComplimentaryLinearAccel = (SwitchPreference) findPreference
                (FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY);
        fSensorKalmanLinearAccel = (SwitchPreference) findPreference(FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY);
        fSensorLpfLinearAccel = (SwitchPreference) findPreference(FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY);
        androidLinearAccel = (SwitchPreference) findPreference(ANDROID_LINEAR_ACCEL_ENABLED_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorKalmanLinearAccel.setChecked(false);
                    fSensorComplimentaryLinearAccel.setChecked(false);
                    androidLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(ANDROID_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
            case FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorKalmanLinearAccel.setChecked(false);
                    fSensorLpfLinearAccel.setChecked(false);
                    androidLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(ANDROID_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
            case FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorComplimentaryLinearAccel.setChecked(false);
                    fSensorLpfLinearAccel.setChecked(false);
                    androidLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(ANDROID_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
            case ANDROID_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorComplimentaryLinearAccel.setChecked(false);
                    fSensorLpfLinearAccel.setChecked(false);
                    fSensorKalmanLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
        }
    }
}
