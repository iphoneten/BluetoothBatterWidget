package com.example.bluetoothbatterwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

final class WidgetRefreshScheduler {
    static final String ACTION_POLL_WIDGETS =
            "com.example.bluetoothbatterwidget.action.POLL_WIDGETS";

    private static final int REQUEST_CODE = 3001;
    private static final long REFRESH_INTERVAL_MS = 5 * 1000L;

    private WidgetRefreshScheduler() {
    }

    static void scheduleIfNeeded(Context context) {
        Context appContext = context.getApplicationContext();
        if (!hasWidgets(appContext)) {
            cancel(appContext);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        long triggerAtMillis = SystemClock.elapsedRealtime() + REFRESH_INTERVAL_MS;
        PendingIntent pendingIntent = createPendingIntent(appContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }

    static void cancel(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(createPendingIntent(context.getApplicationContext()));
        }
    }

    private static boolean hasWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int count2x1 = manager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class)).length;
        int count2x2 = manager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider2x2.class)).length;
        return count2x1 + count2x2 > 0;
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, BluetoothStateReceiver.class);
        intent.setAction(ACTION_POLL_WIDGETS);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
