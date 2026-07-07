package io.github.jessytsiriniaina.gui;

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

    public RoomManagement(MainFrame parent) {
        cancelRoomConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.hideChangingPanel();
            }
        });
    }

    public JPanel getRoomManagementPanel() {
        return roomManagementPanel;
    }
}
