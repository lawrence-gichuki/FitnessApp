package com.udacity.gradle.fitnessapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    int numberOfStepsWalked;
    TextView distanceWalked;
    TextView caloriesBurnt;
    TextView activeMinutesWalked;
    TextView balance;
    float stepsGoal;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

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

        balance = findViewById(R.id.balance);
        balance.setText("Steps Remaining: 500");

        activeMinutesWalked = findViewById(R.id.active_minutes);
        distanceWalked = findViewById(R.id.distance);
        caloriesBurnt = findViewById(R.id.calories);

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
                                goalTextView = findViewById(R.id.goal);
                                stepsGoal = ((float) goal);

                                String myGoalLabel = getResources().getString(R.string.goal_label);
                                String myStepsLabel = getResources().getString(R.string.steps_label);
                                goalTextView.setText(myGoalLabel + " " + goal + " " + myStepsLabel);
                            }
                        });
                    }
                });
            }
        });
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

                                numberOfStepsWalked = (int) total;
                                TextView stepsTextView = findViewById(R.id.steps);
                                String myStepsLabel = getResources().getString(R.string.step_label);
                                stepsTextView.setText(myStepsLabel + " " + numberOfStepsWalked + "");
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

                                String myCaloriesLabel = getResources().getString(R.string.calories_label);
                                String myKcalLabel = getResources().getString(R.string.kcal_label);
                                caloriesBurnt.setText(myCaloriesLabel + " " + round((total),0) + " " + myKcalLabel);

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
                                String myMinutesLabel = getResources().getString(R.string.minutes_label);
                                activeMinutesWalked.setText(myMinutesLabel + " " + total + "");
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

                                String myDistanceLabel = getResources().getString(R.string.distance_label);
                                String myMilesLabel = getResources().getString(R.string.miles_label);
                                distanceWalked.setText(myDistanceLabel + " " + round((total*0.000621371f),2) + " " + myMilesLabel);

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
