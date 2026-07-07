package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.ConstraintType;
import io.github.jessytsiriniaina.model.House;
import io.github.jessytsiriniaina.model.Room;
import io.github.jessytsiriniaina.model.RoomRelationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlanGenerator {
    private Random random = new Random();

    public void generate(House house, List<Room> roomsToPlace, ConstraintManager constraintManager) {
        house.clearRooms();

        // Simple heuristic: Place largest rooms first
        roomsToPlace.sort((r1, r2) -> Double.compare(r2.getWidth() * r2.getHeight(), r1.getWidth() * r1.getHeight()));

        for (Room room : roomsToPlace) {
            boolean placed = attemptPlacement(house, room, constraintManager);
            if (placed) {
                house.addRoom(room);
            }
        }
    }

    private boolean attemptPlacement(House house, Room room, ConstraintManager constraintManager) {
        // Try to find a position that satisfies relationships first
        List<RoomRelationship> related = findRelationshipsForRoom(room, constraintManager);

        if (!related.isEmpty()) {
            for (RoomRelationship rel : related) {
                Room other = (rel.getRoom1() == room) ? rel.getRoom2() : rel.getRoom1();
                // If the other room is already placed, try to place near it
                if (house.getRooms().contains(other)) {
                    if (tryPlaceNear(house, room, other, rel.getType())) {
                        return true;
                    }
                }
            }
        }



        //**** TO ENHANCE ****//

        // Fallback to random placement if relationships can't be satisfied or don't exist
        int maxAttempts = 1000;
        for (int i = 0; i < maxAttempts; i++) {
            double x = random.nextDouble() * (house.getWidth() - room.getWidth());
            double y = random.nextDouble() * (house.getHeight() - room.getHeight());

            // Align to a grid (0.1m) for better look
            x = Math.floor(x * 10) / 10.0;
            y = Math.floor(y * 10) / 10.0;

            room.setX(x);
            room.setY(y);

            if (isValidPosition(house, room)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryPlaceNear(House house, Room room, Room other, ConstraintType type) {
        double x = other.getX();
        double y = other.getY();

        switch (type) {
            case NEXT_TO:
                // Try 4 sides
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

            //OPPOSITE and NOT_ADJACENT_TO remaining
        }
        return false;
    }

    private boolean trySetAndCheck(House house, Room room, double x, double y) {
        room.setX(x);
        room.setY(y);
        return isValidPosition(house, room);
    }

    private List<RoomRelationship> findRelationshipsForRoom(Room room, ConstraintManager constraintManager) {
        List<RoomRelationship> result = new ArrayList<>();
        for (RoomRelationship rel : constraintManager.getRelationships()) {
            if (rel.getRoom1() == room || rel.getRoom2() == room) {
                result.add(rel);
            }
        }
        return result;
    }

    private boolean isValidPosition(House house, Room room) {
        // Must be within house
        if (room.getX() < 0 || room.getY() < 0 ||
                room.getX() + room.getWidth() > house.getWidth() ||
                room.getY() + room.getHeight() > house.getHeight()) {
            return false;
        }
        // Must not overlap
        return !checkCollision(house, room);
    }

    private boolean checkCollision(House house, Room room) {
        for (Room existingRoom : house.getRooms()) {
            if (room.overlaps(existingRoom)) {
                return true;
            }
        }
        return false;
    }
}
