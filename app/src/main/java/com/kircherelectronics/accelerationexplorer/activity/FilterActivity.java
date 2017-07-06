package com.kircherelectronics.accelerationexplorer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.kircherelectronics.accelerationexplorer.activity.config.FilterConfigActivity;
import com.kircherelectronics.accelerationexplorer.prefs.PrefUtils;
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.averaging.MedianFilter;
import com.kircherelectronics.fsensor.filter.fusion.OrientationComplimentaryFusion;
import com.kircherelectronics.fsensor.filter.fusion.OrientationFusion;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationFusion;

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
 * A parent Activity for child classes that need to work with all the available
 * sensor filters. Deals with setting up all the sensors, the sensor
 * preferences, sensor state, etc...
 *
 * @author Kaleb
 */
public abstract class FilterActivity extends Activity implements SensorEventListener {
    private final static String tag = FilterActivity.class.getSimpleName();
    // Outputs for the acceleration and LPFs
    protected volatile float[] acceleration = new float[3];
    protected boolean androidLinearAccelerationEnabled;
    protected boolean axisInverted = false;
    protected volatile boolean dataReady = false;
    protected boolean fSensorLinearAccelerationEnabled;
    protected int frequencySelection;
    // Handler for the UI plots so everything plots smoothly
    protected Handler handler;
    protected float hz = 0;
    protected volatile float[] linearAcceleration = new float[3];
    protected LinearAcceleration linearAccelerationFilter;
    protected OrientationFusion orientationFusion;

    protected LowPassFilter lpfAccelSmoothing;
    protected LowPassFilter lpfMagneticSmoothing;
    protected LowPassFilter lpfRotationSmoothing;
    protected boolean lpfSmoothingEnabled;
    protected float[] magnetic = new float[3];
    protected MeanFilter meanFilterAcceleration;
    protected MeanFilter meanFilterMagneticSmoothing;
    protected MeanFilter meanFilterRotationSmoothing;
    protected boolean meanFilterSmoothingEnabled;
    protected MedianFilter medianFilterAccelSmoothing;
    protected MedianFilter medianFilterMagneticSmoothing;
    protected MedianFilter medianFilterRotationSmoothing;
    protected boolean medianFilterSmoothingEnabled;
    protected float[] rotation = new float[3];
    protected Runnable runable;
    // Sensor manager to access the accelerometer sensor
    protected SensorManager sensorManager;
    protected TextView textViewHzFrequency;
    // Text views for real-time output
    protected TextView textViewXAxis;
    protected TextView textViewYAxis;
    protected TextView textViewZAxis;
    private int count = 0;
    private float startTime = 0;
    private float timestamp = 0;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        meanFilterAcceleration = new MeanFilter();
        meanFilterMagneticSmoothing = new MeanFilter();
        meanFilterRotationSmoothing = new MeanFilter();

        medianFilterAccelSmoothing = new MedianFilter();
        medianFilterMagneticSmoothing = new MedianFilter();
        medianFilterRotationSmoothing = new MedianFilter();

        lpfAccelSmoothing = new LowPassFilter();
        lpfMagneticSmoothing = new LowPassFilter();
        lpfRotationSmoothing = new LowPassFilter();

        orientationFusion = new OrientationComplimentaryFusion();
        linearAccelerationFilter = new LinearAccelerationFusion(orientationFusion);

        sensorManager = (SensorManager) this
                .getSystemService(Context.SENSOR_SERVICE);

        handler = new Handler();
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);

        handler.removeCallbacks(runable);
    }

    @Override
    public void onResume() {
        super.onResume();

        resetSensorFrequencyTimer();
        initFilters();
        getAxisPrefs();
        updateSensorDelay();

        handler.post(runable);
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            calculateSensorFrequency();

            dataReady = true;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get a local copy of the sensor values
            System.arraycopy(event.values, 0, acceleration, 0,
                    event.values.length);

            if (axisInverted) {
                acceleration[0] = -acceleration[0];
                acceleration[1] = -acceleration[1];
                acceleration[2] = -acceleration[2];
            }

            if (meanFilterSmoothingEnabled) {
                acceleration = meanFilterAcceleration.filter(acceleration);
            }

            if (medianFilterSmoothingEnabled) {
                acceleration = medianFilterAccelSmoothing.filter(acceleration);
            }

            if (lpfSmoothingEnabled) {
                acceleration = lpfAccelSmoothing.filter(acceleration);
            }

            if (fSensorLinearAccelerationEnabled) {
                orientationFusion.setAcceleration(acceleration);
                linearAcceleration = linearAccelerationFilter.filter(acceleration);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Get a local copy of the sensor values
            System.arraycopy(event.values, 0, linearAcceleration, 0,
                    event.values.length);

            if (axisInverted) {
                linearAcceleration[0] = -linearAcceleration[0];
                linearAcceleration[1] = -linearAcceleration[1];
                linearAcceleration[2] = -linearAcceleration[2];
            }

            if (meanFilterSmoothingEnabled) {
                linearAcceleration = meanFilterAcceleration.filter(linearAcceleration);
            }

            if (medianFilterSmoothingEnabled) {
                linearAcceleration = medianFilterAccelSmoothing.filter(linearAcceleration);
            }

            if (lpfSmoothingEnabled) {
                linearAcceleration = lpfAccelSmoothing.filter(linearAcceleration);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            // Get a local copy of the sensor values
            System.arraycopy(event.values, 0, magnetic, 0, event.values.length);

            if (meanFilterSmoothingEnabled) {
                magnetic = meanFilterMagneticSmoothing.filter(magnetic);
            }

            if (medianFilterSmoothingEnabled) {
                magnetic = medianFilterMagneticSmoothing.filter(magnetic);
            }

            if (lpfSmoothingEnabled) {
                magnetic = lpfMagneticSmoothing.filter(magnetic);
            }

            if (fSensorLinearAccelerationEnabled) {
                orientationFusion.setMagneticField(magnetic);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Get a local copy of the sensor values
            System.arraycopy(event.values, 0, rotation, 0, event.values.length);

            if (meanFilterSmoothingEnabled) {
                rotation = meanFilterRotationSmoothing.filter(rotation);
            }

            if (medianFilterSmoothingEnabled) {
                rotation = medianFilterRotationSmoothing.filter(rotation);
            }

            if (lpfSmoothingEnabled) {
                rotation = lpfRotationSmoothing.filter(rotation);
            }

            if (fSensorLinearAccelerationEnabled) {
                linearAccelerationFilter.filter(rotation);
            }
        }
    }

    /**
     * Update the acceleration sensor output Text Views.
     */
    protected void updateAccelerationText() {
        if (!fSensorLinearAccelerationEnabled && !androidLinearAccelerationEnabled) {
            // Update the acceleration data
            textViewXAxis.setText(String.format("%.2f", acceleration[0]));
            textViewYAxis.setText(String.format("%.2f", acceleration[1]));
            textViewZAxis.setText(String.format("%.2f", acceleration[2]));
        } else {
            // Update the acceleration data
            textViewXAxis.setText(String.format("%.2f", linearAcceleration[0]));
            textViewYAxis.setText(String.format("%.2f", linearAcceleration[1]));
            textViewZAxis.setText(String.format("%.2f", linearAcceleration[2]));
        }

        textViewHzFrequency.setText(String.format("%.2f", hz));
    }

    private void calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        hz = (count++ / ((timestamp - startTime) / 1000000000.0f));
    }

    private void getAxisPrefs() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        axisInverted = prefs.getBoolean(
                FilterConfigActivity.AXIS_INVERSION_ENABLED_KEY, false);
    }

    private boolean getPrefAndroidLinearAccelEnabled() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return prefs.getBoolean(
                FilterConfigActivity.ANDROID_LINEAR_ACCEL_ENABLED_KEY, false);
    }

    private float getPrefImuLaCfOrienationCoeff() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString(
                FilterConfigActivity.IMULACF_ORIENTATION_COEFF_KEY, "0.5"));
    }

    private boolean getPrefLpfLinearAccelEnabled() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return prefs.getBoolean(
                FilterConfigActivity.LPF_LINEAR_ACCEL_ENABLED_KEY, false);
    }

    private boolean getPrefLpfSmoothingEnabled() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return prefs.getBoolean(FilterConfigActivity.LPF_SMOOTHING_ENABLED_KEY,
                false);
    }

    private float getPrefLpfSmoothingTimeConstant() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString(
                FilterConfigActivity.LPF_SMOOTHING_TIME_CONSTANT_KEY, "0.5"));
    }

    private boolean getPrefMeanFilterSmoothingEnabled() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return prefs.getBoolean(
                FilterConfigActivity.MEAN_FILTER_SMOOTHING_ENABLED_KEY, false);
    }

    private float getPrefMeanFilterSmoothingTimeConstant() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString(
                FilterConfigActivity.MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY,
                "0.5"));
    }

    private boolean getPrefMedianFilterSmoothingEnabled() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return prefs
                .getBoolean(
                        FilterConfigActivity.MEDIAN_FILTER_SMOOTHING_ENABLED_KEY,
                        false);
    }

    private float getPrefMedianFilterSmoothingTimeConstant() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString(
                FilterConfigActivity.MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY,
                "0.5"));
    }

    /**
     * Read in the current user preferences.
     */
    private void getSensorFrequencyPrefs() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        this.frequencySelection = Integer.parseInt(prefs.getString(PrefUtils.SENSOR_FREQUENCY_PREF,
                PrefUtils.SENSOR_FREQUENCY_FAST));
    }

    private void initFilters() {
        meanFilterSmoothingEnabled = getPrefMeanFilterSmoothingEnabled();
        medianFilterSmoothingEnabled = getPrefMedianFilterSmoothingEnabled();
        lpfSmoothingEnabled = getPrefLpfSmoothingEnabled();

        meanFilterAcceleration.setTimeConstant(getPrefMeanFilterSmoothingTimeConstant());
        meanFilterMagneticSmoothing.setTimeConstant(getPrefMeanFilterSmoothingTimeConstant());
        meanFilterRotationSmoothing.setTimeConstant(getPrefMeanFilterSmoothingTimeConstant());

        medianFilterAccelSmoothing.setTimeConstant(getPrefMedianFilterSmoothingTimeConstant());
        medianFilterMagneticSmoothing.setTimeConstant(getPrefMedianFilterSmoothingTimeConstant());
        medianFilterRotationSmoothing.setTimeConstant(getPrefMedianFilterSmoothingTimeConstant());

        lpfAccelSmoothing.setTimeConstant(getPrefLpfSmoothingTimeConstant());
        lpfMagneticSmoothing.setTimeConstant(getPrefLpfSmoothingTimeConstant());
        lpfRotationSmoothing.setTimeConstant(getPrefLpfSmoothingTimeConstant());

        fSensorLinearAccelerationEnabled = getPrefLpfLinearAccelEnabled();
        //lpfLinearAcceleration.setFilterCoefficient(getPrefLpfLinearAccelCoeff());

        if (fSensorLinearAccelerationEnabled) {
            //linearAccelerationFilter = new ImuLaCfOrientation();
            // TODO change this to a time constant
            linearAccelerationFilter.setTimeConstant(getPrefImuLaCfOrienationCoeff());
        }

        androidLinearAccelerationEnabled = getPrefAndroidLinearAccelEnabled();
    }

    private void resetSensorFrequencyTimer() {
        count = 0;
        startTime = 0;
        timestamp = 0;
        hz = 0;
    }

    /**
     * Set the sensor delay based on user preferences. 0 = slow, 1 = medium, 2 =
     * fast.
     *
     * @param position The desired sensor delay.
     */
    private void setSensorDelay(int position) {
        switch (position) {
            case 0:

                if (!androidLinearAccelerationEnabled) {
                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                            SensorManager.SENSOR_DELAY_NORMAL);
                }

                if ((fSensorLinearAccelerationEnabled) && !androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                            SensorManager.SENSOR_DELAY_NORMAL);

                    // Register for sensor updates.
                    sensorManager.registerListener(this,
                            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                            SensorManager.SENSOR_DELAY_NORMAL);
                }

                break;
            case 1:

                if (!androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_GAME);
                } else {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                            SensorManager.SENSOR_DELAY_GAME);
                }

                if ((fSensorLinearAccelerationEnabled) && !androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                            SensorManager.SENSOR_DELAY_GAME);

                    // Register for sensor updates.
                    sensorManager.registerListener(this,
                            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                            SensorManager.SENSOR_DELAY_GAME);
                }

                break;
            case 2:

                if (!androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_FASTEST);
                } else {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                            SensorManager.SENSOR_DELAY_FASTEST);
                }

                if ((fSensorLinearAccelerationEnabled) && !androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(this, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                            SensorManager.SENSOR_DELAY_FASTEST);

                    // Register for sensor updates.
                    sensorManager.registerListener(this,
                            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                            SensorManager.SENSOR_DELAY_FASTEST);
                }

                break;
        }
    }

    /**
     * Updates the sensor delay based on the user preference. 0 = slow, 1 =
     * medium, 2 = fast.
     */
    private void updateSensorDelay() {
        getSensorFrequencyPrefs();

        setSensorDelay(frequencySelection);
    }
}
