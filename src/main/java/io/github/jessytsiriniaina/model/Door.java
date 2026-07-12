package io.github.jessytsiriniaina.model;

public class Door extends Opening {

    public Door(Position position) {
        super(position);
    }

    public Door(Position position, double offset, double width) {
        super(position, offset, width);
    }

    public Door(Position position, double offset, double width, String label) {
        super(position, offset, width, label);
    }

    @Override
    public String toString() {
        return "Porte " + getPosition() + " (" + getOffset() + "m, " + getWidth() + "m)" + (getLabel().isEmpty() ? "" : " - " + getLabel());
    }
}
