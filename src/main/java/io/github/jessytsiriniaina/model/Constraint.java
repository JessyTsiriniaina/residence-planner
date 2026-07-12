package io.github.jessytsiriniaina.model;

import java.util.Objects;

public class Constraint {
    private Room room1;
    private Room room2;
    private ConstraintType type;
    private boolean satisfied;

    public Constraint(Room room1, Room room2, ConstraintType type) {
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

    public void setRoom1(Room room1) {
        this.room1 = room1;
    }

    public void setRoom2(Room room2) {
        this.room2 = room2;
    }

    public void setType(ConstraintType type) {
        this.type = type;
    }

    public void changeTo(Constraint constraint) {
        if (constraint == null) return;
        this.setRoom1(constraint.getRoom1());
        this.setRoom2(constraint.getRoom2());
        this.setType(constraint.getType());
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public void setSatisfied(boolean satisfied) {
        this.satisfied = satisfied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return Objects.equals(room1, that.room1) &&
                Objects.equals(room2, that.room2) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(room1, room2, type);
    }

    @Override
    public String toString() {
        return room1.getName() + "," + type + "," + room2.getName();
    }
}
