package com.example.bluetoothbatterwidget;

import android.icu.util.ChineseCalendar;
import android.icu.util.ULocale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class DateTimeHelper {
    private static final String[] WEEK_TEXTS = {
            "周日", "周一", "周二", "周三", "周四", "周五", "周六"
    };
    private static final String[] LUNAR_MONTHS = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    private static final String[] LUNAR_DAYS = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    private DateTimeHelper() {
    }

    static DateTimeStatus getCurrent() {
        Date now = new Date();
        return new DateTimeStatus(
                new SimpleDateFormat("HH:mm", Locale.CHINA).format(now),
                WEEK_TEXTS[java.util.Calendar.getInstance(Locale.CHINA).get(java.util.Calendar.DAY_OF_WEEK) - 1],
                new SimpleDateFormat("M月d日", Locale.CHINA).format(now),
                formatLunarDate(now)
        );
    }

    private static String formatLunarDate(Date date) {
        ChineseCalendar calendar = new ChineseCalendar(new ULocale("zh_CN@calendar=chinese"));
        calendar.setTime(date);

        int month = calendar.get(ChineseCalendar.MONTH);
        int day = calendar.get(ChineseCalendar.DAY_OF_MONTH);
        boolean leapMonth = calendar.get(ChineseCalendar.IS_LEAP_MONTH) == 1;

        String monthText = month >= 0 && month < LUNAR_MONTHS.length
                ? LUNAR_MONTHS[month]
                : "";
        String dayText = day >= 1 && day <= LUNAR_DAYS.length
                ? LUNAR_DAYS[day - 1]
                : "";
        return (leapMonth ? "闰" : "") + monthText + dayText;
    }

    static final class DateTimeStatus {
        final String timeText;
        final String weekText;
        final String dateText;
        final String lunarText;

        DateTimeStatus(String timeText, String weekText, String dateText, String lunarText) {
            this.timeText = timeText;
            this.weekText = weekText;
            this.dateText = dateText;
            this.lunarText = lunarText;
        }
    }
}
