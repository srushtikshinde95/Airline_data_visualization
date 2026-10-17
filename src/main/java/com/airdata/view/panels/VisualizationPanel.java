package com.airdata.view.panels;

import com.airdata.controller.AppController;
import com.airdata.controller.IDatasetListener;
import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.model.FlightStatus;
import com.airdata.util.ThemeConstants;
import com.airdata.view.chart.*;
import com.airdata.view.components.ModernButton;
import com.airdata.view.components.VectorIcons;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VisualizationPanel extends JPanel implements IDatasetListener {

    private final AppController controller;

    private ModernBarChart barChart;
    private JComboBox<String> barMetricCombo;

    private ModernPieChart pieChart;
    private JComboBox<String> pieMetricCombo;

    private ModernLineChart lineChart;
    private JComboBox<String> lineMetricCombo;

    private ModernComparisonChart comparisonChart;

    public VisualizationPanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        initUI();
        this.controller.addListener(this);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Data Visualizations & Graphical Analytics");
        title.setFont(ThemeConstants.FONT_HERO);
        title.setForeground(ThemeConstants.TEXT_DARK);
        headerPanel.add(title, BorderLayout.WEST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeConstants.FONT_BODY_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Bar Charts",
                VectorIcons.createIcon(VectorIcons.IconType.VISUALIZATION, 16, ThemeConstants.ACCENT),
                createBarChartTab());

        tabbedPane.addTab("Pie / Donut Charts",
                VectorIcons.createIcon(VectorIcons.IconType.DASHBOARD, 16, ThemeConstants.STATUS_ON_TIME),
                createPieChartTab());

        tabbedPane.addTab("Trend Line Charts",
                VectorIcons.createIcon(VectorIcons.IconType.ANALYTICS, 16, ThemeConstants.STATUS_DELAYED),
                createLineChartTab());

        tabbedPane.addTab("Multi-Metric Comparison",
                VectorIcons.createIcon(VectorIcons.IconType.DATASET, 16, ThemeConstants.CHART_PALETTE[3]),
                createComparisonChartTab());

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBarChartTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel lbl = new JLabel("Select Metric:");
        lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
        barMetricCombo = new JComboBox<>(new String[]{
                "Flights by Airline",
                "Passengers by Airline",
                "Revenue by Airline",
                "Top 10 Busiest Routes"
        });
        barMetricCombo.setPreferredSize(new Dimension(200, 32));
        barMetricCombo.setFont(ThemeConstants.FONT_BODY);
        barMetricCombo.addActionListener(e -> updateBarChart());

        ModernButton exportBtn = new ModernButton("Export Chart (PNG)", ModernButton.ButtonStyle.SECONDARY);
        exportBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.EXPORT, 14, ThemeConstants.TEXT_DARK));
        exportBtn.addActionListener(e -> exportChartImage(barChart, "airline_bar_chart.png"));

        toolbar.add(lbl);
        toolbar.add(barMetricCombo);
        toolbar.add(exportBtn);

        barChart = new ModernBarChart("Airline Volume Breakdown", "Comparative metric across airline carriers (Hover on bars)");
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(barChart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPieChartTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel lbl = new JLabel("Select Distribution:");
        lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
        pieMetricCombo = new JComboBox<>(new String[]{
                "Flight Status Distribution",
                "Passenger Share by Airline",
                "Cancelled vs Completed Flights"
        });
        pieMetricCombo.setPreferredSize(new Dimension(230, 32));
        pieMetricCombo.setFont(ThemeConstants.FONT_BODY);
        pieMetricCombo.addActionListener(e -> updatePieChart());

        ModernButton exportBtn = new ModernButton("Export Chart (PNG)", ModernButton.ButtonStyle.SECONDARY);
        exportBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.EXPORT, 14, ThemeConstants.TEXT_DARK));
        exportBtn.addActionListener(e -> exportChartImage(pieChart, "flight_status_pie_chart.png"));

        toolbar.add(lbl);
        toolbar.add(pieMetricCombo);
        toolbar.add(exportBtn);

        pieChart = new ModernPieChart("Flight Status Proportion", "Operational breakdown of completed and delayed flights");
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(pieChart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLineChartTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel lbl = new JLabel("Select Trend:");
        lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
        lineMetricCombo = new JComboBox<>(new String[]{
                "Daily Flight Volume Over Time",
                "Daily Revenue Over Time",
                "Average Delay Trajectory Over Time"
        });
        lineMetricCombo.setPreferredSize(new Dimension(250, 32));
        lineMetricCombo.setFont(ThemeConstants.FONT_BODY);
        lineMetricCombo.addActionListener(e -> updateLineChart());

        ModernButton exportBtn = new ModernButton("Export Chart (PNG)", ModernButton.ButtonStyle.SECONDARY);
        exportBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.EXPORT, 14, ThemeConstants.TEXT_DARK));
        exportBtn.addActionListener(e -> exportChartImage(lineChart, "flight_trend_line_chart.png"));

        toolbar.add(lbl);
        toolbar.add(lineMetricCombo);
        toolbar.add(exportBtn);

        lineChart = new ModernLineChart("Daily Flights Trend", "Temporal volume fluctuations across timeline");
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(lineChart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createComparisonChartTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel desc = new JLabel("Comparing Airline Performance: Total Flights vs Avg Delay (min) vs On-Time Rate (%)");
        desc.setFont(ThemeConstants.FONT_BODY_BOLD);
        desc.setForeground(ThemeConstants.TEXT_DARK);

        ModernButton exportBtn = new ModernButton("Export Chart (PNG)", ModernButton.ButtonStyle.SECONDARY);
        exportBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.EXPORT, 14, ThemeConstants.TEXT_DARK));
        exportBtn.addActionListener(e -> exportChartImage(comparisonChart, "airline_comparison_chart.png"));

        toolbar.add(desc);
        toolbar.add(exportBtn);

        comparisonChart = new ModernComparisonChart("Airline Performance Benchmark", "Grouped metrics across operating carriers");
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(comparisonChart, BorderLayout.CENTER);
        return panel;
    }

    private void updateBarChart() {
        List<Flight> flights = controller.getFilteredFlights();
        String selected = (String) barMetricCombo.getSelectedItem();
        if (selected == null) return;

        if (selected.contains("Passengers")) {
            barChart.setChartTitle("Passengers by Airline");
            barChart.setSubtitle("Total travelers accommodated per carrier");
            barChart.setData(controller.getStatsCalculator().getPassengerCountByAirline(flights), ModernBarChart.ValueFormat.NUMBER);
        } else if (selected.contains("Revenue")) {
            barChart.setChartTitle("Gross Revenue by Airline");
            barChart.setSubtitle("Total commercial revenue earned per carrier");
            barChart.setData(controller.getStatsCalculator().getRevenueByAirline(flights), ModernBarChart.ValueFormat.CURRENCY);
        } else if (selected.contains("Routes")) {
            barChart.setChartTitle("Top 10 Busiest Routes");
            barChart.setSubtitle("Highest traffic airport connection corridors");
            barChart.setData(controller.getStatsCalculator().getTopBusiestRoutes(flights, 10), ModernBarChart.ValueFormat.NUMBER);
        } else {
            barChart.setChartTitle("Flights by Airline");
            barChart.setSubtitle("Scheduled flight count across airlines");
            barChart.setData(controller.getStatsCalculator().getFlightCountByAirline(flights), ModernBarChart.ValueFormat.NUMBER);
        }
    }

    private void updatePieChart() {
        List<Flight> flights = controller.getFilteredFlights();
        String selected = (String) pieMetricCombo.getSelectedItem();
        if (selected == null) return;

        if (selected.contains("Passenger Share")) {
            pieChart.setChartTitle("Passenger Market Share");
            pieChart.setSubtitle("Proportion of total passengers booked by airline");
            pieChart.setData(controller.getStatsCalculator().getPassengerCountByAirline(flights));
        } else if (selected.contains("Cancelled vs Completed")) {
            pieChart.setChartTitle("Completion vs Cancellation");
            pieChart.setSubtitle("Operational completion reliability ratio");
            long cancelled = flights.stream().filter(Flight::isCancelled).count();
            long completed = flights.size() - cancelled;
            Map<String, Long> map = new LinkedHashMap<>();
            map.put("Completed Flights", completed);
            map.put("Cancelled Flights", cancelled);
            pieChart.setCustomColor("Completed Flights", ThemeConstants.STATUS_ON_TIME);
            pieChart.setCustomColor("Cancelled Flights", ThemeConstants.STATUS_CANCELLED);
            pieChart.setData(map);
        } else {
            pieChart.setChartTitle("Flight Status Distribution");
            pieChart.setSubtitle("Proportion of On-Time, Delayed, and Cancelled flights");
            Map<FlightStatus, Integer> raw = controller.getStatsCalculator().getStatusDistribution(flights);
            Map<String, Integer> map = new LinkedHashMap<>();
            raw.forEach((status, count) -> map.put(status.getLabel(), count));
            pieChart.setCustomColor(FlightStatus.ON_TIME.getLabel(), ThemeConstants.STATUS_ON_TIME);
            pieChart.setCustomColor(FlightStatus.DELAYED.getLabel(), ThemeConstants.STATUS_DELAYED);
            pieChart.setCustomColor(FlightStatus.CANCELLED.getLabel(), ThemeConstants.STATUS_CANCELLED);
            pieChart.setData(map);
        }
    }

    private void updateLineChart() {
        List<Flight> flights = controller.getFilteredFlights();
        String selected = (String) lineMetricCombo.getSelectedItem();
        if (selected == null) return;

        if (selected.contains("Revenue")) {
            lineChart.setChartTitle("Daily Revenue Trend");
            lineChart.setSubtitle("Total daily earnings ($) over the timeline");
            lineChart.setData(controller.getStatsCalculator().getRevenueOverTime(flights));
        } else if (selected.contains("Delay")) {
            lineChart.setChartTitle("Average Delay Trajectory");
            lineChart.setSubtitle("Average delay in minutes per departure day");
            lineChart.setData(controller.getStatsCalculator().getAverageDelayOverTime(flights));
        } else {
            lineChart.setChartTitle("Daily Flight Volume");
            lineChart.setSubtitle("Total flights scheduled per departure date");
            lineChart.setData(controller.getStatsCalculator().getFlightsOverTime(flights));
        }
    }

    private void updateComparisonChart() {
        List<Flight> flights = controller.getFilteredFlights();
        var scorecards = controller.getRouteAnalysisService().generateScorecards(flights);

        List<ModernComparisonChart.ComparisonItem> items = new ArrayList<>();
        for (var sc : scorecards) {
            items.add(new ModernComparisonChart.ComparisonItem(
                    sc.airlineName,
                    sc.totalFlights,
                    sc.avgDelayMinutes,
                    sc.onTimePercentage
            ));
        }

        comparisonChart.setData(items, "Total Flights", "Avg Delay (min)", "On-Time Rate (%)");
    }

    private void exportChartImage(IChartComponent chart, String defaultFileName) {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File(defaultFileName));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                chart.exportChartAsImage(target);
                JOptionPane.showMessageDialog(this, "Chart saved successfully as " + target.getName(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting chart: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onDatasetChanged(AirlineDataset fullDataset, List<Flight> currentViewFlights, DatasetSummary currentSummary) {
        updateBarChart();
        updatePieChart();
        updateLineChart();
        updateComparisonChart();
    }
}
