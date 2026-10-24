package com.airdata.maintenance.service;

import com.airdata.maintenance.model.ComponentHealthRecord;
import com.airdata.maintenance.model.MaintenanceAlert;
import com.airdata.maintenance.model.RiskCategory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing real-time predictive alerts, status triage, and audit trail logs.
 */
public class AlertService {

    private final Map<String, MaintenanceAlert> alerts = new LinkedHashMap<>();
    private int alertSequence = 100;

    public synchronized void evaluateAndGenerateAlerts(List<ComponentHealthRecord> healthRecords) {
        if (healthRecords == null) return;

        for (ComponentHealthRecord record : healthRecords) {
            String alertKey = record.getAircraftId() + "-" + record.getComponent().name();
            
            // Generate alert if Risk is HIGH / CRITICAL or Anomaly detected
            if (record.getRiskCategory() == RiskCategory.CRITICAL) {
                if (!hasActiveAlert(alertKey)) {
                    createAlert(record, MaintenanceAlert.Severity.CRITICAL,
                            "CRITICAL: Immediate Component Failure Precursor",
                            String.format("ML Failure Risk at %.1f%% with estimated RUL of %.0f cycles. Immediate grounding recommended.",
                                    record.getFailureRiskScore() * 100, record.getPredictedRulCycles()));
                }
            } else if (record.getRiskCategory() == RiskCategory.HIGH) {
                if (!hasActiveAlert(alertKey)) {
                    createAlert(record, MaintenanceAlert.Severity.WARNING,
                            "HIGH: Accelerated Sensor Wear Detected",
                            String.format("Failure Risk elevated to %.1f%% (RUL: %.0f cycles). Priority maintenance inspection required within 48h.",
                                    record.getFailureRiskScore() * 100, record.getPredictedRulCycles()));
                }
            } else if (record.isAnomalyDetected()) {
                if (!hasActiveAlert(alertKey)) {
                    createAlert(record, MaintenanceAlert.Severity.WARNING,
                            "ANOMALY: Multivariate Sensor Excursion",
                            String.format("Isolation Forest anomaly score %.3f breached safety threshold. Abnormal telemetry patterns detected.",
                                    record.getAnomalyScore()));
                }
            }
        }
    }

    private boolean hasActiveAlert(String alertKey) {
        for (MaintenanceAlert a : alerts.values()) {
            if ((a.getAircraftId() + "-" + a.getComponent().name()).equals(alertKey) 
                    && a.getStatus() != MaintenanceAlert.Status.RESOLVED) {
                return true;
            }
        }
        return false;
    }

    private void createAlert(ComponentHealthRecord record, MaintenanceAlert.Severity severity, String title, String desc) {
        String alertId = String.format("ALT-%04d", alertSequence++);
        String evidence = String.join(", ", record.getAnomalyContributingSensors());
        if (evidence.isEmpty()) evidence = "Multiple sensor drift indices (EGT / Vibration / Pressure)";

        MaintenanceAlert alert = new MaintenanceAlert(
                alertId, severity, record.getAircraftId(), record.getComponent(),
                title, desc, evidence, record.getRecommendedAction()
        );
        alerts.put(alertId, alert);
    }

    public synchronized boolean acknowledgeAlert(String alertId, String user) {
        MaintenanceAlert a = alerts.get(alertId);
        if (a != null && a.getStatus() == MaintenanceAlert.Status.NEW) {
            a.acknowledge(user);
            return true;
        }
        return false;
    }

    public synchronized boolean resolveAlert(String alertId, String user, String resolutionNotes) {
        MaintenanceAlert a = alerts.get(alertId);
        if (a != null) {
            a.resolve(user, resolutionNotes);
            return true;
        }
        return false;
    }

    public synchronized List<MaintenanceAlert> getAllAlerts() {
        return new ArrayList<>(alerts.values());
    }

    public synchronized List<MaintenanceAlert> getActiveAlerts() {
        List<MaintenanceAlert> list = new ArrayList<>();
        for (MaintenanceAlert a : alerts.values()) {
            if (a.getStatus() != MaintenanceAlert.Status.RESOLVED) {
                list.add(a);
            }
        }
        return list;
    }

    public synchronized int getCriticalAlertCount() {
        int count = 0;
        for (MaintenanceAlert a : alerts.values()) {
            if (a.getSeverity() == MaintenanceAlert.Severity.CRITICAL && a.getStatus() != MaintenanceAlert.Status.RESOLVED) {
                count++;
            }
        }
        return count;
    }
}
