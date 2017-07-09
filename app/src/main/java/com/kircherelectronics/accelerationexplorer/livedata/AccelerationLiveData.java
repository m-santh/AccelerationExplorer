package com.kircherelectronics.accelerationexplorer.livedata;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.kircherelectronics.fsensor.filter.averaging.AveragingFilter;
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.averaging.MedianFilter;
import com.kircherelectronics.fsensor.filter.fusion.OrientationComplimentaryFusion;
import com.kircherelectronics.fsensor.filter.fusion.OrientationFusion;
import com.kircherelectronics.fsensor.filter.fusion.OrientationKalmanFusion;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationAveraging;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationFusion;

/**
 * Created by kaleb on 7/7/17.
 */

public class AccelerationLiveData<T> extends LiveData<float[]> {

    private static final String tag = AccelerationLiveData.class.getSimpleName();

    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private boolean meanFilterSmoothingEnabled = false;
    private boolean lpfSmoothingEnabled = false;
    private boolean medianFilterSmoothingEnabled = false;

    private float[] magnetic = new float[3];
    private float[] acceleration = new float[4];
    private float[] rotation = new float[3];

    private boolean androidLinearAccelerationEnabled;
    private boolean fSensorComplimentaryLinearAccelerationEnabled;
    private boolean fSensorKalmanLinearAccelerationEnabled;
    private boolean fSensorLpfLinearAccelerationEnabled;
    private boolean axisInverted = false;

    private LowPassFilter lpfAccelerationSmoothing;
    private MeanFilter meanFilterAccelerationSmoothing;
    private MedianFilter medianFilterAccelerationSmoothing;

    private LinearAcceleration linearAccelerationFilterComplimentary;
    private LinearAcceleration linearAccelerationFilterKalman;
    private LinearAcceleration linearAccelerationFilterLpf;
    private OrientationFusion orientationFusionComplimentary;
    private OrientationFusion orientationFusionKalman;
    private AveragingFilter lpfGravity;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;


    public AccelerationLiveData(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        listener = new SimpleSensorListener();

        initializeLowPassFilter();
        initializeMeanFilters();
        initializeMedianFilters();
        initializeFSensorFusions();
    }

    @Override
    protected void onActive() {
        startTime =0;
        count = 0;

        registerSensors(sensorFrequency);

        if(fSensorKalmanLinearAccelerationEnabled) {
            orientationFusionKalman.startFusion();
        }
    }

    @Override
    protected void onInactive(){
        if(fSensorKalmanLinearAccelerationEnabled) {
            orientationFusionKalman.stopFusion();
        }

        unregisterSensors();
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    public void setAxisInverted(boolean axisInverted) {
        this.axisInverted = axisInverted;
    }

    public boolean isAxisInverted() {
        return this.axisInverted;
    }

    public void setSensorFrequency(int sensorFrequency) {
        this.sensorFrequency = sensorFrequency;
    }

    public int getSensorFrequency() {
        return this.sensorFrequency;
    }

    public void enableAndroidLinearAcceleration(boolean enabled) {
        unregisterSensors();
        this.androidLinearAccelerationEnabled = enabled;
        registerSensors(sensorFrequency);

        // Only one linear acceleration estimation at a time
        if(this.androidLinearAccelerationEnabled) {
            orientationFusionKalman.stopFusion();
            this.fSensorComplimentaryLinearAccelerationEnabled = false;
            this.fSensorKalmanLinearAccelerationEnabled = false;
            this.fSensorLpfLinearAccelerationEnabled = false;
        }
    }

    public boolean isAndroidLinearAccelerationEnabled(boolean enabled) {
        return this.androidLinearAccelerationEnabled;
    }

    public void enableFSensorComplimentaryLinearAcceleration(boolean enabled) {
        unregisterSensors();
        this.fSensorComplimentaryLinearAccelerationEnabled = enabled;
        registerSensors(sensorFrequency);

        // Only one linear acceleration estimation at a time
        if(this.fSensorComplimentaryLinearAccelerationEnabled) {
            orientationFusionKalman.stopFusion();
            this.androidLinearAccelerationEnabled = false;
            this.fSensorKalmanLinearAccelerationEnabled = false;
            this.fSensorLpfLinearAccelerationEnabled = false;
        }
    }

    public boolean isFSensorComplimentaryLinearAccelerationEnabled() {
        return this.fSensorComplimentaryLinearAccelerationEnabled;
    }

    public void enableFSensorKalmanLinearAcceleration(boolean enabled) {
        unregisterSensors();
        this.fSensorKalmanLinearAccelerationEnabled = enabled;
        registerSensors(sensorFrequency);

        // Only one linear acceleration estimation at a time
        if(this.fSensorKalmanLinearAccelerationEnabled) {
            orientationFusionKalman.startFusion();
            this.androidLinearAccelerationEnabled = false;
            this.fSensorComplimentaryLinearAccelerationEnabled = false;
            this.fSensorLpfLinearAccelerationEnabled = false;
        } else {
            orientationFusionKalman.stopFusion();
        }
    }

    public boolean isFSensorKalmanLinearAccelerationEnabled() {
        return this.fSensorKalmanLinearAccelerationEnabled;
    }

    public void enableFSensorLpfLinearAcceleration(boolean enabled) {
        unregisterSensors();
        this.fSensorLpfLinearAccelerationEnabled = enabled;
        registerSensors(sensorFrequency);

        // Only one linear acceleration estimation at a time
        if(this.fSensorLpfLinearAccelerationEnabled) {
            orientationFusionKalman.stopFusion();
            this.androidLinearAccelerationEnabled = false;
            this.fSensorKalmanLinearAccelerationEnabled = false;
            this.fSensorComplimentaryLinearAccelerationEnabled = false;
        }
    }

    public boolean isfSensorLpfLinearAccelerationEnabled() {
        return this.fSensorComplimentaryLinearAccelerationEnabled;
    }

    public void setMeanFilterSmoothingTimeConstant(float timeConstant) {
        this.meanFilterAccelerationSmoothing.setTimeConstant(timeConstant);
    }

    public void enableMeanFilterSmoothing(boolean enabled) {
        this.meanFilterSmoothingEnabled = enabled;
    }

    public boolean isMeanFilterSmoothingEnabled() {
        return this.meanFilterSmoothingEnabled;
    }

    public void setMedianFilterSmoothingTimeConstant(float timeConstant) {
        this.medianFilterAccelerationSmoothing.setTimeConstant(timeConstant);
    }

    public void enableMedianFilterSmoothing(boolean enabled) {
        this.medianFilterSmoothingEnabled = enabled;
    }

    public boolean isMedianFilterSmoothingEnabled() {
        return this.medianFilterSmoothingEnabled;
    }

    public void setLpfSmoothingTimeConstant(float timeConstant) {
        this.lpfAccelerationSmoothing.setTimeConstant(timeConstant);
    }

    public void enableLpfSmoothing(boolean enabled) {
        this.lpfSmoothingEnabled = enabled;
    }

    public boolean isLpfSmoothingEnabled() {
        return this.lpfSmoothingEnabled;
    }

    public void setFSensorComplimentaryLinearAccelerationTimeConstant(float timeConstant) {
        linearAccelerationFilterComplimentary.setTimeConstant(timeConstant);
    }

    public void setFSensorLpfLinearAccelerationTimeConstant(float timeConstant) {
        linearAccelerationFilterLpf.setTimeConstant(timeConstant);
    }

    private float calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float hz = (count++ / ((timestamp - startTime) / 1000000000.0f));

        return hz;
    }

    private float[] invert(float[] values) {
        for(int i = 0; i < 3; i++) {
            values[i] = -values[i];
        }

        return values;
    }

    private void initializeLowPassFilter() {
        lpfAccelerationSmoothing = new LowPassFilter();
    }

    private void initializeMeanFilters() {
        meanFilterAccelerationSmoothing = new MeanFilter();
    }

    private void initializeMedianFilters() {
        medianFilterAccelerationSmoothing = new MedianFilter();
    }

    private void initializeFSensorFusions() {

        lpfGravity = new LowPassFilter();
        orientationFusionComplimentary = new OrientationComplimentaryFusion();
        orientationFusionKalman = new OrientationKalmanFusion();

        linearAccelerationFilterComplimentary = new LinearAccelerationFusion(orientationFusionComplimentary);
        linearAccelerationFilterKalman = new LinearAccelerationFusion(orientationFusionKalman);
        linearAccelerationFilterLpf = new LinearAccelerationAveraging(lpfGravity);

    }

    private void processAcceleration(float[] acceleration) {
        if(axisInverted) {
            acceleration = invert(acceleration);
        }

        if (meanFilterSmoothingEnabled) {
            acceleration = meanFilterAccelerationSmoothing.filter(acceleration);
        } else if (medianFilterSmoothingEnabled) {
            acceleration = medianFilterAccelerationSmoothing.filter(acceleration);
        } else if (lpfSmoothingEnabled) {
            acceleration = lpfAccelerationSmoothing.filter(acceleration);
        }

        System.arraycopy(acceleration, 0, this.acceleration, 0, acceleration.length);
        this.acceleration[3] = calculateSensorFrequency();
    }

    private void processMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, magnetic.length);

        if (fSensorComplimentaryLinearAccelerationEnabled) {
            // Get a local copy of the sensor values
            orientationFusionComplimentary.setMagneticField(this.magnetic);
        } else if( fSensorKalmanLinearAccelerationEnabled) {
            orientationFusionKalman.setMagneticField(this.magnetic);
        }
    }

    private void processRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, rotation.length);

        if (fSensorComplimentaryLinearAccelerationEnabled) {
            // Get a local copy of the sensor values
            orientationFusionComplimentary.filter(this.rotation);
        } else if(fSensorKalmanLinearAccelerationEnabled) {
            orientationFusionKalman.filter(this.rotation);
        }
    }

    private void registerSensors(int sensorDelay) {

        meanFilterAccelerationSmoothing.reset();
        medianFilterAccelerationSmoothing.reset();
        lpfAccelerationSmoothing.reset();

        linearAccelerationFilterLpf.reset();
        lpfGravity.reset();

        linearAccelerationFilterComplimentary.reset();
        orientationFusionComplimentary.reset();

        linearAccelerationFilterKalman.reset();
        orientationFusionKalman.reset();

        switch (sensorDelay) {
            case SensorManager.SENSOR_DELAY_NORMAL:
                if (!androidLinearAccelerationEnabled) {
                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                            SensorManager.SENSOR_DELAY_NORMAL);
                }

                if ((fSensorComplimentaryLinearAccelerationEnabled || fSensorKalmanLinearAccelerationEnabled) && !androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                            SensorManager.SENSOR_DELAY_NORMAL);

                    // Register for sensor updates.
                    sensorManager.registerListener(listener,
                            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                            SensorManager.SENSOR_DELAY_NORMAL);
                }

                break;
            case SensorManager.SENSOR_DELAY_GAME:
                if (!androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_GAME);
                } else {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                            SensorManager.SENSOR_DELAY_GAME);
                }

                if ((fSensorComplimentaryLinearAccelerationEnabled || fSensorKalmanLinearAccelerationEnabled) && !androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                            SensorManager.SENSOR_DELAY_GAME);

                    // Register for sensor updates.
                    sensorManager.registerListener(listener,
                            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                            SensorManager.SENSOR_DELAY_GAME);
                }

                break;
            case SensorManager.SENSOR_DELAY_FASTEST:
                if (!androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_FASTEST);
                } else {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                            SensorManager.SENSOR_DELAY_FASTEST);
                }

                if ((fSensorComplimentaryLinearAccelerationEnabled || fSensorKalmanLinearAccelerationEnabled) && !androidLinearAccelerationEnabled) {

                    // Register for sensor updates.
                    sensorManager.registerListener(listener, sensorManager
                                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                            SensorManager.SENSOR_DELAY_FASTEST);

                    // Register for sensor updates.
                    sensorManager.registerListener(listener,
                            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                            SensorManager.SENSOR_DELAY_FASTEST);
                }

                break;
        }
    }

    public enum FusionType {
        COMPLIMENTARY(0),
        KALMAN(1),
        LPF(2);

        private final int id;

        FusionType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private class SimpleSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (fSensorComplimentaryLinearAccelerationEnabled) {
                    float[] acceleration = new float[3];
                    System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
                    orientationFusionComplimentary.setAcceleration(acceleration);
                    processAcceleration(linearAccelerationFilterComplimentary.filter(acceleration));
                } else if(fSensorKalmanLinearAccelerationEnabled) {
                    float[] acceleration = new float[3];
                    System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
                    orientationFusionKalman.setAcceleration(acceleration);
                    processAcceleration(linearAccelerationFilterKalman.filter(acceleration));
                } else if (fSensorLpfLinearAccelerationEnabled) {
                    float[] acceleration = new float[3];
                    System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
                    lpfGravity.filter(acceleration);
                    processAcceleration(linearAccelerationFilterLpf.filter(acceleration));
                } else {
                    processAcceleration(event.values);
                }
                setValue(acceleration);
            } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                processAcceleration(event.values);
                setValue(acceleration);
            } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                processMagnetic(event.values);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                processRotation(event.values);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}
