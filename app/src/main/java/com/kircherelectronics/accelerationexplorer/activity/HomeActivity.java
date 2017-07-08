package com.kircherelectronics.accelerationexplorer.activity;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.activity.config.FilterConfigActivity;
import com.kircherelectronics.accelerationexplorer.gauge.GaugeAcceleration;
import com.kircherelectronics.accelerationexplorer.livedata.AccelerationLiveData;
import com.kircherelectronics.accelerationexplorer.prefs.PrefUtils;
import com.kircherelectronics.accelerationexplorer.view.VectorDrawableButton;
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
 * A class that provides a navigation menu to the features of Acceleration
 * Explorer.
 *
 * @author Kaleb
 */
public class HomeActivity extends AppCompatActivity  {
    private final static String tag = HomeActivity.class.getSimpleName();

    private AccelerationLiveData liveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViewModel();

        setContentView(R.layout.layout_home);

        initButtonGauge();
        initButtonLogger();
        initButtonVector();
        initButtonSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected Identify single menu
     * item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Start the vector activity
            case R.id.action_help:
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

    private void initButtonGauge() {
        VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_gauge_mode);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,
                        GaugeActivity.class);

                startActivity(intent);
            }
        });
    }

    private void initButtonLogger() {
        VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_logger_mode);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,
                        LoggerActivity.class);

                startActivity(intent);
            }
        });
    }

    private void initButtonVector() {
        VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_vector_mode);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,
                        VectorActivity.class);

                startActivity(intent);
            }
        });
    }

    private void initButtonSettings() {
        VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_config_mode);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,
                        FilterConfigActivity.class);

                startActivity(intent);
            }
        });
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);

        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);
        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = getLayoutInflater()
                .inflate(R.layout.layout_help_home, null);

        helpDialog.setContentView(view);

        helpDialog.show();
    }

    private void initViewModel() {
        AccelerationViewModel model = ViewModelProviders.of(this).get(AccelerationViewModel.class);
        liveData = model.getAccelerationListener();
    }

    private void updateConfiguration() {
        liveData.setSensorFrequency(PrefUtils.getSensorFrequencyPrefs(this));
        liveData.setAxisInverted(PrefUtils.getInvertAxisPrefs(this));

        liveData.setFusionType(AccelerationLiveData.FusionType.values()[PrefUtils.getFusionType(this)]);

        liveData.enableAndroidLinearAcceleration(PrefUtils.getPrefAndroidLinearAccelerationEnabled(this));
        liveData.enableFSensorLinearAcceleration(PrefUtils.getPrefFSensorLinearAccelerationEnabled(this));
        liveData.enableLpfLinearAcceleration(PrefUtils.getPrefLpfLinearAccelerationEnabled(this));

        liveData.enableMeanFilterSmoothing(PrefUtils.getPrefMeanFilterSmoothingEnabled(this));
        liveData.enableMedianFilterSmoothing(PrefUtils.getPrefMedianFilterSmoothingEnabled(this));
        liveData.enableLpfSmoothing(PrefUtils.getPrefLpfSmoothingEnabled(this));

        liveData.setMeanFilterSmoothingTimeConstant(PrefUtils.getPrefMeanFilterSmoothingTimeConstant(this));
        liveData.setMedianFilterSmoothingTimeConstant(PrefUtils.getPrefMedianFilterSmoothingTimeConstant(this));
        liveData.setLpfSmoothingTimeConstant(PrefUtils.getPrefLpfSmoothingTimeConstant(this));
    }
}
