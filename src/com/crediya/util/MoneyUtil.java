package com.crediya.util;

import java.util.Locale;

public final class MoneyUtil {
    private MoneyUtil() {}

    public static double round2(double value) { return Math.round(value * 100.0) / 100.0; }

    public static String format(double value) { return String.format(Locale.US, "$%,.2f", value); }
}
