package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.Constraint;
import io.github.jessytsiriniaina.model.Room;
import io.github.jessytsiriniaina.model.RoomRelationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintManager {
    private List<RoomRelationship> relationships = new ArrayList<>();
    private Map<Room, List<Constraint>> roomConstraints = new HashMap<>();

    public void addRelationship(RoomRelationship relationship) {
        relationships.add(relationship);
    }

    public List<RoomRelationship> getRelationships() {
        return relationships;
    }

    public void addConstraint(Room room, Constraint constraint) {
        roomConstraints.computeIfAbsent(room, k -> new ArrayList<>()).add(constraint);
    }

    public Map<Room, List<Constraint>> getRoomConstraints() {
        return roomConstraints;
    }

    public void clear() {
        relationships.clear();
        roomConstraints.clear();
    }

}
