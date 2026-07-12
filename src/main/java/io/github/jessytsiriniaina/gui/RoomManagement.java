package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.*;
import io.github.jessytsiriniaina.model.Window;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

    // Advanced Opening UI Fields (programmatic)
    private JTextField offsetField;
    private JTextField widthField;
    private JComboBox typeBox;

    public RoomManagement(MainFrame parent) {
        this.parent = parent;
        setup();

        // Programmatically customize openingConfigurationPanel
        buildAdvancedOpeningPanel();

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
                typeBox.setEnabled(true);
                typeBox.setSelectedIndex(0);
            }
        });
        addWIndowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openingConfigurationPanel.setVisible(true);
                isAddingDoor = false;
                typeBox.setEnabled(false);
                typeBox.setSelectedIndex(0);
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
                if(selected != -1) doorListModel.remove(selected);
            }
        });
        deleteWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = windowList.getSelectedIndex();
                if(selected != -1) windowListModel.remove(selected);
            }
        });
        openingConfigurationPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                positionBox.setSelectedIndex(0);
                offsetField.setText("0.0");
                widthField.setText("1.0");
                typeBox.setSelectedIndex(0);
                isAddingDoor = false;
            }
        });
        OKOpeningConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(positionBox.getSelectedItem().equals(Position.NONE)) {
                    return;
                }

                double offset = 0.0;
                double width = 1.0;
                try {
                    offset = Double.parseDouble(offsetField.getText());
                    width = Double.parseDouble(widthField.getText());
                    if (width <= 0) {
                        JOptionPane.showMessageDialog(parent, "La largeur de l'ouverture doit être positive.");
                        return;
                    }
                    if (offset < 0) {
                        JOptionPane.showMessageDialog(parent, "L'offset ne peut pas être négatif.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(parent, "Veuillez entrer des valeurs numériques valides pour l'offset et la largeur.");
                    return;
                }

                if(isAddingDoor) {
                    Door newDoor;
                    if (typeBox.getSelectedIndex() == 1) {
                        newDoor = new MainEntrance((Position) positionBox.getSelectedItem(), offset, width);
                    } else {
                        newDoor = new Door((Position) positionBox.getSelectedItem(), offset, width);
                    }
                    doorListModel.addElement(newDoor);
                } else {
                    Window newWindow = new Window((Position) positionBox.getSelectedItem(), offset, width);
                    windowListModel.addElement(newWindow);
                }

                positionBox.setSelectedIndex(0);
                offsetField.setText("0.0");
                widthField.setText("1.0");
                typeBox.setSelectedIndex(0);
                openingConfigurationPanel.setVisible(false);
            }
        });
    }

    private void setup() {
        doorList.setModel(doorListModel);
        windowList.setModel(windowListModel);
        fillPositionBox();
    }

    private void buildAdvancedOpeningPanel() {
        openingConfigurationPanel.setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel posRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        posRow.add(new JLabel("Position: "));
        posRow.add(positionBox);
        container.add(posRow);

        JPanel offsetRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        offsetRow.add(new JLabel("Offset (m): "));
        offsetField = new JTextField("0.0", 6);
        offsetRow.add(offsetField);
        container.add(offsetRow);

        JPanel widthRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        widthRow.add(new JLabel("Largeur (m): "));
        widthField = new JTextField("1.0", 6);
        widthRow.add(widthField);
        container.add(widthRow);

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeRow.add(new JLabel("Type: "));
        typeBox = new JComboBox(new String[]{"Standard", "Entrée Principale"});
        typeRow.add(typeBox);
        container.add(typeRow);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(OKOpeningConfigurationButton);
        btnRow.add(cancelOpeningConfigurationButton);
        container.add(btnRow);

        openingConfigurationPanel.removeAll();
        openingConfigurationPanel.add(container, BorderLayout.CENTER);
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
        offsetField.setText("0.0");
        widthField.setText("1.0");
        typeBox.setSelectedIndex(0);
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
