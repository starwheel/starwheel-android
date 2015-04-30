package net.omplanet.starwheel.util;

import android.graphics.Color;

import net.omplanet.starwheel.R;

public class ColorUtil {

    public static int getColor(int number) {
        switch (number % 7) {
            case 0:
                return R.color.rainbow_0;
            case 1:
                return R.color.rainbow_1;
            case 2:
                return R.color.rainbow_2;
            case 3:
                return R.color.rainbow_3;
            case 4:
                return R.color.rainbow_4;
            case 5:
                return R.color.rainbow_5;
            case 6:
                return R.color.rainbow_6;

            default:
                return 0;
        }
    }

    public static int getColorCircle(int number) {
        switch (number % 7) {
            case 0:
                return R.drawable.circle_0;
            case 1:
                return R.drawable.circle_1;
            case 2:
                return R.drawable.circle_2;
            case 3:
                return R.drawable.circle_3;
            case 4:
                return R.drawable.circle_4;
            case 5:
                return R.drawable.circle_5;
            case 6:
                return R.drawable.circle_6;

            default:
                return 0;
        }
    }

    public static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    public static int getInvertColor(int color) {
        return Color.rgb(255-Color.red(color),
                255-Color.green(color),
                255-Color.blue(color));
    }

    public static float getColorHue(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        return hsv[0];
    }
}
