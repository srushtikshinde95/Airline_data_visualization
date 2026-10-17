package com.airdata.view.components;

import com.airdata.model.FlightStatus;
import com.airdata.util.ThemeConstants;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom TableCellRenderer that renders FlightStatus as a badge chip.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value instanceof FlightStatus) {
            FlightStatus status = (FlightStatus) value;
            return new BadgeComponent(status.getLabel(), status.getForegroundColor(), status.getBackgroundColor());
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    private static class BadgeComponent extends JPanel {
        private final String label;
        private final Color fgColor;
        private final Color bgColor;

        BadgeComponent(String label, Color fgColor, Color bgColor) {
            this.label = label;
            this.fgColor = fgColor;
            this.bgColor = bgColor;
            setOpaque(false);
            setPreferredSize(new Dimension(85, 26));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = Math.min(getWidth() - 8, 90);
            int height = 20;
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;

            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(x, y, width, height, 10, 10));

            g2.setColor(fgColor);
            g2.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (width - fm.stringWidth(label)) / 2;
            int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(label, textX, textY);

            g2.dispose();
        }
    }
}
