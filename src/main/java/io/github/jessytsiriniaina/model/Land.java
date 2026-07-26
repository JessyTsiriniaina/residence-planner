package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

public class Land {
    private double width;
    private double height;
    private List<House> houses = new ArrayList<>();

    public Land(double width, double height) throws IllegalArgumentException {
        this.setWidth(width);
        this.setHeight(height);
    }

    public Land() {

    }

    public Land copy() {
        Land copy = new Land(width, height);
        for (House h : houses) {
            copy.addHouse(h.copy());
        }
        return copy;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setWidth(double width) throws IllegalArgumentException{
        if (width <= 0) {
            throw new IllegalArgumentException("La longueur du terrain doit être positif.");
        }
        this.width = width;
    }

    public void setHeight(double height) throws IllegalArgumentException{
        if (height <= 0) {
            throw new IllegalArgumentException("La largeur du terrain doit être positive.");
        }
        this.height = height;
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
}
