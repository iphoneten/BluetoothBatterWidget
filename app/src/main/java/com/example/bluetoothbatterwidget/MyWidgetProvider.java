package com.example.bluetoothbatterwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
    static final String ACTION_REFRESH_WIDGET =
            "com.example.bluetoothbatterwidget.action.REFRESH_WIDGET";

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
            updateAllWidgets(context);
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
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
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
        return WidgetRemoteViews.create(
                context,
                R.layout.widget_layout,
                MyWidgetProvider.class,
                ACTION_REFRESH_WIDGET,
                1,
                2,
                34,
                3,
                20
        );
    }
}
