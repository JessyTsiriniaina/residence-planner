package io.github.jessytsiriniaina.model;

public class MainEntrance extends Opening {

    public MainEntrance(Position position) {
        super(position);
    }

    public MainEntrance(Position position, double offset, double width) {
        super(position, offset, width);
    }

    public MainEntrance(Position position, double offset, double width, String label) {
        super(position, offset, width, label);
    }

    @Override
    public String toString() {
        return "MainEntrance " + getPosition() + (getLabel().isEmpty() ? "" : " (" + getLabel() + ")");
    }
}
