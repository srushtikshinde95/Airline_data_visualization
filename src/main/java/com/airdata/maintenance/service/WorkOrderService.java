package com.airdata.maintenance.service;

import com.airdata.maintenance.model.AircraftComponentType;
import com.airdata.maintenance.model.MaintenancePriority;
import com.airdata.maintenance.model.MaintenanceWorkOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service managing maintenance work orders with persistent JSON file storage.
 */
public class WorkOrderService {

    private final File storageFile;
    private final Map<String, MaintenanceWorkOrder> workOrders = new LinkedHashMap<>();
    private final Gson gson;
    private int orderCounter = 200;

    public WorkOrderService(File storageFile) {
        this.storageFile = storageFile;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadFromStorage();
    }

    public synchronized MaintenanceWorkOrder createWorkOrder(String aircraftId, AircraftComponentType component,
                                                            MaintenancePriority priority, String alertRef,
                                                            String title, String inspection, String technician, String notes) {
        String workOrderId = String.format("WO-%04d", orderCounter++);
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder(
                workOrderId, aircraftId, component, priority, alertRef, title, inspection, technician, notes
        );
        workOrders.put(workOrderId, wo);
        saveToStorage();
        return wo;
    }

    public synchronized boolean updateStatus(String workOrderId, MaintenanceWorkOrder.WorkOrderStatus newStatus, String notes) {
        MaintenanceWorkOrder wo = workOrders.get(workOrderId);
        if (wo != null) {
            wo.setStatus(newStatus);
            if (notes != null && !notes.trim().isEmpty()) {
                wo.setTechnicianNotes((wo.getTechnicianNotes() != null ? wo.getTechnicianNotes() + " | " : "") + notes);
            }
            if (newStatus == MaintenanceWorkOrder.WorkOrderStatus.COMPLETED) {
                wo.setCompletedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            saveToStorage();
            return true;
        }
        return false;
    }

    public synchronized List<MaintenanceWorkOrder> getAllWorkOrders() {
        return new ArrayList<>(workOrders.values());
    }

    public synchronized MaintenanceWorkOrder getWorkOrderById(String id) {
        return workOrders.get(id);
    }

    private void saveToStorage() {
        if (storageFile == null) return;
        try {
            File parent = storageFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(workOrders.values(), writer);
            }
        } catch (Exception e) {
            System.err.println("Failed to persist work orders: " + e.getMessage());
        }
    }

    private void loadFromStorage() {
        if (storageFile != null && storageFile.exists() && storageFile.length() > 5) {
            try (FileReader reader = new FileReader(storageFile)) {
                Type listType = new TypeToken<List<MaintenanceWorkOrder>>() {}.getType();
                List<MaintenanceWorkOrder> list = gson.fromJson(reader, listType);
                if (list != null) {
                    for (MaintenanceWorkOrder wo : list) {
                        workOrders.put(wo.getWorkOrderId(), wo);
                        try {
                            int num = Integer.parseInt(wo.getWorkOrderId().replace("WO-", ""));
                            if (num >= orderCounter) orderCounter = num + 1;
                        } catch (Exception ignored) {}
                    }
                    return;
                }
            } catch (Exception e) {
                System.err.println("Could not parse existing work orders JSON: " + e.getMessage());
            }
        }

        // Seed with sample initial operational work orders if file does not exist
        seedInitialWorkOrders();
    }

    private void seedInitialWorkOrders() {
        createWorkOrder("AC-738-01", AircraftComponentType.ENGINE_1, MaintenancePriority.URGENT_AOG,
                "ALT-0100", "Borescope Inspection of HPT Blades & Bearing 4 Survey",
                "Perform Stage 1 HPT blade inspection and measure bearing vibration.", "Senior Insp. J. Vance",
                "High core vibration (3.1 in/s) detected on last 3 flight sectors.");

        createWorkOrder("AC-320-02", AircraftComponentType.HYDRAULICS, MaintenancePriority.HIGH,
                "ALT-0101", "Hydraulic System B Pump Seal Replacement",
                "Pressure test hydraulic reservoir lines and replace EDP seal.", "Technician R. Patel",
                "Gradual pressure loss from 3010 to 2640 PSI over 20 cycles.");

        createWorkOrder("AC-777-01", AircraftComponentType.LANDING_GEAR, MaintenancePriority.MEDIUM,
                "ALT-0102", "Main Gear Brake Assembly Inspection & Pin Measurement",
                "Inspect wear indicator pins on left and right truck assemblies.", "Lead Mech. S. Tanaka",
                "Elevated temperature trends (+35°C vs baseline) following heavy turnaround.");
    }
}
