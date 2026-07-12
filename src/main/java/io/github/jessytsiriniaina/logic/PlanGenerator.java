package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Refactored PlanGenerator implementing a Hybrid Pipeline:
 * 1. Area-based sorting (largest first)
 * 2. Shelf-packing placement with stochastic backtracking fallback
 * 3. Constraint optimization (NEXT_TO, ABOVE, BELOW, LEFT_OF, RIGHT_OF adjustment)
 * 4. Connectivity healing (pull isolated rooms close to other rooms)
 * 5. Opening placement (smart doors/windows on logical walls)
 * 6. Final validation
 */
public class PlanGenerator {
    public static final int MAX_PLACEMENT_ATTEMPTS = 1000;
    public static final double GRID_ALIGNMENT = 10.0;
    public static final double CONNECTIVITY_NEIGHBOR_RADIUS = 0.5;

    private Random random = new Random();

    public void generate(Land land, List<Room> roomsToPlace, ConstraintManager constraintManager) {
        House house = land.getHouse();
        if (house == null) return;
        house.clearRooms();

        // --- STAGE 1: Sort rooms by area (largest first) ---
        roomsToPlace.sort((r1, r2) -> Double.compare(r2.getWidth() * r2.getHeight(), r1.getWidth() * r1.getHeight()));

        // --- STAGE 2: Initial placement using Shelf-Packing with stochastic fallback ---
        double currentX = house.getX();
        double currentY = house.getY();
        double currentShelfHeight = 0.0;

        for (Room room : roomsToPlace) {
            // Check horizontal space inside house boundary
            if (currentX + room.getWidth() > house.getX() + house.getWidth()) {
                // Wrap to next shelf
                currentX = house.getX();
                currentY += currentShelfHeight;
                currentShelfHeight = 0.0;
            }

            // Grid alignment
            double x = Math.floor(currentX * GRID_ALIGNMENT) / GRID_ALIGNMENT;
            double y = Math.floor(currentY * GRID_ALIGNMENT) / GRID_ALIGNMENT;

            room.setX(x);
            room.setY(y);

            if (isValidPosition(house, room)) {
                house.addRoom(room);
                currentX += room.getWidth();
                currentShelfHeight = Math.max(currentShelfHeight, room.getHeight());
            } else {
                // Stochastic fallback placement if shelf packing fails
                boolean placed = attemptPlacement(house, room, constraintManager);
                if (placed) {
                    house.addRoom(room);
                }
            }
        }

        // --- STAGE 3: Constraint Optimization ---
        optimizeConstraints(house, constraintManager);

        // --- STAGE 4: Basic Connectivity ---
        healConnectivity(house);

        // --- STAGE 5: Place Openings ---
        assignSmartOpenings(house);

        // --- STAGE 6: Final Validation ---
        runFinalValidation(house, constraintManager);
    }

    private boolean attemptPlacement(House house, Room room, ConstraintManager constraintManager) {
        List<Constraint> related = findRelationshipsForRoom(room, constraintManager);

        if (!related.isEmpty()) {
            for (Constraint rel : related) {
                Room other = (rel.getRoom1() == room) ? rel.getRoom2() : rel.getRoom1();
                if (house.getRooms().contains(other)) {
                    if (tryPlaceNear(house, room, other, rel.getType())) {
                        return true;
                    }
                }
            }
        }

        // Stochastic fallback to random placement within bounds
        int maxAttempts = MAX_PLACEMENT_ATTEMPTS;
        for (int i = 0; i < maxAttempts; i++) {
            double x = random.nextDouble() * (house.getWidth() - room.getWidth()) + house.getX();
            double y = random.nextDouble() * (house.getHeight() - room.getHeight()) + house.getY();

            x = Math.floor(x * GRID_ALIGNMENT) / GRID_ALIGNMENT;
            y = Math.floor(y * GRID_ALIGNMENT) / GRID_ALIGNMENT;

            room.setX(x);
            room.setY(y);

            if (isValidPosition(house, room)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryPlaceNear(House house, Room room, Room other, ConstraintType type) {
        switch (type) {
            case NEXT_TO:
                if (trySetAndCheck(house, room, other.getX() + other.getWidth(), other.getY())) return true;
                if (trySetAndCheck(house, room, other.getX() - room.getWidth(), other.getY())) return true;
                if (trySetAndCheck(house, room, other.getX(), other.getY() + other.getHeight())) return true;
                if (trySetAndCheck(house, room, other.getX(), other.getY() - room.getHeight())) return true;
                break;
            case LEFT_OF:
                if (trySetAndCheck(house, room, other.getX() - room.getWidth(), other.getY())) return true;
                break;
            case RIGHT_OF:
                if (trySetAndCheck(house, room, other.getX() + other.getWidth(), other.getY())) return true;
                break;
            case ABOVE:
                if (trySetAndCheck(house, room, other.getX(), other.getY() - room.getHeight())) return true;
                break;
            case BELOW:
                if (trySetAndCheck(house, room, other.getX(), other.getY() + other.getHeight())) return true;
                break;
        }
        return false;
    }

    private boolean trySetAndCheck(House house, Room room, double x, double y) {
        room.setX(x);
        room.setY(y);
        return isValidPosition(house, room);
    }

    private List<Constraint> findRelationshipsForRoom(Room room, ConstraintManager constraintManager) {
        List<Constraint> result = new ArrayList<>();
        for (Constraint rel : constraintManager.getRelationships()) {
            if (rel.getRoom1() == room || rel.getRoom2() == room) {
                result.add(rel);
            }
        }
        return result;
    }

    private boolean isValidPosition(House house, Room room) {
        if (room.getX() < house.getX() || room.getY() < house.getY() ||
                room.getX() + room.getWidth() > house.getX() + house.getWidth() ||
                room.getY() + room.getHeight() > house.getY() + house.getHeight()) {
            return false;
        }
        return !checkCollision(house, room);
    }

    private boolean checkCollision(House house, Room room) {
        for (Room existingRoom : house.getRooms()) {
            if (existingRoom != room && room.overlaps(existingRoom)) {
                return true;
            }
        }
        return false;
    }

    private void optimizeConstraints(House house, ConstraintManager constraintManager) {
        for (Constraint rel : constraintManager.getRelationships()) {
            Room r1 = rel.getRoom1();
            Room r2 = rel.getRoom2();
            if (house.getRooms().contains(r1) && house.getRooms().contains(r2)) {
                if (!isRelationshipSatisfied(r1, r2, rel.getType())) {
                    double origX = r1.getX();
                    double origY = r1.getY();
                    house.getRooms().remove(r1);
                    if (tryPlaceNear(house, r1, r2, rel.getType())) {
                        house.addRoom(r1);
                    } else {
                        r1.setX(origX);
                        r1.setY(origY);
                        house.addRoom(r1);
                    }
                }
            }
        }
    }

    private boolean isRelationshipSatisfied(Room r1, Room r2, ConstraintType type) {
        double epsilon = CONNECTIVITY_NEIGHBOR_RADIUS;
        switch (type) {
            case NEXT_TO:
                return isAdjacent(r1, r2, epsilon);
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
        boolean horizontalAdjacency = (Math.abs(r1.getX() + r1.getWidth() - r2.getX()) < epsilon || Math.abs(r2.getX() + r2.getWidth() - r1.getX()) < epsilon) &&
                (r1.getY() < r2.getY() + r2.getHeight() && r1.getY() + r1.getHeight() > r2.getY());

        boolean verticalAdjacency = (Math.abs(r1.getY() + r1.getHeight() - r2.getY()) < epsilon || Math.abs(r2.getY() + r2.getHeight() - r1.getY()) < epsilon) &&
                (r1.getX() < r2.getX() + r2.getWidth() && r1.getX() + r1.getWidth() > r2.getX());

        return horizontalAdjacency || verticalAdjacency;
    }

    private void healConnectivity(House house) {
        List<Room> roomsCopy = new ArrayList<>(house.getRooms());
        for (Room room : roomsCopy) {
            boolean hasNeighbor = false;
            for (Room other : roomsCopy) {
                if (room != other && isAdjacent(room, other, CONNECTIVITY_NEIGHBOR_RADIUS)) {
                    hasNeighbor = true;
                    break;
                }
            }
            if (!hasNeighbor && house.getRooms().size() > 1) {
                Room target = house.getRooms().get(0);
                if (room != target) {
                    double origX = room.getX();
                    double origY = room.getY();
                    house.getRooms().remove(room);
                    if (tryPlaceNear(house, room, target, ConstraintType.NEXT_TO)) {
                        house.addRoom(room);
                    } else {
                        room.setX(origX);
                        room.setY(origY);
                        house.addRoom(room);
                    }
                }
            }
        }
    }

    private void assignSmartOpenings(House house) {
        boolean mainEntrancePlaced = false;
        for (Room room : house.getRooms()) {
            room.clearOpenings();

            // 1. Assign Door
            // Prefer SOUTH or EAST internal walls
            Position doorWall = Position.SOUTH;
            double doorOffset = room.getWidth() / 2.0;
            double doorWidth = 0.9;

            if (Math.abs(room.getY() + room.getHeight() - (house.getY() + house.getHeight())) < 0.1) {
                doorWall = Position.NORTH;
                doorOffset = room.getWidth() / 2.0;
            }

            if (!mainEntrancePlaced && (room.getName().toLowerCase().contains("salon") || room == house.getRooms().get(0))) {
                // This is the main entrance! Place it with customized labels
                room.addOpening(new MainEntrance(doorWall, doorOffset, doorWidth, "Entrée Principale"));
                mainEntrancePlaced = true;
            } else {
                room.addOpening(new Door(doorWall, doorOffset, doorWidth));
            }

            // 2. Assign 1-2 Windows on outer/external boundary walls to prevent overlap
            // North outer wall
            if (Math.abs(room.getY() - house.getY()) < 0.1) {
                if (doorWall != Position.NORTH) {
                    room.addOpening(new Window(Position.NORTH, room.getWidth() / 2.0, 1.2));
                }
            }
            // West outer wall
            if (Math.abs(room.getX() - house.getX()) < 0.1) {
                if (doorWall != Position.WEST) {
                    room.addOpening(new Window(Position.WEST, room.getHeight() / 2.0, 1.2));
                }
            }
            // East outer wall
            if (Math.abs(room.getX() + room.getWidth() - (house.getX() + house.getWidth())) < 0.1) {
                if (doorWall != Position.EAST) {
                    room.addOpening(new Window(Position.EAST, room.getHeight() / 2.0, 1.2));
                }
            }
            // South outer wall
            if (Math.abs(room.getY() + room.getHeight() - (house.getY() + house.getHeight())) < 0.1) {
                if (doorWall != Position.SOUTH) {
                    room.addOpening(new Window(Position.SOUTH, room.getWidth() / 2.0, 1.2));
                }
            }
        }
    }

    private void runFinalValidation(House house, ConstraintManager constraintManager) {
        Validator validator = new Validator();
        List<String> errors = validator.validate(house, constraintManager);
        if (!errors.isEmpty()) {
            System.out.println("Validation Warnings on generated plan: " + errors);
        }
    }
}
