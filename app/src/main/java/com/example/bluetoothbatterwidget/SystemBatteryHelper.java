package com.example.bluetoothbatterwidget;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

final class SystemBatteryHelper {
    private SystemBatteryHelper() {
    }

    static boolean isPowerAction(String action) {
        if (Intent.ACTION_POWER_CONNECTED.equals(action)
                || BatteryManager.ACTION_CHARGING.equals(action)) {
            return true;
        }

        if (Intent.ACTION_POWER_DISCONNECTED.equals(action)
                || BatteryManager.ACTION_DISCHARGING.equals(action)) {
            return true;
        }

        return false;
    }

    static BatteryStatus getBatteryStatus(Context context) {
        BatteryManager batteryManager =
                (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        Intent intent = context.registerReceiver(
                null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );

        if (intent == null && batteryManager == null) {
            return new BatteryStatus(0, "--", "未知");
        }

        int level = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        int status = intent != null
                ? intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                : BatteryManager.BATTERY_STATUS_UNKNOWN;
        int plugged = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;

        int percent = 0;
        if (level >= 0 && scale > 0) {
            percent = Math.round(level * 100f / scale);
        } else if (batteryManager != null) {
            percent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        percent = Math.max(0, Math.min(percent, 100));

        boolean pluggedIn = plugged != 0
                || status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL
                || (batteryManager != null && batteryManager.isCharging());

        String chargeText;
        if (!pluggedIn) {
            chargeText = "未充电";
        } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
            chargeText = "已充满";
        } else {
            chargeText = "充电中";
        }
        return new BatteryStatus(percent, percent + "%", chargeText);
    }

    static final class BatteryStatus {
        final int level;
        final String percentText;
        final String chargeText;

        BatteryStatus(int level, String percentText, String chargeText) {
            this.level = level;
            this.percentText = percentText;
            this.chargeText = chargeText;
        }
    }
}
