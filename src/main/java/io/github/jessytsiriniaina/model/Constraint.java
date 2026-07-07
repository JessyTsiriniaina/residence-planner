package io.github.jessytsiriniaina.model;

public class Constraint {
    private String description;
    private boolean satisfied;

    public Constraint(String description) {
        this.description = description;
        this.satisfied = false;
    }

    public String getDescription() { return description; }
    public boolean isSatisfied() { return satisfied; }
    public void setSatisfied(boolean satisfied) { this.satisfied = satisfied; }
}
