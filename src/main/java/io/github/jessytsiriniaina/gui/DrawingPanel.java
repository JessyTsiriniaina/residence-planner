package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.model.Land;

import javax.swing.JPanel;
import java.awt.*;


public class DrawingPanel extends JPanel {

    private Land land;
    private ScaleConverter scaleConverter;
    private SketchRenderer renderer;

    public DrawingPanel() {
        this.renderer = new SketchRenderer();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(land != null && scaleConverter != null) {
            this.setPreferredSize(new Dimension(scaleConverter.toPixels(land.getWidth()), scaleConverter.toPixels(land.getHeight())));
            renderer.render(g2d, land, scaleConverter);
        }

    }

    public void setLand(Land land) {
        this.land = land;
    }

    public void setScaleConverter(ScaleConverter scaleConverter) {
        this.scaleConverter = scaleConverter;
    }
}