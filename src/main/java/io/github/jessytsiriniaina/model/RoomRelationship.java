package io.github.jessytsiriniaina.model;

public class RoomRelationship {
    private Room room1;
    private Room room2;
    private ConstraintType type;

    public RoomRelationship(Room room1, Room room2, ConstraintType type) {
        this.room1 = room1;
        this.room2 = room2;
        this.type = type;
    }

    public Room getRoom1() {
        return room1;
    }

    public Room getRoom2() {
        return room2;
    }

    public ConstraintType getType() {
        return type;
    }
}
