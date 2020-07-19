package com.deepnxt.tracksafe.fragment;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.deepnxt.tracksafe.R;
import com.deepnxt.tracksafe.gauge.GaugeAcceleration;
import com.deepnxt.tracksafe.prefs.PrefUtils;
import com.deepnxt.tracksafe.viewmodel.SensorViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import butterknife.ButterKnife;
//import butterknife.BindView;

/*
 * AccelerationExplorer
 * Copyright 2018 Kircher Electronics, LLC
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

    WebView webview;

    //private JSONObject trace;
    private JSONArray xValue = new JSONArray(), yValue = new JSONArray(), zValue = new JSONArray();

    private GaugeAcceleration gaugeAcceleration;
    private Handler handler;
    private Runnable runnable;

    private float[] acceleration;

    private double [] referencePoint = new double[3];

    private static long prevTime = 0;
    private static long DELTA_TIME_MS = 20; // 20 millisecond
    private static double ACCEL_SENSITIVITY_LOW = 0.02; // for 20 millisecond
    private static  double DIST_SENSITIVITY_LOW = 0.001; // for 100 millisecond

    public class WebAppInterface {
        private AccelerationGaugeFragment context;

        public WebAppInterface(AccelerationGaugeFragment context) {
            this.context = context;
        }

        @JavascriptInterface
        public JSONObject loadData() {
//            return "[{\"letter\": \"A\", \"frequency\": \".09\" },{\"letter\": \"B\", \"frequency\": \".05\" }]";
            try {
                return createDataSet();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        private final JSONObject createDataSet() throws JSONException{

            JSONObject data = new JSONObject();
            data.put("x", yValue);
            data.put("y", zValue);
            data.put("z", xValue);

            return data;
            /*
            final Random rand = new Random(System.currentTimeMillis());
            final String[] x = new String[] {
                    "A", "B", "C", "D", "E", "F",
                    "G", "H", "I", "J", "K", "L",
                    "M", "N", "O", "P", "Q", "R",
                    "S", "T", "U", "V", "W", "X",
                    "Y", "Z"};
            final List<DataPoint> set = new ArrayList<DataPoint>();
            for (int i = 0; i < x.length ; i++) {
                set.add( new DataPoint(x[i], rand.nextFloat()));
            }
            final DataPoint[] pts = set.toArray( new DataPoint[]{} );
            return new Gson().toJson(pts, DataPoint[].class );

             */
        }
    }

    protected void addPoint(double [] distance) throws JSONException {

        xValue.put((double)distance[0]);
        yValue.put((double)distance[1]);
        zValue.put((double)distance[2]);

        JSONObject data = new JSONObject();
        data.put("x", xValue);
        data.put("y", zValue);
        data.put("z", yValue);

        /*
        Log.d("", " distanceX: " + distance[0] );
        Log.d("", " distanceY: " + distance[1] );
        Log.d("", " DistanceZ: " + distance[2] );

         */

        webview.loadUrl("javascript:PlotlyUpdate('"+data+"')");
    }

    public double calculateEquiDistance(double[] acceleration) {
        double sqrSum = 0;
        for (int i = 0; i < acceleration.length; i++) {
            sqrSum = sqrSum + acceleration[i] * acceleration[i];
        }

        return (double) Math.sqrt(sqrSum);
    }

    protected void scatterPlotUpdate(float [] acceleration) {
        double [] accValues = new double[3];
        double [] speed = new double[3];
        double [] distance = new double[3];

        final double DELTA_TIME = (double)DELTA_TIME_MS / 1000;
        accValues[0] = acceleration[0];
        accValues[1] = acceleration[1];
        accValues[2] = acceleration[2];

        double overallAcc = calculateEquiDistance(accValues);

        if((accValues[0] < ACCEL_SENSITIVITY_LOW) && (accValues[1] < ACCEL_SENSITIVITY_LOW) && (accValues[2] < ACCEL_SENSITIVITY_LOW)) {
            // too low
            return;
        }

        speed[0] = accValues[0] * DELTA_TIME;
        speed[1] = accValues[1] * DELTA_TIME;
        speed[2] = accValues[2] * DELTA_TIME;

        distance[0] = speed[0] * DELTA_TIME;
        distance[1] = speed[1] * DELTA_TIME;
        distance[2] = speed[2] * DELTA_TIME;

        if ((distance[0] < DIST_SENSITIVITY_LOW) && (distance[1] < DIST_SENSITIVITY_LOW) && (distance[2] < DIST_SENSITIVITY_LOW)) {
            // too small a distance
            return;
        }

        Log.d("", " distanceX: " + distance[0] );
        Log.d("", " distanceY: " + distance[1] );
        Log.d("", " DistanceZ: " + distance[2] );

        referencePoint[0] = referencePoint[0] + distance[0];
        referencePoint[1] = referencePoint[1] + distance[1];
        referencePoint[2] = referencePoint[2] + distance[2];

        try {
            addPoint(referencePoint);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateAccelerationGauge();
                handler.postDelayed(this, DELTA_TIME_MS);
            }
        };

        acceleration = new float[4];

        //ButterKnife.bind( this.getActivity() );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acceleration_gauge, container, false);

        gaugeAcceleration = view.findViewById(R.id.gauge_acceleration);
        webview =  (WebView) view.findViewById(R.id.webview);
        //webview = scatter.findViewById(R.id.webview);

        final WebSettings ws = webview.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setPluginState(WebSettings.PluginState.ON);
        ws.setAllowFileAccess(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface( new WebAppInterface( this ), "Android");
        webview.loadUrl("file:///android_asset/main.html");

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

        initViewModel();

        handler.post(runnable);

    }

    private void updateAccelerationGauge() {
        gaugeAcceleration.updatePoint(acceleration[0], acceleration[1]);
        scatterPlotUpdate(acceleration);
    }

    private void initViewModel() {
        SensorViewModel model = ViewModelProviders.of(getActivity()).get(SensorViewModel.class);

        model.getLinearAccelerationSensorLiveData().removeObservers(this);
        model.getLowPassLinearAccelerationSensorLiveData().removeObservers(this);
        model.getComplimentaryLinearAccelerationSensorLiveData().removeObservers(this);
        model.getKalmanLinearAccelerationSensorLiveData().removeObservers(this);
        model.getAccelerationSensorLiveData().removeObservers(this);

        if(PrefUtils.getPrefAndroidLinearAccelerationEnabled(getContext())) {
            Log.d("HELLO", "ALAE");
            model.getLinearAccelerationSensorLiveData().observe(this, new Observer<float[]>() {
                @Override
                public void onChanged(@Nullable float[] floats) {
                    acceleration = floats;
                }
            });
        } else if(PrefUtils.getPrefFSensorLpfLinearAccelerationEnabled(getContext())){
            Log.d("HELLO", "LPFE");
            model.getLowPassLinearAccelerationSensorLiveData().observe(this, new Observer<float[]>() {
                @Override
                public void onChanged(@Nullable float[] floats) {
                    acceleration = floats;
                }
            });
        } else if(PrefUtils.getPrefFSensorComplimentaryLinearAccelerationEnabled(getContext())) {
            Log.d("HELLO", "SLAE");
            model.getComplimentaryLinearAccelerationSensorLiveData().observe(this, new Observer<float[]>() {
                @Override
                public void onChanged(@Nullable float[] floats) {
                    acceleration = floats;
                }
            });
        } else if(PrefUtils.getPrefFSensorKalmanLinearAccelerationEnabled(getContext())) {
            Log.d("HELLO", "SKAE");
            model.getKalmanLinearAccelerationSensorLiveData().observe(this, new Observer<float[]>() {
                @Override
                public void onChanged(@Nullable float[] floats) {
                    acceleration = floats;
                }
            });
        } else {
            Log.d("HELLO", "AcceSen");
            model.getAccelerationSensorLiveData().observe(this, new Observer<float[]>() {
                @Override
                public void onChanged(@Nullable float[] floats) {
                    acceleration = floats;
                }
            });
        }
    }
}
