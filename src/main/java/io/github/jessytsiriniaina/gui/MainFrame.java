package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ConstraintManager;
import io.github.jessytsiriniaina.logic.PlanGenerator;
import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.logic.Validator;
import io.github.jessytsiriniaina.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFrame extends JFrame {
    static {
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumb", Colors.SCROLLBAR_THUMB);
        UIManager.put("ScrollBar.thumbDarkShadow", Colors.SCROLLBAR_THUMB_DARK_SHADOW);
        UIManager.put("ScrollBar.thumbHighlight", Colors.SCROLLBAR_THUMB_HIGHLIGHT);
        UIManager.put("ScrollBar.thumbShadow", Colors.SCROLLBAR_THUMB_SHADOW);
        UIManager.put("ScrollBar.track", Colors.SCROLLBAR_TRACK);

        UIManager.put("ComboBox.background", Colors.COMBO_BACKGROUND);
        UIManager.put("ComboBox.foreground", Colors.COMBO_FOREGROUND);
        UIManager.put("ComboBox.selectionBackground", Colors.COMBO_SELECTION_BG);
        UIManager.put("ComboBox.selectionForeground", Colors.COMBO_SELECTION_FG);
        UIManager.put("ComboBox.buttonBackground", Colors.COMBO_BUTTON_BG);
        UIManager.put("ComboBox.buttonShadow", Colors.COMBO_BUTTON_SHADOW);
        UIManager.put("ComboBox.buttonDarkShadow", Colors.COMBO_BUTTON_DARK_SHADOW);
        UIManager.put("ComboBox.buttonHighlight", Colors.COMBO_BUTTON_HIGHLIGHT);
    }
    private JPanel mainPanel;
    private DrawingPanel drawingPanel;
    private JPanel propertiesPanel;
    private JPanel bottomPanel;
    private JTextArea reportArea;
    private JPanel landPanel;
    private JTextField landWidthField;
    private JTextField landHeightField;
    private JPanel scalePanel;
    private JTextField scaleField;
    private JPanel housePanel;
    private JTextField houseWidthField;
    private JTextField houseHeightField;
    private JList roomList;
    private JButton addRoomButton;
    private JButton removeRoomButton;
    private JPanel roomPanel;
    private JPanel constraintPanel;
    private JList constraintList;
    private JButton addConstraintButton;
    private JButton removeConstraintButton;
    private JPanel actionPanel;
    private JButton generateButton;
    private JButton resetButton;
    private JPanel changingPanel;
    private JTextField houseXField;
    private JTextField houseYField;
    private JList houseList;
    private JButton removeHouseButton;
    private JButton saveHouseButton;
    private JTextField houseNameField;
    private JPanel houseListPanel;
    private CardLayout cardLayout;

    private final ConstraintManagement constraintMananagement = new ConstraintManagement(this);
    private final RoomManagement roomManagement = new RoomManagement(this);

    private final JPanel constraintManagementPanel = constraintMananagement.getConstraintPanel();
    private final JPanel roomManagementPanel = roomManagement.getRoomManagementPanel();
    private final JPanel emptyPanel = new JPanel();

    private final String EMPTY = "EMPTY";
    private final String ROOM = "ROOM";
    private final String CONSTRAINT = "CONSTRAINT";

    private DefaultListModel<Room> roomListModel = new DefaultListModel<>();
    private DefaultListModel<Constraint> constraintListModel = new DefaultListModel<>();
    private DefaultListModel<House> houseListModel = new DefaultListModel<>();

    private boolean isEditingHouse = false;


    private void createUIComponents() {
        drawingPanel = new DrawingPanel();
    }

    public MainFrame() {
        this.cardLayout = (CardLayout) changingPanel.getLayout();
        setup();

        addRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //adapter pour le lier a une maison
                House house = (House) houseList.getSelectedValue();
                if (house == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Veuillez choisir une maison pour ajouter une piece.");
                    return;
                }
                hideChangingPanel();
                showRoomManagementPanel(house, null);
            }
        });

        addConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showConstraintManagementPanel(null);
            }
        });

        removeRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = roomList.getSelectedIndex();
                if (selected != -1) {
                    Room roomToDelete = roomListModel.get(selected);
                    for (int i = constraintListModel.getSize() - 1; i >= 0; i--) {
                        Constraint actualConstraint = constraintListModel.get(i);
                        if (Objects.equals(actualConstraint.getRoom1(), roomToDelete)
                                || Objects.equals(actualConstraint.getRoom2(), roomToDelete)) {
                            constraintListModel.remove(i);
                        }
                    }

                    House h = (House) houseList.getSelectedValue();
                    h.removeRoom(roomListModel.getElementAt(selected));
                    roomListModel.remove(selected);
                }
            }
        });

        removeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = constraintList.getSelectedIndex();
                if (selected != -1) constraintListModel.remove(selected);
            }
        });

        constraintList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = constraintList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        hideChangingPanel();
                        showConstraintManagementPanel(constraintListModel.getElementAt(index));
                    }
                }
            }
        });

        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = roomList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        hideChangingPanel();
                        showRoomManagementPanel(null, roomListModel.getElementAt(index));
                    }
                }
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                generatePlan();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                reset();
            }
        });

        saveHouseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //triple clique sur la liste et un bug se produit, il faut le corriger
                try {
                    if (isEditingHouse) {
                        House house = (House) houseList.getSelectedValue();
                        updateHouseFromInputs(house);
                        houseList.updateUI();
                        showRoomListFor(house);
                        isEditingHouse = false;
                    } else {
                        House newHouse = createHouseFromInputs();
                        houseListModel.addElement(newHouse);
                        showRoomListFor(newHouse);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Veuillez entrer des formats valides");
                    return;
                }

                emptyHouseFields();
            }
        });

        removeHouseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = houseList.getSelectedIndex();
                if (selected != -1) {
                    if (houseListModel.size() > 1) {
                        houseListModel.remove(selected);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this, "Il doit y avoir au moins une maison.");
                    }
                }
            }
        });

        houseList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                House h = (House) houseList.getSelectedValue();
                if (e.getClickCount() == 2) {
                    if (h != null) {
                        fillHouseFieldsWithValueOf(h);
                        isEditingHouse = true;
                    }
                } else {
                    showRoomListFor(h);
                    isEditingHouse = false;
                }
            }
        });
    }

    private void styleTextField(JTextField field) {
        if (field == null) return;
        field.setBorder(new RoundedCornerBorder());
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

    private void setup() {
        setContentPane(mainPanel);
        setTitle("Residence Planer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        styleTextField(landWidthField);
        styleTextField(landHeightField);
        styleTextField(scaleField);
        styleTextField(houseWidthField);
        styleTextField(houseHeightField);
        styleTextField(houseXField);
        styleTextField(houseYField);
        styleTextField(houseNameField);

        styleJList(roomList);
        styleJList(constraintList);
        styleJList(houseList);

        SwingUtilities.invokeLater(() -> styleScrollPanes(mainPanel));

        pack(); // Adjust frame size based on content | Can be replaced with setSize()
        setVisible(true);

        changingPanel.add(emptyPanel, EMPTY);
        changingPanel.add(constraintManagementPanel, CONSTRAINT);
        changingPanel.add(roomManagementPanel, ROOM);

        hideChangingPanel();

        roomList.setModel(roomListModel);
        constraintList.setModel(constraintListModel);
        houseList.setModel(houseListModel);


        ////// TO REMOVE /////
        scaleField.setText("10");
        landWidthField.setText("50");
        landHeightField.setText("50");
        House house = new House("Maison 1", 10, 10, 30, 30);
        house.addRoom(new Room("Chambre 1", 12, 12));
        house.addRoom(new Room("Salon", 12, 12));
        house.addRoom(new Room("Cuisine", 12, 12));
        houseListModel.addElement(house);//ne plus supprimer OU pas
        showRoomListFor(house);
    }

    public void hideChangingPanel() {
        cardLayout.show(changingPanel, EMPTY);
        changingPanel.setVisible(false);
    }

    private void showRoomManagementPanel(House house, Room room) {
        roomManagement.setExistingRoom(room);
        roomManagement.setHouse(house);
        changingPanel.setVisible(true);
        cardLayout.show(changingPanel, ROOM);
    }

    private void showConstraintManagementPanel(Constraint constraint) {
        constraintMananagement.setExistingConstraint(constraint);
        changingPanel.setVisible(true);
        cardLayout.show(changingPanel, CONSTRAINT);
    }

    public void addRoom(House house, Room room) {
        house.addRoom(room);
        roomListModel.addElement(room);
    }

    public DefaultListModel<Room> getRoomListModel() {
        return roomListModel;
    }

    public void addConstraint(Constraint constraint) {
        constraintListModel.addElement(constraint);
    }

    public void updateConstraintList() {
        constraintList.updateUI();
    }

    public DefaultListModel<Constraint> getConstraintListModel() {
        return constraintListModel;
    }

    public void updateRoomList() {
        roomList.updateUI();
    }

    private void generatePlan() {
        double scale;
        double landWidth;
        double landHeight;
        ScaleConverter scaleConverter;
        Land land;
        ConstraintManager constraintManager;
        Land landCopy;
        PlanGenerator generator;

        if (houseListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il doit y avoir au moins une maison");
            return;
        }

        try {
            scale = Double.parseDouble(scaleField.getText());
            landWidth = Double.parseDouble(landWidthField.getText());
            landHeight = Double.parseDouble(landHeightField.getText());

            scaleConverter = new ScaleConverter((int) scale);
            drawingPanel.setScaleConverter(scaleConverter);

            land = new Land(landWidth, landHeight);
            List<House> houses = new ArrayList<>();
            for (int i = 0; i < houseListModel.size(); i++) {
                House h = houseListModel.get(i);
                houses.add(h);
            }
            land.setHouses(houses);

            List<Room> rooms = getRoomsFromInput();
            constraintManager = new ConstraintManager();
            setupConstraints(rooms, constraintManager);
            landCopy = land.copy();
            generator = new PlanGenerator();
            generator.generate(landCopy, constraintManager);

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        drawingPanel.setLand(landCopy);
        drawingPanel.updatePreferredSize();
        drawingPanel.repaint();

        Validator validator = new Validator();
        List<String> errors = validator.validate(landCopy, generator.getRemappedConstraints());
        if (errors.isEmpty()) {
            reportArea.setText("=== PLAN GÉNÉRÉ AVEC SUCCÈS ===\nLe plan a été généré sans aucune erreur ou avertissement.");
            reportArea.setForeground(Colors.REPORT_SUCCESS);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RAPPORT DE VALIDATION (" + errors.size() + " avertissement(s)/erreur(s)) ===\n");
            for (String err : errors) {
                sb.append("- ").append(err).append("\n");
            }
            reportArea.setText(sb.toString());
            reportArea.setForeground(Colors.REPORT_ERROR);

//            drawingPanel.setLand(new Land());
//            drawingPanel.repaint();
        }
    }

    private void setupConstraints(List<Room> rooms, ConstraintManager cm) {
        for (int i = 0; i < constraintListModel.size(); i++) {
            String s = constraintListModel.get(i).toString();
            String[] parts = s.split(",");
            Room r1 = findRoomByName(rooms, parts[0]);
            Room r2 = findRoomByName(rooms, parts[2]);
            ConstraintType type = ConstraintType.valueOf(parts[1]);
            if (r1 != null && r2 != null) {
                cm.addRelationship(new Constraint(r1, r2, type));
            }
        }
    }

    private Room findRoomByName(List<Room> rooms, String name) {
        for (Room r : rooms) if (r.getName().equals(name)) return r;
        return null;
    }

    public List<Room> getRoomsFromInput() {
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < houseListModel.size(); i++) {
            House h = houseListModel.get(i);
            for (int r = 0; r < h.getRooms().size(); r++) {
                rooms.add(h.getRooms().get(r));
            }
        }
        return rooms;
    }

    private void reset() {
        emptyHouseFields();
        drawingPanel.setLand(null);
        drawingPanel.updatePreferredSize();
        houseListModel.clear();
        constraintListModel.clear();
        roomListModel.clear();
        reportArea.setText("");
        drawingPanel.repaint();
    }

    private void showRoomListFor(House house) {
        roomListModel.clear();
        if (house == null) {
            return;
        }

        List<Room> rooms = house.getRooms();
        for (Room r : rooms) {
            roomListModel.addElement(r);
        }
    }

    private House createHouseFromInputs() throws NumberFormatException {
        House house = null;

        String name = houseNameField.getText();
        double w = Double.parseDouble(houseWidthField.getText());
        double h = Double.parseDouble(houseHeightField.getText());
        double x = Double.parseDouble(houseXField.getText());
        double y = Double.parseDouble(houseYField.getText());

        try {
            house = new House(name, x, y, w, h);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return house;
        }

        return house;
    }

    private void updateHouseFromInputs(House house) throws NumberFormatException {
        house.setName(houseNameField.getText());
        house.setWidth(Double.parseDouble(houseWidthField.getText()));
        house.setHeight(Double.parseDouble(houseHeightField.getText()));
        house.setX(Double.parseDouble(houseXField.getText()));
        house.setY(Double.parseDouble(houseYField.getText()));
    }

    private void fillHouseFieldsWithValueOf(House h) {
        houseNameField.setText(h.getName());
        houseWidthField.setText(String.valueOf(h.getWidth()));
        houseHeightField.setText(String.valueOf(h.getHeight()));
        houseXField.setText(String.valueOf(h.getX()));
        houseYField.setText(String.valueOf(h.getY()));
    }

    private void emptyHouseFields() {
        houseNameField.setText("");
        houseWidthField.setText("");
        houseHeightField.setText("");
        houseXField.setText("");
        houseYField.setText("");
    }
}
