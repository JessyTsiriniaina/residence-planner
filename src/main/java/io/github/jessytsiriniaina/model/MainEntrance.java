package io.github.jessytsiriniaina.model;

public class MainEntrance extends Door {

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
        return "Entrée Principale " + getPosition() + " (" + getOffset() + "m, " + getWidth() + "m)" + (getLabel().isEmpty() ? "" : " - " + getLabel());
    }
}
