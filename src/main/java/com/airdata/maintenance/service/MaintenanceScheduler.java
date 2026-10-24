package com.airdata.maintenance.service;

import com.airdata.maintenance.model.*;

/**
 * Intelligent Maintenance Scheduling Engine.
 * Combines ML failure predictions, anomaly scores, RUL urgency, and component criticality
 * to compute deterministic maintenance priorities and recommended inspection procedures.
 */
public class MaintenanceScheduler {

    public void evaluateAndSchedule(ComponentHealthRecord record) {
        if (record == null) return;

        double risk = record.getFailureRiskScore();
        double anomaly = record.getAnomalyScore();
        double rul = Math.max(0.0, record.getPredictedRulCycles());
        double maxRulBaseline = 150.0;
        double rulUrgency = 1.0 - Math.min(1.0, rul / maxRulBaseline);
        double criticality = record.getComponent().getCriticalityWeight();
        
        int cyclesSinceMaint = record.getLatestReading() != null ? record.getLatestReading().getCyclesSinceLastMaint() : 0;
        double ageFactor = Math.min(1.0, (double) cyclesSinceMaint / 120.0);

        // Composite Priority Score
        double priorityScore = (0.35 * risk) + (0.25 * anomaly) + (0.20 * rulUrgency) 
                             + (0.15 * criticality) + (0.05 * ageFactor);

        // Assign Maintenance Priority
        MaintenancePriority priority;
        if (record.getRiskCategory() == RiskCategory.CRITICAL || priorityScore >= 0.78 || anomaly >= 0.82) {
            priority = MaintenancePriority.URGENT_AOG;
        } else if (record.getRiskCategory() == RiskCategory.HIGH || priorityScore >= 0.58 || rul <= 20.0) {
            priority = MaintenancePriority.HIGH;
        } else if (record.getRiskCategory() == RiskCategory.MODERATE || priorityScore >= 0.38 || rul <= 45.0 || record.isAnomalyDetected()) {
            priority = MaintenancePriority.MEDIUM;
        } else {
            priority = MaintenancePriority.ROUTINE;
        }

        record.setPriority(priority);

        // Formulate Subsystem-Specific Inspection Procedure
        String action = formulateRecommendedAction(record, priority);
        record.setRecommendedAction(action);
    }

    private String formulateRecommendedAction(ComponentHealthRecord record, MaintenancePriority priority) {
        AircraftComponentType comp = record.getComponent();
        String prefix = "[" + priority.getTitle() + " - " + priority.getTimeframe() + "] ";

        switch (comp) {
            case ENGINE_1:
            case ENGINE_2:
                if (priority == MaintenancePriority.URGENT_AOG) {
                    return prefix + "Ground aircraft. Conduct emergency borescope inspection of HPT Stage 1 blades, measure No. 4 bearing vibration dampener play, and perform magnetic chip detector inspection.";
                } else if (priority == MaintenancePriority.HIGH) {
                    return prefix + "Schedule borescope check within 48h. Perform engine ground vibration survey and oil scavenge filter debris analysis.";
                } else if (priority == MaintenancePriority.MEDIUM) {
                    return prefix + "Review EGT trend margins at cruise, test fuel nozzle spray atomization, and verify fan blade lubrication.";
                } else {
                    return prefix + "Standard line check. Perform routine visual inspection of fan cowls and inspect thrust reverser linkages.";
                }

            case HYDRAULICS:
                if (priority == MaintenancePriority.URGENT_AOG) {
                    return prefix + "Ground aircraft. Check EDP (Engine Driven Pump) discharge pressure, inspect primary flight control servo actuators for external fluid leaks, and sample hydraulic fluid.";
                } else if (priority == MaintenancePriority.HIGH) {
                    return prefix + "Inspect return line filter bypass indicator, check reservoir accumulator nitrogen pre-charge, and pressure-test system B seals.";
                } else if (priority == MaintenancePriority.MEDIUM) {
                    return prefix + "Check hydraulic reservoir quantity, inspect PTU (Power Transfer Unit) operation, and check heat exchanger cooling flow.";
                } else {
                    return prefix + "Routine check. Verify hydraulic quantity sight glass and inspect wheel well hydraulic lines.";
                }

            case LANDING_GEAR:
                if (priority == MaintenancePriority.URGENT_AOG) {
                    return prefix + "Immediate tire and multi-disc brake assembly replacement. Inspect main gear torque links and shock strut nitrogen charge pressure.";
                } else if (priority == MaintenancePriority.HIGH) {
                    return prefix + "Inspect brake wear pins on all 4 main gear assemblies. Perform anti-skid transducer operational test and repack wheel bearings.";
                } else if (priority == MaintenancePriority.MEDIUM) {
                    return prefix + "Measure brake pin protrusion, lubricate trunnion bearings, and verify tire tread depths.";
                } else {
                    return prefix + "Standard pre-flight inspection. Check tire inflation pressures and verify gear pin lock security.";
                }

            case APU:
                if (priority == MaintenancePriority.URGENT_AOG) {
                    return prefix + "AOG Warning: Disconnect APU generator bus. Inspect APU combustor liner, check ignition exciter leads, and inspect fuel control unit (FCU).";
                } else if (priority == MaintenancePriority.HIGH) {
                    return prefix + "Inspect APU exhaust duct for thermal stress cracking. Clean oil cooler and test load compressor inlet guide vanes.";
                } else if (priority == MaintenancePriority.MEDIUM) {
                    return prefix + "Inspect APU air intake flap actuator, check generator oil level, and download APU BITE memory logs.";
                } else {
                    return prefix + "Routine line maintenance. Verify APU emergency fire bottle squib continuity and check hour meter.";
                }

            case ELECTRICAL:
                if (priority == MaintenancePriority.URGENT_AOG) {
                    return prefix + "Immediate avionics bay inspection. Test TRU (Transformer Rectifier Unit) 1 & 2 ripple voltage and check main bus tie contactor.";
                } else if (priority == MaintenancePriority.HIGH) {
                    return prefix + "Inspect IDG (Integrated Drive Generator) disconnect mechanism, check main battery capacity, and test standby inverter.";
                } else if (priority == MaintenancePriority.MEDIUM) {
                    return prefix + "Perform circuit breaker panel thermal imaging survey and check cooling fan filtration in forward avionics bay.";
                } else {
                    return prefix + "Standard check. Verify emergency lighting battery pack charge and test cockpit flight deck annunciator lamps.";
                }

            default:
                return prefix + "Perform manufacturer standard maintenance inspection as per AMM Chapter 05.";
        }
    }
}
