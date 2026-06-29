package com.example.bluetoothbatterwidget;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQUEST_REQUIRED_PERMISSIONS = 1001;

    private TextView statusView;
    private TextView widgetInfoView;
    private TextView previewDeviceNameView;
    private ImageView previewBatteryImageView;
    private TextView previewBatteryTextView;
    private TextView previewStatusView;
    private EditText stepGoalInputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(32));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView titleView = new TextView(this);
        titleView.setText("蓝牙耳机电量小组件");
        titleView.setTextSize(22);
        titleView.setGravity(Gravity.CENTER);
        root.addView(titleView, matchWidthLayoutParams());

        statusView = new TextView(this);
        statusView.setTextSize(18);
        statusView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = matchWidthLayoutParams();
        statusParams.topMargin = dp(20);
        root.addView(statusView, statusParams);

        widgetInfoView = new TextView(this);
        widgetInfoView.setTextSize(15);
        widgetInfoView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams infoParams = matchWidthLayoutParams();
        infoParams.topMargin = dp(8);
        root.addView(widgetInfoView, infoParams);

        root.addView(createPreviewCard(), previewCardLayoutParams());

        Button addWidget2x1Button = new Button(this);
        addWidget2x1Button.setText("添加 2x1 小组件");
        addWidget2x1Button.setAllCaps(false);
        addWidget2x1Button.setOnClickListener(view -> requestPinWidget(
                MyWidgetProvider.class,
                MyWidgetProvider.ACTION_REFRESH_WIDGET,
                3,
                "2x1"
        ));
        LinearLayout.LayoutParams add2x1ButtonParams = matchWidthLayoutParams();
        add2x1ButtonParams.topMargin = dp(28);
        root.addView(addWidget2x1Button, add2x1ButtonParams);

        Button addWidget2x2Button = new Button(this);
        addWidget2x2Button.setText("添加 2x2 小组件");
        addWidget2x2Button.setAllCaps(false);
        addWidget2x2Button.setOnClickListener(view -> requestPinWidget(
                MyWidgetProvider2x2.class,
                MyWidgetProvider2x2.ACTION_REFRESH_WIDGET,
                4,
                "2x2"
        ));
        LinearLayout.LayoutParams add2x2ButtonParams = matchWidthLayoutParams();
        add2x2ButtonParams.topMargin = dp(12);
        root.addView(addWidget2x2Button, add2x2ButtonParams);

        Button refreshButton = new Button(this);
        refreshButton.setText("刷新小组件状态");
        refreshButton.setAllCaps(false);
        refreshButton.setOnClickListener(view -> refreshWidgetAndStatus());
        LinearLayout.LayoutParams refreshButtonParams = matchWidthLayoutParams();
        refreshButtonParams.topMargin = dp(12);
        root.addView(refreshButton, refreshButtonParams);

        root.addView(createStepGoalRow(), stepGoalLayoutParams());

        setContentView(scrollView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String[] missingPermissions = getMissingPermissions();
        if (missingPermissions.length > 0) {
            statusView.setText(getPermissionStatusText());
            updatePreview(BatteryHelper.getBluetoothBatteryStatus(this));
            requestPermissions(missingPermissions, REQUEST_REQUIRED_PERMISSIONS);
            updateWidgetInfo();
            return;
        }

        refreshWidgetAndStatus();
    }

    private LinearLayout createStepGoalRow() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        stepGoalInputView = new EditText(this);
        stepGoalInputView.setSingleLine(true);
        stepGoalInputView.setTextSize(16);
        stepGoalInputView.setInputType(InputType.TYPE_CLASS_NUMBER);
        stepGoalInputView.setText(String.valueOf(StepGoalHelper.getGoal(this)));
        stepGoalInputView.setHint("目标步数");
        row.addView(stepGoalInputView, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        Button saveButton = new Button(this);
        saveButton.setText("保存");
        saveButton.setAllCaps(false);
        saveButton.setOnClickListener(view -> saveStepGoal());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveParams.leftMargin = dp(8);
        row.addView(saveButton, saveParams);

        return row;
    }

    private void saveStepGoal() {
        String input = stepGoalInputView.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入目标步数", Toast.LENGTH_SHORT).show();
            return;
        }

        int goal;
        try {
            goal = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "目标步数格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        StepGoalHelper.saveGoal(this, goal);
        stepGoalInputView.setText(String.valueOf(StepGoalHelper.getGoal(this)));
        MyWidgetProvider2x2.updateAllWidgets(this);
        Toast.makeText(this, "已保存目标步数", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_REQUIRED_PERMISSIONS) {
            return;
        }

        refreshWidgetAndStatus();
    }

    private void requestPinWidget(
            Class<?> providerClass,
            String updateAction,
            int requestCode,
            String sizeLabel
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "当前系统不支持应用内添加小组件", Toast.LENGTH_SHORT).show();
            return;
        }

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        if (!manager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, "当前桌面不支持应用内添加小组件", Toast.LENGTH_SHORT).show();
            return;
        }

        ComponentName provider = new ComponentName(this, providerClass);
        Intent pinnedIntent = new Intent(this, providerClass);
        pinnedIntent.setAction(updateAction);
        PendingIntent successCallback = PendingIntent.getBroadcast(
                this,
                requestCode,
                pinnedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        boolean requested = manager.requestPinAppWidget(provider, null, successCallback);
        Toast.makeText(
                this,
                requested ? "请在桌面确认添加 " + sizeLabel + " 小组件" : "无法请求添加 " + sizeLabel + " 小组件",
                Toast.LENGTH_SHORT
        ).show();
    }

    private boolean needsBluetoothPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean needsActivityRecognitionPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED;
    }

    private String[] getMissingPermissions() {
        List<String> permissions = new ArrayList<>();
        if (needsBluetoothPermission()) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (needsActivityRecognitionPermission()) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        return permissions.toArray(new String[0]);
    }

    private String getPermissionStatusText() {
        if (needsBluetoothPermission() && needsActivityRecognitionPermission()) {
            return "需要蓝牙和运动权限";
        }
        if (needsBluetoothPermission()) {
            return "需要蓝牙权限才能读取耳机电量";
        }
        return "需要运动权限才能读取步数";
    }

    private void refreshWidgetAndStatus() {
        BatteryHelper.BluetoothBatteryStatus status = BatteryHelper.getBluetoothBatteryStatus(this);
        statusView.setText(status.statusText);
        updatePreview(status);
        MyWidgetProvider.updateAllWidgets(this);
        MyWidgetProvider2x2.updateAllWidgets(this);
        WidgetRefreshScheduler.scheduleIfNeeded(this);
        updateWidgetInfo();
    }

    private LinearLayout createPreviewCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(3), dp(8), dp(3));
        card.setBackgroundResource(R.drawable.widget_background);
        card.setMinimumHeight(dp(40));

        ImageView headphoneView = new ImageView(this);
        headphoneView.setImageResource(R.drawable.headphones);
        card.addView(headphoneView, new LinearLayout.LayoutParams(dp(34), dp(34)));
        previewBatteryImageView = headphoneView;

        LinearLayout infoColumn = new LinearLayout(this);
        infoColumn.setOrientation(LinearLayout.VERTICAL);
        infoColumn.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams infoColumnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        infoColumnParams.leftMargin = dp(8);
        card.addView(infoColumn, infoColumnParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        previewDeviceNameView = new TextView(this);
        previewDeviceNameView.setGravity(Gravity.START);
        previewDeviceNameView.setSingleLine(true);
        previewDeviceNameView.setIncludeFontPadding(true);
        previewDeviceNameView.setTextColor(0xFF111111);
        previewDeviceNameView.setTextSize(10);
        previewDeviceNameView.setTypeface(null, android.graphics.Typeface.BOLD);
        topRow.addView(previewDeviceNameView, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        infoColumn.addView(topRow, matchWidthLayoutParams());

        LinearLayout batteryRow = new LinearLayout(this);
        batteryRow.setGravity(Gravity.CENTER_VERTICAL);
        batteryRow.setOrientation(LinearLayout.HORIZONTAL);

        previewBatteryTextView = new TextView(this);
        previewBatteryTextView.setIncludeFontPadding(true);
        previewBatteryTextView.setTextColor(0xFF111111);
        previewBatteryTextView.setTextSize(14);
        previewBatteryTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams batteryTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        batteryRow.addView(previewBatteryTextView, batteryTextParams);

        previewStatusView = new TextView(this);
        previewStatusView.setGravity(Gravity.START);
        previewStatusView.setSingleLine(true);
        previewStatusView.setIncludeFontPadding(true);
        previewStatusView.setTextColor(0xFF000000);
        previewStatusView.setTextSize(10);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        statusParams.leftMargin = dp(6);
        batteryRow.addView(previewStatusView, statusParams);
        infoColumn.addView(batteryRow, matchWidthLayoutParams());

        return card;
    }

    private void updatePreview(BatteryHelper.BluetoothBatteryStatus status) {
        previewDeviceNameView.setText(status.deviceName);
        previewBatteryTextView.setText(formatBatteryDisplay(status.batteryText));
        previewStatusView.setText(status.statusText);

        boolean showBatteryProgress = status.connected && status.batteryLevel >= 0;
        previewBatteryImageView.setImageBitmap(
                WidgetIconRenderer.createBatteryRingIcon(this, status.batteryLevel, showBatteryProgress)
        );
    }

    private static String formatBatteryDisplay(String batteryText) {
        return "电量 " + batteryText;
    }

    private void updateWidgetInfo() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        int count2x1 = manager.getAppWidgetIds(new ComponentName(this, MyWidgetProvider.class)).length;
        int count2x2 = manager.getAppWidgetIds(new ComponentName(this, MyWidgetProvider2x2.class)).length;
        int count = count2x1 + count2x2;
        widgetInfoView.setText(
                count > 0
                        ? "已添加小组件：" + count + " 个（2x1：" + count2x1 + "，2x2：" + count2x2 + "）"
                        : "尚未添加桌面小组件"
        );
    }

    private LinearLayout.LayoutParams matchWidthLayoutParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams previewCardLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(160), dp(40));
        params.topMargin = dp(24);
        return params;
    }

    private LinearLayout.LayoutParams stepGoalLayoutParams() {
        LinearLayout.LayoutParams params = matchWidthLayoutParams();
        params.topMargin = dp(12);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
