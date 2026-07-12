package io.github.jessytsiriniaina.model;

import java.util.ArrayList;
import java.util.List;

public class Land {
    private double width;
    private double height;
    private House house;

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

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {this.house = house;}

}
