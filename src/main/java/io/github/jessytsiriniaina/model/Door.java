package io.github.jessytsiriniaina.model;

public class Door extends Opening {

    public Door(Position position) {
        super(position);
    }

    public Door(Position position, double offset, double width) {
        super(position, offset, width);
    }

    @Override
    public String toString() {
        return "Door " + getPosition();
    }
}
