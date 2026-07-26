package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.*;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    public static final double DEFAULT_EPSILON = 0.05; // Tolerance

    public List<String> validate(House house, ConstraintManager constraintManager) {
        List<String> errors = new ArrayList<>();
        if (house == null) return errors;

        List<Room> rooms = house.getRooms();

        // 1. Overlapping rooms (overlaps)
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                if (rooms.get(i).overlaps(rooms.get(j))) {
                    errors.add("Chevauchement détecté entre la pièce '" + rooms.get(i).getName() + "' et la pièce '" + rooms.get(j).getName() + "'.");
                }
            }
        }

        // 2. Rooms overflowing their House bounds
        for (Room room : rooms) {
            double overflowLeft = house.getX() - room.getX();
            double overflowRight = (room.getX() + room.getWidth()) - (house.getX() + house.getWidth());
            double overflowTop = house.getY() - room.getY();
            double overflowBottom = (room.getY() + room.getHeight()) - (house.getY() + house.getHeight());

            if (overflowLeft > 0.01) {
                errors.add("La pièce '" + room.getName() + "' déborde de la maison sur la gauche de " + String.format("%.2f", overflowLeft) + "m.");
            }
            if (overflowRight > 0.01) {
                errors.add("La pièce '" + room.getName() + "' déborde de la maison sur la droite de " + String.format("%.2f", overflowRight) + "m.");
            }
            if (overflowTop > 0.01) {
                errors.add("La pièce '" + room.getName() + "' déborde de la maison vers le haut de " + String.format("%.2f", overflowTop) + "m.");
            }
            if (overflowBottom > 0.01) {
                errors.add("La pièce '" + room.getName() + "' déborde de la maison vers le bas de " + String.format("%.2f", overflowBottom) + "m.");
            }
        }

        // 3. Positional / Constraint violations
        for (Constraint rel : constraintManager.getRelationships()) {
            boolean satisfied = checkRelationship(rel);
            rel.setSatisfied(satisfied);
            if (!satisfied) {
                errors.add("Contrainte non satisfaite : la pièce '" + rel.getRoom1().getName() + "' doit être " +
                        getFriendlyConstraintTypeName(rel.getType()) + " '" + rel.getRoom2().getName() + "'.");
            }
        }

        // 4. Rooms with invalid dimensions
        for (Room room : rooms) {
            if (room.getWidth() <= 0 || room.getHeight() <= 0) {
                errors.add("Les dimensions de la pièce '" + room.getName() + "' doivent être strictement positives.");
            }
        }

        // 5. Duplicate room names
        List<String> seenNames = new ArrayList<>();
        for (Room room : rooms) {
            String nameLower = room.getName().trim().toLowerCase();
            if (seenNames.contains(nameLower)) {
                errors.add("Nom de pièce en double détecté : '" + room.getName() + "'.");
            } else {
                seenNames.add(nameLower);
            }
        }

        // 6. Isolated rooms (connectivity check)
        for (Room room : rooms) {
            boolean touchesNeighbor = false;
            for (Room other : rooms) {
                if (room != other && isAdjacent(room, other, 0.1)) {
                    touchesNeighbor = true;
                    break;
                }
            }
            boolean touchesOuterWall = Math.abs(room.getX() - house.getX()) < 0.1 ||
                    Math.abs(room.getY() - house.getY()) < 0.1 ||
                    Math.abs(room.getX() + room.getWidth() - (house.getX() + house.getWidth())) < 0.1 ||
                    Math.abs(room.getY() + room.getHeight() - (house.getY() + house.getHeight())) < 0.1;

            if (!touchesNeighbor && !touchesOuterWall && rooms.size() > 1) {
                errors.add("La pièce '" + room.getName() + "' est isolée (elle ne touche aucune autre pièce ni les murs extérieurs de la maison).");
            }
        }

        // 7. Openings boundaries and collisions validation
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

                // 7.1. Check wall boundaries
                double wallLength = (pos == Position.NORTH || pos == Position.SOUTH) ? room.getWidth() : room.getHeight();
                for (Opening op : wallOpenings) {
                    if (op.getOffset() > 0) {
                        double start = op.getOffset() - op.getWidth() / 2.0;
                        double end = op.getOffset() + op.getWidth() / 2.0;
                        if (start < 0 || end > wallLength) {
                            errors.add("L'ouverture " + op + " de '" + room.getName() + "' dépasse les limites du mur (" + pos + ").");
                        }
                    }
                }

                // 7.2. Check overlap collision on the same wall
                for (int k = 0; k < wallOpenings.size() - 1; k++) {
                    Opening current = wallOpenings.get(k);
                    Opening next = wallOpenings.get(k + 1);
                    if (current.getOffset() > 0 && next.getOffset() > 0) {
                        double currentEnd = current.getOffset() + current.getWidth() / 2.0;
                        double nextStart = next.getOffset() - next.getWidth() / 2.0;
                        if (currentEnd > nextStart) {
                            errors.add("Collision d'ouvertures détectée sur le mur " + pos + " de '" + room.getName() + "'.");
                        }
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Overloaded validate method that checks land-level rules (like house overflows land boundaries)
     * and delegates to house-level validation.
     */
    public List<String> validate(Land land, ConstraintManager constraintManager) {
        List<String> errors = new ArrayList<>();
        if (land == null) return errors;

        List<House> houses = land.getHouses();
        if (houses.isEmpty()) return errors;

        for (int i = 0; i < houses.size(); i++) {
            for (int j = i + 1; j < houses.size(); j++) {
                if (houses.get(i).overlaps(houses.get(j))) {
                    errors.add("Chevauchement détecté entre la maison '" + houses.get(i).getName() + "' et la maison '" + houses.get(j).getName() + "'.");
                }
            }
        }

        // Run House and room-level validation
        for(House house: houses) {
            errors.addAll(validate(house, constraintManager));

            // 8. House overflowing the Land bounds
            double overflowLeft = -house.getX();
            double overflowTop = -house.getY();
            double overflowRight = (house.getX() + house.getWidth()) - land.getWidth();
            double overflowBottom = (house.getY() + house.getHeight()) - land.getHeight();

            if (overflowLeft > 0.01) {
                errors.add("La maison '" + house.getName() + "' est en dehors du terrain sur la gauche de " + String.format("%.2f", overflowLeft) + "m.");
            }
            if (overflowTop > 0.01) {
                errors.add("La maison '" + house.getName() + "' est en dehors du terrain vers le haut de " + String.format("%.2f", overflowTop) + "m.");
            }
            if (overflowRight > 0.01) {
                errors.add("La maison '" + house.getName() + "' dépasse la largeur du terrain sur la droite de " + String.format("%.2f", overflowRight) + "m.");
            }
            if (overflowBottom > 0.01) {
                errors.add("La maison '" + house.getName() + "' dépasse la hauteur du terrain vers le bas de " + String.format("%.2f", overflowBottom) + "m.");
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
            /*case OPPOSITE:
                return (Math.abs(r1.getX() - r2.getX()) < epsilon && Math.abs(r1.getY() - r2.getY()) > Math.min(r1.getHeight(), r2.getHeight())) ||
                        (Math.abs(r1.getY() - r2.getY()) < epsilon && Math.abs(r1.getX() - r2.getX()) > Math.min(r1.getWidth(), r2.getWidth()));*/
            case ABOVE:
                return r1.getY() + r1.getHeight() <= r2.getY() + epsilon;
            case BELOW:
                return r1.getY() >= r2.getY() + r2.getHeight() - epsilon;
            case LEFT_OF:
                return r1.getX() + r1.getWidth() <= r2.getX() + epsilon;
            case RIGHT_OF:
                return r1.getX() >= r2.getX() + r2.getWidth() - epsilon;
            /*case NOT_ADJACENT_TO:
                return !isAdjacent(r1, r2, epsilon);*/
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

    private String getFriendlyConstraintTypeName(ConstraintType type) {
        switch (type) {
            case NEXT_TO: return "à côté de";
            /*case OPPOSITE: return "en face de";*/
            case ABOVE: return "au-dessus de";
            case BELOW: return "en dessous de";
            case LEFT_OF: return "à gauche de";
            case RIGHT_OF: return "à droite de";
            /*case NOT_ADJACENT_TO: return "non adjacente à";*/
            default: return type.toString();
        }
    }
}
