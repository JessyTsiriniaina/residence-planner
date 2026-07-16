package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

public class House {
    private String name;
    private double width;
    private double height;
    private List<Room> rooms = new ArrayList<>();

    private double x;
    private double y;

    public House(String name, double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Les dimensions de la maison doivent être positives.");
        }
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void addRoom(Room room) {
        if(room != null) {
            rooms.add(room);
        }
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public void clearRooms() {
        rooms.clear();
    }

    @Override
    public String toString() {
        return name;
    }
}