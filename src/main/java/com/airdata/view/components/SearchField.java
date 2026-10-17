package com.airdata.view.components;

import com.airdata.util.ThemeConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern text field with placeholder hint and rounded styling.
 */
public class SearchField extends JTextField {

    private final String placeholder;
    private boolean isFocused = false;

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setFont(ThemeConstants.FONT_BODY);
        setForeground(ThemeConstants.TEXT_DARK);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setPreferredSize(new Dimension(220, 36));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

        // Border
        g2.setColor(isFocused ? ThemeConstants.ACCENT : ThemeConstants.BORDER_COLOR);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));

        // Placeholder
        if (getText().isEmpty() && !isFocused) {
            g2.setColor(ThemeConstants.TEXT_MUTED);
            g2.setFont(ThemeConstants.FONT_BODY);
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, 12, y);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
