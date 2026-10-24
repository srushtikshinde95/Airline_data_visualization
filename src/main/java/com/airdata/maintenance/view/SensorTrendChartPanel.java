package com.airdata.maintenance.view;

import com.airdata.maintenance.model.TrendForecast;
import com.airdata.util.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Custom 2D vector chart rendering historical telemetry points,
 * forecasted trajectory, shaded 90% confidence interval, and critical safety thresholds.
 */
public class SensorTrendChartPanel extends JPanel {

    private TrendForecast forecast;

    public SensorTrendChartPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
    }

    public void setForecast(TrendForecast forecast) {
        this.forecast = forecast;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (forecast == null || forecast.getHistoricalValues().isEmpty()) {
            g2.setColor(ThemeConstants.TEXT_MUTED);
            g2.setFont(ThemeConstants.FONT_SUBTITLE);
            String msg = "Select an Aircraft and Component to view Time-Series Trend Forecast";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int padLeft = 65;
        int padRight = 30;
        int padTop = 45;
        int padBottom = 45;

        int plotW = w - padLeft - padRight;
        int plotH = h - padTop - padBottom;

        List<Integer> hCycles = forecast.getHistoricalCycles();
        List<Double> hVals = forecast.getHistoricalValues();
        List<Integer> fCycles = forecast.getForecastCycles();
        List<Double> fVals = forecast.getForecastValues();
        List<Double> uBounds = forecast.getUpperConfidence();
        List<Double> lBounds = forecast.getLowerConfidence();
        double threshold = forecast.getCriticalThreshold();

        // Determine Min / Max ranges
        int minCycle = hCycles.get(0);
        int maxCycle = fCycles.isEmpty() ? hCycles.get(hCycles.size() - 1) : fCycles.get(fCycles.size() - 1);

        double minVal = threshold;
        double maxVal = threshold;

        for (double v : hVals) {
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        for (double v : fVals) {
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        for (double v : uBounds) maxVal = Math.max(maxVal, v);
        for (double v : lBounds) minVal = Math.min(minVal, v);

        double valMargin = Math.max(2.0, (maxVal - minVal) * 0.12);
        minVal -= valMargin;
        maxVal += valMargin;

        // Draw Chart Header
        g2.setColor(ThemeConstants.TEXT_DARK);
        g2.setFont(ThemeConstants.FONT_SUBTITLE);
        String title = String.format("%s Trend & Holt-Linear Forecast (%s [%s])",
                forecast.getSensorDisplayName(), forecast.getAircraftId(), forecast.getComponent().getDisplayName());
        g2.drawString(title, padLeft, 26);

        // Subtitle / Breach Warning
        g2.setFont(ThemeConstants.FONT_SMALL);
        if (forecast.isThresholdBreached()) {
            g2.setColor(new Color(220, 38, 38));
            g2.drawString(String.format("SAFETY ALERT: Forecast breaches critical limit (%.1f %s) in ~%d cycles!",
                    threshold, forecast.getUnit(), forecast.getEstimatedCyclesToBreach()), padLeft, 40);
        } else {
            g2.setColor(new Color(16, 185, 129));
            g2.drawString(String.format("STABLE: Forecast remains within safety margins over %d cycle horizon (Threshold: %.1f %s)",
                    fCycles.size(), threshold, forecast.getUnit()), padLeft, 40);
        }

        // Draw Grid Lines & Y-Axis Labels
        g2.setFont(ThemeConstants.FONT_SMALL);
        int gridSteps = 5;
        for (int i = 0; i <= gridSteps; i++) {
            double ratio = (double) i / gridSteps;
            int y = (int) (padTop + plotH * (1.0 - ratio));
            double val = minVal + ratio * (maxVal - minVal);

            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(padLeft, y, padLeft + plotW, y);

            g2.setColor(ThemeConstants.TEXT_MUTED);
            String label = String.format("%.1f", val);
            g2.drawString(label, padLeft - g2.getFontMetrics().stringWidth(label) - 8, y + 4);
        }

        // Draw Critical Threshold Line
        int yThresh = (int) (padTop + plotH * (1.0 - (threshold - minVal) / (maxVal - minVal)));
        if (yThresh >= padTop && yThresh <= padTop + plotH) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 4.0f}, 0.0f));
            g2.setColor(new Color(239, 68, 68));
            g2.drawLine(padLeft, yThresh, padLeft + plotW, yThresh);
            g2.setStroke(oldStroke);

            g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
            String thText = "Critical Safety Threshold: " + String.format("%.1f %s", threshold, forecast.getUnit());
            g2.drawString(thText, padLeft + plotW - g2.getFontMetrics().stringWidth(thText) - 10, yThresh - 6);
        }

        final double finalMinVal = minVal;
        final double finalMaxVal = maxVal;
        final int finalMinCycle = minCycle;
        final int finalMaxCycle = maxCycle;

        // Helper lambda for coordinate mapping
        CoordMapper mapper = (c, v) -> {
            double xRatio = (double) (c - finalMinCycle) / Math.max(1, (finalMaxCycle - finalMinCycle));
            double yRatio = (v - finalMinVal) / (finalMaxVal - finalMinVal);
            int px = (int) (padLeft + xRatio * plotW);
            int py = (int) (padTop + (1.0 - yRatio) * plotH);
            return new Point(px, py);
        };

        // Draw Confidence Band (Forecast)
        if (!fVals.isEmpty() && !uBounds.isEmpty() && !lBounds.isEmpty()) {
            Path2D.Double band = new Path2D.Double();
            // Start from last historical point
            Point lastHist = mapper.map(hCycles.get(hCycles.size() - 1), hVals.get(hVals.size() - 1));
            band.moveTo(lastHist.x, lastHist.y);

            for (int i = 0; i < fCycles.size(); i++) {
                Point p = mapper.map(fCycles.get(i), uBounds.get(i));
                band.lineTo(p.x, p.y);
            }
            for (int i = fCycles.size() - 1; i >= 0; i--) {
                Point p = mapper.map(fCycles.get(i), lBounds.get(i));
                band.lineTo(p.x, p.y);
            }
            band.closePath();

            g2.setColor(new Color(14, 116, 214, 35)); // Soft blue transparent fill
            g2.fill(band);
        }

        // Draw Historical Trajectory (Solid Blue Line)
        Path2D.Double histPath = new Path2D.Double();
        for (int i = 0; i < hCycles.size(); i++) {
            Point p = mapper.map(hCycles.get(i), hVals.get(i));
            if (i == 0) histPath.moveTo(p.x, p.y);
            else histPath.lineTo(p.x, p.y);
        }
        g2.setColor(new Color(14, 116, 214));
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(histPath);

        // Draw Forecast Line (Dashed Orange Line)
        if (!fVals.isEmpty()) {
            Path2D.Double fPath = new Path2D.Double();
            Point lastHist = mapper.map(hCycles.get(hCycles.size() - 1), hVals.get(hVals.size() - 1));
            fPath.moveTo(lastHist.x, lastHist.y);

            for (int i = 0; i < fCycles.size(); i++) {
                Point p = mapper.map(fCycles.get(i), fVals.get(i));
                fPath.lineTo(p.x, p.y);
            }

            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f, 4.0f}, 0.0f));
            g2.setColor(new Color(249, 115, 22)); // Vibrant Orange
            g2.draw(fPath);

            // Draw Forecast Points
            g2.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < fCycles.size(); i++) {
                Point p = mapper.map(fCycles.get(i), fVals.get(i));
                g2.setColor(Color.WHITE);
                g2.fillOval(p.x - 3, p.y - 3, 6, 6);
                g2.setColor(new Color(249, 115, 22));
                g2.drawOval(p.x - 3, p.y - 3, 6, 6);
            }
        }

        // Draw Historical Point Dots
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < hCycles.size(); i++) {
            Point p = mapper.map(hCycles.get(i), hVals.get(i));
            g2.setColor(Color.WHITE);
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);
            g2.setColor(new Color(14, 116, 214));
            g2.drawOval(p.x - 3, p.y - 3, 6, 6);
        }

        // X-Axis Labels (Flight Cycles)
        g2.setColor(ThemeConstants.TEXT_MUTED);
        g2.setFont(ThemeConstants.FONT_SMALL);
        int cycleStep = Math.max(1, (maxCycle - minCycle) / 8);
        for (int c = minCycle; c <= maxCycle; c += cycleStep) {
            Point p = mapper.map(c, minVal);
            g2.drawLine(p.x, padTop + plotH, p.x, padTop + plotH + 4);
            String cLabel = "C-" + c;
            g2.drawString(cLabel, p.x - g2.getFontMetrics().stringWidth(cLabel) / 2, padTop + plotH + 18);
        }

        // Chart Legend
        drawLegend(g2, padLeft, padTop + plotH + 34);

        g2.dispose();
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        g2.setFont(ThemeConstants.FONT_SMALL);

        // Historical
        g2.setColor(new Color(14, 116, 214));
        g2.drawLine(x, y - 4, x + 16, y - 4);
        g2.fillOval(x + 5, y - 7, 6, 6);
        g2.setColor(ThemeConstants.TEXT_DARK);
        g2.drawString("Historical Telemetry", x + 22, y);

        // Forecast
        int x2 = x + 160;
        g2.setColor(new Color(249, 115, 22));
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 3.0f}, 0.0f));
        g2.drawLine(x2, y - 4, x2 + 16, y - 4);
        g2.setStroke(old);
        g2.setColor(ThemeConstants.TEXT_DARK);
        g2.drawString("Holt-Linear Projection", x2 + 22, y);

        // 90% CI
        int x3 = x2 + 170;
        g2.setColor(new Color(14, 116, 214, 60));
        g2.fillRect(x3, y - 9, 16, 10);
        g2.setColor(ThemeConstants.TEXT_DARK);
        g2.drawString("90% Prediction Interval", x3 + 22, y);
    }

    @FunctionalInterface
    private interface CoordMapper {
        Point map(int cycle, double val);
    }
}
