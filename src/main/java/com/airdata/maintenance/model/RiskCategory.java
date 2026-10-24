package com.airdata.maintenance.model;

import java.awt.Color;

/**
 * Standardized risk classification for component failure within the prediction window.
 */
public enum RiskCategory {
    LOW("Low Risk", "< 0.30", new Color(16, 185, 129), "#10b981", 
        "Normal operational wear; proceed with standard planned maintenance cycle."),
    
    MODERATE("Moderate Risk", "0.30 - 0.65", new Color(245, 158, 11), "#f59e0b",
        "Elevated sensor degradation; schedule diagnostic check at next turn-around inspection."),
    
    HIGH("High Risk", "0.65 - 0.85", new Color(249, 115, 22), "#f97316",
        "Severe component stress; dispatch inspection and borescope required within 48 hours."),
    
    CRITICAL("Critical Risk", ">= 0.85", new Color(239, 68, 68), "#ef4444",
        "Imminent failure threshold breached; ground aircraft (AOG) and replace component immediately.");

    private final String label;
    private final String thresholdText;
    private final Color color;
    private final String hexColor;
    private final String actionDescription;

    RiskCategory(String label, String thresholdText, Color color, String hexColor, String actionDescription) {
        this.label = label;
        this.thresholdText = thresholdText;
        this.color = color;
        this.hexColor = hexColor;
        this.actionDescription = actionDescription;
    }

    public String getLabel() { return label; }
    public String getThresholdText() { return thresholdText; }
    public Color getColor() { return color; }
    public String getHexColor() { return hexColor; }
    public String getActionDescription() { return actionDescription; }

    public static RiskCategory fromScore(double score) {
        if (score >= 0.85) return CRITICAL;
        if (score >= 0.65) return HIGH;
        if (score >= 0.30) return MODERATE;
        return LOW;
    }
}
