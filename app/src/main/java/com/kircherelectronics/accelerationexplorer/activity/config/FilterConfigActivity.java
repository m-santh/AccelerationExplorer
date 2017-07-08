package com.kircherelectronics.accelerationexplorer.activity.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import com.kircherelectronics.accelerationexplorer.R;

/*
 * Acceleration Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Preferences for the smoothing and linear acceleration filters.
 * 
 * @author Kaleb
 *
 */
public class FilterConfigActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener
{

	private static final String tag = FilterConfigActivity.class
			.getSimpleName();

	public static final String AXIS_INVERSION_ENABLED_KEY = "axis_inversion_enabled_preference";

    public final static String SENSOR_FREQUENCY_KEY= "sensor_frequency_preference";
    public final static String SENSOR_FUSION_TYPE_KEY = "sensor_fusion_type_preference";

	// Preference keys for smoothing filters
	public static final String MEAN_FILTER_SMOOTHING_ENABLED_KEY = "mean_filter_smoothing_enabled_preference";
	public static final String MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "mean_filter_smoothing_time_constant_preference";
	public static final String MEDIAN_FILTER_SMOOTHING_ENABLED_KEY = "median_filter_smoothing_enabled_preference";
	public static final String MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "median_filter_smoothing_time_constant_preference";
	public static final String LPF_SMOOTHING_ENABLED_KEY = "lpf_smoothing_enabled_preference";
	public static final String LPF_SMOOTHING_TIME_CONSTANT_KEY = "lpf_smoothing_time_constant_preference";

	// Preference keys for linear acceleration filters
	public static final String LPF_LINEAR_ACCEL_ENABLED_KEY = "lpf_linear_accel_enabled_preference";
	public static final String FSENSOR_LINEAR_ACCEL_ENABLED_KEY = "fsesnor_linear_accel_enabled_preference";
	public static final String ANDROID_LINEAR_ACCEL_ENABLED_KEY = "android_linear_accel_filter_preference";


	private SwitchPreference spLpfLinearAccel;
	private SwitchPreference spAndroidLinearAccel;



	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_filter);

		spLpfLinearAccel = (SwitchPreference) findPreference(LPF_LINEAR_ACCEL_ENABLED_KEY);
		spAndroidLinearAccel = (SwitchPreference) findPreference(ANDROID_LINEAR_ACCEL_ENABLED_KEY);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		if (key.equals(LPF_LINEAR_ACCEL_ENABLED_KEY))
		{
			if (sharedPreferences.getBoolean(key, false))
			{
				Editor edit = sharedPreferences.edit();

				edit.putBoolean(ANDROID_LINEAR_ACCEL_ENABLED_KEY, false);

				edit.apply();

				spAndroidLinearAccel.setChecked(false);
			}
		}
	}
}
