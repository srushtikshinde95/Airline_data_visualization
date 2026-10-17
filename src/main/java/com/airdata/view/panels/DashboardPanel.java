package com.airdata.view.panels;

import com.airdata.controller.AppController;
import com.airdata.controller.IDatasetListener;
import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.model.FlightStatus;
import com.airdata.util.FormatUtils;
import com.airdata.util.ThemeConstants;
import com.airdata.view.chart.ModernBarChart;
import com.airdata.view.chart.ModernPieChart;
import com.airdata.view.components.StatCard;
import com.airdata.view.components.VectorIcons;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel implements IDatasetListener {

    private final AppController controller;

    private StatCard totalFlightsCard;
    private StatCard totalPassengersCard;
    private StatCard totalRevenueCard;
    private StatCard airlinesCountCard;
    private StatCard airportsCountCard;
    private StatCard avgDelayCard;
    private StatCard cancelledCard;
    private StatCard onTimePctCard;

    private ModernBarChart airlineFlightsChart;
    private ModernPieChart statusPieChart;

    private JPanel emptyStateBanner;

    public DashboardPanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        initUI();
        this.controller.addListener(this);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Executive Airline Dashboard");
        titleLabel.setFont(ThemeConstants.FONT_HERO);
        titleLabel.setForeground(ThemeConstants.TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Real-time operational metrics and flight performance indicators");
        subtitleLabel.setFont(ThemeConstants.FONT_BODY);
        subtitleLabel.setForeground(ThemeConstants.TEXT_MUTED);

        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);
        textWrapper.add(titleLabel);
        textWrapper.add(Box.createVerticalStrut(2));
        textWrapper.add(subtitleLabel);
        headerPanel.add(textWrapper, BorderLayout.WEST);

        // Empty state alert banner
        emptyStateBanner = new JPanel(new BorderLayout(12, 0));
        emptyStateBanner.setBackground(new Color(254, 243, 199));
        emptyStateBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 158, 11), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        emptyStateBanner.setVisible(false);

        JLabel alertText = new JLabel("No flights found matching the active filter criteria. Click 'Reset Filters' to view all flights.");
        alertText.setFont(ThemeConstants.FONT_BODY_BOLD);
        alertText.setForeground(new Color(146, 64, 14));
        alertText.setIcon(VectorIcons.createIcon(VectorIcons.IconType.ALERT, 16, new Color(217, 119, 6)));

        ModernButton clearFilterBtn = new ModernButton("Reset Filters", ModernButton.ButtonStyle.SECONDARY);
        clearFilterBtn.setPreferredSize(new Dimension(110, 28));
        clearFilterBtn.addActionListener(e -> controller.resetFilters());

        emptyStateBanner.add(alertText, BorderLayout.WEST);
        emptyStateBanner.add(clearFilterBtn, BorderLayout.EAST);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        mainContent.add(emptyStateBanner);
        mainContent.add(Box.createVerticalStrut(12));

        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        kpiGrid.setOpaque(false);

        totalFlightsCard = new StatCard("Total Flights", "0", "Scheduled flights", ThemeConstants.ACCENT, VectorIcons.IconType.AIRPLANE);
        totalPassengersCard = new StatCard("Total Passengers", "0", "Travelers booked", ThemeConstants.CHART_PALETTE[3], VectorIcons.IconType.PASSENGER);
        totalRevenueCard = new StatCard("Total Revenue", "$0", "Gross earnings", ThemeConstants.STATUS_ON_TIME, VectorIcons.IconType.REVENUE);
        airlinesCountCard = new StatCard("Airlines", "0", "Carriers operating", ThemeConstants.CHART_PALETTE[5], VectorIcons.IconType.DASHBOARD);

        airportsCountCard = new StatCard("Airports", "0", "Origins & destinations", ThemeConstants.CHART_PALETTE[6], VectorIcons.IconType.AIRPORT);
        avgDelayCard = new StatCard("Average Delay", "0 min", "Flight delay average", ThemeConstants.STATUS_DELAYED, VectorIcons.IconType.CLOCK);
        cancelledCard = new StatCard("Cancelled Flights", "0", "Disrupted operations", ThemeConstants.STATUS_CANCELLED, VectorIcons.IconType.ALERT);
        onTimePctCard = new StatCard("On-Time Rate", "0%", "Punctuality index", ThemeConstants.STATUS_ON_TIME, VectorIcons.IconType.CHECK);

        kpiGrid.add(totalFlightsCard);
        kpiGrid.add(totalPassengersCard);
        kpiGrid.add(totalRevenueCard);
        kpiGrid.add(airlinesCountCard);
        kpiGrid.add(airportsCountCard);
        kpiGrid.add(avgDelayCard);
        kpiGrid.add(cancelledCard);
        kpiGrid.add(onTimePctCard);

        mainContent.add(kpiGrid);
        mainContent.add(Box.createVerticalStrut(16));

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 16));
        chartsRow.setOpaque(false);
        chartsRow.setPreferredSize(new Dimension(800, 390));

        airlineFlightsChart = new ModernBarChart("Flights by Airline", "Volume distribution across carriers (Hover for details)");
        statusPieChart = new ModernPieChart("Flight Status Distribution", "Proportion of on-time, delayed & cancelled");
        statusPieChart.setCustomColor(FlightStatus.ON_TIME.getLabel(), ThemeConstants.STATUS_ON_TIME);
        statusPieChart.setCustomColor(FlightStatus.DELAYED.getLabel(), ThemeConstants.STATUS_DELAYED);
        statusPieChart.setCustomColor(FlightStatus.CANCELLED.getLabel(), ThemeConstants.STATUS_CANCELLED);

        chartsRow.add(airlineFlightsChart);
        chartsRow.add(statusPieChart);

        mainContent.add(chartsRow);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void onDatasetChanged(AirlineDataset fullDataset, List<Flight> currentViewFlights, DatasetSummary currentSummary) {
        boolean isEmpty = (currentSummary.getTotalFlights() == 0);
        emptyStateBanner.setVisible(isEmpty);

        totalFlightsCard.setValue(FormatUtils.formatNumber(currentSummary.getTotalFlights()));
        totalPassengersCard.setValue(FormatUtils.formatNumber(currentSummary.getTotalPassengers()));
        totalRevenueCard.setValue(FormatUtils.formatCurrency(currentSummary.getTotalRevenue()));
        airlinesCountCard.setValue(String.valueOf(currentSummary.getUniqueAirlinesCount()));
        airportsCountCard.setValue(String.valueOf(currentSummary.getUniqueAirportsCount()));
        avgDelayCard.setValue(String.format("%.1f min", currentSummary.getAverageDelayMinutes()));
        cancelledCard.setValue(FormatUtils.formatNumber(currentSummary.getCancelledFlightsCount()));
        onTimePctCard.setValue(FormatUtils.formatPercentage(currentSummary.getOnTimePercentage()));

        Map<String, Integer> byAirline = controller.getStatsCalculator().getFlightCountByAirline(currentViewFlights);
        airlineFlightsChart.setData(byAirline, ModernBarChart.ValueFormat.NUMBER);

        Map<FlightStatus, Integer> statusMap = controller.getStatsCalculator().getStatusDistribution(currentViewFlights);
        Map<String, Integer> pieData = new java.util.LinkedHashMap<>();
        statusMap.forEach((status, count) -> pieData.put(status.getLabel(), count));
        statusPieChart.setData(pieData);
    }
}
