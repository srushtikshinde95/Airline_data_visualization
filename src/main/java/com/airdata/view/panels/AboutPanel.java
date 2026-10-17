package com.airdata.view.panels;

import com.airdata.util.ThemeConstants;

import javax.swing.*;
import java.awt.*;

public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(Color.WHITE);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(ThemeConstants.FONT_BODY);

        editorPane.setText(buildHtmlContent());
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private String buildHtmlContent() {
        return "<html><body style='font-family:Segoe UI, sans-serif; padding:18px; color:#1e293b;'>" +
                "<h1 style='color:#0e74d6; margin-bottom:4px;'>AirData Analytics Suite</h1>" +
                "<p style='color:#64748b; font-size:13px; margin-top:0;'>Airline Data Visualization Software Using Object-Oriented Programming (OOP) in Java</p>" +
                "<hr style='border:0; border-top:1px solid #e2e8f0; margin:16px 0;'/>" +

                "<h2 style='color:#0f172a;'>1. Project Overview</h2>" +
                "<p>This desktop software allows aviation analysts, students, and airline managers to import, clean, validate, filter, analyze, and visualize complex airline operations datasets. It renders real-time dynamic statistics, multi-metric charts, and performance scorecards from raw CSV datasets.</p>" +

                "<h2 style='color:#0f172a;'>2. Object-Oriented Programming (OOP) Implementation</h2>" +
                "<table border='1' cellpadding='8' style='border-collapse:collapse; width:100%; border-color:#e2e8f0;'>" +
                "<tr style='background-color:#f8fafc; font-weight:bold;'>" +
                "<th>OOP Concept</th><th>Implementation in Project</th><th>Key Classes</th>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Classes & Objects</b></td>" +
                "<td>Clear domain modeling representing physical and operational aviation entities.</td>" +
                "<td><code>Flight, Airline, Airport, Route, StatCard</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Encapsulation</b></td>" +
                "<td>Private fields with validated getters and setters, immutable summary snapshots, defensive copying.</td>" +
                "<td><code>Flight, AirlineDataset, DatasetSummary</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Inheritance</b></td>" +
                "<td>Class hierarchies sharing common state and behavior.</td>" +
                "<td><code>BaseEntity &rarr; Flight &rarr; CommercialFlight</code><br/><code>AbstractChartComponent &rarr; ModernBarChart</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Polymorphism</b></td>" +
                "<td>Dynamic method dispatch in chart rendering, revenue computation, and filter predicates.</td>" +
                "<td><code>IChartComponent.renderChart()</code><br/><code>CommercialFlight.calculateRevenue()</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Abstraction & Interfaces</b></td>" +
                "<td>Strict contracts separating presentation, business logic, and data ingestion layers.</td>" +
                "<td><code>IDataLoader, IDataExporter, IDataFilter, IStatisticsCalculator, IDatasetListener</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Composition / Aggregation</b></td>" +
                "<td>Complex entities composed of specialized domain objects.</td>" +
                "<td><code>Flight</code> has-a <code>Airline, Airport, Route, FlightStatus</code><br/><code>AirlineDataset</code> aggregates <code>List&lt;Flight&gt;</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Method Overloading</b></td>" +
                "<td>Multiple constructor overloads and statistical methods handling diverse parameter types.</td>" +
                "<td><code>DateUtils.formatDate()</code>, Constructor Overloads in <code>Flight, Airline, Airport</code></td>" +
                "</tr>" +
                "<tr>" +
                "<td><b>Method Overriding</b></td>" +
                "<td>Custom implementations of <code>toString(), equals(), hashCode(), paintComponent()</code>.</td>" +
                "<td><code>CommercialFlight, ModernBarChart, StatusBadgeRenderer</code></td>" +
                "</tr>" +
                "</table>" +

                "<h2 style='color:#0f172a;'>3. Architecture Design (MVC)</h2>" +
                "<ul>" +
                "<li><b>Presentation Layer (View):</b> <code>MainFrame, DashboardPanel, DatasetTablePanel, VisualizationPanel, AnalyticsPanel, ModernBarChart, ModernPieChart, ModernLineChart</code></li>" +
                "<li><b>Controller Layer:</b> <code>AppController, IDatasetListener</code> (Observer Pattern coordinating updates)</li>" +
                "<li><b>Business Logic (Service):</b> <code>CSVDataLoader, CSVDataExporter, DataFilter, StatisticsCalculator, RouteAnalysisService</code></li>" +
                "<li><b>Data Model Layer:</b> <code>BaseEntity, Flight, CommercialFlight, Airline, Airport, Route, FlightStatus, AirlineDataset</code></li>" +
                "</ul>" +

                "<h2 style='color:#0f172a;'>4. User Guide & Features</h2>" +
                "<ol>" +
                "<li><b>Import CSV:</b> Click 'Open CSV File' on the sidebar or 'Import CSV' in Dataset tab to load custom flight data.</li>" +
                "<li><b>Live Filter Bar:</b> Select Airline, Origin, Destination, or Status and type in search to filter records in real-time.</li>" +
                "<li><b>Interactive Charts:</b> In the Visualizations tab, switch between Bar, Donut, Line, and Comparison charts. Hover on bars for tooltips. Click 'Export Chart (PNG)' to save.</li>" +
                "<li><b>Dataset CRUD:</b> Add new flight, Edit, or Delete with automatic validation and real-time dashboard sync.</li>" +
                "<li><b>Export Data:</b> Export filtered records to CSV using 'Export CSV'.</li>" +
                "</ol>" +

                "<p style='color:#64748b; font-size:12px; margin-top:20px;'>Version 1.0.0 | Academic College Level Java Project</p>" +
                "</body></html>";
    }
}
