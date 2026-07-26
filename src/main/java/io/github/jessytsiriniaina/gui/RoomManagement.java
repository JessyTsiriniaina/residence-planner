package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.model.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
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
    private JTextField openingWidthField;
    private JTextField offsetField;
    private final MainFrame parent;
    private Room existingRoom;
    private House house;

    private DefaultListModel<Door> doorListModel = new DefaultListModel<>();
    private DefaultListModel<Window> windowListModel = new DefaultListModel<>();
    private boolean isAddingDoor = false;
    private Opening editingOpening = null;


    public RoomManagement(MainFrame parent) {
        this.parent = parent;
        setup();
        openingConfigurationPanel.setVisible(false);
        cancelRoomConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.hideChangingPanel();
                existingRoom = null;
                openingConfigurationPanel.setVisible(false);
            }
        });

        addDoorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editingOpening = null;
                openingConfigurationPanel.setVisible(true);
                isAddingDoor = true;
            }
        });

        addWIndowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editingOpening = null;
                openingConfigurationPanel.setVisible(true);
                isAddingDoor = false;
            }
        });

        cancelOpeningConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editingOpening = null;
                openingConfigurationPanel.setVisible(false);
                isAddingDoor = false;
            }
        });

        OKRoomConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String name = roomName.getText();
                double width = 0;
                double height = 0;

                try {
                    width = Double.parseDouble(roomWidth.getText());
                    height = Double.parseDouble(roomHeight.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(parent, "Veuillez entrer des valeurs numériques valides pour la longueur et la largeur");
                    return;
                }

                Room newRoom = null;
                try {
                    newRoom = new Room(name, width, height);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(parent, e.getMessage());
                    return;
                }

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

                    parent.addRoom(house, newRoom);
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
                JOptionPane.showMessageDialog(parent, "Piece " + name + "(" + width + "m x " + height + "m)" + " enregistrée avec succes");
                existingRoom = null;
            }
        });

        roomManagementPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                emptyInputsFields();
            }
        });

        roomManagementPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                emptyInputsFields();
                if (!(existingRoom == null)) {
                    setupExistingValue();
                }
            }
        });

        deleteDoorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = doorList.getSelectedIndex();
                if (selected != -1) doorListModel.remove(selected);
            }
        });

        deleteWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = windowList.getSelectedIndex();
                if (selected != -1) windowListModel.remove(selected);
            }
        });

        doorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = doorList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        editOpening(doorListModel.getElementAt(index), true);
                    }
                }
            }
        });

        windowList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = windowList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        editOpening(windowListModel.getElementAt(index), false);
                    }
                }
            }
        });

        openingConfigurationPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                positionBox.setSelectedIndex(0);
                isAddingDoor = false;
                editingOpening = null;
            }
        });

        OKOpeningConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (positionBox.getSelectedItem().equals(Position.NONE)) {
                    JOptionPane.showMessageDialog(parent, "Veuillez sélectionner une position.");
                    return;
                }

                double offset = 0;
                double width = 1.0;

                try {
                    offset = Double.parseDouble(offsetField.getText());
                    width = Double.parseDouble(openingWidthField.getText());
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

                if (editingOpening != null) {
                    editingOpening.setPosition((Position) positionBox.getSelectedItem());
                    try {
                        editingOpening.setOffset(offset);
                        editingOpening.setWidth(width);
                    } catch (IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(parent, e.getMessage());
                        return;
                    }
                    doorList.repaint();
                    windowList.repaint();
                } else if (isAddingDoor) {
                    Door newDoor = new Door((Position) positionBox.getSelectedItem(), offset, width);
                    doorListModel.addElement(newDoor);
                } else {
                    Window newWindow = new Window((Position) positionBox.getSelectedItem(), offset, width);
                    windowListModel.addElement(newWindow);
                }

                editingOpening = null;
                positionBox.setSelectedIndex(0);
                openingWidthField.setText("");
                offsetField.setText("");
                openingConfigurationPanel.setVisible(false);
            }
        });
    }

    private void styleTextField(JTextField field) {
        if (field == null) return;
        field.setBorder(new RoundedCornerBorder());
    }

    private void styleJList(JList<?> list) {
        if (list == null) return;
        list.setBorder(new RoundedCornerBorder(8));
        Container parent = list.getParent();
        if (parent instanceof JViewport) {
            Container scrollpane = parent.getParent();
            if (scrollpane instanceof JScrollPane) {
                ((JScrollPane) scrollpane).setBorder(null);
                ((JScrollPane) scrollpane).setViewportBorder(null);
            }
        }
    }

    private void styleScrollPanes(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JScrollPane) {
                ((JScrollPane) c).setBorder(null);
            }
            if (c instanceof Container) {
                styleScrollPanes((Container) c);
            }
        }
    }

    private void styleComboBox(JComboBox<?> box) {
        if (box == null) return;
        box.setBorder(null);
        box.setBackground(new Color(240, 240, 240));
    }

    private void setup() {
        styleTextField(roomName);
        styleTextField(roomWidth);
        styleTextField(roomHeight);
        styleTextField(openingWidthField);
        styleTextField(offsetField);

        styleJList(doorList);
        styleJList(windowList);
        styleComboBox(positionBox);
        SwingUtilities.invokeLater(() -> styleScrollPanes(roomManagementPanel));

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
        openingWidthField.setText("");
        offsetField.setText("");
        positionBox.setSelectedIndex(0);
        isAddingDoor = false;
    }

    private void editOpening(Opening opening, boolean isDoor) {
        editingOpening = opening;
        isAddingDoor = isDoor;
        positionBox.setSelectedItem(opening.getPosition());
        openingWidthField.setText(String.valueOf(opening.getWidth()));
        offsetField.setText(String.valueOf(opening.getOffset()));
        openingConfigurationPanel.setVisible(true);
    }

    public void setExistingRoom(Room existingRoom) {
        this.existingRoom = existingRoom;
    }

    private void setupExistingValue() {
        if (existingRoom == null) return;
        roomName.setText(existingRoom.getName());
        roomWidth.setText(String.valueOf(existingRoom.getWidth()));
        roomHeight.setText(String.valueOf(existingRoom.getHeight()));

        for (Door d : existingRoom.getDoors()) {
            doorListModel.addElement(d);
        }

        for (Window w : existingRoom.getWindows()) {
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

    public void setHouse(House house) {
        this.house = house;
    }
}
