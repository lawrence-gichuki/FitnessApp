package com.udacity.gradle.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.txusballesteros.widgets.FitChart;
import com.txusballesteros.widgets.FitChartValue;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    int steps_walked;
    TextView steps_tv;
    SensorManager sensorManager;
    boolean isSensorPresent = false;
    //public FitChart fitChart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        steps_tv = findViewById(R.id.steps);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            isSensorPresent = true;
        } else {
            isSensorPresent = false;
        }

        steps_walked = 0;

        FitChart fitChart = findViewById(R.id.fitchart);
        fitChart.setMinValue(0f);
        fitChart.setMaxValue(50f);

        fitChart.setValue(steps_walked);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor,SensorManager.SENSOR_DELAY_FASTEST);
            isSensorPresent = true;

        } else
        {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();
            isSensorPresent = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isSensorPresent) {
            //steps_tv.setText(String.valueOf(((int) event.values[0])));
            steps_tv.setText(String.valueOf(steps_walked));


            FitChart fitChart = findViewById(R.id.fitchart);
            fitChart.setMinValue(0f);
            fitChart.setMaxValue(50f);

            fitChart.setValue(steps_walked, false);

            steps_walked++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

