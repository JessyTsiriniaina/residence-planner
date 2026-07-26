package io.github.jessytsiriniaina.model;

public class Window extends Opening{
    public Window(Position position) {
        super(position);
    }

    public Window(Position position, double offset, double width) {
        super(position, offset, width);
    }


    @Override
    public String toString() {
        return "Fenêtre " + getPosition() + " (" + getOffset() + "m, " + getWidth() + "m)";
    }
}
