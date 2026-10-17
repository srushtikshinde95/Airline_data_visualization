package com.airdata.view.chart;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

/**
 * Interface defining contract for visual charting components.
 * Demonstrates:
 * 1. Abstraction (Common visual contract for all chart types)
 * 2. Polymorphism (Enables interchangeable rendering of Bar, Pie, Line, and Comparison charts)
 */
public interface IChartComponent {
    String getChartTitle();
    void setChartTitle(String title);
    void renderChart(Graphics2D g2, int width, int height);
    void exportChartAsImage(File targetFile) throws IOException;
    void refresh();
}
