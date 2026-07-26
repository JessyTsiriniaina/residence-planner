package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.*;

import java.util.ArrayList;
import java.util.List;

public class PlanGenerator {
    public static final double GRID_ALIGNMENT = 10.0;
    public static final double CONNECTIVITY_NEIGHBOR_RADIUS = 0.5;

    private static final double SEARCH_STEP = 0.5;
    private static final double[] ADJACENT_OFFSET_STEPS = {0.0, 0.5, 1.0, 1.5, 2.0};
    private static final int MAX_OPTIMIZATION_PASSES = 5;

    private static final double SURFACE_WEIGHT = 1.0;
    private static final double CONSTRAINT_WEIGHT = 8.0;
    private static final double CONNECTIVITY_DISTANCE_PENALTY = 0.5;

    private enum Side { LEFT, RIGHT, TOP, BOTTOM }
    private enum Alignment { START, CENTER, END }

    public void generate(Land land, ConstraintManager constraintManager) throws RuntimeException{
        List<House> houses = land.getHouses();
        if (houses.isEmpty()) return;

        for (House house : houses) {
            List<Room> roomsToPlace = new ArrayList<>(house.getRooms());
            roomsToPlace.sort((r1, r2) -> Double.compare(
                    computeRoomScore(r2, constraintManager),
                    computeRoomScore(r1, constraintManager)));
            house.clearRooms();

            for (Room room : roomsToPlace) {
                boolean placed = attemptPlacementInHouse(house, room, constraintManager);
                if (placed) {
                    house.addRoom(room);
                } else {
                    throw new RuntimeException("Pas assez d'espace pour la piece " + room.getName() + " dans la maison " + house.getName());
                }
            }

            optimizeConstraints(house, constraintManager);
            healConnectivity(house);
            runFinalValidation(house, constraintManager);
        }
    }


    private double computeRoomScore(Room room, ConstraintManager constraintManager) {
        double surface = room.getWidth() * room.getHeight();
        int constraintCount = countConstraintsForRoom(room, constraintManager);
        return surface * SURFACE_WEIGHT + constraintCount * CONSTRAINT_WEIGHT;
    }

    private int countConstraintsForRoom(Room room, ConstraintManager constraintManager) {
        int count = 0;
        for (Constraint rel : constraintManager.getRelationships()) {
            if (rel.getRoom1() == room || rel.getRoom2() == room) {
                count++;
            }
        }
        return count;
    }

    //Shelf-packing
    private boolean attemptPlacementInHouse(House house, Room room, ConstraintManager constraintManager) {
        double currentX = house.getX();
        double currentY = house.getY();
        double currentShelfHeight = 0.0;

        for (Room existing : house.getRooms()) {
            if (currentX + existing.getWidth() > house.getX() + house.getWidth()) {
                currentX = house.getX();
                currentY += currentShelfHeight;
                currentShelfHeight = 0.0;
            }
            currentX += existing.getWidth();
            currentShelfHeight = Math.max(currentShelfHeight, existing.getHeight());
        }

        if (currentX + room.getWidth() <= house.getX() + house.getWidth()) {
            double x = snapToGrid(currentX);
            double y = snapToGrid(currentY);

            room.setX(x);
            room.setY(y);

            if (isValidPosition(house, room)) {
                return true;
            }
        }

        return attemptPlacementSystematic(house, room, constraintManager);
    }

    private boolean attemptPlacementSystematic(House house, Room room, ConstraintManager constraintManager) {
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

        return attemptGridSearch(house, room);
    }

    private boolean attemptGridSearch(House house, Room room) {
        double maxLocalX = house.getWidth() - room.getWidth();
        double maxLocalY = house.getHeight() - room.getHeight();
        if (maxLocalX < 0 || maxLocalY < 0) {
            return false;
        }

        List<double[]> candidates = new ArrayList<>();
        for (double localY = 0; localY <= maxLocalY + 1e-9; localY += SEARCH_STEP) {
            double clampedY = Math.min(localY, maxLocalY);
            for (double localX = 0; localX <= maxLocalX + 1e-9; localX += SEARCH_STEP) {
                double clampedX = Math.min(localX, maxLocalX);
                candidates.add(new double[]{clampedX, clampedY});
            }
        }

        double[] reference = computeReferencePoint(house, room);
        candidates.sort((a, b) -> Double.compare(
                distanceSquaredToCandidateCenter(a, room, reference),
                distanceSquaredToCandidateCenter(b, room, reference)));

        for (double[] candidate : candidates) {
            double x = snapToGrid(house.getX() + candidate[0]);
            double y = snapToGrid(house.getY() + candidate[1]);

            room.setX(x);
            room.setY(y);

            if (isValidPosition(house, room)) {
                return true;
            }
        }
        return false;
    }

    private double[] computeReferencePoint(House house, Room room) {
        if (house.getRooms().isEmpty()) {
            return new double[]{house.getX() + house.getWidth() / 2.0, house.getY() + house.getHeight() / 2.0};
        }

        double sumX = 0.0;
        double sumY = 0.0;
        int count = 0;
        for (Room existing : house.getRooms()) {
            sumX += existing.getX() + existing.getWidth() / 2.0;
            sumY += existing.getY() + existing.getHeight() / 2.0;
            count++;
        }
        return new double[]{sumX / count, sumY / count};
    }

    private double distanceSquaredToCandidateCenter(double[] localCandidate, Room room, double[] reference) {
        double centerX = localCandidate[0] + room.getWidth() / 2.0;
        double centerY = localCandidate[1] + room.getHeight() / 2.0;
        double dx = centerX - reference[0];
        double dy = centerY - reference[1];
        return dx * dx + dy * dy;
    }

    private boolean tryPlaceNear(House house, Room room, Room other, ConstraintType type) {
        switch (type) {
            case NEXT_TO:
                return tryPlaceAdjacent(house, room, other, null);
            case LEFT_OF:
                return tryPlaceAdjacent(house, room, other, Side.LEFT);
            case RIGHT_OF:
                return tryPlaceAdjacent(house, room, other, Side.RIGHT);
            case ABOVE:
                return tryPlaceAdjacent(house, room, other, Side.TOP);
            case BELOW:
                return tryPlaceAdjacent(house, room, other, Side.BOTTOM);
            default:
                return false;
        }
    }

    private boolean tryPlaceAdjacent(House house, Room room, Room other, Side imposedSide) {
        Side[] sidesToTry = (imposedSide != null)
                ? new Side[]{imposedSide}
                : new Side[]{Side.RIGHT, Side.LEFT, Side.BOTTOM, Side.TOP};

        for (Side side : sidesToTry) {
            for (double offset : ADJACENT_OFFSET_STEPS) {
                for (Alignment alignment : Alignment.values()) {
                    double[] pos = computeAdjacentPosition(room, other, side, offset, alignment);
                    if (trySetAndCheck(house, room, pos[0], pos[1])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double[] computeAdjacentPosition(Room room, Room other, Side side, double offset, Alignment alignment) {
        double x;
        double y;

        switch (side) {
            case RIGHT:
                x = other.getX() + other.getWidth() + offset;
                y = alignAlongVerticalAxis(other, room, alignment);
                break;
            case LEFT:
                x = other.getX() - room.getWidth() - offset;
                y = alignAlongVerticalAxis(other, room, alignment);
                break;
            case BOTTOM:
                y = other.getY() + other.getHeight() + offset;
                x = alignAlongHorizontalAxis(other, room, alignment);
                break;
            case TOP:
            default:
                y = other.getY() - room.getHeight() - offset;
                x = alignAlongHorizontalAxis(other, room, alignment);
                break;
        }

        return new double[]{x, y};
    }

    private double alignAlongVerticalAxis(Room other, Room room, Alignment alignment) {
        switch (alignment) {
            case START:
                return other.getY();
            case END:
                return other.getY() + other.getHeight() - room.getHeight();
            case CENTER:
            default:
                return other.getY() + other.getHeight() / 2.0 - room.getHeight() / 2.0;
        }
    }

    private double alignAlongHorizontalAxis(Room other, Room room, Alignment alignment) {
        switch (alignment) {
            case START:
                return other.getX();
            case END:
                return other.getX() + other.getWidth() - room.getWidth();
            case CENTER:
            default:
                return other.getX() + other.getWidth() / 2.0 - room.getWidth() / 2.0;
        }
    }

    private boolean trySetAndCheck(House house, Room room, double x, double y) {
        room.setX(snapToGrid(x));
        room.setY(snapToGrid(y));
        return isValidPosition(house, room);
    }

    private double snapToGrid(double value) {
        return Math.floor(value * GRID_ALIGNMENT) / GRID_ALIGNMENT;
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
        for (int pass = 0; pass < MAX_OPTIMIZATION_PASSES; pass++) {
            boolean movedAnyRoom = false;

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
                            movedAnyRoom = true;
                        } else {
                            r1.setX(origX);
                            r1.setY(origY);
                            house.addRoom(r1);
                        }
                    }
                }
            }

            if (!movedAnyRoom) {
                break;
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
            /*case NOT_ADJACENT_TO:
                return !isAdjacent(r1, r2, epsilon);*/
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
            if (house.getRooms().size() <= 1) {
                continue;
            }
            if (hasNeighbor(room, house.getRooms())) {
                continue;
            }

            Room target = findBestConnectivityTarget(house, room);
            if (target != null) {
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

    private boolean hasNeighbor(Room room, List<Room> rooms) {
        for (Room other : rooms) {
            if (room != other && isAdjacent(room, other, CONNECTIVITY_NEIGHBOR_RADIUS)) {
                return true;
            }
        }
        return false;
    }

    private Room findBestConnectivityTarget(House house, Room isolatedRoom) {
        Room best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Room candidate : house.getRooms()) {
            if (candidate == isolatedRoom) continue;

            double wallPotential = Math.max(candidate.getWidth(), candidate.getHeight());
            double distance = distanceBetweenCenters(isolatedRoom, candidate);
            double score = wallPotential - distance * CONNECTIVITY_DISTANCE_PENALTY;

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private double distanceBetweenCenters(Room r1, Room r2) {
        double cx1 = r1.getX() + r1.getWidth() / 2.0;
        double cy1 = r1.getY() + r1.getHeight() / 2.0;
        double cx2 = r2.getX() + r2.getWidth() / 2.0;
        double cy2 = r2.getY() + r2.getHeight() / 2.0;
        double dx = cx1 - cx2;
        double dy = cy1 - cy2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void runFinalValidation(House house, ConstraintManager constraintManager) {
        Validator validator = new Validator();
        List<String> errors = validator.validate(house, constraintManager);
        if (!errors.isEmpty()) {
            System.out.println("Validation Warnings on generated plan: " + errors);
        }
    }
}