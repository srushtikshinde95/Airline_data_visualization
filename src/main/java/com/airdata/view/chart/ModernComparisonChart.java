package com.airdata.view.chart;

import com.airdata.util.ThemeConstants;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Grouped comparison chart comparing multiple metrics side-by-side per entity.
 */
public class ModernComparisonChart extends AbstractChartComponent {

    public static class ComparisonItem {
        public String category;
        public double metricA; // e.g. Flights
        public double metricB; // e.g. Avg Delay (scaled)
        public double metricC; // e.g. Punctuality %

        public ComparisonItem(String category, double metricA, double metricB, double metricC) {
            this.category = category;
            this.metricA = metricA;
            this.metricB = metricB;
            this.metricC = metricC;
        }
    }

    private final List<ComparisonItem> items = new ArrayList<>();
    private String labelA = "Flights";
    private String labelB = "Avg Delay (min)";
    private String labelC = "On-Time %";

    public ModernComparisonChart(String title, String subtitle) {
        super(title, subtitle);
    }

    public void setData(List<ComparisonItem> newItems, String labelA, String labelB, String labelC) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.labelA = labelA;
        this.labelB = labelB;
        this.labelC = labelC;
        repaint();
    }

    @Override
    public void renderChart(Graphics2D g2, int width, int height) {
        if (items.isEmpty()) {
            drawEmptyPlaceholder(g2, width, height);
            return;
        }

        int startX = padding.left + 50;
        int endX = width - padding.right - 20;
        int startY = padding.top + 60;
        int endY = height - padding.bottom - 45;

        int plotWidth = endX - startX;
        int plotHeight = endY - startY;
        if (plotWidth <= 0 || plotHeight <= 0) return;

        // Draw Legend Header
        drawLegend(g2, startX, padding.top + 35);

        double maxVal = 0.0;
        for (ComparisonItem item : items) {
            maxVal = Math.max(maxVal, Math.max(item.metricA, Math.max(item.metricB, item.metricC)));
        }
        if (maxVal <= 0) maxVal = 1.0;

        // Grid lines
        int gridLines = 4;
        g2.setFont(ThemeConstants.FONT_SMALL);
        for (int i = 0; i <= gridLines; i++) {
            int y = endY - (i * plotHeight / gridLines);
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(startX, y, endX, y);

            double val = (maxVal / gridLines) * i;
            g2.setColor(ThemeConstants.TEXT_MUTED);
            g2.drawString(String.format("%.0f", val), startX - 45, y + 4);
        }

        int groupCount = items.size();
        int groupWidth = plotWidth / groupCount;
        int barWidth = Math.max(8, (groupWidth - 20) / 3);

        Color colA = ThemeConstants.CHART_PALETTE[0];
        Color colB = ThemeConstants.CHART_PALETTE[2];
        Color colC = ThemeConstants.CHART_PALETTE[1];

        for (int i = 0; i < items.size(); i++) {
            ComparisonItem item = items.get(i);
            int groupStartX = startX + (i * groupWidth) + 10;

            // Bar A
            drawSingleBar(g2, groupStartX, endY, barWidth, item.metricA, maxVal, plotHeight, colA);
            // Bar B
            drawSingleBar(g2, groupStartX + barWidth + 3, endY, barWidth, item.metricB, maxVal, plotHeight, colB);
            // Bar C
            drawSingleBar(g2, groupStartX + (barWidth + 3) * 2, endY, barWidth, item.metricC, maxVal, plotHeight, colC);

            // Group Label
            g2.setFont(ThemeConstants.FONT_SMALL);
            g2.setColor(ThemeConstants.TEXT_DARK);
            String cat = truncate(item.category, 8);
            FontMetrics fm = g2.getFontMetrics();
            int catX = groupStartX + (barWidth * 3) / 2 - (fm.stringWidth(cat) / 2);
            g2.drawString(cat, catX, endY + 18);
        }
    }

    private void drawSingleBar(Graphics2D g2, int x, int endY, int barWidth, double val, double maxVal, int plotHeight, Color color) {
        int barHeight = (int) ((val / maxVal) * plotHeight);
        int y = endY - barHeight;

        g2.setColor(color);
        g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 4, 4));

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(ThemeConstants.TEXT_DARK);
        String s = String.format("%.0f", val);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, x + (barWidth - fm.stringWidth(s)) / 2, Math.max(endY - plotHeight, y - 3));
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        drawLegendItem(g2, x, y, ThemeConstants.CHART_PALETTE[0], labelA);
        drawLegendItem(g2, x + 150, y, ThemeConstants.CHART_PALETTE[2], labelB);
        drawLegendItem(g2, x + 300, y, ThemeConstants.CHART_PALETTE[1], labelC);
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color c, String label) {
        g2.setColor(c);
        g2.fillRoundRect(x, y - 9, 10, 10, 3, 3);
        g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
        g2.setColor(ThemeConstants.TEXT_DARK);
        g2.drawString(label, x + 14, y);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }

    private void drawEmptyPlaceholder(Graphics2D g2, int width, int height) {
        g2.setFont(ThemeConstants.FONT_BODY);
        g2.setColor(ThemeConstants.TEXT_MUTED);
        String msg = "No comparison data available";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
    }
}
