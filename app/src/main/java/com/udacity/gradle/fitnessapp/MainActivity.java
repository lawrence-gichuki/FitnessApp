package com.udacity.gradle.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.txusballesteros.widgets.FitChart;
import com.udacity.gradle.fitnessapp.database.AppDatabase;
import com.udacity.gradle.fitnessapp.database.UserProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    int numberOfStepsWalked;
    TextView stepsLiveCounter;
    TextView distanceWalked;
    TextView caloriesBurnt;
    TextView activeMinutesWalked;
    SensorManager sensorManager;
    float stepsGoal;
    private AppDatabase mDb;
    FitChart fitChart;
    boolean isSensorPresent = false;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViewModel();

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED)
                        .addDataType(DataType.TYPE_MOVE_MINUTES)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
            readSteps();
            readCalories();
            readMinutes();
            readDistance();
        }

        stepsLiveCounter = findViewById(R.id.steps);
        distanceWalked = findViewById(R.id.distance_walked_textview);
        caloriesBurnt = findViewById(R.id.calories_burnt_textview);
        activeMinutesWalked = findViewById(R.id.time_walked_textview);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        isSensorPresent = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null;

        fitChart = findViewById(R.id.fitchart);
        fitChart.setMinValue(0f);
        Log.d(TAG,   stepsGoal + "");
        fitChart.setMaxValue(stepsGoal);


        fitChart.setValue(numberOfStepsWalked);

    }

    private void setupViewModel() {
        final MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<UserProfile>>() {
            @Override
            public void onChanged(@Nullable List<UserProfile> taskEntries) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb = AppDatabase.getInstance(getApplicationContext());
                        final int goal = mDb.userDao().loadGoal();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView goalTextView;
                                goalTextView = findViewById(R.id.steps_goal);
                                stepsGoal = ((float) goal);
                                fitChart.setMaxValue(stepsGoal);
                                String myGoalLabel = getResources().getString(R.string.goal_label);
                                String myStepsLabel = getResources().getString(R.string.steps_label);
                                goalTextView.setText(myGoalLabel + goal + myStepsLabel);
                            }
                        });
                    }
                });
            }
        });
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

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        } else
            if (id == R.id.action_report) {
                Intent intent = new Intent(this, ReportActivity.class);
                startActivity(intent);
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
            stepsLiveCounter.setText(String.valueOf(numberOfStepsWalked));
            fitChart.setMinValue(0f);
            fitChart.setMaxValue(stepsGoal);
            fitChart.setValue(numberOfStepsWalked, false);
            numberOfStepsWalked++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Records step data by requesting a subscription to background step data.
     */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Successfully subscribed!");
                                } else {
                                    Log.d(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private void readSteps() {
        Fitness.getHistoryClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.d(TAG, "Total steps: " + total);

                                stepsLiveCounter.setText((int) total + "");
                                numberOfStepsWalked = (int) total;

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }

    private void readCalories() {
        Fitness.getHistoryClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                float total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                                Log.d(TAG, "Total calories: " + round((total),0));

                                caloriesBurnt.setText(round((total),0) + "");

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the calories total.", e);
                            }
                        });
    }

    private void readMinutes() {
        Fitness.getHistoryClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();
                                Log.d(TAG, "Total Minutes: " + total);

                                activeMinutesWalked.setText(total + "");

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the active minutes total.", e);
                            }
                        });
    }

    private void readDistance() {
        Fitness.getHistoryClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                float total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                                Log.d(TAG, "Total Miles: " + round((total*0.000621371f),2));

                                distanceWalked.setText(round((total*0.000621371f),2) + "");

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the active minutes total.", e);
                            }
                        });
    }

    /**
     * Round to certain number of decimals
     */
    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

}

