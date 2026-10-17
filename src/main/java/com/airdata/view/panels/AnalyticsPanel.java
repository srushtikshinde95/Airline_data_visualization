package com.airdata.view.panels;

import com.airdata.controller.AppController;
import com.airdata.controller.IDatasetListener;
import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.service.RouteAnalysisService;
import com.airdata.util.FormatUtils;
import com.airdata.util.ThemeConstants;
import com.airdata.view.components.StatCard;
import com.airdata.view.components.VectorIcons;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class AnalyticsPanel extends JPanel implements IDatasetListener {

    private final AppController controller;

    private StatCard busiestAirlineCard;
    private StatCard busiestOriginCard;
    private StatCard busiestDestCard;
    private StatCard avgTicketPriceCard;
    private StatCard avgPaxPerFlightCard;
    private StatCard maxDelayCard;

    private DefaultTableModel scorecardsModel;
    private JTable scorecardsTable;

    public AnalyticsPanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 14));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        initUI();
        this.controller.addListener(this);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Airline Operational Analytics & Scorecards");
        title.setFont(ThemeConstants.FONT_HERO);
        title.setForeground(ThemeConstants.TEXT_DARK);
        headerPanel.add(title, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JPanel insightGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        insightGrid.setOpaque(false);

        busiestAirlineCard = new StatCard("Busiest Airline", "N/A", "Highest flight volume", ThemeConstants.CHART_PALETTE[0], VectorIcons.IconType.AIRPLANE);
        busiestOriginCard = new StatCard("Busiest Origin Hub", "N/A", "Top departure airport", ThemeConstants.CHART_PALETTE[1], VectorIcons.IconType.AIRPORT);
        busiestDestCard = new StatCard("Busiest Destination", "N/A", "Top arrival airport", ThemeConstants.CHART_PALETTE[2], VectorIcons.IconType.AIRPORT);
        avgTicketPriceCard = new StatCard("Avg Ticket Price", "$0.00", "Mean fare across flights", ThemeConstants.CHART_PALETTE[3], VectorIcons.IconType.REVENUE);
        avgPaxPerFlightCard = new StatCard("Avg Pax per Flight", "0", "Mean passenger density", ThemeConstants.CHART_PALETTE[4], VectorIcons.IconType.PASSENGER);
        maxDelayCard = new StatCard("Maximum Delay", "0 min", "Longest recorded delay", ThemeConstants.STATUS_CANCELLED, VectorIcons.IconType.CLOCK);

        insightGrid.add(busiestAirlineCard);
        insightGrid.add(busiestOriginCard);
        insightGrid.add(busiestDestCard);
        insightGrid.add(avgTicketPriceCard);
        insightGrid.add(avgPaxPerFlightCard);
        insightGrid.add(maxDelayCard);

        contentPanel.add(insightGrid);
        contentPanel.add(Box.createVerticalStrut(16));

        JPanel tableContainer = new JPanel(new BorderLayout(0, 8));
        tableContainer.setBackground(ThemeConstants.BG_CARD);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel tableTitle = new JLabel("Airline Performance & Punctuality Leaderboard");
        tableTitle.setFont(ThemeConstants.FONT_TITLE);
        tableTitle.setForeground(ThemeConstants.TEXT_DARK);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Airline", "Flights", "Total Passengers", "Total Revenue", "Avg Delay", "On-Time Rate", "Revenue / Pax"};
        scorecardsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        scorecardsTable = new JTable(scorecardsModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                } else {
                    c.setBackground(new Color(224, 242, 254));
                }
                return c;
            }
        };

        scorecardsTable.setRowHeight(32);
        scorecardsTable.setFont(ThemeConstants.FONT_BODY);
        scorecardsTable.setShowGrid(true);
        scorecardsTable.setGridColor(ThemeConstants.BORDER_COLOR);
        scorecardsTable.setSelectionBackground(new Color(224, 242, 254));
        scorecardsTable.setSelectionForeground(ThemeConstants.TEXT_DARK);

        JTableHeader th = scorecardsTable.getTableHeader();
        th.setFont(ThemeConstants.FONT_BODY_BOLD);
        th.setBackground(new Color(241, 245, 249));
        th.setForeground(ThemeConstants.TEXT_DARK);
        th.setPreferredSize(new Dimension(0, 36));

        JScrollPane tableScroll = new JScrollPane(scorecardsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR));
        tableScroll.setPreferredSize(new Dimension(800, 240));
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        contentPanel.add(tableContainer);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    @Override
    public void onDatasetChanged(AirlineDataset fullDataset, List<Flight> currentViewFlights, DatasetSummary currentSummary) {
        busiestAirlineCard.setValue(currentSummary.getBusiestAirline());
        busiestOriginCard.setValue(currentSummary.getBusiestOrigin());
        busiestDestCard.setValue(currentSummary.getBusiestDestination());
        avgTicketPriceCard.setValue(FormatUtils.formatCurrency(currentSummary.getAverageTicketPrice()));
        avgPaxPerFlightCard.setValue(String.format("%.0f pax", currentSummary.getAveragePassengersPerFlight()));
        maxDelayCard.setValue(currentSummary.getMaxDelayMinutes() + " min");

        scorecardsModel.setRowCount(0);
        List<RouteAnalysisService.AirlineScorecard> scorecards =
                controller.getRouteAnalysisService().generateScorecards(currentViewFlights);

        for (RouteAnalysisService.AirlineScorecard sc : scorecards) {
            scorecardsModel.addRow(new Object[]{
                    sc.airlineName,
                    FormatUtils.formatNumber(sc.totalFlights),
                    FormatUtils.formatNumber(sc.totalPassengers),
                    FormatUtils.formatCurrency(sc.totalRevenue),
                    String.format("%.1f min", sc.avgDelayMinutes),
                    FormatUtils.formatPercentage(sc.onTimePercentage),
                    FormatUtils.formatCurrency(sc.revenuePerPax)
            });
        }
    }
}
