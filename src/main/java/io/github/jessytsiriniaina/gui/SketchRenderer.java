package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.model.*;
import io.github.jessytsiriniaina.model.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SketchRenderer {
    public void render(Graphics2D g, Land land, ScaleConverter sc) {
        int tw = sc.toPixels(land.getWidth());
        int th = sc.toPixels(land.getHeight());

        House house = land.getHouse();

        // Draw Terrain Boundary
        g.setColor(new Color(240, 255, 240));
        g.fillRect(0, 0, tw, th);
        g.setColor(new Color(34, 139, 34)); // Forest Green
        g.setStroke(new BasicStroke(1.5F));
        g.drawRect(0, 0, tw, th);
        g.setFont(new Font("Arial", Font.PLAIN , 11));
        g.drawString(String.format("Terrain: %.1f m x %.1f m", land.getWidth(), land.getHeight()), 5, th - 5);

        // House position relative to terrain (simple centering or at 0,0)
        // For now, let's assume house is placed at (0,0) inside the drawing context provided by DrawingPanel
        // which already centered the terrain.

        if(!(house == null)) {
            int w = sc.toPixels(house.getWidth());
            int h = sc.toPixels(house.getHeight());
            int x = sc.toPixels(house.getX());
            int y = sc.toPixels(house.getY());

            // Draw House Boundary
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x, y, w, h);

            // Draw House Dimensions
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.drawString(String.format("%.1f m x %.1f m (%.1f m²)", house.getWidth(), house.getHeight(), house.getWidth() * house.getHeight()), sc.toPixels(house.getX() + 1), sc.toPixels((house.getHeight() + house.getY() + 2)));

            // Draw Rooms

            for (Room room : house.getRooms()) {
                drawRoom(g, room, sc);
            }
        }

    }

    private void drawRoom(Graphics2D g, Room room, ScaleConverter sc) {
        int x = sc.toPixels(room.getX());
        int y = sc.toPixels(room.getY());
        int w = sc.toPixels(room.getWidth());
        int h = sc.toPixels(room.getHeight());

        // Fill room
        g.setColor(new Color(245, 245, 245));
        g.fillRect(x, y, w, h);

        // Room border
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, w, h);

        // Room label and dimensions
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String name = room.getName();
        g.drawString(name, x + (w - fm.stringWidth(name)) / 2, y + h / 2 - 5);

        g.setFont(new Font("Arial", Font.PLAIN, 10));
        fm = g.getFontMetrics();
        String dims = String.format("%.1f m × %.1f m", room.getWidth(), room.getHeight());
        g.drawString(dims, x + (w - fm.stringWidth(dims)) / 2, y + h / 2 + 10);

        // Draw doors
        drawOpenings(g, x, y, w, h, room.getDoors(), true);

        // Draw windows
        drawOpenings(g, x, y, w, h, room.getWindows(), false);
    }

    private void drawOpenings(Graphics2D g, int x, int y, int w, int h, List<?> openings, boolean isDoor) {
        Map<Object, List<Object>> byPos = new HashMap<>();
        for (Object o : openings) {
            Object pos = isDoor ? ((Door)o).getPosition() : ((Window)o).getPosition();
            byPos.computeIfAbsent(pos, k -> new ArrayList<>()).add(o);
        }

        for (Map.Entry<Object, List<Object>> entry : byPos.entrySet()) {
            Object pos = entry.getKey();
            List<Object> list = entry.getValue();
            int count = list.size();
            int size = isDoor ? 25 : 35;

            for (int i = 0; i < count; i++) {
                double offsetFrac = (i + 1.0) / (count + 1.0);
                if (isDoor) {
                    g.setColor(new Color(139, 69, 19));
                    g.setStroke(new BasicStroke(4));
                } else {
                    g.setColor(new Color(100, 200, 255));
                    g.setStroke(new BasicStroke(3));
                }

                int cx, cy;
                if (pos == Position.NORTH || pos == Position.NORTH) {
                    cx = x + (int)(w * offsetFrac);
                    g.drawLine(cx - size/2, y, cx + size/2, y);
                    if (isDoor) drawDoorSwing(g, cx, y, size, -1);
                } else if (pos == Position.SOUTH || pos == Position.SOUTH) {
                    cx = x + (int)(w * offsetFrac);
                    g.drawLine(cx - size/2, y + h, cx + size/2, y + h);
                    if (isDoor) drawDoorSwing(g, cx, y + h, size, 1);
                } else if (pos == Position.EAST || pos == Position.EAST) {
                    cy = y + (int)(h * offsetFrac);
                    g.drawLine(x + w, cy - size/2, x + w, cy + size/2);
                    if (isDoor) drawDoorSwing(g, x + w, cy, size, 2);
                } else if (pos == Position.WEST || pos == Position.WEST) {
                    cy = y + (int)(h * offsetFrac);
                    g.drawLine(x, cy - size/2, x, cy + size/2);
                    if (isDoor) drawDoorSwing(g, x, cy, size, 3);
                }
            }
        }
    }

    private void drawDoorSwing(Graphics2D g, int x, int y, int size, int side) {
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        if (side == -1) g.drawArc(x - size/2, y - size/2, size, size, 0, 90);
        else if (side == 1) g.drawArc(x - size/2, y - size/2, size, size, 180, 90);
        else if (side == 2) g.drawArc(x - size/2, y - size/2, size, size, 90, 90);
        else if (side == 3) g.drawArc(x - size/2, y - size/2, size, size, 270, 90);
    }
}
