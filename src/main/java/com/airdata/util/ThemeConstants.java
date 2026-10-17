package com.airdata.util;

import java.awt.Color;
import java.awt.Font;

/**
 * UI Theme Constants providing colors, fonts, and styling parameters for the desktop GUI.
 */
public final class ThemeConstants {

    // Primary Colors
    public static final Color PRIMARY = new Color(24, 43, 73);         // Deep Navy Blue
    public static final Color PRIMARY_LIGHT = new Color(41, 72, 125);   // Lighter Navy
    public static final Color ACCENT = new Color(14, 116, 214);         // Electric Blue Accent
    public static final Color ACCENT_HOVER = new Color(9, 92, 173);

    // Neutral / Background Colors
    public static final Color BG_MAIN = new Color(245, 247, 250);       // Soft Clean Gray/White
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(18, 32, 54);       // Dark Slate
    public static final Color BG_SIDEBAR_HOVER = new Color(30, 52, 86);
    public static final Color BG_SIDEBAR_SELECTED = new Color(14, 116, 214);
    public static final Color BORDER_COLOR = new Color(226, 232, 240);  // Subtle Gray border

    // Text Colors
    public static final Color TEXT_DARK = new Color(15, 23, 42);        // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);
    public static final Color TEXT_SIDEBAR = new Color(203, 213, 225);

    // Status Colors (Badges & Charts)
    public static final Color STATUS_ON_TIME = new Color(16, 185, 129);      // Emerald Green
    public static final Color STATUS_ON_TIME_BG = new Color(236, 253, 245);
    public static final Color STATUS_DELAYED = new Color(245, 158, 11);       // Amber
    public static final Color STATUS_DELAYED_BG = new Color(254, 243, 199);
    public static final Color STATUS_CANCELLED = new Color(239, 68, 68);      // Crimson Red
    public static final Color STATUS_CANCELLED_BG = new Color(254, 242, 242);

    // Chart Palette (Distinguishable categorical series colors)
    public static final Color[] CHART_PALETTE = {
        new Color(14, 116, 214),   // Blue
        new Color(16, 185, 129),   // Emerald
        new Color(245, 158, 11),   // Amber
        new Color(139, 92, 246),   // Purple
        new Color(236, 72, 153),   // Pink
        new Color(6, 182, 212),    // Cyan
        new Color(249, 115, 22),   // Orange
        new Color(99, 102, 241),   // Indigo
        new Color(168, 85, 247),   // Violet
        new Color(20, 184, 166)    // Teal
    };

    // Typography
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD, 20);

    private ThemeConstants() {
        // Prevent instantiation
    }
}
