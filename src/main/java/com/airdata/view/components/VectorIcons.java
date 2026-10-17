package com.airdata.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Scalable Vector Icon Renderer for modern desktop GUI.
 */
public class VectorIcons {

    public enum IconType {
        AIRPLANE,
        DASHBOARD,
        DATASET,
        VISUALIZATION,
        ANALYTICS,
        ABOUT,
        IMPORT,
        EXPORT,
        ADD,
        EDIT,
        DELETE,
        SEARCH,
        FILTER,
        RESET,
        PASSENGER,
        REVENUE,
        AIRPORT,
        CLOCK,
        CHECK,
        ALERT
    }

    public static Icon createIcon(IconType type, int size, Color color) {
        return new VectorIcon(type, size, color);
    }

    private static class VectorIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        VectorIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);

            float s = (float) size / 24.0f;
            g2.scale(s, s);

            g2.setColor(color != null ? color : Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case AIRPLANE:
                    Path2D.Float plane = new Path2D.Float();
                    plane.moveTo(12, 2);
                    plane.lineTo(15, 9);
                    plane.lineTo(22, 11);
                    plane.lineTo(22, 13);
                    plane.lineTo(15, 14);
                    plane.lineTo(14, 20);
                    plane.lineTo(17, 22);
                    plane.lineTo(17, 23);
                    plane.lineTo(12, 22);
                    plane.lineTo(7, 23);
                    plane.lineTo(7, 22);
                    plane.lineTo(10, 20);
                    plane.lineTo(9, 14);
                    plane.lineTo(2, 13);
                    plane.lineTo(2, 11);
                    plane.lineTo(9, 9);
                    plane.closePath();
                    g2.fill(plane);
                    break;

                case DASHBOARD:
                    g2.fill(new RoundRectangle2D.Float(3, 3, 7.5f, 7.5f, 2, 2));
                    g2.fill(new RoundRectangle2D.Float(13.5f, 3, 7.5f, 7.5f, 2, 2));
                    g2.fill(new RoundRectangle2D.Float(3, 13.5f, 7.5f, 7.5f, 2, 2));
                    g2.fill(new RoundRectangle2D.Float(13.5f, 13.5f, 7.5f, 7.5f, 2, 2));
                    break;

                case DATASET:
                    g2.draw(new RoundRectangle2D.Float(3, 4, 18, 16, 3, 3));
                    g2.drawLine(3, 9, 21, 9);
                    g2.drawLine(9, 4, 9, 20);
                    g2.drawLine(15, 4, 15, 20);
                    break;

                case VISUALIZATION:
                    g2.fill(new RoundRectangle2D.Float(4, 12, 3.5f, 8, 1, 1));
                    g2.fill(new RoundRectangle2D.Float(10, 6, 3.5f, 14, 1, 1));
                    g2.fill(new RoundRectangle2D.Float(16, 2, 3.5f, 18, 1, 1));
                    g2.drawLine(2, 21, 22, 21);
                    break;

                case ANALYTICS:
                    g2.drawLine(2, 21, 22, 21);
                    Path2D.Float line = new Path2D.Float();
                    line.moveTo(4, 16);
                    line.lineTo(9, 10);
                    line.lineTo(15, 13);
                    line.lineTo(20, 5);
                    g2.draw(line);
                    g2.fill(new Ellipse2D.Float(18.5f, 3.5f, 3, 3));
                    break;

                case ABOUT:
                    g2.draw(new Ellipse2D.Float(3, 3, 18, 18));
                    g2.fill(new Ellipse2D.Float(11, 7, 2, 2));
                    g2.drawLine(12, 11, 12, 17);
                    break;

                case IMPORT:
                    Path2D.Float folder = new Path2D.Float();
                    folder.moveTo(3, 6);
                    folder.lineTo(9, 6);
                    folder.lineTo(11, 8);
                    folder.lineTo(21, 8);
                    folder.lineTo(21, 18);
                    folder.lineTo(3, 18);
                    folder.closePath();
                    g2.draw(folder);
                    g2.drawLine(12, 11, 12, 15);
                    g2.drawLine(12, 11, 10, 13);
                    g2.drawLine(12, 11, 14, 13);
                    break;

                case EXPORT:
                    g2.draw(new RoundRectangle2D.Float(4, 16, 16, 4, 2, 2));
                    g2.drawLine(12, 4, 12, 13);
                    g2.drawLine(12, 13, 9, 10);
                    g2.drawLine(12, 13, 15, 10);
                    break;

                case ADD:
                    g2.drawLine(12, 5, 12, 19);
                    g2.drawLine(5, 12, 19, 12);
                    break;

                case EDIT:
                    Path2D.Float pencil = new Path2D.Float();
                    pencil.moveTo(15, 4);
                    pencil.lineTo(20, 9);
                    pencil.lineTo(9, 20);
                    pencil.lineTo(4, 20);
                    pencil.lineTo(4, 15);
                    pencil.closePath();
                    g2.draw(pencil);
                    break;

                case DELETE:
                    g2.drawLine(4, 6, 20, 6);
                    g2.drawLine(9, 6, 9, 3);
                    g2.drawLine(15, 6, 15, 3);
                    g2.drawLine(9, 3, 15, 3);
                    Path2D.Float can = new Path2D.Float();
                    can.moveTo(6, 6);
                    can.lineTo(7, 20);
                    can.lineTo(17, 20);
                    can.lineTo(18, 6);
                    g2.draw(can);
                    break;

                case SEARCH:
                    g2.draw(new Ellipse2D.Float(4, 4, 11, 11));
                    g2.drawLine(13, 13, 20, 20);
                    break;

                case FILTER:
                    Path2D.Float funnel = new Path2D.Float();
                    funnel.moveTo(3, 4);
                    funnel.lineTo(21, 4);
                    funnel.lineTo(14, 12);
                    funnel.lineTo(14, 19);
                    funnel.lineTo(10, 17);
                    funnel.lineTo(10, 12);
                    funnel.closePath();
                    g2.draw(funnel);
                    break;

                case RESET:
                    g2.draw(new Arc2D.Float(4, 4, 16, 16, 45, 270, Arc2D.OPEN));
                    g2.drawLine(16, 4, 19, 7);
                    g2.drawLine(16, 4, 13, 7);
                    break;

                case PASSENGER:
                    g2.draw(new Ellipse2D.Float(8, 4, 8, 8));
                    g2.draw(new Arc2D.Float(4, 14, 16, 10, 0, 180, Arc2D.OPEN));
                    break;

                case REVENUE:
                    g2.draw(new Ellipse2D.Float(3, 3, 18, 18));
                    g2.drawLine(12, 6, 12, 18);
                    g2.drawLine(9, 9, 15, 9);
                    g2.drawLine(9, 12, 15, 12);
                    g2.drawLine(9, 15, 15, 15);
                    break;

                case AIRPORT:
                    g2.draw(new Ellipse2D.Float(6, 4, 12, 12));
                    g2.fill(new Ellipse2D.Float(10, 8, 4, 4));
                    g2.drawLine(12, 16, 12, 22);
                    break;

                case CLOCK:
                    g2.draw(new Ellipse2D.Float(3, 3, 18, 18));
                    g2.drawLine(12, 6, 12, 12);
                    g2.drawLine(12, 12, 16, 14);
                    break;

                case CHECK:
                    g2.drawLine(4, 12, 9, 17);
                    g2.drawLine(9, 17, 20, 6);
                    break;

                case ALERT:
                    g2.draw(new Ellipse2D.Float(3, 3, 18, 18));
                    g2.drawLine(12, 7, 12, 13);
                    g2.fill(new Ellipse2D.Float(11, 15, 2, 2));
                    break;
            }
            g2.dispose();
        }
    }
}
