package com.kircherelectronics.accelerationexplorer.activity;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.kircherelectronics.accelerationexplorer.R;
import com.kircherelectronics.accelerationexplorer.activity.config.FilterConfigActivity;
import com.kircherelectronics.accelerationexplorer.plot.DynamicChart;
import com.kircherelectronics.accelerationexplorer.plot.PlotColor;
import com.kircherelectronics.accelerationexplorer.view.VectorDrawableButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

/*
 * Acceleration Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * An Activity that plots the three axes outputs of the acceleration sensor in
 * real-time, as well as displays the tilt of the device and acceleration of the
 * device in two-dimensions. The acceleration sensor can be logged to an
 * external .CSV file.
 *
 * @author Kaleb
 * @version %I%, %G%
 */
public class LoggerActivity extends FilterActivity implements Runnable {
    // Plot keys for the acceleration plot
    private final static String tag = LoggerActivity.class.getSimpleName();
    // Plot colors

    private DecimalFormat df;
    // Graph plot for the UI outputs
    private DynamicChart dynamicChart;
    // The generation of the log output
    private int generation = 0;
    // Output log
    private String log;
    // Indicate if the output should be logged to a .csv file
    private boolean logData = false;
    // Log output time stamp
    private long logTime = 0;
    // Color keys for the acceleration plot
    // Acceleration plot titles
    private String plotAccelXAxisTitle = "X-Axis";
    private String plotAccelYAxisTitle = "Y-Axis";
    private String plotAccelZAxisTitle = "Z-Axis";
    private String plotSensorFrequencyTitle = "Frequency";
    private Thread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_logger);

        textViewXAxis = (TextView) findViewById(R.id.value_x_axis);
        textViewYAxis = (TextView) findViewById(R.id.value_y_axis);
        textViewZAxis = (TextView) findViewById(R.id.value_z_axis);
        textViewHzFrequency = (TextView) findViewById(R.id.value_hz_frequency);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        df = (DecimalFormat) nf;
        df.applyPattern("###.####");

        initPlots();
        initStartButton();

        runable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 10);
                updateAccelerationText();
                dynamicChart.setAcceleration(acceleration);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logger, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected Identify single menu
     * item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Log the data
            case R.id.action_settings_sensor:
                startIntentSensorSettings();
                return true;

            // Start the vector activity
            case R.id.action_help:
                showHelpDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dynamicChart.onStopPlot();
        dynamicChart.onPause();
        stopDataLog();
    }

    @Override
    public void onResume() {
        super.onResume();
        dynamicChart.onResume();
        dynamicChart.onStartPlot();
    }

    /**
     * Output and logs are run on their own thread to keep the UI from hanging
     * and the output smooth.
     */
    @Override
    public void run() {
        while (logData && !Thread.currentThread().isInterrupted()) {
            logData();
        }

        Thread.currentThread().interrupt();
    }

    /**
     * Initialize the plots.
     */
    private void initPlots() {
        // Create the graph plot
        LineChart plot = (LineChart) findViewById(R.id.plot_sensor);

        dynamicChart = new DynamicChart(this, plot);
    }

    private void initStartButton() {
        final VectorDrawableButton button = (VectorDrawableButton) findViewById(R.id.button_start);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!logData) {
                    button.setBackgroundResource(R.drawable.stop_button_background);
                    button.setText("Stop Log");

                    startDataLog();

                    thread = new Thread(LoggerActivity.this);

                    thread.start();
                } else {
                    button.setBackgroundResource(R.drawable.start_button_background);
                    button.setText("Start Log");

                    stopDataLog();
                }
            }
        });
    }

    /**
     * Log output data to an external .csv file.
     */
    private void logData() {
        if (logData && dataReady) {
            if (generation == 0) {
                logTime = System.currentTimeMillis();
            }

            log += generation++ + ",";

            float timestamp = (System.currentTimeMillis() - logTime) / 1000.0f;

            log += df.format(timestamp) + ",";

            if (!fSensorLinearAccelerationEnabled && !androidLinearAccelerationEnabled
                    ) {
                log += df.format(acceleration[0]) + ",";
                log += df.format(acceleration[1]) + ",";
                log += df.format(acceleration[2]) + ",";
            } else {
                log += df.format(linearAcceleration[0]) + ",";
                log += df.format(linearAcceleration[1]) + ",";
                log += df.format(linearAcceleration[2]) + ",";
            }

            log += df.format(hz) + ",";

            log += System.getProperty("line.separator");

            dataReady = false;
        }
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);

        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);
        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = getLayoutInflater().inflate(R.layout.layout_help_logger,
                null);

        helpDialog.setContentView(view);

        helpDialog.show();
    }

    /**
     * Begin logging data to an external .csv file.
     */
    private void startDataLog() {
        if (logData == false) {
            generation = 0;

            CharSequence text = "Logging Data";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();

            String headers = "Generation" + ",";

            headers += "Timestamp" + ",";

            headers += this.plotAccelXAxisTitle + ",";

            headers += this.plotAccelYAxisTitle + ",";

            headers += this.plotAccelZAxisTitle + ",";

            headers += this.plotSensorFrequencyTitle + ",";

            log = headers;

            log += System.getProperty("line.separator");

            logData = true;
        }
    }

    /**
     * Show a settings dialog.
     */
    private void startIntentSensorSettings() {
        Intent intent = new Intent(LoggerActivity.this,
                FilterConfigActivity.class);

        startActivity(intent);
    }

    private void stopDataLog() {
        if (logData) {
            writeLogToFile();
        }

        if (logData && thread != null) {
            logData = false;

            thread.interrupt();

            thread = null;
        }
    }

    /**
     * Write the logged data out to a persisted file.
     */
    private void writeLogToFile() {
        Calendar c = Calendar.getInstance();
        String filename = "AccelerationExplorer-" + c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR)
                + "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
                + ".csv";

        File dir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "AccelerationExplorer" + File.separator
                + "Logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, filename);

        FileOutputStream fos;
        byte[] data = log.getBytes();
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();

            CharSequence text = "Log Saved";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        } catch (FileNotFoundException e) {
            CharSequence text = e.toString();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        } catch (IOException e) {
            // handle exception
        } finally {
            // Update the MediaStore so we can view the file without rebooting.
            // Note that it appears that the ACTION_MEDIA_MOUNTED approach is
            // now blocked for non-system apps on Android 4.4.
            MediaScannerConnection.scanFile(this, new String[]
                            {file.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(final String path,
                                                    final Uri uri) {

                        }
                    });
        }
    }

}
