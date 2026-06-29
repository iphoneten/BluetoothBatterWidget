package com.example.bluetoothbatterwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class MyWidgetProvider2x2 extends AppWidgetProvider {
    static final String ACTION_REFRESH_WIDGET =
            "com.example.bluetoothbatterwidget.action.REFRESH_WIDGET_2X2";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (ACTION_REFRESH_WIDGET.equals(action)) {
            updateAllWidgets(context);
            return;
        }

        if (SystemBatteryHelper.isPowerAction(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, MyWidgetProvider2x2.class);
            updateAllWidgets(context, manager, manager.getAppWidgetIds(thisWidget));
            WidgetRefreshScheduler.scheduleIfNeeded(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        updateAllWidgets(context);
        WidgetRefreshScheduler.scheduleIfNeeded(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetRefreshScheduler.scheduleIfNeeded(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateAllWidgets(context, manager, appWidgetIds);
        WidgetRefreshScheduler.scheduleIfNeeded(context);
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider2x2.class);
        updateAllWidgets(context, manager, manager.getAppWidgetIds(thisWidget));
    }

    public static void updateAllWidgets(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            return;
        }

        for (int appWidgetId : appWidgetIds) {
            manager.updateAppWidget(appWidgetId, createRemoteViews(context));
        }
    }

    private static RemoteViews createRemoteViews(Context context) {
        RemoteViews views = WidgetRemoteViews.create(
                context,
                R.layout.widget_layout_2x2,
                12,
                38,
                3,
                22
        );
        BatteryHelper.BluetoothBatteryStatus bluetoothStatus =
                BatteryHelper.getBluetoothBatteryStatus(context);
        views.setViewVisibility(
                R.id.connection_status,
                bluetoothStatus.connected ? View.GONE : View.VISIBLE
        );

        SystemBatteryHelper.BatteryStatus systemBattery =
                SystemBatteryHelper.getBatteryStatus(context);
        views.setImageViewBitmap(
                R.id.phone_battery_icon,
                WidgetIconRenderer.createSystemBatteryRingIcon(context, systemBattery.level)
        );
        views.setTextViewText(R.id.phone_battery_percent, systemBattery.percentText);
        views.setTextViewText(R.id.phone_charge_status, systemBattery.chargeText);

        DateTimeHelper.DateTimeStatus dateTime = DateTimeHelper.getCurrent();
        views.setTextViewText(R.id.time_text, dateTime.timeText);
        views.setTextViewText(R.id.week_text, dateTime.weekText);
        views.setTextViewText(R.id.date_text, dateTime.dateText);
        views.setTextViewText(R.id.lunar_text, dateTime.lunarText);
        return views;
    }
}
