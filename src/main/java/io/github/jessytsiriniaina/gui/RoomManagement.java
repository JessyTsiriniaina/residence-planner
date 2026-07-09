package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.Room;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoomManagement {
    private JTextField roomName;
    private JTextField roomWidth;
    private JTextField roomHeight;
    private JList doorList;
    private JList windowList;
    private JButton cancelRoomConfigurationButton;
    private JButton OKRoomConfigurationButton;
    private JPanel roomManagementPanel;
    private JButton deleteDoorButton;
    private JButton addDoorButton;
    private JButton deleteWindowButton;
    private JButton addWIndowButton;
    private JPanel openingConfigurationPanel;
    private JComboBox positionBox;
    private JButton cancelOpeningConfigurationButton;
    private JButton OKOpeningConfigurationButton;
    MainFrame parent;

    public RoomManagement(MainFrame parent) {
        this.parent = parent;
        openingConfigurationPanel.setVisible(false);
        cancelRoomConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.hideChangingPanel();
                openingConfigurationPanel.setVisible(false);
            }
        });
        addDoorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openingConfigurationPanel.setVisible(true);
            }
        });
        addWIndowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openingConfigurationPanel.setVisible(true);
            }
        });
        cancelOpeningConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openingConfigurationPanel.setVisible(false);
            }
        });
        OKRoomConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String name = roomName.getText();
                double width = Double.parseDouble(roomWidth.getText());
                double height = Double.parseDouble(roomHeight.getText());
                parent.addRoom(new Room(name, width, height));
                emptyInputsFields();
                JOptionPane.showMessageDialog(parent, "Piece " + name + "(" + width + "m x " + height + "m)" +" cree avec succes");
            }
        });
    }

    public JPanel getRoomManagementPanel() {
        return roomManagementPanel;
    }

    public void emptyInputsFields() {
        roomName.setText("");
        roomWidth.setText("");
        roomHeight.setText("");
    }
}
