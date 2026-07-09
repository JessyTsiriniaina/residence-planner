package io.github.jessytsiriniaina.gui;

import javax.swing.JPanel;
import java.awt.*;


public class DrawingPanel extends JPanel {

    private final Dimension preferredSize = new Dimension(2000, 1000);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillRect(50, 30, 1500, 100);
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
}