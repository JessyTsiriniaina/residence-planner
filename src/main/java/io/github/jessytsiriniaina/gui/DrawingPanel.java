package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ConstraintManager;
import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.model.House;
import io.github.jessytsiriniaina.model.Land;

import javax.swing.JPanel;
import java.awt.*;


public class DrawingPanel extends JPanel {

    private final Dimension preferredSize = new Dimension(2000, 1000);
    private Land land;
    private ScaleConverter scaleConverter;
    private SketchRenderer renderer;

    public DrawingPanel(Land land, ScaleConverter scaleConverter) {
        this.land = land;
        this.scaleConverter = scaleConverter;
        this.renderer = new SketchRenderer();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLUE);

        renderer.render(g2d, land, scaleConverter);
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public void setPreferredSize(Dimension dim) {
        this.preferredSize.setSize(dim);
        revalidate();
        repaint();
    }

    public void setLand(Land land) {
        this.land = land;
    }

    public void setScaleConverter(ScaleConverter scaleConverter) {
        this.scaleConverter = scaleConverter;
    }
}