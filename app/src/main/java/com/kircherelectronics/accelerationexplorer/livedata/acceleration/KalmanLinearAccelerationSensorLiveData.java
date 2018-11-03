package com.kircherelectronics.accelerationexplorer.livedata.acceleration;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.kircherelectronics.fsensor.sensor.acceleration.KalmanLinearAccelerationSensor;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class KalmanLinearAccelerationSensorLiveData extends LiveData<float[]> {
    private KalmanLinearAccelerationSensor sensor;
    private CompositeDisposable compositeDisposable;

    public KalmanLinearAccelerationSensorLiveData(Context context) {
        this.sensor = new KalmanLinearAccelerationSensor(context);
    }

    @Override
    protected void onActive() {
        this.compositeDisposable = new CompositeDisposable();
        this.sensor.getPublishSubject().subscribe(new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(float[] values) {
                setValue(values);
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
