package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {
    private String name;
    private double width;
    private double height;

    private double x;
    private double y;

    private List<Opening> openings = new ArrayList<>();

    public Room(String name, double width, double height) throws  IllegalArgumentException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Les dimensions de la pièce doivent être positives.");
        }
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("La largeur doit être positive.");
        }
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Le longueur doit être positif.");
        }
        this.width = width;
    }

    public List<Opening> getOpenings() {
        return openings;
    }

    public void addOpening(Opening opening) {
        if (opening != null) {
            openings.add(opening);
        }
    }

    public void clearOpenings() {
        openings.clear();
    }

    public List<Door> getDoors() {
        List<Door> list = new ArrayList<>();
        for (Opening op : openings) {
            if (op instanceof Door) {
                list.add((Door) op);
            }
        }
        return list;
    }

    public void addDoor(Door door) {
        if (door != null) {
            openings.add(door);
        }
    }

    public void clearDoors() {
        openings.removeIf(op -> op instanceof Door);
    }

    public List<Window> getWindows() {
        List<Window> list = new ArrayList<>();
        for (Opening op : openings) {
            if (op instanceof Window) {
                list.add((Window) op);
            }
        }
        return list;
    }

    public void addWindow(Window window) {
        if (window != null) {
            openings.add(window);
        }
    }

    public void clearWindows() {
        openings.removeIf(op -> op instanceof Window);
    }


    public boolean hasOverlappingOpenings() {
        for (Position pos : Position.values()) {
            if (pos == Position.NONE) continue;
            List<Opening> wallOpenings = new ArrayList<>();
            for (Opening op : openings) {
                if (op.getPosition() == pos && op.getOffset() > 0) {
                    wallOpenings.add(op);
                }
            }
            // Sort by offset
            wallOpenings.sort((o1, o2) -> Double.compare(o1.getOffset(), o2.getOffset()));
            for (int i = 0; i < wallOpenings.size() - 1; i++) {
                Opening current = wallOpenings.get(i);
                Opening next = wallOpenings.get(i + 1);
                double currentEnd = current.getOffset() + current.getWidth() / 2.0;
                double nextStart = next.getOffset() - next.getWidth() / 2.0;
                if (currentEnd > nextStart) {
                    return true; // Overlap detected
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean overlaps(Room other) {
        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y;
    }

    public void changeTo(Room room) {
        if(room == null) return;
        this.name = room.getName();
        this.width = room.getWidth();
        this.height = room.getHeight();

        this.openings.clear();
        for(Opening op : room.getOpenings()) {
            this.addOpening(op);
        }
    }
}
