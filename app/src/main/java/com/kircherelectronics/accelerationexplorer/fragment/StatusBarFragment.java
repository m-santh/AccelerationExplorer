package com.kircherelectronics.accelerationexplorer.fragment;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.viewmodel.AccelerationViewModel;

import java.util.Locale;

/**
 * Created by kaleb on 7/7/17.
 */

public class StatusBarFragment extends LifecycleFragment {

    // Text views for real-time output
    private TextView textViewXAxis;
    private TextView textViewYAxis;
    private TextView textViewZAxis;
    private TextView textViewHzFrequency;

    private Handler handler;
    private Runnable runnable;

    private float[] acceleration;

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AccelerationViewModel model = ViewModelProviders.of(this).get(AccelerationViewModel.class);

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
                handler.postDelayed(this, 20);
                updateAccelerationText();
            }
        };

        acceleration = new float[4];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status_bar, container, false);

        textViewXAxis = (TextView) view.findViewById(R.id.value_x_axis);
        textViewYAxis = (TextView) view.findViewById(R.id.value_y_axis);
        textViewZAxis = (TextView) view.findViewById(R.id.value_z_axis);
        textViewHzFrequency = (TextView) view.findViewById(R.id.value_hz_frequency);

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

    private void updateAccelerationText()
    {
        // Update the acceleration data
        textViewXAxis.setText(String.format(Locale.getDefault(), "%.2f", acceleration[0]));
        textViewYAxis.setText(String.format(Locale.getDefault(),"%.2f", acceleration[1]));
        textViewZAxis.setText(String.format(Locale.getDefault(),"%.2f", acceleration[2]));
        textViewHzFrequency.setText(String.format(Locale.getDefault(),"%.0f", acceleration[3]));
    }
}
