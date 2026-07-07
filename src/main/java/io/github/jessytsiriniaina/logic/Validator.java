package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.*;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    public List<String> validate(House house, ConstraintManager constraintManager) {
        List<String> errors = new ArrayList<>();

        // Overlaps
        List<Room> rooms = house.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                if (rooms.get(i).overlaps(rooms.get(j))) {
                    errors.add("Chevauchement détecté entre " + rooms.get(i).getName() + " et " + rooms.get(j).getName());
                }
            }
        }

        // Debordement
        for (Room room : rooms) {
            if (room.getX() < -0.01 || room.getY() < -0.01 ||
                    room.getX() + room.getWidth() > house.getWidth() + 0.01 ||
                    room.getY() + room.getHeight() > house.getHeight() + 0.01) {
                errors.add(room.getName() + " est en dehors des limites de la maison.");
            }
        }

        // Positional constraints
        for (RoomRelationship rel : constraintManager.getRelationships()) {
            boolean satisfied = checkRelationship(rel);
        }

        // Rooms
        for (Room room : rooms) {
            if (room.getWidth() <= 0 || room.getHeight() <= 0) {
                errors.add("Les dimensions de " + room.getName() + " doivent être positives.");
            }
        }

        return errors;
    }

    private boolean checkRelationship(RoomRelationship rel) {
        Room r1 = rel.getRoom1();
        Room r2 = rel.getRoom2();
        double epsilon = 0.05; // Tolerance

        switch (rel.getType()) {
            case NEXT_TO:
                return isAdjacent(r1, r2, epsilon);
            case OPPOSITE:
                //same x or y but separate by some distance (to enhance | heuristic)
                return (Math.abs(r1.getX() - r2.getX()) < epsilon && Math.abs(r1.getY() - r2.getY()) > Math.min(r1.getHeight(), r2.getHeight())) ||
                        (Math.abs(r1.getY() - r2.getY()) < epsilon && Math.abs(r1.getX() - r2.getX()) > Math.min(r1.getWidth(), r2.getWidth()));
            case ABOVE:
                return r1.getY() + r1.getHeight() <= r2.getY() + epsilon;
            case BELOW:
                return r1.getY() >= r2.getY() + r2.getHeight() - epsilon;
            case LEFT_OF:
                return r1.getX() + r1.getWidth() <= r2.getX() + epsilon;
            case RIGHT_OF:
                return r1.getX() >= r2.getX() + r2.getWidth() - epsilon;
            case NOT_ADJACENT_TO:
                return !isAdjacent(r1, r2, epsilon);
            default:
                return false;
        }
    }

    private boolean isAdjacent(Room r1, Room r2, double epsilon) {
        // Check if they are close enough horizontally and overlap vertically
        boolean horizontalAdjacency = (Math.abs(r1.getX() + r1.getWidth() - r2.getX()) < epsilon || Math.abs(r2.getX() + r2.getWidth() - r1.getX()) < epsilon) &&
                (r1.getY() < r2.getY() + r2.getHeight() && r1.getY() + r1.getHeight() > r2.getY());

        // Check if they are close enough vertically and overlap horizontally
        boolean verticalAdjacency = (Math.abs(r1.getY() + r1.getHeight() - r2.getY()) < epsilon || Math.abs(r2.getY() + r2.getHeight() - r1.getY()) < epsilon) &&
                (r1.getX() < r2.getX() + r2.getWidth() && r1.getX() + r1.getWidth() > r2.getX());

        return horizontalAdjacency || verticalAdjacency;
    }
}
