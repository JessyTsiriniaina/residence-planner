package io.github.jessytsiriniaina.logic;

import io.github.jessytsiriniaina.model.Constraint;
import io.github.jessytsiriniaina.model.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintManager {
    private List<Constraint> relationships = new ArrayList<>();

    public void addRelationship(Constraint relationship) {
        relationships.add(relationship);
    }

    public List<Constraint> getRelationships() {
        return relationships;
    }
}
