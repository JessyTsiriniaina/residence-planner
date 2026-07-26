package io.github.jessytsiriniaina.model;

public abstract class Opening {
    protected Position position;
    protected double offset;
    protected double width;

    public Opening(Position position) {
        this.position = position;
        this.offset = 0.0;
        this.width = 1.0;
    }

    public Opening(Position position, double offset, double width) throws IllegalArgumentException{
        this.position = position;
        if(offset < 0) {
            throw new IllegalArgumentException("Offset doit être positif");
        }
        this.offset = offset;

        if(width <= 0) {
            throw new IllegalArgumentException("La largeur de la porte doit être positive");
        }
        this.width = width;
    }

    public Position getPosition() {
        return position;
    }

    public double getOffset() {
        return offset;
    }

    public double getWidth() {
        return width;
    }
}
