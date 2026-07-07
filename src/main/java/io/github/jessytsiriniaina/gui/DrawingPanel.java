package io.github.jessytsiriniaina.gui;

import javax.swing.JPanel;
import java.awt.*;

public class DrawingPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        // Cette ligne nettoie l'écran et prépare le panneau
        super.paintComponent(g);

        // Choisissez une couleur pour le rectangle
        g.setColor(Color.BLUE);

        // Dessine le rectangle plein : (x, y, largeur, hauteur)
        g.fillRect(50, 30, 1500, 100);
    }
}
