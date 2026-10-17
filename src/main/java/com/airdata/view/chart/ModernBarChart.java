package com.airdata.view.chart;

import com.airdata.util.FormatUtils;
import com.airdata.util.ThemeConstants;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModernBarChart extends AbstractChartComponent {

    public enum ValueFormat {
        NUMBER, CURRENCY, MINUTES
    }

    private final Map<String, Double> dataset = new LinkedHashMap<>();
    private ValueFormat valueFormat = ValueFormat.NUMBER;

    private int hoveredBarIndex = -1;
    private Point mousePoint = null;

    public ModernBarChart(String title, String subtitle) {
        super(title, subtitle);
        this.padding = new Insets(20, 24, 75, 24);

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
                hoveredBarIndex = -1;
                mousePoint = null;
                repaint();
            }
        });
    }

    public void setData(Map<String, ? extends Number> rawData, ValueFormat format) {
        this.dataset.clear();
        if (rawData != null) {
            rawData.forEach((k, v) -> dataset.put(k, v.doubleValue()));
        }
        this.valueFormat = (format != null) ? format : ValueFormat.NUMBER;
        this.hoveredBarIndex = -1;
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

        int barCount = dataset.size();
        int availableBarSpace = plotWidth / barCount;
        int barWidth = Math.min(Math.max(16, availableBarSpace - 14), 48);

        int oldHover = hoveredBarIndex;
        hoveredBarIndex = -1;

        int index = 0;
        for (Map.Entry<String, Double> entry : dataset.entrySet()) {
            double val = entry.getValue();
            int barHeight = (int) ((val / maxVal) * plotHeight);
            int x = startX + (index * availableBarSpace) + (availableBarSpace - barWidth) / 2;
            int y = endY - barHeight;

            Rectangle barRect = new Rectangle(x, y, barWidth, barHeight);
            if (barRect.contains(p)) {
                hoveredBarIndex = index;
                break;
            }
            index++;
        }

        if (oldHover != hoveredBarIndex) {
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
            String label = formatValue(val);
            g2.setColor(ThemeConstants.TEXT_MUTED);
            g2.drawString(label, startX - 45, y + 4);
        }

        int barCount = dataset.size();
        int availableBarSpace = plotWidth / barCount;
        int barWidth = Math.min(Math.max(16, availableBarSpace - 14), 48);

        List<String> keys = new ArrayList<>(dataset.keySet());
        String hoveredKey = null;
        double hoveredValue = 0;

        for (int index = 0; index < keys.size(); index++) {
            String key = keys.get(index);
            double val = dataset.get(key);
            int barHeight = Math.max(4, (int) ((val / maxVal) * plotHeight));
            int x = startX + (index * availableBarSpace) + (availableBarSpace - barWidth) / 2;
            int y = endY - barHeight;

            boolean isHovered = (index == hoveredBarIndex);
            if (isHovered) {
                hoveredKey = key;
                hoveredValue = val;
            }

            Color barColor = ThemeConstants.CHART_PALETTE[index % ThemeConstants.CHART_PALETTE.length];
            if (isHovered) {
                barColor = barColor.brighter();
            }

            GradientPaint gp = new GradientPaint(x, y, barColor, x, endY, barColor.darker());
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

            if (isHovered) {
                g2.setColor(ThemeConstants.TEXT_DARK);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));
            }

            g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
            g2.setColor(ThemeConstants.TEXT_DARK);
            String valStr = formatValue(val);
            FontMetrics fm = g2.getFontMetrics();
            int valX = x + (barWidth - fm.stringWidth(valStr)) / 2;
            g2.drawString(valStr, valX, Math.max(startY - 2, y - 6));

            drawRotatedLabel(g2, key, x + (barWidth / 2), endY + 12);
        }

        if (hoveredKey != null && mousePoint != null) {
            drawTooltip(g2, hoveredKey, hoveredValue, mousePoint.x, mousePoint.y);
        }
    }

    private void drawRotatedLabel(Graphics2D g2, String text, int x, int y) {
        Graphics2D g2d = (Graphics2D) g2.create();
        g2d.setFont(ThemeConstants.FONT_SMALL);
        g2d.setColor(ThemeConstants.TEXT_DARK);

        String display = text.length() > 14 ? text.substring(0, 12) + ".." : text;

        AffineTransform orig = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(-Math.PI / 4.0);

        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(display, -fm.stringWidth(display) + 4, fm.getAscent());
        g2d.setTransform(orig);
        g2d.dispose();
    }

    private void drawTooltip(Graphics2D g2, String key, double value, int mouseX, int mouseY) {
        String line1 = key;
        String line2 = "Value: " + formatValue(value);

        g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
        FontMetrics fm = g2.getFontMetrics();
        int textW = Math.max(fm.stringWidth(line1), fm.stringWidth(line2));
        int boxW = textW + 20;
        int boxH = 46;

        int tooltipX = Math.min(mouseX + 12, getWidth() - boxW - 10);
        int tooltipY = Math.max(mouseY - boxH - 6, 10);

        g2.setColor(new Color(15, 23, 42, 230));
        g2.fill(new RoundRectangle2D.Float(tooltipX, tooltipY, boxW, boxH, 8, 8));

        g2.setColor(new Color(255, 255, 255, 240));
        g2.drawString(line1, tooltipX + 10, tooltipY + 18);

        g2.setFont(ThemeConstants.FONT_SMALL);
        g2.setColor(new Color(147, 197, 253));
        g2.drawString(line2, tooltipX + 10, tooltipY + 36);
    }

    private String formatValue(double val) {
        switch (valueFormat) {
            case CURRENCY:
                if (val >= 1_000_000) return String.format("$%.1fM", val / 1_000_000);
                if (val >= 1_000) return String.format("$%.0fK", val / 1_000);
                return "$" + (int) val;
            case MINUTES:
                return (int) val + "m";
            case NUMBER:
            default:
                if (val >= 1_000) return String.format("%.0fK", val / 1_000);
                return String.valueOf((long) val);
        }
    }

    private void drawEmptyPlaceholder(Graphics2D g2, int width, int height) {
        g2.setFont(ThemeConstants.FONT_BODY);
        g2.setColor(ThemeConstants.TEXT_MUTED);
        String msg = "No data available for the current filter criteria";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
    }
}
