package com.airdata.view.chart;

import com.airdata.util.ThemeConstants;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModernLineChart extends AbstractChartComponent {

    private final Map<String, Double> dataset = new LinkedHashMap<>();
    private final Color lineColor = ThemeConstants.ACCENT;

    private int hoveredPointIndex = -1;
    private Point mousePoint = null;

    public ModernLineChart(String title, String subtitle) {
        super(title, subtitle);
        this.padding = new Insets(20, 24, 60, 24);

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                checkHover(e.getPoint());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredPointIndex = -1;
                mousePoint = null;
                repaint();
            }
        });
    }

    public void setData(Map<String, ? extends Number> rawData) {
        this.dataset.clear();
        if (rawData != null) {
            rawData.forEach((k, v) -> dataset.put(k, v.doubleValue()));
        }
        this.hoveredPointIndex = -1;
        repaint();
    }

    private void checkHover(Point p) {
        if (dataset.isEmpty()) return;

        int startX = padding.left + 50;
        int endX = getWidth() - padding.right - 20;
        int startY = padding.top + 45;
        int endY = getHeight() - padding.bottom - 20;

        int plotWidth = endX - startX;
        int plotHeight = endY - startY;
        if (plotWidth <= 0 || plotHeight <= 0) return;

        double maxVal = dataset.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxVal <= 0) maxVal = 1.0;

        int pointCount = dataset.size();
        int stepX = pointCount > 1 ? plotWidth / (pointCount - 1) : plotWidth / 2;

        int oldHover = hoveredPointIndex;
        hoveredPointIndex = -1;

        List<String> keys = new ArrayList<>(dataset.keySet());
        for (int i = 0; i < keys.size(); i++) {
            double val = dataset.get(keys.get(i));
            int x = pointCount > 1 ? startX + (i * stepX) : startX + (plotWidth / 2);
            int y = endY - (int) ((val / maxVal) * plotHeight);

            if (p.distance(x, y) <= 12) {
                hoveredPointIndex = i;
                break;
            }
        }

        if (oldHover != hoveredPointIndex) {
            repaint();
        }
    }

    @Override
    public void renderChart(Graphics2D g2, int width, int height) {
        if (dataset.isEmpty()) {
            drawEmptyPlaceholder(g2, width, height);
            return;
        }

        int startX = padding.left + 50;
        int endX = width - padding.right - 20;
        int startY = padding.top + 45;
        int endY = height - padding.bottom - 20;

        int plotWidth = endX - startX;
        int plotHeight = endY - startY;
        if (plotWidth <= 0 || plotHeight <= 0) return;

        double maxVal = dataset.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxVal <= 0) maxVal = 1.0;

        int gridLines = 4;
        g2.setFont(ThemeConstants.FONT_SMALL);
        for (int i = 0; i <= gridLines; i++) {
            int y = endY - (i * plotHeight / gridLines);
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(startX, y, endX, y);

            double val = (maxVal / gridLines) * i;
            g2.setColor(ThemeConstants.TEXT_MUTED);
            String label = val >= 1000 ? String.format("%.0fK", val / 1000) : String.valueOf((long) val);
            g2.drawString(label, startX - 45, y + 4);
        }

        List<Point> points = new ArrayList<>();
        List<String> keys = new ArrayList<>(dataset.keySet());
        int pointCount = dataset.size();
        int stepX = pointCount > 1 ? plotWidth / (pointCount - 1) : plotWidth / 2;

        for (int idx = 0; idx < keys.size(); idx++) {
            String key = keys.get(idx);
            double val = dataset.get(key);
            int x = pointCount > 1 ? startX + (idx * stepX) : startX + (plotWidth / 2);
            int y = endY - (int) ((val / maxVal) * plotHeight);
            points.add(new Point(x, y));

            drawRotatedLabel(g2, key, x, endY + 12);
        }

        if (points.size() > 1) {
            GeneralPath areaPath = new GeneralPath();
            areaPath.moveTo(points.get(0).x, endY);
            for (Point p : points) {
                areaPath.lineTo(p.x, p.y);
            }
            areaPath.lineTo(points.get(points.size() - 1).x, endY);
            areaPath.closePath();

            Color startCol = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 60);
            Color endCol = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 0);
            g2.setPaint(new GradientPaint(0, startY, startCol, 0, endY, endCol));
            g2.fill(areaPath);

            GeneralPath linePath = new GeneralPath();
            linePath.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                linePath.lineTo(points.get(i).x, points.get(i).y);
            }
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linePath);
        }

        String hoveredKey = null;
        double hoveredValue = 0;

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            double val = dataset.get(keys.get(i));
            boolean isHovered = (i == hoveredPointIndex);

            if (isHovered) {
                hoveredKey = keys.get(i);
                hoveredValue = val;
            }

            int pointRadius = isHovered ? 7 : 5;
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(p.x - pointRadius, p.y - pointRadius, pointRadius * 2, pointRadius * 2));
            g2.setColor(isHovered ? ThemeConstants.STATUS_ON_TIME : lineColor);
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(new Ellipse2D.Double(p.x - pointRadius, p.y - pointRadius, pointRadius * 2, pointRadius * 2));

            g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
            g2.setColor(ThemeConstants.TEXT_DARK);
            String valStr = val >= 1000 ? String.format("%.0fK", val / 1000) : String.valueOf((long) val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, p.x - (fm.stringWidth(valStr) / 2), p.y - 8);
        }

        if (hoveredKey != null && mousePoint != null) {
            drawTooltip(g2, hoveredKey, hoveredValue, mousePoint.x, mousePoint.y);
        }
    }

    private void drawRotatedLabel(Graphics2D g2, String text, int x, int y) {
        Graphics2D g2d = (Graphics2D) g2.create();
        g2d.setFont(ThemeConstants.FONT_SMALL);
        g2d.setColor(ThemeConstants.TEXT_MUTED);

        String display = text.length() > 5 ? text.substring(5) : text;

        AffineTransform orig = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(-Math.PI / 4.0);

        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(display, -fm.stringWidth(display) + 4, fm.getAscent());
        g2d.setTransform(orig);
        g2d.dispose();
    }

    private void drawTooltip(Graphics2D g2, String key, double value, int mouseX, int mouseY) {
        String line1 = "Date: " + key;
        String line2 = "Metric: " + (value >= 1000 ? String.format("%,.0f", value) : String.valueOf((long) value));

        g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
        FontMetrics fm = g2.getFontMetrics();
        int textW = Math.max(fm.stringWidth(line1), fm.stringWidth(line2));
        int boxW = textW + 20;
        int boxH = 46;

        int tooltipX = Math.min(mouseX + 12, getWidth() - boxW - 10);
        int tooltipY = Math.max(mouseY - boxH - 6, 10);

        g2.setColor(new Color(15, 23, 42, 230));
        g2.fill(new RoundRectangle2D.Float(tooltipX, tooltipY, boxW, boxH, 8, 8));

        g2.setColor(Color.WHITE);
        g2.drawString(line1, tooltipX + 10, tooltipY + 18);

        g2.setFont(ThemeConstants.FONT_SMALL);
        g2.setColor(new Color(147, 197, 253));
        g2.drawString(line2, tooltipX + 10, tooltipY + 36);
    }

    private void drawEmptyPlaceholder(Graphics2D g2, int width, int height) {
        g2.setFont(ThemeConstants.FONT_BODY);
        g2.setColor(ThemeConstants.TEXT_MUTED);
        String msg = "No trend data available for selected criteria";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
    }
}
