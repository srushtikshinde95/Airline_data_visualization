package com.airdata.view.components;

import com.airdata.util.ThemeConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;
    private final Icon icon;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this(title, initialValue, subtitle, accentColor, null);
    }

    public StatCard(String title, String initialValue, String subtitle, Color accentColor, VectorIcons.IconType iconType) {
        this.accentColor = (accentColor != null) ? accentColor : ThemeConstants.ACCENT;
        this.icon = (iconType != null) ? VectorIcons.createIcon(iconType, 20, this.accentColor) : null;

        setLayout(new BorderLayout(8, 6));
        setBackground(ThemeConstants.BG_CARD);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
        titleLabel.setForeground(ThemeConstants.TEXT_MUTED);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(ThemeConstants.FONT_STAT_VALUE);
        valueLabel.setForeground(ThemeConstants.TEXT_DARK);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(ThemeConstants.FONT_SMALL);
        subtitleLabel.setForeground(ThemeConstants.TEXT_MUTED);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleLabel, BorderLayout.WEST);
        if (icon != null) {
            JLabel iconLbl = new JLabel(icon);
            topRow.add(iconLbl, BorderLayout.EAST);
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(topRow);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(subtitleLabel);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);

        g2.setColor(ThemeConstants.BORDER_COLOR);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        g2.dispose();
    }
}
