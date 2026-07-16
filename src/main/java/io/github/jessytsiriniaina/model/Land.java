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

    public Land() {

    }

    public double getWidth() {
        return width;
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

    public void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Le longueur doit être positif.");
        }
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

    public void clearHouses() {
        houses.clear();
    }
}
