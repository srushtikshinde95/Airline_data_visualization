package com.airdata.model;

import com.airdata.util.ThemeConstants;
import java.awt.Color;

/**
 * Enum representing the operational status of a flight.
 * Demonstrates Type Safety, Encapsulated Properties, and Behavior in Enums.
 */
public enum FlightStatus {
    ON_TIME("On Time", ThemeConstants.STATUS_ON_TIME, ThemeConstants.STATUS_ON_TIME_BG),
    DELAYED("Delayed", ThemeConstants.STATUS_DELAYED, ThemeConstants.STATUS_DELAYED_BG),
    CANCELLED("Cancelled", ThemeConstants.STATUS_CANCELLED, ThemeConstants.STATUS_CANCELLED_BG);

    private final String label;
    private final Color foregroundColor;
    private final Color backgroundColor;

    FlightStatus(String label, Color foregroundColor, Color backgroundColor) {
        this.label = label;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    public String getLabel() {
        return label;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Factory method to parse status string safely with fallback.
     */
    public static FlightStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ON_TIME;
        }
        String clean = statusStr.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (FlightStatus status : values()) {
            if (status.name().equalsIgnoreCase(clean) || status.label.equalsIgnoreCase(statusStr.trim())) {
                return status;
            }
        }
        return ON_TIME;
    }

    @Override
    public String toString() {
        return label;
    }
}
