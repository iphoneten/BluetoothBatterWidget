package com.example.bluetoothbatterwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

final class WidgetRemoteViews {
    private WidgetRemoteViews() {
    }

    static RemoteViews create(
            Context context,
            int layoutResId,
            int openAppRequestCode,
            int iconSizeDp,
            int iconStrokeDp,
            int innerIconSizeDp
    ) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
        BatteryHelper.BluetoothBatteryStatus status = BatteryHelper.getBluetoothBatteryStatus(context);
        PendingIntent clickIntent = createOpenAppPendingIntent(context, openAppRequestCode);

        boolean showBatteryProgress = status.connected && status.batteryLevel >= 0;
        views.setImageViewBitmap(
                R.id.imageView,
                WidgetIconRenderer.createBatteryRingIcon(
                        context,
                        status.batteryLevel,
                        showBatteryProgress,
                        iconSizeDp,
                        iconStrokeDp,
                        innerIconSizeDp
                )
        );
        views.setViewVisibility(R.id.battery_text, View.GONE);

        views.setTextViewText(R.id.device_name, status.deviceName);
        views.setViewVisibility(R.id.device_name, status.connected ? View.VISIBLE : View.GONE);
        views.setTextViewText(R.id.battery_percent, formatBatteryDisplay(status.batteryText));
        views.setTextViewText(R.id.connection_status, status.statusText);
        views.setOnClickPendingIntent(R.id.widget_root, clickIntent);
        views.setOnClickPendingIntent(R.id.imageView, clickIntent);
        views.setOnClickPendingIntent(R.id.battery_text, clickIntent);
        views.setOnClickPendingIntent(R.id.device_name, clickIntent);
        views.setOnClickPendingIntent(R.id.battery_percent, clickIntent);
        views.setOnClickPendingIntent(R.id.connection_status, clickIntent);
        return views;
    }

    private static String formatBatteryDisplay(String batteryText) {
        return "电量 " + batteryText;
    }

    private static PendingIntent createOpenAppPendingIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
