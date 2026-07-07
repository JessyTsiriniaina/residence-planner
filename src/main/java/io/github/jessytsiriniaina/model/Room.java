package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

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
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
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
    public String toString() {
        return name;
    }

    public boolean overlaps(Room other) {
        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y;
    }
}
