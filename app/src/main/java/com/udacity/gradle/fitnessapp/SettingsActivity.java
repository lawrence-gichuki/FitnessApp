package com.udacity.gradle.fitnessapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.udacity.gradle.fitnessapp.database.AppDatabase;
import com.udacity.gradle.fitnessapp.database.UserProfile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner mGender;
    Spinner mGoal;
    EditText mWeight;
    EditText mHeight;
    Button mSave;
    ArrayAdapter<CharSequence> adapterSteps;
    ArrayAdapter<CharSequence> adapterGender;
    Toast toast;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mGender = findViewById(R.id.gender);
        mGoal = findViewById(R.id.steps);
        mWeight = findViewById(R.id.weight);
        mHeight = findViewById(R.id.height);
        mSave = findViewById(R.id.save);

        mDb = AppDatabase.getInstance(getApplicationContext());

        mSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSaveButtonClicked();
            }
        });

        //Gender
        mGender.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout

        adapterGender = ArrayAdapter.createFromResource(Objects.requireNonNull(getApplicationContext()),
                R.array.gender, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mGender.setAdapter(adapterGender);


        //Steps Goal
        mGoal.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout

        adapterSteps = ArrayAdapter.createFromResource(Objects.requireNonNull(getApplicationContext()),
                R.array.steps, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSteps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mGoal.setAdapter(adapterSteps);

        retrieveProfile();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onSaveButtonClicked() {

        String gender = mGender.getSelectedItem().toString();
        String stepGoal = mGoal.getSelectedItem().toString();
        int goal = Integer.parseInt(stepGoal);
        int weight = Integer.parseInt(mWeight.getText().toString());
        int height = Integer.parseInt(mHeight.getText().toString());
        Date updatedAt = new Date();

        if ((weight < 5) || (weight > 300) || (height < 50) || (height > 240)) {
            Context context = getApplicationContext();
            CharSequence text = "Please enter your actual weight(KG) and height(CM)";
            int duration = Toast.LENGTH_LONG;

            //Cancel previous toast
            if (toast != null) {
                toast.cancel();
            }

            toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {

            final UserProfile userProfile = new UserProfile(gender, goal, weight, height, updatedAt);


            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.userDao().insertUser(userProfile);
                    finish();
                }
            });
        }

    }

    public void retrieveProfile() {
        final LiveData<List<UserProfile>> userProfile = mDb.userDao().loadAllTasks();
        userProfile.observe(this, new Observer<List<UserProfile>>() {
            @Override
            public void onChanged(List<UserProfile> userProfiles) {
                userProfile.removeObserver(this);
                Log.i("FITNESS","Loading..");
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        final String gender = mDb.userDao().loadGender();
                        final int goal = mDb.userDao().loadGoal();
                        final int weight = mDb.userDao().loadWeight();
                        final int height = mDb.userDao().loadHeight();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mGender.setSelection(adapterGender.getPosition(gender));
                                mGoal.setSelection(adapterSteps.getPosition(goal + ""));
                                mWeight.setText(weight + "");
                                mHeight.setText(height + "");
                            }
                        });
                    }
                });
            }
        });
    }
}