package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a piece of land.
 * Starting from Phase 2, a single Land terrain can support multiple houses (List<House>)
 * to allow advanced configurations, while maintaining backward-compatible single-house methods.
 */
public class Land {
    private double width;
    private double height;
    private List<House> houses = new ArrayList<>();

    public Land(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Land() {

    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getArea() {
        return width * height;
    }

    public List<House> getHouses() {
        return houses;
    }

    public void setHouses(List<House> houses) {
        this.houses = houses != null ? houses : new ArrayList<>();
    }

    public void addHouse(House house) {
        if (house != null) {
            houses.add(house);
        }
    }

    public House getHouse() {
        return houses.isEmpty() ? null : houses.get(0);
    }

    public void setHouse(House house) {
        houses.clear();
        if (house != null) {
            houses.add(house);
        }
    }

}
