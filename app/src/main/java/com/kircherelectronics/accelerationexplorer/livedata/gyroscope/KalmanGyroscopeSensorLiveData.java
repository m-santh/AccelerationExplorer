package com.kircherelectronics.accelerationexplorer.livedata.gyroscope;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.kircherelectronics.fsensor.sensor.gyroscope.KalmanGyroscopeSensor;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class KalmanGyroscopeSensorLiveData extends LiveData<float[]> {
    private KalmanGyroscopeSensor sensor;
    private CompositeDisposable compositeDisposable;

    public KalmanGyroscopeSensorLiveData(Context context) {
        this.sensor = new KalmanGyroscopeSensor (context);
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
