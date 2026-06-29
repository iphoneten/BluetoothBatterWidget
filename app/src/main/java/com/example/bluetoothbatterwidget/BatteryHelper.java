package com.example.bluetoothbatterwidget;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.Set;

public class BatteryHelper {
    static final int UNKNOWN_BATTERY_LEVEL = -1;

    public static BluetoothBatteryStatus getBluetoothBatteryStatus(Context context) {
        if (!hasBluetoothConnectPermission(context)) {
            return BluetoothBatteryStatus.permissionRequired();
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return BluetoothBatteryStatus.bluetoothOff();
        }

        Set<BluetoothDevice> devices;
        try {
            devices = adapter.getBondedDevices();
        } catch (SecurityException ignored) {
            return BluetoothBatteryStatus.permissionRequired();
        }

        boolean audioProfileConnected = isAudioProfileConnected(adapter);
        BluetoothBatteryStatus connectedWithoutBattery = null;
        for (BluetoothDevice device : devices) {
            if (!isAudioDevice(device)) {
                continue;
            }

            Integer batteryLevel = readBatteryLevel(device);
            Boolean connected = readConnectedState(device);
            boolean hasBattery = batteryLevel != null && batteryLevel >= 0;
            boolean isConnected = Boolean.TRUE.equals(connected)
                    || (connected == null && audioProfileConnected && hasBattery);

            if (!isConnected) {
                continue;
            }

            String deviceName = getDeviceName(device);
            if (hasBattery) {
                return BluetoothBatteryStatus.connectedWithBattery(deviceName, batteryLevel);
            }

            if (connectedWithoutBattery == null) {
                connectedWithoutBattery = BluetoothBatteryStatus.connectedWithoutBattery(deviceName);
            }
        }

        if (connectedWithoutBattery != null) {
            return connectedWithoutBattery;
        }

        return BluetoothBatteryStatus.disconnected();
    }

    public static int getBluetoothBatteryLevel(Context context) {
        return getBluetoothBatteryStatus(context).batteryLevel;
    }

    public static String getBluetoothBatteryInfo(Context context) {
        return getBluetoothBatteryStatus(context).statusText;
    }

    public static boolean isBluetoothDeviceConnected(Context context) {
        return getBluetoothBatteryStatus(context).connected;
    }

    static int getBatteryImageResId(int batteryLevel) {
        switch (bucketBatteryLevel(batteryLevel)) {
            case 10:
                return R.drawable.battery_10;
            case 15:
                return R.drawable.battery_15;
            case 20:
                return R.drawable.battery_20;
            case 25:
                return R.drawable.battery_25;
            case 30:
                return R.drawable.battery_30;
            case 35:
                return R.drawable.battery_35;
            case 40:
                return R.drawable.battery_40;
            case 45:
                return R.drawable.battery_45;
            case 50:
                return R.drawable.battery_50;
            case 55:
                return R.drawable.battery_55;
            case 60:
                return R.drawable.battery_60;
            case 65:
                return R.drawable.battery_65;
            case 70:
                return R.drawable.battery_70;
            case 75:
                return R.drawable.battery_75;
            case 80:
                return R.drawable.battery_80;
            case 85:
                return R.drawable.battery_85;
            case 90:
                return R.drawable.battery_90;
            case 95:
                return R.drawable.battery_95;
            case 100:
                return R.drawable.battery_100;
            case 5:
            default:
                return R.drawable.battery_5;
        }
    }

    static int bucketBatteryLevel(int batteryLevel) {
        if (batteryLevel < 0) {
            return UNKNOWN_BATTERY_LEVEL;
        }

        int boundedLevel = Math.min(batteryLevel, 100);
        int roundedLevel = (boundedLevel / 5) * 5;
        return Math.max(roundedLevel, 5);
    }

    private static boolean hasBluetoothConnectPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isAudioDevice(BluetoothDevice device) {
        BluetoothClass bluetoothClass;
        try {
            bluetoothClass = device.getBluetoothClass();
        } catch (SecurityException ignored) {
            return false;
        }

        if (bluetoothClass == null) {
            return false;
        }

        int deviceClass = bluetoothClass.getDeviceClass();
        return deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
                || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE;
    }

    private static boolean isAudioProfileConnected(BluetoothAdapter adapter) {
        try {
            if (adapter.getProfileConnectionState(BluetoothProfile.A2DP)
                    == BluetoothProfile.STATE_CONNECTED) {
                return true;
            }

            if (adapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                    == BluetoothProfile.STATE_CONNECTED) {
                return true;
            }

            if (adapter.getProfileConnectionState(BluetoothProfile.HEARING_AID)
                    == BluetoothProfile.STATE_CONNECTED) {
                return true;
            }

            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && adapter.getProfileConnectionState(BluetoothProfile.LE_AUDIO)
                    == BluetoothProfile.STATE_CONNECTED;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    private static Integer readBatteryLevel(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("getBatteryLevel");
            Object value = method.invoke(device);
            if (value instanceof Integer) {
                return (Integer) value;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static Boolean readConnectedState(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("isConnected");
            Object value = method.invoke(device);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String getDeviceName(BluetoothDevice device) {
        try {
            String name = device.getName();
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
        } catch (SecurityException ignored) {
        }

        return "蓝牙耳机";
    }

    public static final class BluetoothBatteryStatus {
        public final int batteryLevel;
        public final String deviceName;
        public final String statusText;
        public final String batteryText;
        public final boolean connected;
        public final boolean permissionRequired;
        public final long updatedAtMillis;

        private BluetoothBatteryStatus(
                int batteryLevel,
                String deviceName,
                String statusText,
                String batteryText,
                boolean connected,
                boolean permissionRequired,
                long updatedAtMillis
        ) {
            this.batteryLevel = batteryLevel;
            this.deviceName = deviceName;
            this.statusText = statusText;
            this.batteryText = batteryText;
            this.connected = connected;
            this.permissionRequired = permissionRequired;
            this.updatedAtMillis = updatedAtMillis;
        }

        private static BluetoothBatteryStatus permissionRequired() {
            return unavailable("需要蓝牙权限");
        }

        private static BluetoothBatteryStatus bluetoothOff() {
            return unavailable("蓝牙未开启");
        }

        private static BluetoothBatteryStatus disconnected() {
            return unavailable("未连接耳机");
        }

        private static BluetoothBatteryStatus connectedWithoutBattery(String deviceName) {
            return new BluetoothBatteryStatus(
                    UNKNOWN_BATTERY_LEVEL,
                    deviceName,
                    "已连接",
                    "--",
                    true,
                    false,
                    System.currentTimeMillis()
            );
        }

        private static BluetoothBatteryStatus connectedWithBattery(String deviceName, int batteryLevel) {
            return new BluetoothBatteryStatus(
                    batteryLevel,
                    deviceName,
                    "已连接",
                    batteryLevel + "%",
                    true,
                    false,
                    System.currentTimeMillis()
            );
        }

        private static BluetoothBatteryStatus unavailable(String statusText) {
            return new BluetoothBatteryStatus(
                    UNKNOWN_BATTERY_LEVEL,
                    "蓝牙耳机",
                    statusText,
                    "--",
                    false,
                    "需要蓝牙权限".equals(statusText),
                    System.currentTimeMillis()
            );
        }
    }
}
