package com.airdata.view.panels;

import com.airdata.controller.AppController;
import com.airdata.maintenance.model.*;
import com.airdata.maintenance.service.MaintenanceCoordinator;
import com.airdata.maintenance.view.SensorTrendChartPanel;
import com.airdata.util.ThemeConstants;
import com.airdata.view.components.ModernButton;
import com.airdata.view.components.StatCard;
import com.airdata.view.components.VectorIcons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Advanced Aircraft Predictive Maintenance and Intelligence Dashboard.
 * Features AI-powered failure prediction, remaining useful life estimation,
 * multivariate anomaly detection, sensor trend forecasting, alert triage, and work order tracking.
 */
public class MaintenancePanel extends JPanel {

    private final AppController controller;
    private final MaintenanceCoordinator coordinator;

    // Header StatCards
    private StatCard aircraftCard;
    private StatCard componentsCard;
    private StatCard highRiskCard;
    private StatCard anomaliesCard;
    private StatCard alertsCard;

    // Tabs
    private JTabbedPane tabbedPane;

    // Tab 1: Fleet Grid
    private JTable fleetTable;
    private DefaultTableModel fleetTableModel;
    private TableRowSorter<DefaultTableModel> fleetSorter;
    private JComboBox<String> acFilterCombo;
    private JComboBox<String> compFilterCombo;
    private JComboBox<String> riskFilterCombo;

    // Tab 2: Component Deep-Dive
    private JComboBox<String> deepDiveAcCombo;
    private JComboBox<AircraftComponentType> deepDiveCompCombo;
    private JPanel sensorTelemetryPanel;
    private JPanel xaiPanel;
    private JTextArea diagnosticText;
    private JLabel deepDiveStatusBadge;

    // Tab 3: Forecasting
    private JComboBox<String> forecastAcCombo;
    private JComboBox<AircraftComponentType> forecastCompCombo;
    private JComboBox<String> forecastSensorCombo;
    private SensorTrendChartPanel trendChart;
    private JLabel forecastSummaryLabel;

    // Tab 4: Alerts
    private JTable alertTable;
    private DefaultTableModel alertTableModel;

    // Tab 5: Work Orders
    private JTable workOrderTable;
    private DefaultTableModel workOrderTableModel;

    // Tab 6: Model Performance
    private JPanel modelPerfPanel;

    public MaintenancePanel(AppController controller) {
        this.controller = controller;
        this.coordinator = MaintenanceCoordinator.getInstance();

        setLayout(new BorderLayout(0, 14));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        initHeader();
        initTabs();
        refreshAllData();
    }

    private void initHeader() {
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Header Title Bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftTitle.setOpaque(false);

        JLabel iconLabel = new JLabel(VectorIcons.createIcon(VectorIcons.IconType.AIRPLANE, 32, ThemeConstants.ACCENT));
        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Aircraft Predictive Maintenance & Intelligence System");
        titleLabel.setFont(ThemeConstants.FONT_HERO);
        titleLabel.setForeground(ThemeConstants.TEXT_DARK);

        JLabel subLabel = new JLabel("Machine Learning Turbofan Diagnostics • Failure Risk Classification • Remaining Useful Life • Anomaly Detection");
        subLabel.setFont(ThemeConstants.FONT_SMALL);
        subLabel.setForeground(ThemeConstants.TEXT_MUTED);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(subLabel);

        leftTitle.add(iconLabel);
        leftTitle.add(textGroup);
        titleBar.add(leftTitle, BorderLayout.WEST);

        // Quick Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        ModernButton retrainBtn = new ModernButton("Retrain ML Models", ModernButton.ButtonStyle.SECONDARY);
        retrainBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.RESET, 14, ThemeConstants.TEXT_DARK));
        retrainBtn.addActionListener(e -> onRetrainModels());

        ModernButton newWoBtn = new ModernButton("Create Work Order", ModernButton.ButtonStyle.PRIMARY);
        newWoBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.ADD, 14, Color.WHITE));
        newWoBtn.addActionListener(e -> onCreateWorkOrderDialog());

        actionPanel.add(retrainBtn);
        actionPanel.add(newWoBtn);
        titleBar.add(actionPanel, BorderLayout.EAST);

        topContainer.add(titleBar, BorderLayout.NORTH);

        // KPI Metric Cards Grid
        JPanel statCardsGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        statCardsGrid.setOpaque(false);

        aircraftCard = new StatCard("Monitored Fleet", "12", "Commercial Aircraft", ThemeConstants.ACCENT, VectorIcons.IconType.AIRPORT);
        componentsCard = new StatCard("Subsystems", "72", "Monitored Components", new Color(99, 102, 241), VectorIcons.IconType.DATASET);
        highRiskCard = new StatCard("High/Critical Risk", "0", "Requires Priority Inspection", new Color(239, 68, 68), VectorIcons.IconType.ALERT);
        anomaliesCard = new StatCard("Sensor Anomalies", "0", "Multivariate Excursions", new Color(245, 158, 11), VectorIcons.IconType.SEARCH);
        alertsCard = new StatCard("Active Alerts", "0", "Open Triage Items", new Color(236, 72, 153), VectorIcons.IconType.CLOCK);

        statCardsGrid.add(aircraftCard);
        statCardsGrid.add(componentsCard);
        statCardsGrid.add(highRiskCard);
        statCardsGrid.add(anomaliesCard);
        statCardsGrid.add(alertsCard);

        topContainer.add(statCardsGrid, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeConstants.FONT_BODY_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Fleet Health & Risk Grid", createFleetGridTab());
        tabbedPane.addTab("Component Deep-Dive & XAI", createComponentDeepDiveTab());
        tabbedPane.addTab("Sensor Trend Forecasting", createForecastingTab());
        tabbedPane.addTab("Maintenance Alert Center", createAlertCenterTab());
        tabbedPane.addTab("Work Order Management", createWorkOrderTab());
        tabbedPane.addTab("ML Model Performance & Evaluation", createModelPerformanceTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: FLEET HEALTH & RISK GRID
    // ==========================================
    private JPanel createFleetGridTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("Aircraft:"));
        acFilterCombo = new JComboBox<>();
        acFilterCombo.addItem("All Aircraft");
        acFilterCombo.addActionListener(e -> applyFleetFilters());
        filterBar.add(acFilterCombo);

        filterBar.add(new JLabel("Component:"));
        compFilterCombo = new JComboBox<>();
        compFilterCombo.addItem("All Components");
        for (AircraftComponentType c : AircraftComponentType.values()) {
            compFilterCombo.addItem(c.name());
        }
        compFilterCombo.addActionListener(e -> applyFleetFilters());
        filterBar.add(compFilterCombo);

        filterBar.add(new JLabel("Risk Category:"));
        riskFilterCombo = new JComboBox<>();
        riskFilterCombo.addItem("All Risk Levels");
        for (RiskCategory r : RiskCategory.values()) {
            riskFilterCombo.addItem(r.name());
        }
        riskFilterCombo.addActionListener(e -> applyFleetFilters());
        filterBar.add(riskFilterCombo);

        ModernButton inspectBtn = new ModernButton("Inspect Selected Component", ModernButton.ButtonStyle.PRIMARY);
        inspectBtn.addActionListener(e -> openSelectedInDeepDive());
        filterBar.add(inspectBtn);

        panel.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {
            "Aircraft ID", "Aircraft Model", "Component", "Failure Risk", "Risk Category",
            "Predicted RUL", "Anomaly Score", "Priority", "Recommended Maintenance Action"
        };
        fleetTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        fleetTable = new JTable(fleetTableModel);
        fleetSorter = new TableRowSorter<>(fleetTableModel);
        fleetTable.setRowSorter(fleetSorter);
        fleetTable.setRowHeight(32);
        fleetTable.setFont(ThemeConstants.FONT_BODY);
        fleetTable.getTableHeader().setFont(ThemeConstants.FONT_BODY_BOLD);
        fleetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Cell Renderers for Badges
        fleetTable.getColumnModel().getColumn(4).setCellRenderer(new RiskBadgeRenderer());
        fleetTable.getColumnModel().getColumn(7).setCellRenderer(new PriorityBadgeRenderer());

        fleetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedInDeepDive();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fleetTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void applyFleetFilters() {
        if (fleetSorter == null) return;
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String ac = (String) acFilterCombo.getSelectedItem();
        if (ac != null && !ac.equals("All Aircraft")) {
            filters.add(RowFilter.regexFilter("^" + ac + "$", 0));
        }

        String comp = (String) compFilterCombo.getSelectedItem();
        if (comp != null && !comp.equals("All Components")) {
            filters.add(RowFilter.regexFilter(comp, 2));
        }

        String risk = (String) riskFilterCombo.getSelectedItem();
        if (risk != null && !risk.equals("All Risk Levels")) {
            filters.add(RowFilter.regexFilter(risk, 4));
        }

        if (filters.isEmpty()) {
            fleetSorter.setRowFilter(null);
        } else {
            fleetSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void openSelectedInDeepDive() {
        int row = fleetTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a component row to inspect.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = fleetTable.convertRowIndexToModel(row);
        String acId = (String) fleetTableModel.getValueAt(modelRow, 0);
        String compStr = (String) fleetTableModel.getValueAt(modelRow, 2);

        deepDiveAcCombo.setSelectedItem(acId);
        for (int i = 0; i < deepDiveCompCombo.getItemCount(); i++) {
            if (deepDiveCompCombo.getItemAt(i).getDisplayName().equals(compStr) ||
                deepDiveCompCombo.getItemAt(i).name().equals(compStr)) {
                deepDiveCompCombo.setSelectedIndex(i);
                break;
            }
        }

        tabbedPane.setSelectedIndex(1); // Switch to Deep-Dive Tab
    }

    // ==========================================
    // TAB 2: COMPONENT DEEP-DIVE & XAI
    // ==========================================
    private JPanel createComponentDeepDiveTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Selector Bar
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        selectorBar.setOpaque(false);

        selectorBar.add(new JLabel("Aircraft:"));
        deepDiveAcCombo = new JComboBox<>();
        deepDiveAcCombo.addActionListener(e -> updateDeepDiveView());
        selectorBar.add(deepDiveAcCombo);

        selectorBar.add(new JLabel("Subsystem / Component:"));
        deepDiveCompCombo = new JComboBox<>(AircraftComponentType.values());
        deepDiveCompCombo.addActionListener(e -> updateDeepDiveView());
        selectorBar.add(deepDiveCompCombo);

        deepDiveStatusBadge = new JLabel("STATUS: NORMAL");
        deepDiveStatusBadge.setFont(ThemeConstants.FONT_BODY_BOLD);
        selectorBar.add(Box.createHorizontalStrut(20));
        selectorBar.add(deepDiveStatusBadge);

        ModernButton woBtn = new ModernButton("Dispatch Work Order", ModernButton.ButtonStyle.PRIMARY);
        woBtn.addActionListener(e -> onCreateWorkOrderFromDeepDive());
        selectorBar.add(Box.createHorizontalStrut(15));
        selectorBar.add(woBtn);

        panel.add(selectorBar, BorderLayout.NORTH);

        // Split Pane (Left: Live Telemetry Gauges, Right: XAI & Diagnostics)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.48);
        splitPane.setBorder(null);

        // Left Panel: Sensor Telemetry Grid
        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setBackground(Color.WHITE);
        leftContainer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR), "Real-Time Sensor Telemetry"));

        sensorTelemetryPanel = new JPanel(new GridLayout(6, 2, 10, 8));
        sensorTelemetryPanel.setBackground(Color.WHITE);
        sensorTelemetryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftContainer.add(new JScrollPane(sensorTelemetryPanel), BorderLayout.CENTER);

        // Right Panel: XAI & Diagnostics
        JPanel rightContainer = new JPanel(new BorderLayout(0, 10));
        rightContainer.setBackground(Color.WHITE);
        rightContainer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR), "Explainable AI (XAI) & Diagnostic Attribution"));

        xaiPanel = new JPanel();
        xaiPanel.setLayout(new BoxLayout(xaiPanel, BoxLayout.Y_AXIS));
        xaiPanel.setBackground(Color.WHITE);
        xaiPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        diagnosticText = new JTextArea(5, 30);
        diagnosticText.setEditable(false);
        diagnosticText.setFont(ThemeConstants.FONT_BODY);
        diagnosticText.setLineWrap(true);
        diagnosticText.setWrapStyleWord(true);
        diagnosticText.setBackground(new Color(248, 250, 252));
        diagnosticText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JPanel diagnosticWrapper = new JPanel(new BorderLayout());
        diagnosticWrapper.setOpaque(false);
        JLabel diagHeader = new JLabel("Aviation Maintenance Specialist Diagnostic Narrative:");
        diagHeader.setFont(ThemeConstants.FONT_BODY_BOLD);
        diagnosticWrapper.add(diagHeader, BorderLayout.NORTH);
        diagnosticWrapper.add(diagnosticText, BorderLayout.CENTER);

        rightContainer.add(new JScrollPane(xaiPanel), BorderLayout.CENTER);
        rightContainer.add(diagnosticWrapper, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftContainer);
        splitPane.setRightComponent(rightContainer);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateDeepDiveView() {
        String acId = (String) deepDiveAcCombo.getSelectedItem();
        AircraftComponentType comp = (AircraftComponentType) deepDiveCompCombo.getSelectedItem();
        if (acId == null || comp == null) return;

        ComponentHealthRecord record = coordinator.getComponentHealth(acId, comp);
        if (record == null) return;

        // Update Badge
        deepDiveStatusBadge.setText(String.format("STATUS: %s (Risk: %.1f%% | RUL: %.0f cycles)",
                record.getRiskCategory().getLabel().toUpperCase(), record.getFailureRiskScore() * 100, record.getPredictedRulCycles()));
        deepDiveStatusBadge.setForeground(record.getRiskCategory().getColor());

        // Update Sensor Telemetry Grid
        sensorTelemetryPanel.removeAll();
        SensorReading r = record.getLatestReading();
        if (r != null) {
            addSensorMetricBox(sensorTelemetryPanel, "Exhaust Gas Temp (EGT)", String.format("%.1f °C", r.getEgtCelsius()), "Nominal: 650-800 °C", r.getEgtCelsius() > 880);
            addSensorMetricBox(sensorTelemetryPanel, "Core Vibration (RMS)", String.format("%.2f in/s", r.getCoreVibrationIps()), "Nominal: < 1.8 in/s", r.getCoreVibrationIps() > 2.5);
            addSensorMetricBox(sensorTelemetryPanel, "Fan Vibration (RMS)", String.format("%.2f in/s", r.getFanVibrationIps()), "Nominal: < 1.5 in/s", r.getFanVibrationIps() > 2.0);
            addSensorMetricBox(sensorTelemetryPanel, "Engine Oil Pressure", String.format("%.1f PSI", r.getOilPressurePsi()), "Nominal: 45-65 PSI", r.getOilPressurePsi() < 38);
            addSensorMetricBox(sensorTelemetryPanel, "Oil Temperature", String.format("%.1f °C", r.getOilTempCelsius()), "Nominal: 90-120 °C", r.getOilTempCelsius() > 135);
            addSensorMetricBox(sensorTelemetryPanel, "Fuel Flow Rate", String.format("%.0f pph", r.getFuelFlowPph()), "Nominal: 1800-2400 pph", r.getFuelFlowPph() > 2700);
            addSensorMetricBox(sensorTelemetryPanel, "Hydraulic Pressure", String.format("%.0f PSI", r.getHydraulicPressurePsi()), "Nominal: 2950-3050 PSI", r.getHydraulicPressurePsi() < 2700);
            addSensorMetricBox(sensorTelemetryPanel, "Hydraulic Fluid Temp", String.format("%.1f °C", r.getHydraulicTempCelsius()), "Nominal: 45-75 °C", r.getHydraulicTempCelsius() > 90);
            addSensorMetricBox(sensorTelemetryPanel, "Brake Assembly Temp", String.format("%.1f °C", r.getBrakeTempCelsius()), "Nominal: 140-300 °C", r.getBrakeTempCelsius() > 380);
            addSensorMetricBox(sensorTelemetryPanel, "APU Exhaust Gas Temp", String.format("%.1f °C", r.getApuEgtCelsius()), "Nominal: 550-680 °C", r.getApuEgtCelsius() > 740);
            addSensorMetricBox(sensorTelemetryPanel, "Bleed Air Pressure", String.format("%.1f PSI", r.getBleedPressurePsi()), "Nominal: 38-45 PSI", r.getBleedPressurePsi() < 35);
            addSensorMetricBox(sensorTelemetryPanel, "Cycles Since Maint.", String.format("%d cycles", r.getCyclesSinceLastMaint()), "Target: < 100 cycles", r.getCyclesSinceLastMaint() > 120);
        }
        sensorTelemetryPanel.revalidate();
        sensorTelemetryPanel.repaint();

        // Update XAI Feature Contribution List
        xaiPanel.removeAll();
        for (FeatureContribution fc : record.getFeatureContributions()) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(fc.isDeviating() ? new Color(254, 242, 242) : new Color(248, 250, 252));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));

            JLabel nameLbl = new JLabel(fc.getDisplayName());
            nameLbl.setFont(ThemeConstants.FONT_BODY_BOLD);
            nameLbl.setForeground(fc.isDeviating() ? new Color(220, 38, 38) : ThemeConstants.TEXT_DARK);

            String sign = fc.getDeviationPercentage() >= 0 ? "+" : "";
            JLabel valLbl = new JLabel(String.format("%.2f %s (%s%.1f%%, %.1fσ) | Importance: %.1f%%",
                    fc.getCurrentValue(), fc.getUnit(), sign, fc.getDeviationPercentage(), fc.getZScore(), fc.getImportanceWeight() * 100));
            valLbl.setFont(ThemeConstants.FONT_SMALL);
            valLbl.setForeground(fc.isDeviating() ? new Color(185, 28, 28) : ThemeConstants.TEXT_MUTED);

            card.add(nameLbl, BorderLayout.WEST);
            card.add(valLbl, BorderLayout.EAST);
            xaiPanel.add(card);
        }
        xaiPanel.revalidate();
        xaiPanel.repaint();

        // Diagnostic narrative
        diagnosticText.setText(record.getDiagnosticExplanation() + "\n\nAction Plan:\n" + record.getRecommendedAction());
    }

    private void addSensorMetricBox(JPanel container, String title, String value, String nominal, boolean isAlert) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(isAlert ? new Color(254, 242, 242) : new Color(248, 250, 252));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isAlert ? new Color(239, 68, 68) : ThemeConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
        tLbl.setForeground(ThemeConstants.TEXT_MUTED);

        JLabel vLbl = new JLabel(value);
        vLbl.setFont(ThemeConstants.FONT_SUBTITLE);
        vLbl.setForeground(isAlert ? new Color(220, 38, 38) : ThemeConstants.TEXT_DARK);

        JLabel nLbl = new JLabel(nominal);
        nLbl.setFont(ThemeConstants.FONT_SMALL);
        nLbl.setForeground(isAlert ? new Color(185, 28, 28) : ThemeConstants.TEXT_MUTED);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(tLbl);
        p.add(vLbl);
        p.add(nLbl);

        box.add(p, BorderLayout.CENTER);
        container.add(box);
    }

    private void onCreateWorkOrderFromDeepDive() {
        String acId = (String) deepDiveAcCombo.getSelectedItem();
        AircraftComponentType comp = (AircraftComponentType) deepDiveCompCombo.getSelectedItem();
        if (acId == null || comp == null) return;

        ComponentHealthRecord record = coordinator.getComponentHealth(acId, comp);
        if (record == null) return;

        coordinator.getWorkOrderService().createWorkOrder(
                acId, comp, record.getPriority(), "MANUAL-DISPATCH",
                "Inspection for " + comp.getDisplayName(),
                record.getRecommendedAction(),
                "Assigned On-Duty Lead",
                "Generated from Component Deep-Dive Inspection."
        );
        refreshAllData();
        tabbedPane.setSelectedIndex(4); // Switch to Work Orders
        JOptionPane.showMessageDialog(this, "Work Order successfully created for " + acId + " (" + comp.getDisplayName() + ")!",
                "Work Order Created", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    // TAB 3: SENSOR TREND FORECASTING
    // ==========================================
    private JPanel createForecastingTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Selector Bar
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        selectorBar.setOpaque(false);

        selectorBar.add(new JLabel("Aircraft:"));
        forecastAcCombo = new JComboBox<>();
        forecastAcCombo.addActionListener(e -> updateForecastingView());
        selectorBar.add(forecastAcCombo);

        selectorBar.add(new JLabel("Component:"));
        forecastCompCombo = new JComboBox<>(AircraftComponentType.values());
        forecastCompCombo.addActionListener(e -> updateForecastingView());
        selectorBar.add(forecastCompCombo);

        selectorBar.add(new JLabel("Sensor Channel:"));
        forecastSensorCombo = new JComboBox<>(new String[]{
            "egt_celsius", "core_vibration_ips", "fan_vibration_ips", "oil_pressure_psi",
            "hydraulic_pressure_psi", "brake_temp_celsius", "apu_egt_celsius"
        });
        forecastSensorCombo.addActionListener(e -> updateForecastingView());
        selectorBar.add(forecastSensorCombo);

        panel.add(selectorBar, BorderLayout.NORTH);

        trendChart = new SensorTrendChartPanel();
        panel.add(trendChart, BorderLayout.CENTER);

        forecastSummaryLabel = new JLabel("Holt-Linear Time Series Forecaster: 15-cycle projection with 90% confidence envelope.");
        forecastSummaryLabel.setFont(ThemeConstants.FONT_SMALL);
        forecastSummaryLabel.setForeground(ThemeConstants.TEXT_MUTED);
        forecastSummaryLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(forecastSummaryLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateForecastingView() {
        String acId = (String) forecastAcCombo.getSelectedItem();
        AircraftComponentType comp = (AircraftComponentType) forecastCompCombo.getSelectedItem();
        String sensor = (String) forecastSensorCombo.getSelectedItem();
        if (acId == null || comp == null || sensor == null) return;

        TrendForecast tf = coordinator.forecastSensorTrend(acId, comp, sensor, 15);
        trendChart.setForecast(tf);

        if (tf.isThresholdBreached()) {
            forecastSummaryLabel.setText(String.format("CRITICAL WARNING: Forecast projects %s will breach threshold (%.1f %s) in ~%d cycles!",
                    tf.getSensorDisplayName(), tf.getCriticalThreshold(), tf.getUnit(), tf.getEstimatedCyclesToBreach()));
            forecastSummaryLabel.setForeground(new Color(220, 38, 38));
        } else {
            forecastSummaryLabel.setText(String.format("STABLE: %s trend remains within certified limits over the 15-cycle projection horizon.",
                    tf.getSensorDisplayName()));
            forecastSummaryLabel.setForeground(new Color(16, 185, 129));
        }
    }

    // ==========================================
    // TAB 4: ALERT CENTER
    // ==========================================
    private JPanel createAlertCenterTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolBar.setOpaque(false);

        ModernButton ackBtn = new ModernButton("Acknowledge Alert", ModernButton.ButtonStyle.PRIMARY);
        ackBtn.addActionListener(e -> onAcknowledgeAlert());
        toolBar.add(ackBtn);

        ModernButton resBtn = new ModernButton("Resolve Alert", ModernButton.ButtonStyle.SECONDARY);
        resBtn.addActionListener(e -> onResolveAlert());
        toolBar.add(resBtn);

        ModernButton trailBtn = new ModernButton("View Audit Trail", ModernButton.ButtonStyle.SECONDARY);
        trailBtn.addActionListener(e -> onViewAuditTrail());
        toolBar.add(trailBtn);

        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Alert ID", "Severity", "Aircraft ID", "Subsystem", "Title", "Supporting Sensor Evidence", "Status", "Timestamp"};
        alertTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        alertTable = new JTable(alertTableModel);
        alertTable.setRowHeight(30);
        alertTable.setFont(ThemeConstants.FONT_BODY);
        alertTable.getTableHeader().setFont(ThemeConstants.FONT_BODY_BOLD);
        alertTable.getColumnModel().getColumn(1).setCellRenderer(new SeverityRenderer());

        panel.add(new JScrollPane(alertTable), BorderLayout.CENTER);
        return panel;
    }

    private void onAcknowledgeAlert() {
        int row = alertTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an alert to acknowledge.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String alertId = (String) alertTableModel.getValueAt(row, 0);
        coordinator.getAlertService().acknowledgeAlert(alertId, "Station Supervisor");
        refreshAllData();
    }

    private void onResolveAlert() {
        int row = alertTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an alert to resolve.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String alertId = (String) alertTableModel.getValueAt(row, 0);
        String notes = JOptionPane.showInputDialog(this, "Enter resolution inspection notes:", "Resolve Maintenance Alert", JOptionPane.PLAIN_MESSAGE);
        if (notes != null && !notes.trim().isEmpty()) {
            coordinator.getAlertService().resolveAlert(alertId, "Certified Line Inspector", notes.trim());
            refreshAllData();
        }
    }

    private void onViewAuditTrail() {
        int row = alertTable.getSelectedRow();
        if (row < 0) return;
        String alertId = (String) alertTableModel.getValueAt(row, 0);
        for (MaintenanceAlert a : coordinator.getAlertService().getAllAlerts()) {
            if (a.getAlertId().equals(alertId)) {
                StringBuilder sb = new StringBuilder();
                for (String entry : a.getAuditTrail()) sb.append("• ").append(entry).append("\n");
                JTextArea area = new JTextArea(sb.toString());
                area.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Audit Trail: " + alertId, JOptionPane.INFORMATION_MESSAGE);
                break;
            }
        }
    }

    // ==========================================
    // TAB 5: WORK ORDERS
    // ==========================================
    private JPanel createWorkOrderTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolBar.setOpaque(false);

        ModernButton newBtn = new ModernButton("New Work Order", ModernButton.ButtonStyle.PRIMARY);
        newBtn.addActionListener(e -> onCreateWorkOrderDialog());
        toolBar.add(newBtn);

        ModernButton progressBtn = new ModernButton("Set In-Progress", ModernButton.ButtonStyle.SECONDARY);
        progressBtn.addActionListener(e -> updateSelectedWorkOrderStatus(MaintenanceWorkOrder.WorkOrderStatus.IN_PROGRESS));
        toolBar.add(progressBtn);

        ModernButton completeBtn = new ModernButton("Complete Work Order", ModernButton.ButtonStyle.SECONDARY);
        completeBtn.addActionListener(e -> updateSelectedWorkOrderStatus(MaintenanceWorkOrder.WorkOrderStatus.COMPLETED));
        toolBar.add(completeBtn);

        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"WO Number", "Aircraft ID", "Subsystem", "Priority", "Status", "Assigned Lead", "Created Date", "Task Description & Notes"};
        workOrderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        workOrderTable = new JTable(workOrderTableModel);
        workOrderTable.setRowHeight(30);
        workOrderTable.setFont(ThemeConstants.FONT_BODY);
        workOrderTable.getTableHeader().setFont(ThemeConstants.FONT_BODY_BOLD);
        workOrderTable.getColumnModel().getColumn(3).setCellRenderer(new PriorityBadgeRenderer());

        panel.add(new JScrollPane(workOrderTable), BorderLayout.CENTER);
        return panel;
    }

    private void onCreateWorkOrderDialog() {
        JTextField acField = new JTextField("AC-738-01", 10);
        JComboBox<AircraftComponentType> compBox = new JComboBox<>(AircraftComponentType.values());
        JComboBox<MaintenancePriority> prioBox = new JComboBox<>(MaintenancePriority.values());
        JTextField titleField = new JTextField("Borescope Inspection of Turbofan High Pressure Turbine", 25);
        JTextField actionField = new JTextField("Measure blade tip clearances and examine stage 1 nozzles.", 25);
        JTextField techField = new JTextField("Lead Technician Vance", 15);
        JTextArea notesArea = new JTextArea("Initiated via predictive maintenance risk trigger.", 3, 25);

        JPanel form = new JPanel(new GridLayout(7, 2, 6, 8));
        form.add(new JLabel("Aircraft ID:")); form.add(acField);
        form.add(new JLabel("Component:")); form.add(compBox);
        form.add(new JLabel("Priority:")); form.add(prioBox);
        form.add(new JLabel("Title:")); form.add(titleField);
        form.add(new JLabel("Inspection Task:")); form.add(actionField);
        form.add(new JLabel("Assigned Tech:")); form.add(techField);
        form.add(new JLabel("Notes:")); form.add(new JScrollPane(notesArea));

        int res = JOptionPane.showConfirmDialog(this, form, "Create Maintenance Work Order", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            coordinator.getWorkOrderService().createWorkOrder(
                    acField.getText().trim(), (AircraftComponentType) compBox.getSelectedItem(),
                    (MaintenancePriority) prioBox.getSelectedItem(), "MANUAL",
                    titleField.getText().trim(), actionField.getText().trim(),
                    techField.getText().trim(), notesArea.getText().trim()
            );
            refreshAllData();
        }
    }

    private void updateSelectedWorkOrderStatus(MaintenanceWorkOrder.WorkOrderStatus status) {
        int row = workOrderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a work order row first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String woId = (String) workOrderTableModel.getValueAt(row, 0);
        String notes = JOptionPane.showInputDialog(this, "Enter update remarks:", "Status Update", JOptionPane.PLAIN_MESSAGE);
        coordinator.getWorkOrderService().updateStatus(woId, status, notes != null ? notes : "");
        refreshAllData();
    }

    // ==========================================
    // TAB 6: MODEL PERFORMANCE & EVALUATION
    // ==========================================
    private JPanel createModelPerformanceTab() {
        modelPerfPanel = new JPanel();
        modelPerfPanel.setLayout(new BoxLayout(modelPerfPanel, BoxLayout.Y_AXIS));
        modelPerfPanel.setBackground(Color.WHITE);
        modelPerfPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(modelPerfPanel);
        scroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateModelPerformanceView() {
        if (modelPerfPanel == null) return;
        modelPerfPanel.removeAll();

        ModelEvaluationReport rep = coordinator.getEvaluationReport();
        if (rep == null) return;

        // Title
        JLabel title = new JLabel("Machine Learning Evaluation Report & Model Diagnostics");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_DARK);
        modelPerfPanel.add(title);
        modelPerfPanel.add(Box.createVerticalStrut(4));

        JLabel meta = new JLabel(String.format("Trained on: %s | Training Records: %d | Test Records: %d | Evaluated: %s",
                rep.getTrainingDatasetName(), rep.getTrainingSampleCount(), rep.getTestSampleCount(), rep.getLastTrainedTimestamp()));
        meta.setFont(ThemeConstants.FONT_SMALL);
        meta.setForeground(ThemeConstants.TEXT_MUTED);
        modelPerfPanel.add(meta);
        modelPerfPanel.add(Box.createVerticalStrut(16));

        // 3 Cards: Classification, Regression, Anomaly
        JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        metricsGrid.setOpaque(false);

        // Classification Card
        JPanel cCard = createMetricSectionCard("Random Forest Classifier (Failure Risk)",
                new String[][]{
                    {"Accuracy", String.format("%.2f%%", rep.getClassificationAccuracy() * 100)},
                    {"Precision", String.format("%.2f%%", rep.getPrecision() * 100)},
                    {"Recall", String.format("%.2f%%", rep.getRecall() * 100)},
                    {"F1-Score", String.format("%.4f", rep.getF1Score())},
                    {"ROC-AUC", String.format("%.4f", rep.getRocAuc())}
                });

        // Confusion Matrix sub-grid
        int[][] cm = rep.getConfusionMatrix();
        JPanel cmPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        cmPanel.setOpaque(false);
        cmPanel.setBorder(BorderFactory.createTitledBorder("Test Set Confusion Matrix"));
        cmPanel.add(new JLabel("TN: " + cm[0][0], SwingConstants.CENTER));
        cmPanel.add(new JLabel("FP: " + cm[0][1], SwingConstants.CENTER));
        cmPanel.add(new JLabel("FN: " + cm[1][0], SwingConstants.CENTER));
        cmPanel.add(new JLabel("TP: " + cm[1][1], SwingConstants.CENTER));
        cCard.add(cmPanel, BorderLayout.SOUTH);

        // Regression Card
        JPanel rCard = createMetricSectionCard("Random Forest Regressor (RUL Cycles)",
                new String[][]{
                    {"Mean Absolute Error (MAE)", String.format("%.2f cycles", rep.getRulMeanAbsoluteError())},
                    {"Root Mean Squared Error (RMSE)", String.format("%.2f cycles", rep.getRulRootMeanSquaredError())},
                    {"R² Determination Score", String.format("%.4f", rep.getRulR2Score())},
                    {"Prediction Bounds", "90% Empirical Confidence Interval"},
                    {"Algorithm", "Bagged Regression Trees (40 Trees)"}
                });

        // Anomaly Card
        JPanel aCard = createMetricSectionCard("Isolation Forest (Unsupervised Anomalies)",
                new String[][]{
                    {"Anomaly Detection Rate", String.format("%.1f%%", rep.getAnomalyRatePct())},
                    {"Flagged Anomaly Count", String.valueOf(rep.getDetectedAnomalyCount())},
                    {"Average Anomaly Score", String.format("%.3f", rep.getAvgAnomalyScore())},
                    {"Anomaly Threshold", "s(x) >= 0.62"},
                    {"iTree Forest Size", "100 Trees (Subsample 256)"}
                });

        metricsGrid.add(cCard);
        metricsGrid.add(rCard);
        metricsGrid.add(aCard);
        modelPerfPanel.add(metricsGrid);
        modelPerfPanel.add(Box.createVerticalStrut(16));

        // Global Feature Importance Table
        JLabel featTitle = new JLabel("Global Feature Importance (Gini Impurity Reduction Across All Trees):");
        featTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        modelPerfPanel.add(featTitle);
        modelPerfPanel.add(Box.createVerticalStrut(6));

        String[] fCols = {"Rank", "Sensor Feature Name", "Relative Importance Weight", "Bar Representation"};
        DefaultTableModel fModel = new DefaultTableModel(fCols, 0);
        int rank = 1;
        for (Map.Entry<String, Double> entry : rep.getFeatureImportanceRankings().entrySet()) {
            int pct = (int) Math.round(entry.getValue() * 100);
            StringBuilder bar = new StringBuilder();
            for (int b = 0; b < pct; b++) bar.append("█");
            fModel.addRow(new Object[]{rank++, entry.getKey(), String.format("%.2f%%", entry.getValue() * 100), bar.toString()});
        }
        JTable fTable = new JTable(fModel);
        fTable.setRowHeight(24);
        fTable.setFont(ThemeConstants.FONT_BODY);
        JScrollPane fScroll = new JScrollPane(fTable);
        fScroll.setPreferredSize(new Dimension(800, 160));
        modelPerfPanel.add(fScroll);
        modelPerfPanel.add(Box.createVerticalStrut(16));

        // Documented Limitations
        JLabel limTitle = new JLabel("Documented Model Limitations & Aviation Maintenance Compliance:");
        limTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        modelPerfPanel.add(limTitle);
        modelPerfPanel.add(Box.createVerticalStrut(6));

        JTextArea limArea = new JTextArea();
        limArea.setEditable(false);
        limArea.setFont(ThemeConstants.FONT_BODY);
        limArea.setBackground(new Color(248, 250, 252));
        limArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        StringBuilder lsb = new StringBuilder();
        for (String lim : rep.getDocumentedLimitations()) {
            lsb.append("• ").append(lim).append("\n");
        }
        limArea.setText(lsb.toString());
        modelPerfPanel.add(limArea);

        modelPerfPanel.revalidate();
        modelPerfPanel.repaint();
    }

    private JPanel createMetricSectionCard(String title, String[][] metrics) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(ThemeConstants.FONT_SUBTITLE);
        tLbl.setForeground(ThemeConstants.TEXT_DARK);
        card.add(tLbl, BorderLayout.NORTH);

        JPanel mGrid = new JPanel(new GridLayout(metrics.length, 2, 4, 4));
        mGrid.setOpaque(false);
        for (String[] m : metrics) {
            JLabel k = new JLabel(m[0]);
            k.setFont(ThemeConstants.FONT_SMALL);
            k.setForeground(ThemeConstants.TEXT_MUTED);

            JLabel v = new JLabel(m[1], SwingConstants.RIGHT);
            v.setFont(ThemeConstants.FONT_BODY_BOLD);
            v.setForeground(ThemeConstants.TEXT_DARK);

            mGrid.add(k);
            mGrid.add(v);
        }
        card.add(mGrid, BorderLayout.CENTER);
        return card;
    }

    private void onRetrainModels() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingUtilities.invokeLater(() -> {
            try {
                coordinator.initializeSystem();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Machine Learning models successfully retrained and evaluated on latest telemetry!",
                        "Retraining Complete", JOptionPane.INFORMATION_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    // ==========================================
    // DATA REFRESH LOGIC
    // ==========================================
    public void refreshAllData() {
        // Refresh Header Cards
        aircraftCard.setValue(String.valueOf(coordinator.getTotalAircraftCount()));
        componentsCard.setValue(String.valueOf(coordinator.getFleetHealthRecords().size()));
        highRiskCard.setValue(String.valueOf(coordinator.getHighOrCriticalRiskCount()));
        anomaliesCard.setValue(String.valueOf(coordinator.getAnomalyCount()));
        alertsCard.setValue(String.valueOf(coordinator.getAlertService().getActiveAlerts().size()));

        // Populate Aircraft Combos
        List<String> acList = coordinator.getUniqueAircraft();
        updateComboBox(acFilterCombo, "All Aircraft", acList);
        updateComboBox(deepDiveAcCombo, null, acList);
        updateComboBox(forecastAcCombo, null, acList);

        // Refresh Fleet Table
        fleetTableModel.setRowCount(0);
        for (ComponentHealthRecord r : coordinator.getFleetHealthRecords()) {
            fleetTableModel.addRow(new Object[]{
                r.getAircraftId(),
                r.getAircraftModel(),
                r.getComponent().getDisplayName(),
                String.format("%.1f%%", r.getFailureRiskScore() * 100),
                r.getRiskCategory(),
                String.format("%.0f cycles", r.getPredictedRulCycles()),
                String.format("%.3f", r.getAnomalyScore()),
                r.getPriority(),
                r.getRecommendedAction()
            });
        }

        // Refresh Alert Table
        alertTableModel.setRowCount(0);
        for (MaintenanceAlert a : coordinator.getAlertService().getAllAlerts()) {
            alertTableModel.addRow(new Object[]{
                a.getAlertId(),
                a.getSeverity(),
                a.getAircraftId(),
                a.getComponent().getDisplayName(),
                a.getTitle(),
                a.getSupportingEvidence(),
                a.getStatus(),
                a.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            });
        }

        // Refresh Work Order Table
        workOrderTableModel.setRowCount(0);
        for (MaintenanceWorkOrder wo : coordinator.getWorkOrderService().getAllWorkOrders()) {
            workOrderTableModel.addRow(new Object[]{
                wo.getWorkOrderId(),
                wo.getAircraftId(),
                wo.getComponent().getDisplayName(),
                wo.getPriority(),
                wo.getStatus(),
                wo.getAssignedTechnician(),
                wo.getCreatedAt(),
                wo.getTitle() + " - " + wo.getTechnicianNotes()
            });
        }

        updateDeepDiveView();
        updateForecastingView();
        updateModelPerformanceView();
    }

    private void updateComboBox(JComboBox<String> combo, String allPrefix, List<String> items) {
        if (combo == null) return;
        Object curr = combo.getSelectedItem();
        combo.removeAllItems();
        if (allPrefix != null) combo.addItem(allPrefix);
        for (String item : items) combo.addItem(item);
        if (curr != null) combo.setSelectedItem(curr);
        else if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
    }

    // ==========================================
    // CUSTOM TABLE CELL RENDERERS
    // ==========================================
    private static class RiskBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof RiskCategory rc) {
                lbl.setText(rc.getLabel().toUpperCase());
                lbl.setForeground(rc.getColor());
                lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
                if (!isSel) lbl.setBackground(new Color(rc.getColor().getRed(), rc.getColor().getGreen(), rc.getColor().getBlue(), 25));
            }
            return lbl;
        }
    }

    private static class PriorityBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof MaintenancePriority mp) {
                lbl.setText(mp.getTitle().toUpperCase());
                lbl.setForeground(mp.getBadgeColor());
                lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
                if (!isSel) lbl.setBackground(new Color(mp.getBadgeColor().getRed(), mp.getBadgeColor().getGreen(), mp.getBadgeColor().getBlue(), 25));
            }
            return lbl;
        }
    }

    private static class SeverityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof MaintenanceAlert.Severity sev) {
                lbl.setText(sev.name());
                lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
                Color c = sev == MaintenanceAlert.Severity.CRITICAL ? new Color(239, 68, 68) :
                          (sev == MaintenanceAlert.Severity.WARNING ? new Color(245, 158, 11) : new Color(59, 130, 246));
                lbl.setForeground(c);
            }
            return lbl;
        }
    }
}
