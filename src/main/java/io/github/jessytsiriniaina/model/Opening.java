package io.github.jessytsiriniaina.model;

public abstract class Opening {
    protected Position position;
    protected double offset;
    protected double width;
    protected String label;

    public Opening(Position position) {
        this.position = position;
        this.offset = 0.0;
        this.width = 1.0; // Default width
        this.label = "";
    }

    public Opening(Position position, double offset, double width) {
        this.position = position;
        this.offset = offset;
        this.width = width;
        this.label = "";
    }

    public Opening(Position position, double offset, double width, String label) {
        this.position = position;
        this.offset = offset;
        this.width = width;
        this.label = label != null ? label : "";
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }
}
