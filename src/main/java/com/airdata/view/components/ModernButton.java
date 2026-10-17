package com.airdata.view.components;

import com.airdata.util.ThemeConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern custom JButton with smooth rounded corners, flat styling, and hover feedback.
 */
public class ModernButton extends JButton {

    public enum ButtonStyle {
        PRIMARY(ThemeConstants.ACCENT, ThemeConstants.ACCENT_HOVER, Color.WHITE),
        SECONDARY(new Color(241, 245, 249), new Color(226, 232, 240), ThemeConstants.TEXT_DARK),
        SUCCESS(ThemeConstants.STATUS_ON_TIME, new Color(5, 150, 105), Color.WHITE),
        DANGER(ThemeConstants.STATUS_CANCELLED, new Color(220, 38, 38), Color.WHITE);

        final Color normalBg;
        final Color hoverBg;
        final Color textFg;

        ButtonStyle(Color normalBg, Color hoverBg, Color textFg) {
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.textFg = textFg;
        }
    }

    private final ButtonStyle style;
    private boolean isHovered = false;
    private int cornerRadius = 8;

    public ModernButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public ModernButton(String text, ButtonStyle style) {
        super(text);
        this.style = (style != null) ? style : ButtonStyle.PRIMARY;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(ThemeConstants.FONT_BODY_BOLD);
        setForeground(this.style.textFg);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = isHovered ? style.hoverBg : style.normalBg;
        if (!isEnabled()) {
            bg = new Color(203, 213, 225);
            g2.setColor(bg);
        } else {
            g2.setColor(bg);
        }

        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Subtle border for secondary style
        if (style == ButtonStyle.SECONDARY) {
            g2.setColor(ThemeConstants.BORDER_COLOR);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
