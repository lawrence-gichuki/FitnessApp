package com.udacity.gradle.fitnessapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.udacity.gradle.fitnessapp.database.AppDatabase;
import com.udacity.gradle.fitnessapp.database.UserProfile;

import java.util.Date;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner mGender;
    Spinner mGoal;
    EditText mWeight;
    EditText mHeight;
    Button mSave;
    ArrayAdapter<CharSequence> adapterSteps;
    ArrayAdapter<CharSequence> adapterGender;
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

        UserProfile userProfile = new UserProfile(gender, goal, weight, height, updatedAt);
        mDb.userDao().insertUser(userProfile);

        Log.i("Spinner", gender);
        Log.i("Spinner", goal + "");
        Log.i("Spinner", weight + "");
        Log.i("Spinner", height + "");
        Log.i("Spinner", updatedAt + "");

        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();

        String gender = mDb.userDao().loadGender();
        int goal = mDb.userDao().loadGoal();
        int weight = mDb.userDao().loadWeight();
        int height = mDb.userDao().loadHeight();

        Log.i("Spinner", gender + "");
        Log.i("Spinner", goal + "");
        Log.i("Spinner", weight + "");
        Log.i("Spinner", height + "");

        mGender.setSelection(adapterGender.getPosition(gender));
        mGoal.setSelection(adapterSteps.getPosition(goal + ""));
        mWeight.setText(weight + "");
        mHeight.setText(height + "");

    }
}