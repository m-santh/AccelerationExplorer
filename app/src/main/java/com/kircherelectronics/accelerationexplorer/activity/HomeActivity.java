package com.kircherelectronics.accelerationexplorer.activity;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.TextView;

import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.gauge.GaugeAcceleration;
import com.kircherelectronics.accelerationexplorer.view.VectorDrawableButton;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;

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
 *
 */
public class HomeActivity extends AppCompatActivity implements SensorEventListener
{
	private final static String tag = HomeActivity.class.getSimpleName();

	// The acceleration, in units of meters per second, as measured by the
	// accelerometer.
	private float[] acceleration = new float[3];

	// Handler for the UI plots so everything plots smoothly
	private Handler handler;

	private Runnable runnable;

	// Sensor manager to access the accelerometer
	private SensorManager sensorManager;

	private GaugeAcceleration gaugeAcceleration;


    private int accelerationDotColor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_home);

        this.accelerationDotColor = Color.parseColor("#2196F3");

		gaugeAcceleration = (GaugeAcceleration) findViewById(R.id.gauge_acceleration);

		initButtonGauge();
		initButtonLogger();
		initButtonVector();

		sensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);

		handler = new Handler();

		runnable = new Runnable()
		{
			@Override
			public void run()
			{
				handler.postDelayed(this, 20);
                updateGauge();
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
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

		// Start the vector activity
		case R.id.action_help:
			showHelpDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		sensorManager.unregisterListener(this);

		handler.removeCallbacks(runnable);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);

		handler.post(runnable);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			// Get a local copy of the acceleration measurements
			System.arraycopy(event.values, 0, acceleration, 0,event.values.length);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	private void initButtonGauge()
	{
		VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_gauge_mode);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(HomeActivity.this,
						GaugeActivity.class);

				startActivity(intent);
			}
		});
	}

	private void initButtonLogger()
	{
        VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_logger_mode);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(HomeActivity.this,
						LoggerActivity.class);

				startActivity(intent);
			}
		});
	}

	private void initButtonVector()
	{
        VectorDrawableButton button = (VectorDrawableButton) this.findViewById(R.id.button_vector_mode);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(HomeActivity.this,
						VectorActivity.class);

				startActivity(intent);
			}
		});
	}

	private void showHelpDialog()
	{
		Dialog helpDialog = new Dialog(this);

		helpDialog.setCancelable(true);
		helpDialog.setCanceledOnTouchOutside(true);
		helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		View view = getLayoutInflater()
				.inflate(R.layout.layout_help_home, null);

		helpDialog.setContentView(view);

		helpDialog.show();
	}

	private void updateGauge() {
        gaugeAcceleration.updatePoint(acceleration[0], acceleration[1], accelerationDotColor );
    }
}
