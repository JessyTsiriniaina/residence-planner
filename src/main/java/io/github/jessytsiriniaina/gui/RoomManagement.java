package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;

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
    private final MainFrame parent;
    private Room existingRoom;

    private DefaultListModel<Door> doorListModel = new DefaultListModel<>();
    private DefaultListModel<Window> windowListModel = new DefaultListModel<>();
    private boolean isAddingDoor = false;


    public RoomManagement(MainFrame parent) {
        this.parent = parent;
        setup();
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
                isAddingDoor = true;
            }
        });
        addWIndowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openingConfigurationPanel.setVisible(true);
                isAddingDoor = false;
            }
        });
        cancelOpeningConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openingConfigurationPanel.setVisible(false);
                isAddingDoor = false;
            }
        });
        OKRoomConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String name = roomName.getText();
                double width = Double.parseDouble(roomWidth.getText());
                double height = Double.parseDouble(roomHeight.getText());

                Room newRoom = new Room(name, width, height);

                for (int i = 0; i < doorListModel.getSize(); i++) {
                    newRoom.addDoor(doorListModel.getElementAt(i));
                }

                for (int i = 0; i < windowListModel.getSize(); i++) {
                    newRoom.addWindow(windowListModel.getElementAt(i));
                }

                if (existingRoom == null) {
                    if (roomAlreadyExists(newRoom, null)) {
                        JOptionPane.showMessageDialog(parent, "Une pièce portant ce nom existe déjà.");
                        return;
                    }
                    parent.addRoom(newRoom);
                } else {
                    if (roomAlreadyExists(newRoom, existingRoom)) {
                        JOptionPane.showMessageDialog(parent, "Une pièce portant ce nom existe déjà.");
                        return;
                    }

                    existingRoom.changeTo(newRoom);
                    parent.updateRoomList();
                    parent.updateConstraintList();
                }

                emptyInputsFields();
                JOptionPane.showMessageDialog(parent, "Piece " + name + "(" + width + "m x " + height + "m)" +" cree avec succes");
            }
        });
        roomManagementPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                existingRoom = null;
                emptyInputsFields();
            }
        });
        roomManagementPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                if (!(existingRoom == null)) {
                    setupExistingValue();
                }
            }
        });
        deleteDoorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = doorList.getSelectedIndex();
                if(selected != 1) doorListModel.remove(selected);
            }
        });
        deleteWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = windowList.getSelectedIndex();
                if(selected != 1) windowListModel.remove(selected);
            }
        });
        openingConfigurationPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                positionBox.setSelectedIndex(0);
                isAddingDoor = false;
            }
        });
        OKOpeningConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(positionBox.getSelectedItem().equals(Position.NONE)) {
                    return;
                }
                if(isAddingDoor) {
                    Door newDoor = new Door((Position) positionBox.getSelectedItem());
                    doorListModel.addElement(newDoor);
                } else {
                    Window newWindow= new Window((Position) positionBox.getSelectedItem());
                    windowListModel.addElement(newWindow);
                }

                positionBox.setSelectedIndex(0);

            }
        });
    }

    private void setup() {
        doorList.setModel(doorListModel);
        windowList.setModel(windowListModel);
        fillPositionBox();
    }

    public JPanel getRoomManagementPanel() {
        return roomManagementPanel;
    }

    public void emptyInputsFields() {
        roomName.setText("");
        roomWidth.setText("");
        roomHeight.setText("");
        doorListModel.clear();
        windowListModel.clear();
        positionBox.setSelectedIndex(0);
        isAddingDoor = false;
    }

    public void setExistingRoom(Room existingRoom) {
        this.existingRoom = existingRoom;
    }

    private void setupExistingValue() {
        if (existingRoom == null) return;
        roomName.setText(existingRoom.getName());
        roomWidth.setText(String.valueOf(existingRoom.getWidth()));
        roomHeight.setText(String.valueOf(existingRoom.getHeight()));

        for(Door d: existingRoom.getDoors()) {
            doorListModel.addElement(d);
        }

        for(Window w: existingRoom.getWindows()) {
            windowListModel.addElement(w);
        }
    }

    private boolean roomAlreadyExists(Room room, Room roomToIgnore) {
        DefaultListModel<Room> roomList = parent.getRoomListModel();

        for (int i = 0; i < roomList.size(); i++) {
            Room current = roomList.get(i);

            if (current == roomToIgnore) {
                continue;
            }

            if (Objects.equals(room.getName(), current.getName())) {
                return true;
            }
        }

        return false;
    }

    private void fillPositionBox() {
        for (int i = 0; i < Position.values().length; i++) {
            positionBox.addItem(Position.values()[i]);
        }
    }
}
