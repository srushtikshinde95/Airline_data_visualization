package com.airdata.view.chart;

import com.airdata.util.ThemeConstants;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModernPieChart extends AbstractChartComponent {

    private final Map<String, Double> dataset = new LinkedHashMap<>();
    private final Map<String, Color> customColors = new LinkedHashMap<>();
    private boolean isDonut = true;

    public ModernPieChart(String title, String subtitle) {
        super(title, subtitle);
        this.padding = new Insets(20, 20, 20, 20);
    }

    public void setData(Map<String, ? extends Number> rawData) {
        this.dataset.clear();
        if (rawData != null) {
            rawData.forEach((k, v) -> dataset.put(k, v.doubleValue()));
        }
        repaint();
    }

    public void setCustomColor(String key, Color color) {
        customColors.put(key, color);
    }

    public void setDonut(boolean donut) {
        this.isDonut = donut;
        repaint();
    }

    @Override
    public void renderChart(Graphics2D g2, int width, int height) {
        if (dataset.isEmpty() || dataset.values().stream().mapToDouble(Double::doubleValue).sum() <= 0) {
            drawEmptyPlaceholder(g2, width, height);
            return;
        }

        double total = dataset.values().stream().mapToDouble(Double::doubleValue).sum();

        int legendWidth = 175;
        int chartAreaWidth = width - legendWidth - padding.left - padding.right;
        int chartAreaHeight = height - padding.top - padding.bottom - 45;

        int diameter = Math.min(chartAreaWidth, chartAreaHeight) - 20;
        if (diameter <= 30) return;

        int centerX = padding.left + (chartAreaWidth / 2);
        int centerY = padding.top + 35 + (chartAreaHeight / 2);
        int radius = diameter / 2;

        double currentAngle = 90.0;
        int colorIdx = 0;

        for (Map.Entry<String, Double> entry : dataset.entrySet()) {
            double val = entry.getValue();
            double sliceAngle = (val / total) * 360.0;

            Color sliceColor = customColors.getOrDefault(entry.getKey(),
                    ThemeConstants.CHART_PALETTE[colorIdx % ThemeConstants.CHART_PALETTE.length]);

            g2.setColor(sliceColor);
            Arc2D.Double arc = new Arc2D.Double(
                    centerX - radius, centerY - radius,
                    diameter, diameter,
                    currentAngle, -sliceAngle, Arc2D.PIE
            );
            g2.fill(arc);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(arc);

            currentAngle -= sliceAngle;
            colorIdx++;
        }

        if (isDonut) {
            int innerDiameter = (int) (diameter * 0.56);
            int innerRadius = innerDiameter / 2;
            g2.setColor(ThemeConstants.BG_CARD);
            g2.fill(new Ellipse2D.Double(centerX - innerRadius, centerY - innerRadius, innerDiameter, innerDiameter));

            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(ThemeConstants.TEXT_DARK);
            String totalStr = total >= 1000 ? String.format("%,.0f", total) : String.valueOf((long) total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalStr, centerX - (fm.stringWidth(totalStr) / 2), centerY + 4);

            g2.setFont(ThemeConstants.FONT_SMALL);
            g2.setColor(ThemeConstants.TEXT_MUTED);
            String sub = "Total";
            FontMetrics fmSub = g2.getFontMetrics();
            g2.drawString(sub, centerX - (fmSub.stringWidth(sub) / 2), centerY + 18);
        }

        int legendX = width - legendWidth - padding.right + 10;
        int legendY = padding.top + 55;
        colorIdx = 0;

        for (Map.Entry<String, Double> entry : dataset.entrySet()) {
            Color itemColor = customColors.getOrDefault(entry.getKey(),
                    ThemeConstants.CHART_PALETTE[colorIdx % ThemeConstants.CHART_PALETTE.length]);

            g2.setColor(itemColor);
            g2.fill(new RoundRectangle2D.Float(legendX, legendY, 12, 12, 4, 4));

            g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
            g2.setColor(ThemeConstants.TEXT_DARK);
            double pct = (entry.getValue() / total) * 100.0;
            String text = entry.getKey() + " (" + String.format("%.1f%%", pct) + ")";
            g2.drawString(text, legendX + 18, legendY + 10);

            legendY += 28;
            colorIdx++;
        }
    }

    private void drawEmptyPlaceholder(Graphics2D g2, int width, int height) {
        g2.setFont(ThemeConstants.FONT_BODY);
        g2.setColor(ThemeConstants.TEXT_MUTED);
        String msg = "No distribution data available";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
    }
}
