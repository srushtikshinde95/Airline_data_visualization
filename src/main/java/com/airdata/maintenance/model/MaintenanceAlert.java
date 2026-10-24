package com.airdata.maintenance.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Operational maintenance alert triggered by ML inference or physical safety threshold breaches.
 */
public class MaintenanceAlert implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Severity {
        INFO("#3b82f6"),
        WARNING("#f59e0b"),
        CRITICAL("#ef4444");

        private final String hexColor;
        Severity(String hexColor) { this.hexColor = hexColor; }
        public String getHexColor() { return hexColor; }
    }

    public enum Status {
        NEW,
        ACKNOWLEDGED,
        IN_PROGRESS,
        RESOLVED
    }

    private String alertId;
    private Severity severity;
    private Status status;
    private String aircraftId;
    private AircraftComponentType component;
    private String title;
    private String description;
    private String supportingEvidence;
    private String recommendedInspection;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String acknowledgedBy;
    private String resolvedBy;
    private List<String> auditTrail;

    public MaintenanceAlert() {
        this.status = Status.NEW;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.auditTrail = new ArrayList<>();
    }

    public MaintenanceAlert(String alertId, Severity severity, String aircraftId, AircraftComponentType component,
                            String title, String description, String supportingEvidence, String recommendedInspection) {
        this();
        this.alertId = alertId;
        this.severity = severity;
        this.aircraftId = aircraftId;
        this.component = component;
        this.title = title;
        this.description = description;
        this.supportingEvidence = supportingEvidence;
        this.recommendedInspection = recommendedInspection;
        this.auditTrail.add(formatTimestamp(this.createdAt) + " - Alert generated automatically by Predictive Maintenance Engine.");
    }

    public void acknowledge(String user) {
        this.status = Status.ACKNOWLEDGED;
        this.acknowledgedBy = user;
        this.updatedAt = LocalDateTime.now();
        this.auditTrail.add(formatTimestamp(this.updatedAt) + " - Alert acknowledged by " + user);
    }

    public void resolve(String user, String resolutionNotes) {
        this.status = Status.RESOLVED;
        this.resolvedBy = user;
        this.updatedAt = LocalDateTime.now();
        this.auditTrail.add(formatTimestamp(this.updatedAt) + " - Alert resolved by " + user + " (" + resolutionNotes + ")");
    }

    private String formatTimestamp(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // --- Getters and Setters ---
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public AircraftComponentType getComponent() { return component; }
    public void setComponent(AircraftComponentType component) { this.component = component; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; }

    public String getRecommendedInspection() { return recommendedInspection; }
    public void setRecommendedInspection(String recommendedInspection) { this.recommendedInspection = recommendedInspection; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public List<String> getAuditTrail() { return auditTrail; }
    public void setAuditTrail(List<String> auditTrail) { this.auditTrail = auditTrail; }
}
