package com.example.bluetoothbatterwidget;

import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BluetoothStateReceiver extends BroadcastReceiver {
    private static final String ACTION_BATTERY_LEVEL_CHANGED =
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    private static final String ACTION_A2DP_CONNECTION_STATE_CHANGED =
            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ACTION_HEADSET_CONNECTION_STATE_CHANGED =
            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ACTION_HEARING_AID_CONNECTION_STATE_CHANGED =
            "android.bluetooth.hearingaid.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ACTION_POWER_CONNECTED = Intent.ACTION_POWER_CONNECTED;
    private static final String ACTION_POWER_DISCONNECTED = Intent.ACTION_POWER_DISCONNECTED;
    private static final String ACTION_CHARGING = BatteryManager.ACTION_CHARGING;
    private static final String ACTION_DISCHARGING = BatteryManager.ACTION_DISCHARGING;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !shouldRefreshWidget(intent.getAction())) {
            return;
        }

        Context appContext = context.getApplicationContext();
        String action = intent.getAction();
        Log.d("BatteryHelper", "收到广播：" + action);

        if (WidgetRefreshScheduler.ACTION_POLL_WIDGETS.equals(action)) {
            updateWidgets(appContext);
            WidgetRefreshScheduler.scheduleIfNeeded(appContext);
            return;
        }

        BroadcastReceiver.PendingResult pendingResult = goAsync();
        Handler handler = new Handler(Looper.getMainLooper());
        updateWidgets(appContext);
        handler.postDelayed(() -> updateWidgets(appContext), 500);
        handler.postDelayed(() -> updateWidgets(appContext), 3000);
        handler.postDelayed(() -> {
            try {
                updateWidgets(appContext);
                WidgetRefreshScheduler.scheduleIfNeeded(appContext);
            } finally {
                pendingResult.finish();
            }
        }, 8000);
    }

    private static void updateWidgets(Context appContext) {
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);

        ComponentName widget2x1 = new ComponentName(appContext, MyWidgetProvider.class);
        MyWidgetProvider.updateAllWidgets(appContext, manager, manager.getAppWidgetIds(widget2x1));

        ComponentName widget2x2 = new ComponentName(appContext, MyWidgetProvider2x2.class);
        MyWidgetProvider2x2.updateAllWidgets(appContext, manager, manager.getAppWidgetIds(widget2x2));
    }

    private static boolean shouldRefreshWidget(String action) {
        return BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)
                || BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)
                || BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)
                || BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)
                || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)
                || ACTION_BATTERY_LEVEL_CHANGED.equals(action)
                || ACTION_A2DP_CONNECTION_STATE_CHANGED.equals(action)
                || ACTION_HEADSET_CONNECTION_STATE_CHANGED.equals(action)
                || ACTION_HEARING_AID_CONNECTION_STATE_CHANGED.equals(action)
                || ACTION_POWER_CONNECTED.equals(action)
                || ACTION_POWER_DISCONNECTED.equals(action)
                || ACTION_CHARGING.equals(action)
                || ACTION_DISCHARGING.equals(action)
                || WidgetRefreshScheduler.ACTION_POLL_WIDGETS.equals(action);
    }
}
