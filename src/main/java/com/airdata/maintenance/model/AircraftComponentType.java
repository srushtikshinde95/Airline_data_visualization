package com.airdata.maintenance.model;

/**
 * Supported critical aircraft subsystems and components.
 */
public enum AircraftComponentType {
    ENGINE_1("Turbofan Engine 1 (Port)", "EGT, Core Vibration, N1/N2 RPM, Oil Subsystem", 1.00),
    ENGINE_2("Turbofan Engine 2 (Starboard)", "EGT, Core Vibration, N1/N2 RPM, Oil Subsystem", 1.00),
    HYDRAULICS("Main Hydraulic System", "High-Pressure Actuators, Pumps & Reservoir Fluid", 0.95),
    LANDING_GEAR("Landing Gear & Brakes", "Brake Assemblies, Strut Pressure & Retraction", 0.90),
    APU("Auxiliary Power Unit", "APU Gas Turbine, Generator Output & Bleed Air", 0.75),
    ELECTRICAL("Avionics & Electrical Bus", "Main Generators, TRU Voltage & Inverters", 0.85);

    private final String displayName;
    private final String monitoredParameters;
    private final double criticalityWeight;

    AircraftComponentType(String displayName, String monitoredParameters, double criticalityWeight) {
        this.displayName = displayName;
        this.monitoredParameters = monitoredParameters;
        this.criticalityWeight = criticalityWeight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMonitoredParameters() {
        return monitoredParameters;
    }

    public double getCriticalityWeight() {
        return criticalityWeight;
    }

    public static AircraftComponentType fromString(String text) {
        if (text == null) return ENGINE_1;
        String clean = text.trim().toUpperCase().replace(" ", "_");
        for (AircraftComponentType type : values()) {
            if (type.name().equals(clean) || type.displayName.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return ENGINE_1;
    }
}
