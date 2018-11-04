package com.kircherelectronics.accelerationexplorer.livedata.acceleration;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.kircherelectronics.accelerationexplorer.prefs.PrefUtils;
import com.kircherelectronics.fsensor.filter.averaging.AveragingFilter;
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.averaging.MedianFilter;
import com.kircherelectronics.fsensor.sensor.acceleration.ComplimentaryLinearAccelerationSensor;
import com.kircherelectronics.fsensor.sensor.acceleration.LowPassLinearAccelerationSensor;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ComplimentaryLinearAccelerationSensorLiveData extends LiveData<float[]> {
    private ComplimentaryLinearAccelerationSensor sensor;
    private CompositeDisposable compositeDisposable;
    private Context context;
    private AveragingFilter averagingFilter;

    public ComplimentaryLinearAccelerationSensorLiveData(Context context) {
        this.context = context;
        this.sensor = new ComplimentaryLinearAccelerationSensor(context);
    }

    @Override
    protected void onActive() {
        this.sensor.setFSensorComplimentaryLinearAccelerationTimeConstant(PrefUtils.getPrefFSensorComplimentaryLinearAccelerationTimeConstant(context));
        this.sensor.setSensorFrequency(PrefUtils.getSensorFrequencyPrefs(context));

        if(PrefUtils.getPrefLpfSmoothingEnabled(context)) {
            averagingFilter = new LowPassFilter();
            ((LowPassFilter) averagingFilter).setTimeConstant(PrefUtils.getPrefLpfSmoothingTimeConstant(context));
        } else if(PrefUtils.getPrefMeanFilterSmoothingEnabled(context)) {
            averagingFilter = new MeanFilter();
            ((MeanFilter) averagingFilter).setTimeConstant(PrefUtils.getPrefMeanFilterSmoothingTimeConstant(context));
        } else if(PrefUtils.getPrefMedianFilterSmoothingEnabled(context)) {
            averagingFilter = new MedianFilter();
            ((MedianFilter) averagingFilter).setTimeConstant(PrefUtils.getPrefMedianFilterSmoothingTimeConstant(context));
        } else {
            averagingFilter = null;
        }

        this.compositeDisposable = new CompositeDisposable();
        this.sensor.getPublishSubject().subscribe(new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(float[] values) {
                if(averagingFilter != null) {
                    setValue(averagingFilter.filter(values));
                } else {
                    setValue(values);
                }
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() {}
        });
        this.sensor.onStart();
    }

    @Override
    protected void onInactive() {
        this.compositeDisposable.dispose();
        this.sensor.onStop();
    }
}
