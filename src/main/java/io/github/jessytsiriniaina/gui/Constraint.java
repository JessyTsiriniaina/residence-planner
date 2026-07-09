package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.ConstraintType;
import io.github.jessytsiriniaina.model.Room;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Constraint {
    private JComboBox firstRoom;
    private JPanel constraintPanel;
    private JComboBox constraintBox;
    private JComboBox secondRoom;
    private JButton cancelButton;
    private JButton OKButton;
    private MainFrame parent;

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
            }
        });
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Room room1 = (Room) firstRoom.getSelectedItem();
                Room room2 = (Room) secondRoom.getSelectedItem();

                if(room1.equals(room2)) {
                    JOptionPane.showMessageDialog(parent, "Impossible de lier une pièce à elle-même.");
                    return;
                }

                if(room1 == null || room2 == null) {
                    JOptionPane.showMessageDialog(parent, "Veuillez selectionner deux pieces distinctes");
                }

                ConstraintType type = ((ConstraintTypeWrapper) constraintBox.getSelectedItem()).getType();
                parent.addConstraint(new io.github.jessytsiriniaina.model.Constraint(room1, room2, type));
            }
        });
    }

    public JPanel getConstraintPanel() {
        return constraintPanel;
    }

    private void refreshRoomLists() {
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
}
