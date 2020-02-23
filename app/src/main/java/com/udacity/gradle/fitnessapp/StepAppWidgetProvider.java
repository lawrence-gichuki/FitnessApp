package com.udacity.gradle.fitnessapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class StepAppWidgetProvider extends AppWidgetProvider {

    private PendingIntent service;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent i = new Intent(context, UpdateWidgetService.class);

        if (service == null) {
            service = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, service);
         Log.d("UpdatingWidget: ","onUpdate");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("UpdatingWidget: ","onReceive");

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d("UpdatingWidget: ","onAppWidgetOptionsChanged");

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d("UpdatingWidget: ","onDeleted");

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("UpdatingWidget: ","onEnabled");

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("UpdatingWidget: ","onDisabled");

    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        Log.d("UpdatingWidget: ","onRestored");

    }
}