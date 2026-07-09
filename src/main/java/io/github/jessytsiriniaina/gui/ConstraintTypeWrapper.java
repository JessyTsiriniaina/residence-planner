package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.ConstraintType;

public class ConstraintTypeWrapper {
    private ConstraintType type;

    public ConstraintTypeWrapper(ConstraintType type) {
        this.type = type;
    }

    public ConstraintType getType() {
        return type;
    }

    @Override
    public String toString() {
        switch (type) {
            case NEXT_TO: return "À côté de";
            case OPPOSITE: return "En face de";
            case ABOVE: return "Au-dessus de";
            case BELOW: return "En dessous de";
            case LEFT_OF: return "À gauche de";
            case RIGHT_OF: return "À droite de";
            case CONNECTED_TO: return "Connecté à";
            case NOT_ADJACENT_TO: return "Non adjacent à";
            default: return type.toString();
        }
    }
}
