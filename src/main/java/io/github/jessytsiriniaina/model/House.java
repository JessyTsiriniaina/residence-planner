package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

public class House {
    private double width;
    private double height;
    private List<Room> rooms = new ArrayList<>();

    private double x;
    private double y;

    public House(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public House(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
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
}
