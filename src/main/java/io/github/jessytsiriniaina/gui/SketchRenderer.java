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

        // Draw Terrain Boundary
        g.setColor(new Color(240, 255, 240));
        g.fillRect(0, 0, tw, th);
        g.setColor(new Color(34, 139, 34)); // Forest Green
        g.setStroke(new BasicStroke(1.5F));
        g.drawRect(0, 0, tw, th);
        g.setFont(new Font("Arial", Font.PLAIN , 11));
        g.drawString(String.format("Terrain: %.1f m x %.1f m", land.getWidth(), land.getHeight()), 5, th - 5);

        // Render all houses on the land
        for (House house : land.getHouses()) {
            if (house != null) {
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
                g.drawString(String.format("%s: %.1f m x %.1f m (%.1f m²)", house.getName(), house.getWidth(), house.getHeight(), house.getWidth() * house.getHeight()), sc.toPixels(house.getX() + 1), sc.toPixels((house.getHeight() + house.getY() + 2)));

                // Draw Rooms
                for (Room room : house.getRooms()) {
                    drawRoom(g, room, sc);
                }
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
        drawOpenings(g, x, y, w, h, room.getDoors(), true, sc);

        // Draw windows
        drawOpenings(g, x, y, w, h, room.getWindows(), false, sc);
    }

    private void drawOpenings(Graphics2D g, int x, int y, int w, int h, List<? extends Opening> openings, boolean isDoor, ScaleConverter sc) {
        Map<Position, List<Opening>> byPos = new HashMap<>();
        for (Opening op : openings) {
            byPos.computeIfAbsent(op.getPosition(), k -> new ArrayList<>()).add(op);
        }

        for (Map.Entry<Position, List<Opening>> entry : byPos.entrySet()) {
            Position pos = entry.getKey();
            List<Opening> list = entry.getValue();
            int count = list.size();

            for (int i = 0; i < count; i++) {
                Opening op = list.get(i);
                int size = isDoor ? (op instanceof MainEntrance ? 30 : 25) : 35;
                if (op.getWidth() > 0 && op.getWidth() != 1.0) {
                    size = sc.toPixels(op.getWidth());
                }

                if (isDoor) {
                    if (op instanceof MainEntrance) {
                        g.setColor(new Color(220, 20, 60)); // Crimson for Main Entrance
                    } else {
                        g.setColor(new Color(139, 69, 19)); // Saddle Brown
                    }
                    g.setStroke(new BasicStroke(4));
                } else {
                    g.setColor(new Color(100, 200, 255));
                    g.setStroke(new BasicStroke(3));
                }

                int cx, cy;
                if (pos == Position.NORTH) {
                    if (op.getOffset() > 0) {
                        cx = x + sc.toPixels(op.getOffset());
                    } else {
                        double offsetFrac = (i + 1.0) / (count + 1.0);
                        cx = x + (int)(w * offsetFrac);
                    }
                    g.drawLine(cx - size/2, y, cx + size/2, y);
                    if (isDoor) drawDoorSwing(g, cx, y, size, -1);
                } else if (pos == Position.SOUTH) {
                    if (op.getOffset() > 0) {
                        cx = x + sc.toPixels(op.getOffset());
                    } else {
                        double offsetFrac = (i + 1.0) / (count + 1.0);
                        cx = x + (int)(w * offsetFrac);
                    }
                    g.drawLine(cx - size/2, y + h, cx + size/2, y + h);
                    if (isDoor) drawDoorSwing(g, cx, y + h, size, 1);
                } else if (pos == Position.EAST) {
                    if (op.getOffset() > 0) {
                        cy = y + sc.toPixels(op.getOffset());
                    } else {
                        double offsetFrac = (i + 1.0) / (count + 1.0);
                        cy = y + (int)(h * offsetFrac);
                    }
                    g.drawLine(x + w, cy - size/2, x + w, cy + size/2);
                    if (isDoor) drawDoorSwing(g, x + w, cy, size, 2);
                } else if (pos == Position.WEST) {
                    if (op.getOffset() > 0) {
                        cy = y + sc.toPixels(op.getOffset());
                    } else {
                        double offsetFrac = (i + 1.0) / (count + 1.0);
                        cy = y + (int)(h * offsetFrac);
                    }
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
