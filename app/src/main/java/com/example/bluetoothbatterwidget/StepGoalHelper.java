package com.example.bluetoothbatterwidget;

import android.content.Context;

import java.util.Locale;

final class StepGoalHelper {
    private static final String PREFS_NAME = "step_goal";
    private static final String KEY_GOAL = "goal";
    private static final int DEFAULT_GOAL = 10000;
    private static final int MIN_GOAL = 1000;
    private static final int MAX_GOAL = 100000;

    private StepGoalHelper() {
    }

    static int getGoal(Context context) {
        int goal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_GOAL, DEFAULT_GOAL);
        return clampGoal(goal);
    }

    static void saveGoal(Context context, int goal) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_GOAL, clampGoal(goal))
                .apply();
    }

    static String formatGoal(Context context) {
        return String.format(Locale.CHINA, "%,d", getGoal(context));
    }

    private static int clampGoal(int goal) {
        return Math.max(MIN_GOAL, Math.min(goal, MAX_GOAL));
    }
}
