package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

public class Land {
    private double width;
    private double height;
    private List<House> houses = new ArrayList<>();

    public Land(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getArea() {
        return width * height;
    }

    public List<House> getHouses() {
        return houses;
    }

    public void addHouses(House house) {
        houses.add(house);
    }

    public void clearHouses() {
        houses.clear();
    }
}
