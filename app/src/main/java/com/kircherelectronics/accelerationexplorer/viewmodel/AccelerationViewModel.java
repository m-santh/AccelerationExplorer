package com.kircherelectronics.accelerationexplorer.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.kircherelectronics.accelerationexplorer.livedata.AccelerationLiveData;

/**
 * Created by kaleb on 7/7/17.
 */

public class AccelerationViewModel extends AndroidViewModel {
    AccelerationLiveData<float[]> accelerationListener;

    public AccelerationViewModel(Application application) {
        super(application);
        accelerationListener = new AccelerationLiveData(application);
    }

    public AccelerationLiveData<float[]> getAccelerationListener() {
        return accelerationListener;
    }

}
