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

    public void updatePreferredSize() {
        if (land != null && scaleConverter != null) {
            setPreferredSize(new Dimension(
                    scaleConverter.toPixels(land.getWidth()),
                    scaleConverter.toPixels(land.getHeight())));
            revalidate();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(land != null && scaleConverter != null) {
            renderer.render(g2d, land, scaleConverter);
        }

        drawLegend(g2d);
    }

    private void drawLegend(Graphics2D g) {
        int x = getWidth() - 130;
        int y = 10;
        int w = 120;
        int h = 60;

        g.setColor(Colors.LEGEND_BG);
        g.fillRect(x, y, w, h);
        g.setColor(Colors.LEGEND_BORDER);
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, w, h);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int ly = y + 20;

        g.setColor(Colors.DOOR);
        g.setStroke(new BasicStroke(4));
        g.drawLine(x + 8, ly, x + 28, ly);
        g.setColor(Colors.LEGEND_TEXT);
        g.drawString("Porte", x + 34, ly + 4);

        ly += 22;
        g.setColor(Colors.WINDOW);
        g.setStroke(new BasicStroke(3));
        g.drawLine(x + 8, ly, x + 28, ly);
        g.setColor(Colors.LEGEND_TEXT);
        g.drawString("Fenêtre", x + 34, ly + 4);
    }

    public void setLand(Land land) {
        this.land = land;
    }

    public void setScaleConverter(ScaleConverter scaleConverter) {
        this.scaleConverter = scaleConverter;
    }
}