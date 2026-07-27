package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SketchRenderer {

    private record OpeningCoord(int cx, int cy, int swingSide) {}

    public void render(Graphics2D g, Land land, ScaleConverter sc) {
        int tw = sc.toPixels(land.getWidth());
        int th = sc.toPixels(land.getHeight());

        g.setColor(Colors.TERRAIN_FILL);
        g.fillRect(0, 0, tw, th);
        g.setColor(Colors.TERRAIN_BORDER);
        g.setStroke(new BasicStroke(1.5F));
        g.drawRect(0, 0, tw, th);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString(String.format("Terrain: %.1f m x %.1f m", land.getWidth(), land.getHeight()), 5, th - 5);

        for (House house : land.getHouses()) {
            if (house != null) {
                int w = sc.toPixels(house.getWidth());
                int h = sc.toPixels(house.getHeight());
                int x = sc.toPixels(house.getX());
                int y = sc.toPixels(house.getY());

                g.setColor(Colors.HOUSE_BORDER);
                g.setStroke(new BasicStroke(2));
                g.drawRect(x, y, w, h);

                g.setFont(new Font("Arial", Font.PLAIN, 11));
                FontMetrics fm = g.getFontMetrics();
                String name = house.getName();
                int labelX = x + (w - fm.stringWidth(name)) / 2;
                int labelY = y - fm.getDescent() - 2;
                g.setColor(Colors.HOUSE_TEXT);
                g.drawString(name, labelX, labelY);

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

        g.setColor(Colors.ROOM_FILL);
        g.fillRect(x, y, w, h);

        g.setColor(Colors.ROOM_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, w, h);

        if (h >= 20 && w >= 40) {
            g.setColor(Colors.ROOM_TEXT);
            g.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            String name = room.getName();
            g.drawString(name, x + (w - fm.stringWidth(name)) / 2, y + h / 2 + 4);
        }

        drawOpenings(g, x, y, w, h, room.getOpenings(), sc);
    }

    private void drawOpenings(Graphics2D g, int x, int y, int w, int h, List<Opening> openings, ScaleConverter sc) {
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
                int size = Math.max(sc.toPixels(op.getWidth()), 8);

                if (op instanceof Door) {
                    g.setColor(Colors.DOOR);
                    g.setStroke(new BasicStroke(4));
                } else {
                    g.setColor(Colors.WINDOW);
                    g.setStroke(new BasicStroke(3));
                }

                OpeningCoord p = computeOpeningPos(x, y, w, h, op, i, count, pos, sc);

                boolean horizontal = pos == Position.NORTH || pos == Position.SOUTH;
                if (horizontal) {
                    g.drawLine(p.cx() - size / 2, p.cy(), p.cx() + size / 2, p.cy());
                } else {
                    g.drawLine(p.cx(), p.cy() - size / 2, p.cx(), p.cy() + size / 2);
                }

//                if (isDoor) {
//                    drawDoorSwing(g, p.cx(), p.cy(), size, p.swingSide());
//                }
            }
        }
    }

    private OpeningCoord computeOpeningPos(int x, int y, int w, int h, Opening op, int i, int count, Position pos, ScaleConverter sc) {
        int cx, cy, swingSide;

        switch (pos) {
            case NORTH:
                cx = (op.getOffset() > 0) ? x + sc.toPixels(op.getOffset()) : x + (int) (w * (i + 1.0) / (count + 1.0));
                cy = y;
                swingSide = -1;
                break;
            case SOUTH:
                cx = (op.getOffset() > 0) ? x + sc.toPixels(op.getOffset()) : x + (int) (w * (i + 1.0) / (count + 1.0));
                cy = y + h;
                swingSide = 1;
                break;
            case EAST:
                cx = x + w;
                cy = (op.getOffset() > 0) ? y + sc.toPixels(op.getOffset()) : y + (int) (h * (i + 1.0) / (count + 1.0));
                swingSide = 2;
                break;
            default:
                cx = x;
                cy = (op.getOffset() > 0) ? y + sc.toPixels(op.getOffset()) : y + (int) (h * (i + 1.0) / (count + 1.0));
                swingSide = 3;
                break;
        }

        return new OpeningCoord(cx, cy, swingSide);
    }

    private void drawDoorSwing(Graphics2D g, int x, int y, int size, int side) {
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        if (side == -1) g.drawArc(x - size / 2, y - size / 2, size, size, 0, 90);
        else if (side == 1) g.drawArc(x - size / 2, y - size / 2, size, size, 180, 90);
        else if (side == 2) g.drawArc(x - size / 2, y - size / 2, size, size, 90, 90);
        else if (side == 3) g.drawArc(x - size / 2, y - size / 2, size, size, 270, 90);
    }
}
