package com.udacity.gradle.fitnessapp;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;

public class UpdateWidgetService extends Service {
    int numberOfStepsWalked;
    String lastUpdate = "0";
    private static final String TAG = UpdateWidgetService.class.getSimpleName();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //This code snippet will read the current steps covered by the user today
        // from GoogleFit and update the lastUpdate variable with the new value.
        //The lastUpdate variable will be used to update the home screen widget
        //The update interval is 60 seconds
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
                                lastUpdate = numberOfStepsWalked + "";
                                Log.d(TAG,lastUpdate);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the step count.", e);
                                lastUpdate = "10";
                            }
                        });

        // Reaches the view on widget and displays the number
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.stepapp_widget);
        view.setTextViewText(R.id.widget_steps, lastUpdate);
        ComponentName theWidget = new ComponentName(this, StepAppWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(theWidget, view);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
