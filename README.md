# BluetoothBatterWidget

一个 Android 桌面小组件项目，用于显示蓝牙耳机电量，并提供 2x1、2x2 两种小组件样式。

## 功能

- 2x1 小组件：显示蓝牙耳机名称、连接状态和耳机电量。
- 2x2 小组件：上方显示蓝牙耳机信息，下方显示手机电量、充电状态和步数信息。
- 支持应用内请求添加 2x1 / 2x2 小组件。
- 支持手动刷新小组件状态。
- 步数来自 Android 运动传感器 `TYPE_STEP_COUNTER`。
- 目标步数可在应用页面手动设置。

## 权限

项目会按系统版本请求以下权限：

- `BLUETOOTH_CONNECT`：Android 12+ 读取蓝牙连接设备信息。
- `ACTIVITY_RECOGNITION`：Android 10+ 读取运动传感器步数。
- `SCHEDULE_EXACT_ALARM`：用于小组件电量/充电状态轮询兜底。

## 运行

连接已开启 USB 调试的 Android 真机后执行：

```bash
./run_dev.sh
```

如果需要清除旧安装和桌面小组件缓存：

```bash
./run_dev.sh --fresh-install
```

`--fresh-install` 会卸载旧应用，桌面上已添加的小组件也会被移除，需要重新添加。

## 注意

- `ACTION_BATTERY_CHANGED` 不能在 `AndroidManifest.xml` 中静态注册，项目中通过 `registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED))` 主动读取当前电池状态。
- Redmi / MIUI 可能限制后台广播或闹钟，小组件充电状态更新可能存在延迟。
- 健康 App 中的目标步数通常没有公开 Android API 可直接读取，因此当前使用应用内设置的目标步数。
- `TYPE_STEP_COUNTER` 返回的是设备开机以来累计步数，项目会在本地转换为展示步数。
