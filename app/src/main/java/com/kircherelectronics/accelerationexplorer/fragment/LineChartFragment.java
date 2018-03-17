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

import com.github.mikephil.charting.charts.LineChart;
import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.plot.DynamicChart;
import com.kircherelectronics.accelerationexplorer.viewmodel.AccelerationViewModel;

/**
 * Created by kaleb on 7/9/17.
 */

public class LineChartFragment extends Fragment {

    // Graph plot for the UI outputs
    private DynamicChart dynamicChart;

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
                handler.postDelayed(this, 20);
                dynamicChart.setAcceleration(acceleration);
            }
        };

        acceleration = new float[4];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_line_chart, container, false);

        // Create the graph plot
        LineChart plot = (LineChart) view.findViewById(R.id.line_chart);
        dynamicChart = new DynamicChart(getContext(), plot);

        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        dynamicChart.onStopPlot();
        dynamicChart.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
        dynamicChart.onResume();
        dynamicChart.onStartPlot();
    }
}
