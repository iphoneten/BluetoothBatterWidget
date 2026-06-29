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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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
    private TextView preview2x2DeviceNameView;
    private ImageView preview2x2BatteryImageView;
    private TextView preview2x2StatusView;
    private ImageView previewPhoneBatteryImageView;
    private TextView previewPhoneBatteryPercentView;
    private TextView previewPhoneChargeStatusView;
    private TextView previewStepsCountView;
    private ImageView previewStepsProgressView;
    private TextView previewStepsGoalView;
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

        root.addView(createWidgetPreviewPager(), previewPagerLayoutParams());

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
            updateWidgetMetricsPreview();
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
        updateWidgetMetricsPreview();
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
        updateWidgetMetricsPreview();
        StepCounterHelper.requestStepUpdate(this, this::updateWidgetMetricsPreview);
        MyWidgetProvider.updateAllWidgets(this);
        MyWidgetProvider2x2.updateAllWidgets(this);
        WidgetRefreshScheduler.scheduleIfNeeded(this);
        updateWidgetInfo();
    }

    private HorizontalScrollView createWidgetPreviewPager() {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setFillViewport(false);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        scrollView.addView(row, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        row.addView(createPreviewCard(), new LinearLayout.LayoutParams(dp(160), dp(40)));

        LinearLayout.LayoutParams preview2x2Params = new LinearLayout.LayoutParams(dp(160), dp(160));
        preview2x2Params.leftMargin = dp(16);
        row.addView(createPreview2x2Card(), preview2x2Params);

        return scrollView;
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

    private LinearLayout createPreview2x2Card() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(8), dp(8), dp(8), dp(8));
        root.setBackgroundResource(R.drawable.widget_2x2_background);

        LinearLayout bluetoothRow = new LinearLayout(this);
        bluetoothRow.setOrientation(LinearLayout.HORIZONTAL);
        bluetoothRow.setGravity(Gravity.CENTER_VERTICAL);
        bluetoothRow.setPadding(dp(8), 0, dp(8), 0);
        bluetoothRow.setBackgroundResource(R.drawable.widget_background);
        root.addView(bluetoothRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        ));

        preview2x2BatteryImageView = new ImageView(this);
        bluetoothRow.addView(preview2x2BatteryImageView, new LinearLayout.LayoutParams(dp(38), dp(38)));

        LinearLayout bluetoothTextColumn = new LinearLayout(this);
        bluetoothTextColumn.setOrientation(LinearLayout.VERTICAL);
        bluetoothTextColumn.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bluetoothTextParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        bluetoothTextParams.leftMargin = dp(8);
        bluetoothRow.addView(bluetoothTextColumn, bluetoothTextParams);

        LinearLayout bluetoothTopLine = new LinearLayout(this);
        bluetoothTopLine.setGravity(Gravity.CENTER_VERTICAL);
        bluetoothTopLine.setOrientation(LinearLayout.HORIZONTAL);
        bluetoothTextColumn.addView(bluetoothTopLine, matchWidthLayoutParams());

        preview2x2DeviceNameView = new TextView(this);
        preview2x2DeviceNameView.setSingleLine(true);
        preview2x2DeviceNameView.setIncludeFontPadding(true);
        preview2x2DeviceNameView.setTextColor(0xFF111111);
        preview2x2DeviceNameView.setTextSize(12);
        preview2x2DeviceNameView.setTypeface(null, android.graphics.Typeface.BOLD);
        bluetoothTopLine.addView(preview2x2DeviceNameView, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        preview2x2StatusView = new TextView(this);
        preview2x2StatusView.setSingleLine(true);
        preview2x2StatusView.setIncludeFontPadding(true);
        preview2x2StatusView.setTextColor(0xFF111111);
        preview2x2StatusView.setTextSize(13);
        preview2x2StatusView.setTypeface(null, android.graphics.Typeface.BOLD);
        bluetoothTopLine.addView(preview2x2StatusView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout panelsRow = new LinearLayout(this);
        panelsRow.setGravity(Gravity.CENTER);
        panelsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams panelsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        panelsParams.topMargin = dp(8);
        root.addView(panelsRow, panelsParams);

        panelsRow.addView(createPhoneBatteryPreviewPanel(), new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        ));

        LinearLayout.LayoutParams stepsPanelParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );
        stepsPanelParams.leftMargin = dp(8);
        panelsRow.addView(createStepsPreviewPanel(), stepsPanelParams);

        return root;
    }

    private LinearLayout createPhoneBatteryPreviewPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setBackgroundResource(R.drawable.widget_panel_blue);

        previewPhoneBatteryImageView = new ImageView(this);
        panel.addView(previewPhoneBatteryImageView, new LinearLayout.LayoutParams(dp(44), dp(44)));

        previewPhoneBatteryPercentView = createCenteredPreviewText(16, true);
        LinearLayout.LayoutParams percentParams = matchWidthLayoutParams();
        percentParams.topMargin = dp(4);
        panel.addView(previewPhoneBatteryPercentView, percentParams);

        previewPhoneChargeStatusView = createCenteredPreviewText(11, true);
        panel.addView(previewPhoneChargeStatusView, matchWidthLayoutParams());
        return panel;
    }

    private LinearLayout createStepsPreviewPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setBackgroundResource(R.drawable.widget_panel_green);

        previewStepsCountView = createCenteredPreviewText(18, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            previewStepsCountView.setAutoSizeTextTypeUniformWithConfiguration(
                    10,
                    18,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
            );
        }
        panel.addView(previewStepsCountView, matchWidthLayoutParams());

        previewStepsProgressView = new ImageView(this);
        previewStepsProgressView.setScaleType(ImageView.ScaleType.FIT_XY);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(8)
        );
        progressParams.topMargin = dp(6);
        panel.addView(previewStepsProgressView, progressParams);

        previewStepsGoalView = createCenteredPreviewText(10, false);
        LinearLayout.LayoutParams goalParams = matchWidthLayoutParams();
        goalParams.topMargin = dp(4);
        panel.addView(previewStepsGoalView, goalParams);
        return panel;
    }

    private TextView createCenteredPreviewText(int textSize, boolean bold) {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(true);
        textView.setIncludeFontPadding(true);
        textView.setTextColor(0xFF111111);
        textView.setTextSize(textSize);
        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return textView;
    }

    private void updatePreview(BatteryHelper.BluetoothBatteryStatus status) {
        previewDeviceNameView.setText(status.deviceName);
        previewDeviceNameView.setVisibility(status.connected ? View.VISIBLE : View.GONE);
        previewBatteryTextView.setText(formatBatteryDisplay(status.batteryText));
        previewStatusView.setText(status.statusText);
        preview2x2DeviceNameView.setText(status.deviceName);
        preview2x2DeviceNameView.setVisibility(status.connected ? View.VISIBLE : View.GONE);
        preview2x2StatusView.setText(status.statusText);
        preview2x2StatusView.setVisibility(status.connected ? View.GONE : View.VISIBLE);

        boolean showBatteryProgress = status.connected && status.batteryLevel >= 0;
        previewBatteryImageView.setImageBitmap(WidgetIconRenderer.createBatteryRingIcon(
                this,
                status.batteryLevel,
                showBatteryProgress,
                34,
                3,
                20
        ));
        preview2x2BatteryImageView.setImageBitmap(WidgetIconRenderer.createBatteryRingIcon(
                this,
                status.batteryLevel,
                showBatteryProgress,
                38,
                3,
                22
        ));
    }

    private void updateWidgetMetricsPreview() {
        SystemBatteryHelper.BatteryStatus systemBattery =
                SystemBatteryHelper.getBatteryStatus(this);
        previewPhoneBatteryImageView.setImageBitmap(
                WidgetIconRenderer.createSystemBatteryRingIcon(this, systemBattery.level)
        );
        previewPhoneBatteryPercentView.setText(systemBattery.percentText);
        previewPhoneChargeStatusView.setText(systemBattery.chargeText);

        StepCounterHelper.StepStatus stepStatus = StepCounterHelper.getStepStatus(this);
        previewStepsCountView.setText(stepStatus.stepsText);
        previewStepsProgressView.setImageBitmap(
                WidgetIconRenderer.createStepProgressBar(this, stepStatus.steps, stepStatus.goal)
        );
        previewStepsGoalView.setText(stepStatus.goalText);
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

    private LinearLayout.LayoutParams previewPagerLayoutParams() {
        LinearLayout.LayoutParams params = matchWidthLayoutParams();
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
