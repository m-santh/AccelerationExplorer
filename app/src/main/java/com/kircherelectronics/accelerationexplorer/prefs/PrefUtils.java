package com.kircherelectronics.accelerationexplorer.prefs;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import com.kircherelectronics.accelerationexplorer.activity.config.FilterConfigActivity;
import com.kircherelectronics.accelerationexplorer.livedata.AccelerationLiveData;

public class PrefUtils
{


	public static boolean getInvertAxisPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FilterConfigActivity.AXIS_INVERSION_ENABLED_KEY, false);
	}

	public static boolean getPrefAndroidLinearAccelerationEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FilterConfigActivity.ANDROID_LINEAR_ACCEL_ENABLED_KEY, false);
	}

	public static boolean getPrefLpfLinearAccelerationEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FilterConfigActivity.LPF_LINEAR_ACCEL_ENABLED_KEY, false);
	}

    public static boolean getPrefFSensorLinearAccelerationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(FilterConfigActivity.FSENSOR_LINEAR_ACCEL_ENABLED_KEY, false);
    }

    public static boolean getPrefLpfSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FilterConfigActivity.LPF_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefLpfSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(FilterConfigActivity.LPF_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static boolean getPrefMeanFilterSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FilterConfigActivity.MEAN_FILTER_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefMeanFilterSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(FilterConfigActivity.MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static boolean getPrefMedianFilterSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FilterConfigActivity.MEDIAN_FILTER_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefMedianFilterSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(FilterConfigActivity.MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static int getSensorFrequencyPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(FilterConfigActivity.SENSOR_FREQUENCY_KEY, String.valueOf(SensorManager.SENSOR_DELAY_FASTEST)));
	}

	public static int getFusionType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(FilterConfigActivity.SENSOR_FUSION_TYPE_KEY, AccelerationLiveData.FusionType.COMPLIMENTARY.getId());
    }
}
