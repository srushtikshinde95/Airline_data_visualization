package com.airdata.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class providing helper methods for parsing and formatting Date/Time.
 * Demonstrates Method Overloading and Static Utility design pattern.
 */
public final class DateUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private DateUtils() {
        // Prevent instantiation
    }

    /**
     * Parse date string in yyyy-MM-dd format.
     */
    public static LocalDate parseDate(String dateStr) throws ValidationException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new ValidationException("Date string cannot be empty");
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format: '" + dateStr + "'. Expected format: YYYY-MM-DD", e);
        }
    }

    /**
     * Parse time string in HH:mm format.
     */
    public static LocalTime parseTime(String timeStr) throws ValidationException {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new ValidationException("Time string cannot be empty");
        }
        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid time format: '" + timeStr + "'. Expected format: HH:mm", e);
        }
    }

    /**
     * Method Overloading: Format LocalDate to display string.
     */
    public static String formatDate(LocalDate date) {
        return (date != null) ? date.format(DISPLAY_DATE_FORMATTER) : "N/A";
    }

    /**
     * Method Overloading: Format LocalTime to standard HH:mm string.
     */
    public static String formatTime(LocalTime time) {
        return (time != null) ? time.format(TIME_FORMATTER) : "N/A";
    }
}
