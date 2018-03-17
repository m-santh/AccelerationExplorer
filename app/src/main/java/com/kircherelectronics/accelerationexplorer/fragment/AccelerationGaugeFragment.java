package com.kircherelectronics.accelerationexplorer.fragment;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.gauge.GaugeAcceleration;
import com.kircherelectronics.accelerationexplorer.viewmodel.AccelerationViewModel;

/*
 * AccelerationExplorer
 * Copyright 2017 Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by kaleb on 7/8/17.
 */

public class AccelerationGaugeFragment extends Fragment {

    private GaugeAcceleration gaugeAcceleration;
    private Handler handler;
    private Runnable runnable;

    private float[] acceleration;

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AccelerationViewModel model = ViewModelProviders.of(getActivity()).get(AccelerationViewModel.class);

        model.getAccelerationListener().observe(this, new Observer<float[]>() {
            @Override
            public void onChanged(@Nullable float[] floats) {
                acceleration = floats;
            }
        });

        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateAccelerationGauge();
                handler.postDelayed(this, 20);
            }
        };

        acceleration = new float[4];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acceleration_gauge, container, false);

        gaugeAcceleration = (GaugeAcceleration) view.findViewById(R.id.gauge_acceleration);

        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    private void updateAccelerationGauge() {
        gaugeAcceleration.updatePoint(acceleration[0], acceleration[1]);
    }
}
