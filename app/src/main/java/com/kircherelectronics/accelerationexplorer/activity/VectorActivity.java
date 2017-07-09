package com.kircherelectronics.accelerationexplorer.activity;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.activity.config.FilterConfigActivity;
import com.kircherelectronics.accelerationexplorer.gauge.GaugeVector;
import com.kircherelectronics.accelerationexplorer.livedata.AccelerationLiveData;
import com.kircherelectronics.accelerationexplorer.prefs.PrefUtils;
import com.kircherelectronics.accelerationexplorer.viewmodel.AccelerationViewModel;

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
 * Draws a two dimensional vector of the acceleration sensors measurements.
 * 
 * @author Kaleb
 * 
 */
public class VectorActivity extends AppCompatActivity
{
    private AccelerationLiveData liveData;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		initViewModel();

		setContentView(R.layout.layout_vector);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_vector, menu);
		return true;
	}

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		// Log the data
		case R.id.action_settings_sensor:
			Intent intent = new Intent(this, FilterConfigActivity.class);
			startActivity(intent);
			return true;

			// Log the data
		case R.id.menu_settings_help:
			showHelpDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConfiguration();
    }

	private void showHelpDialog()
	{
		Dialog helpDialog = new Dialog(this);
		helpDialog.setCancelable(true);
		helpDialog.setCanceledOnTouchOutside(true);

		helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		helpDialog.setContentView(getLayoutInflater().inflate(
				R.layout.layout_help_vector, null));

		helpDialog.show();
	}

	private void initViewModel() {
		AccelerationViewModel model = ViewModelProviders.of(this).get(AccelerationViewModel.class);
		liveData = model.getAccelerationListener();
	}

	private void updateConfiguration() {
		liveData.setSensorFrequency(PrefUtils.getSensorFrequencyPrefs(this));
		liveData.setAxisInverted(PrefUtils.getInvertAxisPrefs(this));

		liveData.enableAndroidLinearAcceleration(PrefUtils.getPrefAndroidLinearAccelerationEnabled(this));
		liveData.enableFSensorComplimentaryLinearAcceleration(PrefUtils.getPrefFSensorComplimentaryLinearAccelerationEnabled(this));
		liveData.enableFSensorKalmanLinearAcceleration(PrefUtils.getPrefFSensorKalmanLinearAccelerationEnabled(this));
		liveData.enableFSensorLpfLinearAcceleration(PrefUtils.getPrefFSensorLpfLinearAccelerationEnabled(this));

		liveData.setFSensorComplimentaryLinearAccelerationTimeConstant(PrefUtils.getPrefFSensorComplimentaryLinearAccelerationTimeConstant(this));
		liveData.setFSensorLpfLinearAccelerationTimeConstant(PrefUtils.getPrefFSensorLpfLinearAccelerationTimeConstant(this));

		liveData.enableMeanFilterSmoothing(PrefUtils.getPrefMeanFilterSmoothingEnabled(this));
		liveData.enableMedianFilterSmoothing(PrefUtils.getPrefMedianFilterSmoothingEnabled(this));
		liveData.enableLpfSmoothing(PrefUtils.getPrefLpfSmoothingEnabled(this));

		liveData.setMeanFilterSmoothingTimeConstant(PrefUtils.getPrefMeanFilterSmoothingTimeConstant(this));
		liveData.setMedianFilterSmoothingTimeConstant(PrefUtils.getPrefMedianFilterSmoothingTimeConstant(this));
		liveData.setLpfSmoothingTimeConstant(PrefUtils.getPrefLpfSmoothingTimeConstant(this));
	}
}
