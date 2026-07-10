package io.github.jessytsiriniaina.model;

public class Window {
    private Position position;

    public Window(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Window " + position;
    }
}
