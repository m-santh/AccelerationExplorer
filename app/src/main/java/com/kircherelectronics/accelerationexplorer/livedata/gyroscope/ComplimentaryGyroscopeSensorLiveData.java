package com.kircherelectronics.accelerationexplorer.livedata.gyroscope;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.kircherelectronics.fsensor.sensor.gyroscope.ComplimentaryGyroscopeSensor;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ComplimentaryGyroscopeSensorLiveData extends LiveData<float[]> {
    private ComplimentaryGyroscopeSensor sensor;
    private CompositeDisposable compositeDisposable;

    public ComplimentaryGyroscopeSensorLiveData(Context context) {
        this.sensor = new ComplimentaryGyroscopeSensor (context);
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
        sensor.onStart();
    }

    @Override
    protected void onInactive() {
        this.compositeDisposable.dispose();
        this.sensor.onStop();
    }
}
