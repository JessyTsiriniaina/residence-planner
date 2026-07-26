package io.github.jessytsiriniaina.gui;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class RoundedCornerBorder extends AbstractBorder {
    private final int radius;

    public RoundedCornerBorder() {
        this(12);
    }

    public RoundedCornerBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Container parent = c.getParent();
        if (parent != null) {
            g2.setColor(parent.getBackground());
            Area corners = new Area(new Rectangle(x, y, width, height));
            corners.subtract(new Area(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius)));
            g2.fill(corners);
        }

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = 8;
        insets.top = 4;
        insets.right = 8;
        insets.bottom = 4;
        return insets;
    }
}
