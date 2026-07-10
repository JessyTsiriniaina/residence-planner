package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.ConstraintType;
import io.github.jessytsiriniaina.model.Room;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;

public class Constraint {
    private JComboBox firstRoom;
    private JPanel constraintPanel;
    private JComboBox constraintBox;
    private JComboBox secondRoom;
    private JButton cancelButton;
    private JButton OKButton;
    private final MainFrame parent;
    private io.github.jessytsiriniaina.model.Constraint existingConstraint;

    public Constraint(MainFrame parent) {
        this.parent = parent;
        fillConstraintType();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.hideChangingPanel();
            }
        });

        constraintPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                refreshRoomLists();
                if (!(existingConstraint == null)) {
                    setupExistingValue();
                }
            }
        });
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Room room1 = (Room) firstRoom.getSelectedItem();
                Room room2 = (Room) secondRoom.getSelectedItem();

                if (room1.equals(room2)) {
                    JOptionPane.showMessageDialog(parent, "Impossible de lier une pièce à elle-même.");
                    return;
                }

                if (room1 == null || room2 == null) {
                    JOptionPane.showMessageDialog(parent, "Veuillez selectionner deux pieces distinctes");
                    return;
                }

                ConstraintType type = ((ConstraintTypeWrapper) constraintBox.getSelectedItem()).getType();

                io.github.jessytsiriniaina.model.Constraint newConstraint = new io.github.jessytsiriniaina.model.Constraint(room1, room2, type);

                if (existingConstraint == null) {
                    if (constraintAlreadyExists(newConstraint, null)) {
                        JOptionPane.showMessageDialog(parent, "Cette contrainte existe déjà");
                        return;
                    }

                    parent.addConstraint(newConstraint);
                } else {
                    if (constraintAlreadyExists(newConstraint, existingConstraint)) {
                        JOptionPane.showMessageDialog(parent, "Cette contrainte existe déjà");
                        return;
                    }

                    existingConstraint.changeTo(newConstraint);
                    parent.updateConstraintList();
                }
            }
        });
        constraintPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                existingConstraint = null;
            }
        });
    }

    public JPanel getConstraintPanel() {
        return constraintPanel;
    }

    private void refreshRoomLists() {
        firstRoom.removeAllItems();
        secondRoom.removeAllItems();
        DefaultListModel<Room> roomList = parent.getRoomListModel();
        for (int i = 0; i < roomList.getSize(); i++) {
            firstRoom.addItem(roomList.get(i));
            secondRoom.addItem(roomList.get(i));
        }
    }

    private void fillConstraintType() {
        for (int i = 0; i < ConstraintType.values().length; i++) {
            constraintBox.addItem(new ConstraintTypeWrapper(ConstraintType.values()[i]));
        }
    }

    public void setExistingConstraint(io.github.jessytsiriniaina.model.Constraint existingConstraint) {
        this.existingConstraint = existingConstraint;
    }

    private void setupExistingValue() {
        if (existingConstraint == null) return;
        firstRoom.setSelectedItem(existingConstraint.getRoom1());
        secondRoom.setSelectedItem(existingConstraint.getRoom2());
        constraintBox.setSelectedItem(existingConstraint.getType());
    }

    private boolean constraintAlreadyExists(
            io.github.jessytsiriniaina.model.Constraint constraint,
            io.github.jessytsiriniaina.model.Constraint constraintToIgnore) {

        DefaultListModel<io.github.jessytsiriniaina.model.Constraint> constraintList =
                parent.getConstraintListModel();

        for (int i = 0; i < constraintList.getSize(); i++) {
            io.github.jessytsiriniaina.model.Constraint actualConstraint = constraintList.get(i);

            if (actualConstraint == constraintToIgnore) {
                continue;
            }

            if (Objects.equals(actualConstraint.getRoom1(), constraint.getRoom1())
                    && Objects.equals(actualConstraint.getRoom2(), constraint.getRoom2())
                    && actualConstraint.getType() == constraint.getType()) {
                return true;
            }
        }

        return false;
    }
}
