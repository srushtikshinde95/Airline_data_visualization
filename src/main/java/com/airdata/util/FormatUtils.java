package com.airdata.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formatter utilities for clean numeric, currency, and duration representation.
 */
public final class FormatUtils {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0'%'");

    private FormatUtils() {
        // Utility constructor
    }

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatNumber(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatPercentage(double percent) {
        return PERCENT_FORMAT.format(percent);
    }

    public static String formatDuration(int minutes) {
        int hours = minutes / 60;
        int remainingMins = minutes % 60;
        if (hours == 0) {
            return remainingMins + "m";
        }
        return hours + "h " + remainingMins + "m";
    }
}
