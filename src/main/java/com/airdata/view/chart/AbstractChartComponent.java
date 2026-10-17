package com.airdata.view.chart;

import com.airdata.util.ThemeConstants;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Abstract Base Class for all custom charts.
 * Demonstrates:
 * 1. Abstraction & Template Method Pattern
 * 2. Encapsulation of common chart styling, margins, and exporting
 * 3. Inheritance (Subclassed by ModernBarChart, ModernPieChart, ModernLineChart, ModernComparisonChart)
 */
public abstract class AbstractChartComponent extends JPanel implements IChartComponent {

    protected String chartTitle;
    protected String subtitle;
    protected Insets padding = new Insets(20, 20, 20, 20);

    public AbstractChartComponent(String chartTitle) {
        this(chartTitle, "");
    }

    public AbstractChartComponent(String chartTitle, String subtitle) {
        this.chartTitle = chartTitle;
        this.subtitle = subtitle;
        setBackground(ThemeConstants.BG_CARD);
        setOpaque(true);
    }

    @Override
    public String getChartTitle() {
        return chartTitle;
    }

    @Override
    public void setChartTitle(String title) {
        this.chartTitle = title;
        repaint();
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        repaint();
    }

    @Override
    public void refresh() {
        repaint();
    }

    @Override
    public void exportChartAsImage(File targetFile) throws IOException {
        int w = Math.max(getWidth(), 800);
        int h = Math.max(getHeight(), 500);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        paint(g2);
        g2.dispose();
        ImageIO.write(image, "png", targetFile);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable crisp Anti-Aliasing and text rendering hints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw Card Background and Border
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.setColor(ThemeConstants.BORDER_COLOR);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        // Draw Title & Subtitle Header
        int currentY = padding.top + 10;
        if (chartTitle != null && !chartTitle.isEmpty()) {
            g2.setFont(ThemeConstants.FONT_TITLE);
            g2.setColor(ThemeConstants.TEXT_DARK);
            g2.drawString(chartTitle, padding.left + 10, currentY);
            currentY += 18;
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            g2.setFont(ThemeConstants.FONT_SMALL);
            g2.setColor(ThemeConstants.TEXT_MUTED);
            g2.drawString(subtitle, padding.left + 10, currentY);
        }

        // Delegate content rendering to concrete subclass (Template Method / Polymorphism)
        renderChart(g2, getWidth(), getHeight());

        g2.dispose();
    }
}
