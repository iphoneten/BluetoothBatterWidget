package com.example.bluetoothbatterwidget;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class StepCounterHelper {
    private static final String PREFS_NAME = "step_counter";
    private static final String KEY_DAY = "day";
    private static final String KEY_BASELINE = "baseline";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_SCHEMA_VERSION = "schema_version";
    private static final int SCHEMA_VERSION = 2;
    private static final long SENSOR_TIMEOUT_MS = 10000L;

    private StepCounterHelper() {
    }

    static StepStatus getStepStatus(Context context) {
        int dailyGoal = StepGoalHelper.getGoal(context);

        if (needsActivityRecognitionPermission(context)) {
            return new StepStatus(0, dailyGoal, "开启权限", formatGoalText(dailyGoal));
        }

        if (!hasStepCounterSensor(context)) {
            return new StepStatus(0, dailyGoal, "无传感器", formatGoalText(dailyGoal));
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = todayKey();
        String day = prefs.getString(KEY_DAY, "");
        int steps = prefs.getInt(KEY_STEPS, -1);
        long updatedAt = prefs.getLong(KEY_UPDATED_AT, 0L);

        if (!today.equals(day)) {
            return new StepStatus(0, dailyGoal, "待刷新", formatGoalText(dailyGoal));
        }

        if (steps < 0 || updatedAt <= 0L) {
            return new StepStatus(0, dailyGoal, "待刷新", formatGoalText(dailyGoal));
        }

        steps = Math.max(0, steps);
        return new StepStatus(
                steps,
                dailyGoal,
                String.format(Locale.CHINA, "%,d 步", steps),
                formatGoalText(dailyGoal)
        );
    }

    static boolean requestStepUpdate(Context context, Runnable onUpdated) {
        if (needsActivityRecognitionPermission(context)) {
            return false;
        }

        Context appContext = context.getApplicationContext();
        SensorManager sensorManager =
                (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            return false;
        }

        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounter == null) {
            return false;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        SensorEventListener listener = new SensorEventListener() {
            private boolean finished;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (finished || event == null || event.values.length == 0) {
                    return;
                }

                finished = true;
                sensorManager.unregisterListener(this);
                saveStepCounterValue(appContext, Math.round(event.values[0]));
                if (onUpdated != null) {
                    onUpdated.run();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        boolean registered = sensorManager.registerListener(
                listener,
                stepCounter,
                SensorManager.SENSOR_DELAY_NORMAL,
                handler
        );
        if (!registered) {
            return false;
        }

        handler.postDelayed(() -> sensorManager.unregisterListener(listener), SENSOR_TIMEOUT_MS);
        return true;
    }

    private static void saveStepCounterValue(Context context, int totalSinceBoot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = todayKey();
        String savedDay = prefs.getString(KEY_DAY, "");
        int baseline = prefs.getInt(KEY_BASELINE, -1);
        int schemaVersion = prefs.getInt(KEY_SCHEMA_VERSION, 0);

        if (schemaVersion != SCHEMA_VERSION
                || !today.equals(savedDay)
                || baseline < 0
                || totalSinceBoot < baseline) {
            baseline = 0;
        }

        int steps = Math.max(0, totalSinceBoot - baseline);
        prefs.edit()
                .putString(KEY_DAY, today)
                .putInt(KEY_BASELINE, baseline)
                .putInt(KEY_STEPS, steps)
                .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                .putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
                .apply();
    }

    private static boolean hasStepCounterSensor(Context context) {
        SensorManager sensorManager =
                (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        return sensorManager != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null;
    }

    private static boolean needsActivityRecognitionPermission(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED;
    }

    private static String todayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    private static String formatGoalText(int goal) {
        return String.format(Locale.CHINA, "%,d", goal);
    }

    static final class StepStatus {
        final int steps;
        final int goal;
        final String stepsText;
        final String goalText;

        StepStatus(int steps, int goal, String stepsText, String goalText) {
            this.steps = steps;
            this.goal = goal;
            this.stepsText = stepsText;
            this.goalText = goalText;
        }
    }
}
