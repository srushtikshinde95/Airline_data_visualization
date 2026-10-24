package com.airdata.maintenance.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Maintenance Work Order generated from predictive alerts or manual dispatch.
 */
public class MaintenanceWorkOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum WorkOrderStatus {
        OPEN("#3b82f6"),
        SCHEDULED("#8b5cf6"),
        IN_PROGRESS("#f59e0b"),
        COMPLETED("#10b981"),
        CANCELLED("#64748b");

        private final String hexColor;
        WorkOrderStatus(String hexColor) { this.hexColor = hexColor; }
        public String getHexColor() { return hexColor; }
    }

    private String workOrderId;
    private String aircraftId;
    private AircraftComponentType component;
    private MaintenancePriority priority;
    private String alertReferenceId;
    private String title;
    private String recommendedInspection;
    private String assignedTechnician;
    private WorkOrderStatus status;
    private String createdAt;
    private String scheduledDate;
    private String completedAt;
    private String technicianNotes;

    public MaintenanceWorkOrder() {
        this.status = WorkOrderStatus.OPEN;
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }

    public MaintenanceWorkOrder(String workOrderId, String aircraftId, AircraftComponentType component,
                                MaintenancePriority priority, String alertReferenceId, String title,
                                String recommendedInspection, String assignedTechnician, String technicianNotes) {
        this();
        this.workOrderId = workOrderId;
        this.aircraftId = aircraftId;
        this.component = component;
        this.priority = priority;
        this.alertReferenceId = alertReferenceId;
        this.title = title;
        this.recommendedInspection = recommendedInspection;
        this.assignedTechnician = assignedTechnician;
        this.technicianNotes = technicianNotes;
    }

    // --- Getters and Setters ---
    public String getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(String workOrderId) { this.workOrderId = workOrderId; }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public AircraftComponentType getComponent() { return component; }
    public void setComponent(AircraftComponentType component) { this.component = component; }

    public MaintenancePriority getPriority() { return priority; }
    public void setPriority(MaintenancePriority priority) { this.priority = priority; }

    public String getAlertReferenceId() { return alertReferenceId; }
    public void setAlertReferenceId(String alertReferenceId) { this.alertReferenceId = alertReferenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRecommendedInspection() { return recommendedInspection; }
    public void setRecommendedInspection(String recommendedInspection) { this.recommendedInspection = recommendedInspection; }

    public String getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(String assignedTechnician) { this.assignedTechnician = assignedTechnician; }

    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getTechnicianNotes() { return technicianNotes; }
    public void setTechnicianNotes(String technicianNotes) { this.technicianNotes = technicianNotes; }
}
