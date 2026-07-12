package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.*;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    public static final double DEFAULT_EPSILON = 0.05; // Tolerance

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
        for (Constraint rel : constraintManager.getRelationships()) {
            boolean satisfied = checkRelationship(rel);
        }

        // Rooms
        for (Room room : rooms) {
            if (room.getWidth() <= 0 || room.getHeight() <= 0) {
                errors.add("Les dimensions de " + room.getName() + " doivent être positives.");
            }
        }

        // Openings boundaries and collisions validation
        for (Room room : rooms) {
            for (Position pos : Position.values()) {
                if (pos == Position.NONE) continue;
                List<Opening> wallOpenings = new ArrayList<>();
                for (Opening op : room.getOpenings()) {
                    if (op.getPosition() == pos) {
                        wallOpenings.add(op);
                    }
                }

                // Sort by offset to easily check adjacent overlaps
                wallOpenings.sort((o1, o2) -> Double.compare(o1.getOffset(), o2.getOffset()));

                // 1. Check boundary limits
                double wallLength = (pos == Position.NORTH || pos == Position.SOUTH) ? room.getWidth() : room.getHeight();
                for (Opening op : wallOpenings) {
                    if (op.getOffset() > 0) {
                        double start = op.getOffset() - op.getWidth() / 2.0;
                        double end = op.getOffset() + op.getWidth() / 2.0;
                        if (start < 0 || end > wallLength) {
                            errors.add("L'ouverture " + op + " de " + room.getName() + " dépasse les limites du mur (" + pos + ").");
                        }
                    }
                }

                // 2. Check overlap collision on the same wall
                for (int k = 0; k < wallOpenings.size() - 1; k++) {
                    Opening current = wallOpenings.get(k);
                    Opening next = wallOpenings.get(k + 1);
                    if (current.getOffset() > 0 && next.getOffset() > 0) {
                        double currentEnd = current.getOffset() + current.getWidth() / 2.0;
                        double nextStart = next.getOffset() - next.getWidth() / 2.0;
                        if (currentEnd > nextStart) {
                            errors.add("Collision d'ouvertures détectée sur le mur " + pos + " de " + room.getName() + ".");
                        }
                    }
                }
            }
        }

        return errors;
    }

    private boolean checkRelationship(Constraint rel) {
        Room r1 = rel.getRoom1();
        Room r2 = rel.getRoom2();
        double epsilon = DEFAULT_EPSILON;

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
