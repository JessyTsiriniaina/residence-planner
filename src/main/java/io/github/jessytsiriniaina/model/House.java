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

    public House(String name, double x, double y, double width, double height) throws IllegalArgumentException {
        setName(name);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }

    public House copy() {
        House copy = new House(name, x, y, width, height);
        for (Room r : rooms) {
            copy.addRoom(r.copy());
        }
        return copy;
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

    public void setName(String name) throws IllegalArgumentException {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la maison ne doit pas être vide");
        }
        this.name = name;
    }

    public void setWidth(double width) throws IllegalArgumentException {
        if (width <= 0) {
            throw new IllegalArgumentException("La longueur de la maison doit être positif.");
        }
        this.width = width;
    }

    public void setHeight(double height) throws IllegalArgumentException {
        if (height <= 0) {
            throw new IllegalArgumentException("La largeur de la maison doit être positive.");
        }
        this.height = height;
    }

    public void setX(double x) throws IllegalArgumentException {
        if (x < 0) {
            throw new IllegalArgumentException("Les coordonnees de la maison doivent être positives");
        }
        this.x = x;
    }

    public void setY(double y) throws IllegalArgumentException {
        if (y < 0) {
            throw new IllegalArgumentException("Les coordonnees de la maison doivent être positives");
        }
        this.y = y;
    }

    public void addRoom(Room room) {
        if (room != null) {
            rooms.add(room);
        }
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public void clearRooms() {
        rooms.clear();
    }

    public boolean overlaps(House other) {
        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y;

    }

    @Override
    public String toString() {
        return name;
    }
}