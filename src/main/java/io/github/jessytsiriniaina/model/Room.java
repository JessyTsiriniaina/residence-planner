package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {
    private String name;
    private double width;
    private double height;

    //Position - Coordinates
    private double x;
    private double y;

    private List<Door> doors = new ArrayList<>();
    private List<Window> windows = new ArrayList<>();

    public Room(String name, double width, double height) {
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
            throw new IllegalArgumentException("La hauteur doit être positive.");
        }
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("La largeur doit être positive.");
        }
        this.width = width;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public void clearDoors() {
        doors.clear();
    }

    public List<Window> getWindows() {
        return windows;
    }

    public void addWindow(Window window) {
        windows.add(window);
    }

    public void clearWindows() {
        windows.clear();
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

        this.clearDoors();
        for(Door d: room.getDoors()) {
            addDoor(d);
        }

        this.clearWindows();
        for(Window w: room.getWindows()) {
            addWindow(w);
        }
    }
}
