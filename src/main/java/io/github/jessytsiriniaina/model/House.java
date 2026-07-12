package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

public class House {
    private String name = "Maison 1";
    private double width;
    private double height;
    private List<Room> rooms = new ArrayList<>();

    private double x;
    private double y;

    public House(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Les dimensions de la maison doivent être positives.");
        }
        this.width = width;
        this.height = height;
    }

    public House(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Les dimensions de la maison doivent être positives.");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void clearRooms() {
        rooms.clear();
    }

    @Override
    public String toString() {
        return name + " (" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ") " + String.format("%.1f", width) + "x" + String.format("%.1f", height) + "m";
    }
}
