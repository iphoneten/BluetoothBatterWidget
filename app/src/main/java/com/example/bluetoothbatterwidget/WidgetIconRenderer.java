package com.example.bluetoothbatterwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

final class WidgetIconRenderer {
    private WidgetIconRenderer() {
    }

    static Bitmap createBatteryRingIcon(Context context, int batteryLevel, boolean showProgress) {
        return createBatteryRingIcon(context, batteryLevel, showProgress, 34, 3, 20);
    }

    static Bitmap createStepProgressBar(Context context, int steps, int goal) {
        int width = dp(context, 72);
        int height = dp(context, 8);
        int radius = dp(context, 4);
        int safeGoal = Math.max(1, goal);
        int safeSteps = Math.max(0, Math.min(steps, safeGoal));
        float progress = safeSteps / (float) safeGoal;

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF bounds = new RectF(0, 0, width, height);

        paint.setColor(0xFFBBD9C2);
        canvas.drawRoundRect(bounds, radius, radius, paint);

        if (progress > 0f) {
            RectF progressBounds = new RectF(0, 0, Math.max(dp(context, 2), width * progress), height);
            paint.setColor(0xFF278B55);
            canvas.drawRoundRect(progressBounds, radius, radius, paint);
        }

        return output;
    }

    static Bitmap createSystemBatteryRingIcon(Context context, int batteryLevel) {
        int size = dp(context, 44);
        int stroke = Math.max(3, dp(context, 4));
        int progress = Math.max(0, Math.min(batteryLevel, 100));

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float inset = stroke / 2f + dp(context, 1);
        RectF ringBounds = new RectF(inset, inset, size - inset, size - inset);

        paint.setColor(0xFFBFD5DA);
        canvas.drawArc(ringBounds, -90, 360, false, paint);
        paint.setColor(0xFF3B8190);
        canvas.drawArc(ringBounds, -90, 360f * progress / 100f, false, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(context, 2));
        paint.setColor(0xFF3B8190);
        paint.setStrokeCap(Paint.Cap.SQUARE);

        float bodyWidth = dp(context, 12);
        float bodyHeight = dp(context, 18);
        float left = (size - bodyWidth) / 2f;
        float top = (size - bodyHeight) / 2f + dp(context, 2);
        RectF batteryBody = new RectF(left, top, left + bodyWidth, top + bodyHeight);
        canvas.drawRoundRect(batteryBody, dp(context, 2), dp(context, 2), paint);

        paint.setStyle(Paint.Style.FILL);
        RectF batteryHead = new RectF(
                left + dp(context, 4),
                top - dp(context, 4),
                left + bodyWidth - dp(context, 4),
                top
        );
        canvas.drawRoundRect(batteryHead, dp(context, 1), dp(context, 1), paint);

        return output;
    }

    static Bitmap createBatteryRingIcon(
            Context context,
            int batteryLevel,
            boolean showProgress,
            int sizeDp,
            int strokeDp,
            int iconSizeDp
    ) {
        int size = dp(context, sizeDp);
        int stroke = Math.max(2, dp(context, strokeDp));
        int iconSize = dp(context, iconSizeDp);
        int progress = Math.max(0, Math.min(batteryLevel, 100));

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float inset = stroke / 2f + dp(context, 1);
        RectF ringBounds = new RectF(inset, inset, size - inset, size - inset);

        paint.setColor(0xFFE5E7EB);
        canvas.drawArc(ringBounds, -90, 360, false, paint);

        if (showProgress) {
            paint.setColor(progressColor(progress));
            canvas.drawArc(ringBounds, -90, 360f * progress / 100f, false, paint);
        }

        Bitmap headphones = BitmapFactory.decodeResource(context.getResources(), R.drawable.headphones);
        if (headphones != null) {
            RectF iconBounds = new RectF(
                    (size - iconSize) / 2f,
                    (size - iconSize) / 2f,
                    (size + iconSize) / 2f,
                    (size + iconSize) / 2f
            );
            Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(headphones, null, iconBounds, imagePaint);
        }

        return output;
    }

    private static int progressColor(int progress) {
        if (progress <= 20) {
            return 0xFFE53935;
        }
        if (progress <= 50) {
            return 0xFFF9A825;
        }
        return 0xFF22A06B;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
