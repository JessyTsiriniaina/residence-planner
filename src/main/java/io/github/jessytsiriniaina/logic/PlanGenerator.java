package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.*;

import java.util.ArrayList;
import java.util.List;

public class PlanGenerator {
    public static final double GRID_ALIGNMENT = 10.0;
    public static final double CONNECTIVITY_NEIGHBOR_RADIUS = 0.5;

    private static final double SEARCH_STEP = 1.0;
    private static final double[] ADJACENT_OFFSET_STEPS = {0.0};
    private static final double ALIGNMENT_GROUP_THRESHOLD = 0.15;
    private static final int MAX_OPTIMIZATION_PASSES = 10;

    private static final double SURFACE_WEIGHT = 1.0;
    private static final double CONSTRAINT_WEIGHT = 8.0;
    private static final double CONNECTIVITY_DISTANCE_PENALTY = 0.5;

    private List<String> lastValidationErrors = new ArrayList<>();
    private ConstraintManager remappedConstraints;

    public List<String> getLastValidationErrors() {
        return lastValidationErrors;
    }

    public ConstraintManager getRemappedConstraints() {
        return remappedConstraints;
    }

    private enum Side { LEFT, RIGHT, TOP, BOTTOM }
    private enum Alignment { START, CENTER, END }

    public void generate(Land land, ConstraintManager constraintManager) throws RuntimeException{
        lastValidationErrors.clear();
        List<House> houses = land.getHouses();
        if (houses.isEmpty()) return;

        ConstraintManager cm = remapConstraints(land, constraintManager);
        remappedConstraints = cm;

        for (House house : houses) {
            List<Room> roomsToPlace = new ArrayList<>(house.getRooms());
            roomsToPlace.sort((r1, r2) -> Double.compare(
                    computeRoomScore(r2, cm),
                    computeRoomScore(r1, cm)));
            house.clearRooms();

            for (Room room : roomsToPlace) {
                boolean placed = attemptPlacementInHouse(house, room, cm);
                if (placed) {
                    house.addRoom(room);
                } else {
                    placed = attemptPlacementWithBacktracking(house, room, cm);
                }
                if (!placed) {
                    throw new RuntimeException("Pas assez d'espace pour la piece " + room.getName() + " dans la maison " + house.getName());
                }
            }

            optimizeConstraints(house, cm);
            optimizeGlobalAlignment(house);
            compactifyLayout(house);
            optimizeAlignment(house);
            healConnectivity(house);
            optimizeConstraints(house, cm);
            optimizeGlobalAlignment(house);
            optimizeAlignment(house);
            runFinalValidation(house, cm);
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

    private boolean attemptPlacementInHouse(House house, Room room, ConstraintManager constraintManager) {
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
        if (attemptGridAlignedPacking(house, room)) {
            return true;
        }
        return attemptPlacementSystematic(house, room, constraintManager);
    }

    private boolean attemptPlacementWithBacktracking(House house, Room room, ConstraintManager constraintManager) {
        List<Room> placed = house.getRooms();
        int backtrackDepth = Math.min(placed.size(), 2);
        for (int i = placed.size() - 1; i >= placed.size() - backtrackDepth; i--) {
            Room swapped = placed.get(i);
            double sx = swapped.getX();
            double sy = swapped.getY();

            house.getRooms().remove(swapped);
            room.setX(sx);
            room.setY(sy);
            if (isValidPosition(house, room)) {
                house.addRoom(room);
                if (attemptPlacementInHouse(house, swapped, constraintManager)) {
                    house.addRoom(swapped);
                    return true;
                }
                house.getRooms().remove(room);
            }
            swapped.setX(sx);
            swapped.setY(sy);
            house.addRoom(swapped);
        }
        return false;
    }

    private boolean attemptPlacementSystematic(House house, Room room, ConstraintManager constraintManager) {
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
        candidates.sort((a, b) -> {
            int alignA = countEdgeAlignmentsAt(house, room, a[0], a[1]);
            int alignB = countEdgeAlignmentsAt(house, room, b[0], b[1]);
            if (alignA != alignB) {
                return Integer.compare(alignB, alignA);
            }
            double wallA = distanceToNearestWall(house, room, a[0], a[1]);
            double wallB = distanceToNearestWall(house, room, b[0], b[1]);
            if (Math.abs(wallA - wallB) > 0.01) {
                return Double.compare(wallA, wallB);
            }
            return Double.compare(
                    distanceSquaredToCandidateCenter(a, room, reference),
                    distanceSquaredToCandidateCenter(b, room, reference));
        });

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

    private double distanceToNearestWall(House house, Room room, double localX, double localY) {
        double x = house.getX() + localX;
        double y = house.getY() + localY;
        double distLeft = x - house.getX();
        double distRight = (house.getX() + house.getWidth()) - (x + room.getWidth());
        double distTop = y - house.getY();
        double distBottom = (house.getY() + house.getHeight()) - (y + room.getHeight());
        return Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));
    }

    private double distanceSquaredToCandidateCenter(double[] localCandidate, Room room, double[] reference) {
        double centerX = localCandidate[0] + room.getWidth() / 2.0;
        double centerY = localCandidate[1] + room.getHeight() / 2.0;
        double dx = centerX - reference[0];
        double dy = centerY - reference[1];
        return dx * dx + dy * dy;
    }

    private int countEdgeAlignmentsAt(House house, Room room, double localX, double localY) {
        double x = house.getX() + localX;
        double y = house.getY() + localY;
        int count = 0;
        double eps = 0.05;
        for (Room other : house.getRooms()) {
            if (other == room) continue;
            if (Math.abs(x - other.getX()) < eps) count++;
            if (Math.abs(x + room.getWidth() - other.getX()) < eps) count++;
            if (Math.abs(x - (other.getX() + other.getWidth())) < eps) count++;
            if (Math.abs(x + room.getWidth() - (other.getX() + other.getWidth())) < eps) count++;
            if (Math.abs(y - other.getY()) < eps) count++;
            if (Math.abs(y + room.getHeight() - other.getY()) < eps) count++;
            if (Math.abs(y - (other.getY() + other.getHeight())) < eps) count++;
            if (Math.abs(y + room.getHeight() - (other.getY() + other.getHeight())) < eps) count++;
        }
        return count;
    }

    private boolean attemptGridAlignedPacking(House house, Room room) {
        List<Room> placed = house.getRooms();
        if (placed.isEmpty()) {
            room.setX(snapToGrid(house.getX()));
            room.setY(snapToGrid(house.getY()));
            return isValidPosition(house, room);
        }

        List<double[]> candidates = new ArrayList<>();
        for (Room r : placed) {
            addGridCandidate(candidates, house, room, r.getX() + r.getWidth(), r.getY());
            addGridCandidate(candidates, house, room, r.getX() - room.getWidth(), r.getY());
            addGridCandidate(candidates, house, room, r.getX(), r.getY() + r.getHeight());
            addGridCandidate(candidates, house, room, r.getX(), r.getY() - room.getHeight());
            addGridCandidate(candidates, house, room, r.getX(), r.getY());
            addGridCandidate(candidates, house, room, r.getX() + r.getWidth() - room.getWidth(), r.getY());
            addGridCandidate(candidates, house, room, r.getX(), r.getY() + r.getHeight() - room.getHeight());
            addGridCandidate(candidates, house, room, r.getX() + r.getWidth() - room.getWidth(), r.getY() + r.getHeight() - room.getHeight());
        }

        double[] reference = computeReferencePoint(house, room);
        candidates.sort((a, b) -> {
            int alignA = countEdgeAlignmentsAt(house, room, a[0] - house.getX(), a[1] - house.getY());
            int alignB = countEdgeAlignmentsAt(house, room, b[0] - house.getX(), b[1] - house.getY());
            if (alignA != alignB) return Integer.compare(alignB, alignA);
            double wallA = distanceToNearestWall(house, room, a[0] - house.getX(), a[1] - house.getY());
            double wallB = distanceToNearestWall(house, room, b[0] - house.getX(), b[1] - house.getY());
            if (Math.abs(wallA - wallB) > 0.01) return Double.compare(wallA, wallB);
            return Double.compare(
                distanceSquaredToPoint(a[0] + room.getWidth() / 2, a[1] + room.getHeight() / 2, reference[0], reference[1]),
                distanceSquaredToPoint(b[0] + room.getWidth() / 2, b[1] + room.getHeight() / 2, reference[0], reference[1]));
        });

        for (double[] candidate : candidates) {
            room.setX(snapToGrid(candidate[0]));
            room.setY(snapToGrid(candidate[1]));
            if (isValidPosition(house, room)) {
                return true;
            }
        }
        return false;
    }

    private void addGridCandidate(List<double[]> candidates, House house, Room room, double x, double y) {
        double sx = snapToGrid(x);
        double sy = snapToGrid(y);
        if (sx < house.getX() || sy < house.getY() ||
            sx + room.getWidth() > house.getX() + house.getWidth() ||
            sy + room.getHeight() > house.getY() + house.getHeight()) {
            return;
        }
        for (double[] c : candidates) {
            if (Math.abs(c[0] - sx) < 0.01 && Math.abs(c[1] - sy) < 0.01) return;
        }
        candidates.add(new double[]{sx, sy});
    }

    private void optimizeAlignment(House house) {
        List<Room> rooms = house.getRooms();
        if (rooms.size() < 2) return;
        for (int pass = 0; pass < 10; pass++) {
            boolean moved = false;
            for (Room room : new ArrayList<>(rooms)) {
                if (tryOptimizeRoomAlignment(house, room)) {
                    moved = true;
                }
            }
            if (!moved) break;
        }
    }

    private boolean tryOptimizeRoomAlignment(House house, Room room) {
        int currentScore = countEdgeAlignmentsAt(house, room, room.getX() - house.getX(), room.getY() - house.getY());
        double bestX = room.getX();
        double bestY = room.getY();
        int bestScore = currentScore;

        for (Room other : house.getRooms()) {
            if (other == room) continue;

            double[][] alignments = {
                {other.getX(), other.getY()},
                {other.getX() + other.getWidth() - room.getWidth(), other.getY()},
                {other.getX(), other.getY() + other.getHeight() - room.getHeight()},
                {other.getX() + other.getWidth() - room.getWidth(), other.getY() + other.getHeight() - room.getHeight()},
                {other.getX() + other.getWidth(), other.getY()},
                {other.getX() - room.getWidth(), other.getY()},
                {other.getX(), other.getY() + other.getHeight()},
                {other.getX(), other.getY() - room.getHeight()},
                {other.getX() + other.getWidth(), other.getY() + other.getHeight()},
                {other.getX() - room.getWidth(), other.getY() + other.getHeight()},
                {other.getX() + other.getWidth(), other.getY() - room.getHeight()},
                {other.getX() - room.getWidth(), other.getY() - room.getHeight()},
            };

            for (double[] pos : alignments) {
                double sx = snapToGrid(pos[0]);
                double sy = snapToGrid(pos[1]);
                if (sx < house.getX() || sy < house.getY() ||
                    sx + room.getWidth() > house.getX() + house.getWidth() ||
                    sy + room.getHeight() > house.getY() + house.getHeight()) {
                    continue;
                }
                if (Math.abs(sx - bestX) < 0.01 && Math.abs(sy - bestY) < 0.01) continue;

                double origX = room.getX(), origY = room.getY();
                room.setX(sx);
                room.setY(sy);
                if (isValidPosition(house, room)) {
                    int score = countEdgeAlignmentsAt(house, room, sx - house.getX(), sy - house.getY());
                    if (score > bestScore) {
                        bestX = sx;
                        bestY = sy;
                        bestScore = score;
                    }
                }
                room.setX(origX);
                room.setY(origY);
            }
        }

        if (bestX != room.getX() || bestY != room.getY()) {
            double origX = room.getX(), origY = room.getY();
            room.setX(bestX);
            room.setY(bestY);
            if (isValidPosition(house, room)) {
                return true;
            }
            room.setX(origX);
            room.setY(origY);
        }
        return false;
    }

    private double distanceSquaredToPoint(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    private void compactifyLayout(House house) {
        List<Room> rooms = house.getRooms();
        if (rooms.size() < 2) return;
        boolean moved;
        int maxPasses = 5;
        do {
            moved = false;
            List<Room> sortedByX = new ArrayList<>(rooms);
            sortedByX.sort((a, b) -> Double.compare(a.getX(), b.getX()));
            for (Room room : sortedByX) {
                moved |= tryCompactLeft(house, room);
            }
            List<Room> sortedByY = new ArrayList<>(rooms);
            sortedByY.sort((a, b) -> Double.compare(a.getY(), b.getY()));
            for (Room room : sortedByY) {
                moved |= tryCompactUp(house, room);
            }
        } while (moved && --maxPasses > 0);
    }

    private boolean tryCompactLeft(House house, Room room) {
        double minX = house.getX();
        for (Room other : house.getRooms()) {
            if (other == room) continue;
            if (room.getY() < other.getY() + other.getHeight() &&
                room.getY() + room.getHeight() > other.getY()) {
                minX = Math.max(minX, other.getX() + other.getWidth());
            }
        }
        double newX = snapToGrid(minX);
        if (Math.abs(newX - room.getX()) < 0.01) return false;
        double origX = room.getX();
        room.setX(newX);
        if (isValidPosition(house, room)) return true;
        room.setX(origX);
        return false;
    }

    private boolean tryCompactUp(House house, Room room) {
        double minY = house.getY();
        for (Room other : house.getRooms()) {
            if (other == room) continue;
            if (room.getX() < other.getX() + other.getWidth() &&
                room.getX() + room.getWidth() > other.getX()) {
                minY = Math.max(minY, other.getY() + other.getHeight());
            }
        }
        double newY = snapToGrid(minY);
        if (Math.abs(newY - room.getY()) < 0.01) return false;
        double origY = room.getY();
        room.setY(newY);
        if (isValidPosition(house, room)) return true;
        room.setY(origY);
        return false;
    }

    private void optimizeGlobalAlignment(House house) {
        List<Room> rooms = house.getRooms();
        if (rooms.size() < 2) return;

        List<List<Room>> rows = detectRows(rooms);
        for (List<Room> row : rows) {
            if (row.size() < 2) continue;
            double alignY = snapToGrid(row.stream().mapToDouble(Room::getY).min().orElse(0));
            for (Room room : row) {
                double origY = room.getY();
                room.setY(alignY);
                if (!isValidPosition(house, room)) {
                    room.setY(origY);
                }
            }
        }

        List<List<Room>> columns = detectColumns(rooms);
        for (List<Room> col : columns) {
            if (col.size() < 2) continue;
            double alignX = snapToGrid(col.stream().mapToDouble(Room::getX).min().orElse(0));
            for (Room room : col) {
                double origX = room.getX();
                room.setX(alignX);
                if (!isValidPosition(house, room)) {
                    room.setX(origX);
                }
            }
        }
    }

    private List<List<Room>> detectRows(List<Room> rooms) {
        List<Room> sorted = new ArrayList<>(rooms);
        sorted.sort((a, b) -> Double.compare(a.getY(), b.getY()));
        List<List<Room>> rows = new ArrayList<>();
        int i = 0;
        while (i < sorted.size()) {
            List<Room> row = new ArrayList<>();
            double baseY = sorted.get(i).getY();
            row.add(sorted.get(i));
            i++;
            while (i < sorted.size() && Math.abs(sorted.get(i).getY() - baseY) < ALIGNMENT_GROUP_THRESHOLD) {
                row.add(sorted.get(i));
                i++;
            }
            rows.add(row);
        }
        return rows;
    }

    private List<List<Room>> detectColumns(List<Room> rooms) {
        List<Room> sorted = new ArrayList<>(rooms);
        sorted.sort((a, b) -> Double.compare(a.getX(), b.getX()));
        List<List<Room>> cols = new ArrayList<>();
        int i = 0;
        while (i < sorted.size()) {
            List<Room> col = new ArrayList<>();
            double baseX = sorted.get(i).getX();
            col.add(sorted.get(i));
            i++;
            while (i < sorted.size() && Math.abs(sorted.get(i).getX() - baseX) < ALIGNMENT_GROUP_THRESHOLD) {
                col.add(sorted.get(i));
                i++;
            }
            cols.add(col);
        }
        return cols;
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
                        movedAnyRoom |= enforceConstraint(house, r1, r2, rel.getType());
                    }
                }
            }

            if (!movedAnyRoom) {
                break;
            }
        }
    }

    private boolean enforceConstraint(House house, Room r1, Room r2, ConstraintType type) {
        // Strategy 1: move r1 near r2
        double ox1 = r1.getX(), oy1 = r1.getY();
        if (tryPlaceNear(house, r1, r2, type)) return true;
        r1.setX(ox1); r1.setY(oy1);

        // Strategy 2: move r2 near r1 (invert direction)
        double ox2 = r2.getX(), oy2 = r2.getY();
        ConstraintType inverted = invertConstraintType(type);
        if (tryPlaceNear(house, r2, r1, inverted)) return true;
        r2.setX(ox2); r2.setY(oy2);

        // Strategy 3: swap positions, then try to move r1 near r2
        house.getRooms().remove(r1);
        house.getRooms().remove(r2);
        r1.setX(ox2); r1.setY(oy2);
        r2.setX(ox1); r2.setY(oy1);
        house.addRoom(r1);
        house.addRoom(r2);
        if (isRelationshipSatisfied(r1, r2, type)) return true;
        double ox3 = r1.getX(), oy3 = r1.getY();
        if (tryPlaceNear(house, r1, r2, type)) return true;
        r1.setX(ox3); r1.setY(oy3);

        // Strategy 4: remove both, place r2 first, then r1 near it
        house.getRooms().remove(r1);
        house.getRooms().remove(r2);
        r1.setX(ox1); r1.setY(oy1);
        r2.setX(ox2); r2.setY(oy2);
        house.addRoom(r2);
        if (tryPlaceNear(house, r1, r2, type)) {
            house.addRoom(r1);
            return true;
        }
        house.getRooms().remove(r2);
        r1.setX(ox1); r1.setY(oy1);
        r2.setX(ox2); r2.setY(oy2);
        house.addRoom(r1);
        house.addRoom(r2);

        return false;
    }

    private ConstraintType invertConstraintType(ConstraintType type) {
        switch (type) {
            case LEFT_OF: return ConstraintType.RIGHT_OF;
            case RIGHT_OF: return ConstraintType.LEFT_OF;
            case ABOVE: return ConstraintType.BELOW;
            case BELOW: return ConstraintType.ABOVE;
            default: return type;
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

    private ConstraintManager remapConstraints(Land land, ConstraintManager original) {
        ConstraintManager remapped = new ConstraintManager();
        for (Constraint rel : original.getRelationships()) {
            Room r1 = findRoomInLand(land, rel.getRoom1().getName());
            Room r2 = findRoomInLand(land, rel.getRoom2().getName());
            if (r1 != null && r2 != null) {
                remapped.addRelationship(new Constraint(r1, r2, rel.getType()));
            }
        }
        return remapped;
    }

    private Room findRoomInLand(Land land, String name) {
        for (House house : land.getHouses()) {
            for (Room room : house.getRooms()) {
                if (room.getName().equals(name)) return room;
            }
        }
        return null;
    }

    private void runFinalValidation(House house, ConstraintManager constraintManager) {
        Validator validator = new Validator();
        List<String> errors = validator.validate(house, constraintManager);
        if (!errors.isEmpty()) {
            lastValidationErrors.addAll(errors);
        }
    }
}