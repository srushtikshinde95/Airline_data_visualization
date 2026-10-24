package com.airdata.maintenance.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable snapshot of aircraft telemetry and component sensor measurements.
 */
public class SensorReading implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recordId;
    private String aircraftId;
    private String aircraftModel;
    private AircraftComponentType component;
    private String flightId;
    private int cycleNumber;
    private double totalOperatingHours;
    private int cyclesSinceLastMaint;
    
    // Core physical sensor telemetry
    private double egtCelsius;              // Exhaust Gas Temperature (°C)
    private double coreVibrationIps;        // Core Vibration (in/sec RMS)
    private double fanVibrationIps;         // Fan Vibration (in/sec RMS)
    private double oilPressurePsi;          // Engine/System Oil Pressure (PSI)
    private double oilTempCelsius;          // Oil Temperature (°C)
    private double fuelFlowPph;             // Fuel Flow (pounds/hour)
    private double hydraulicPressurePsi;    // Main Hydraulic Loop Pressure (PSI)
    private double hydraulicTempCelsius;    // Hydraulic Fluid Reservoir Temp (°C)
    private double brakeTempCelsius;        // Landing Gear Brake Temperature (°C)
    private double apuEgtCelsius;           // APU Turbine EGT (°C)
    private double bleedPressurePsi;        // Bleed Air Manifold Pressure (PSI)
    private double n1SpeedPct;              // Low Pressure Spool Speed (% RPM)
    private double n2SpeedPct;              // High Pressure Spool Speed (% RPM)
    private double ambientTempCelsius;      // Outside Air Temp (°C)
    private double altitudeFeet;            // Cruise/Flight Altitude (ft)
    
    // Ground truth / verification targets
    private int actualRulCycles;            // True Remaining Useful Life
    private int failureWithin30Cycles;      // 1 if failure occurs in <= 30 cycles
    private int anomalyFlag;                // 1 if sensor suite exhibits anomalous excursion
    private String faultMode;               // Verified mechanical fault mode
    private LocalDateTime timestamp;

    public SensorReading() {
        this.timestamp = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public AircraftComponentType getComponent() { return component; }
    public void setComponent(AircraftComponentType component) { this.component = component; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public int getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(int cycleNumber) { this.cycleNumber = cycleNumber; }

    public double getTotalOperatingHours() { return totalOperatingHours; }
    public void setTotalOperatingHours(double totalOperatingHours) { this.totalOperatingHours = totalOperatingHours; }

    public int getCyclesSinceLastMaint() { return cyclesSinceLastMaint; }
    public void setCyclesSinceLastMaint(int cyclesSinceLastMaint) { this.cyclesSinceLastMaint = cyclesSinceLastMaint; }

    public double getEgtCelsius() { return egtCelsius; }
    public void setEgtCelsius(double egtCelsius) { this.egtCelsius = egtCelsius; }

    public double getCoreVibrationIps() { return coreVibrationIps; }
    public void setCoreVibrationIps(double coreVibrationIps) { this.coreVibrationIps = coreVibrationIps; }

    public double getFanVibrationIps() { return fanVibrationIps; }
    public void setFanVibrationIps(double fanVibrationIps) { this.fanVibrationIps = fanVibrationIps; }

    public double getOilPressurePsi() { return oilPressurePsi; }
    public void setOilPressurePsi(double oilPressurePsi) { this.oilPressurePsi = oilPressurePsi; }

    public double getOilTempCelsius() { return oilTempCelsius; }
    public void setOilTempCelsius(double oilTempCelsius) { this.oilTempCelsius = oilTempCelsius; }

    public double getFuelFlowPph() { return fuelFlowPph; }
    public void setFuelFlowPph(double fuelFlowPph) { this.fuelFlowPph = fuelFlowPph; }

    public double getHydraulicPressurePsi() { return hydraulicPressurePsi; }
    public void setHydraulicPressurePsi(double hydraulicPressurePsi) { this.hydraulicPressurePsi = hydraulicPressurePsi; }

    public double getHydraulicTempCelsius() { return hydraulicTempCelsius; }
    public void setHydraulicTempCelsius(double hydraulicTempCelsius) { this.hydraulicTempCelsius = hydraulicTempCelsius; }

    public double getBrakeTempCelsius() { return brakeTempCelsius; }
    public void setBrakeTempCelsius(double brakeTempCelsius) { this.brakeTempCelsius = brakeTempCelsius; }

    public double getApuEgtCelsius() { return apuEgtCelsius; }
    public void setApuEgtCelsius(double apuEgtCelsius) { this.apuEgtCelsius = apuEgtCelsius; }

    public double getBleedPressurePsi() { return bleedPressurePsi; }
    public void setBleedPressurePsi(double bleedPressurePsi) { this.bleedPressurePsi = bleedPressurePsi; }

    public double getN1SpeedPct() { return n1SpeedPct; }
    public void setN1SpeedPct(double n1SpeedPct) { this.n1SpeedPct = n1SpeedPct; }

    public double getN2SpeedPct() { return n2SpeedPct; }
    public void setN2SpeedPct(double n2SpeedPct) { this.n2SpeedPct = n2SpeedPct; }

    public double getAmbientTempCelsius() { return ambientTempCelsius; }
    public void setAmbientTempCelsius(double ambientTempCelsius) { this.ambientTempCelsius = ambientTempCelsius; }

    public double getAltitudeFeet() { return altitudeFeet; }
    public void setAltitudeFeet(double altitudeFeet) { this.altitudeFeet = altitudeFeet; }

    public int getActualRulCycles() { return actualRulCycles; }
    public void setActualRulCycles(int actualRulCycles) { this.actualRulCycles = actualRulCycles; }

    public int getFailureWithin30Cycles() { return failureWithin30Cycles; }
    public void setFailureWithin30Cycles(int failureWithin30Cycles) { this.failureWithin30Cycles = failureWithin30Cycles; }

    public int getAnomalyFlag() { return anomalyFlag; }
    public void setAnomalyFlag(int anomalyFlag) { this.anomalyFlag = anomalyFlag; }

    public String getFaultMode() { return faultMode; }
    public void setFaultMode(String faultMode) { this.faultMode = faultMode; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
