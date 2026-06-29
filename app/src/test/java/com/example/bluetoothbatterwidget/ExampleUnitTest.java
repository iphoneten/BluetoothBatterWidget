package com.example.bluetoothbatterwidget;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void bucketBatteryLevel_handlesInvalidLevels() {
        assertEquals(BatteryHelper.UNKNOWN_BATTERY_LEVEL, BatteryHelper.bucketBatteryLevel(-1));
    }

    @Test
    public void bucketBatteryLevel_usesExistingBatteryDrawables() {
        assertEquals(5, BatteryHelper.bucketBatteryLevel(0));
        assertEquals(5, BatteryHelper.bucketBatteryLevel(4));
        assertEquals(5, BatteryHelper.bucketBatteryLevel(5));
        assertEquals(80, BatteryHelper.bucketBatteryLevel(84));
        assertEquals(100, BatteryHelper.bucketBatteryLevel(100));
        assertEquals(100, BatteryHelper.bucketBatteryLevel(120));
    }
}
