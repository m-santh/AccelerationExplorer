package com.kircherelectronics.accelerationexplorer.livedata.gyroscope;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.kircherelectronics.fsensor.sensor.gyroscope.GyroscopeSensor;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class GyroscopeSensorLiveData extends LiveData<float[]> {
    private GyroscopeSensor sensor;
    private CompositeDisposable compositeDisposable;

    public GyroscopeSensorLiveData(Context context) {
        this.sensor = new GyroscopeSensor(context);
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
