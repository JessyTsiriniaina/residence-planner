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
    private Land land;
    private ConstraintManager constraintManager;
    private PlanGenerator generator;
    private Validator validator;
    private ScaleConverter scaleConverter;


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
    private CardLayout cardLayout;

    private final Constraint constraintMananagement = new Constraint(this);
    private final RoomManagement roomManagement = new RoomManagement(this);

    private final JPanel constraintManagementPanel = constraintMananagement.getConstraintPanel();
    private final JPanel roomManagementPanel = roomManagement.getRoomManagementPanel();
    private final JPanel emptyPanel = new JPanel();

    private final String EMPTY = "EMPTY";
    private final String ROOM = "ROOM";
    private final String CONSTRAINT = "CONSTRAINT";

    private DefaultListModel<Room> roomListModel = new DefaultListModel<>();
    private DefaultListModel<io.github.jessytsiriniaina.model.Constraint> constraintListModel = new DefaultListModel<>();



    private void createUIComponents() {
        drawingPanel = new DrawingPanel(new Land(), new ScaleConverter());
    }

    public MainFrame() {
        this.cardLayout = (CardLayout) changingPanel.getLayout();
        scaleConverter =  new ScaleConverter();
        constraintManager = new ConstraintManager();
        generator = new PlanGenerator();
        land = new Land();

        setup();

        scaleField.setText("10");
        landWidthField.setText("50");
        landHeightField.setText("50");
        houseWidthField.setText("30");
        houseHeightField.setText("30");
        houseXField.setText("10");
        houseYField.setText("10");

        addRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showRoomManagementPanel(null);
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
                        io.github.jessytsiriniaina.model.Constraint actualConstraint = constraintListModel.get(i);

                        if (Objects.equals(actualConstraint.getRoom1(), roomToDelete)
                                || Objects.equals(actualConstraint.getRoom2(), roomToDelete)) {
                            constraintListModel.remove(i);
                        }
                    }

                    roomListModel.remove(selected);
                }
            }
        });
        removeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selected = constraintList.getSelectedIndex();
                if(selected != -1) constraintListModel.remove(selected);
            }
        });
        constraintList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    int index = constraintList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        showConstraintManagementPanel(constraintListModel.getElementAt(index));
                    }
                }
            }
        });
        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    int index = roomList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        showRoomManagementPanel(roomListModel.getElementAt(index));
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
    }

    private void setup() {
        setContentPane(mainPanel);
        setTitle("Residence Planer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack(); // Adjust frame size based on content | Can be replaced with setSize()
        setVisible(true);

        changingPanel.add(emptyPanel, EMPTY);
        changingPanel.add(constraintManagementPanel, CONSTRAINT);
        changingPanel.add(roomManagementPanel, ROOM);

        hideChangingPanel();

        roomList.setModel(roomListModel);
        constraintList.setModel(constraintListModel);

        roomListModel.addElement(new Room("Chambre 1", 12, 12));
        roomListModel.addElement(new Room("Salon", 12, 12));
        roomListModel.addElement(new Room("Cuisine", 12, 12));
    }

    public void hideChangingPanel() {
        cardLayout.show(changingPanel, EMPTY);
        changingPanel.setVisible(false);
    }

    private void showRoomManagementPanel(Room room) {
        if(!(room == null)) {
            roomManagement.setExistingRoom(room);
        }
        changingPanel.setVisible(true);
        cardLayout.show(changingPanel, ROOM);
    }

    private void showConstraintManagementPanel(io.github.jessytsiriniaina.model.Constraint constraint) {
        if(!(constraint == null)) {
            constraintMananagement.setExistingConstraint(constraint);
        }
        changingPanel.setVisible(true);
        cardLayout.show(changingPanel, CONSTRAINT);
    }

    public void addRoom(Room room) {
        roomListModel.addElement(room);
    }

    public DefaultListModel<Room> getRoomListModel() {
        return roomListModel;
    }

    public void addConstraint(io.github.jessytsiriniaina.model.Constraint constraint) {
        constraintListModel.addElement(constraint);
    }

    public void updateConstraintList() {
        constraintList.updateUI();
    }

    public DefaultListModel<io.github.jessytsiriniaina.model.Constraint> getConstraintListModel() {
        return constraintListModel;
    }

    public void updateRoomList() {
        roomList.updateUI();
    }

    private void generatePlan() {
        double scale = Double.parseDouble(scaleField.getText());
        double landWidth = Double.parseDouble((landWidthField.getText()));
        double landHeight = Double.parseDouble((landHeightField.getText()));
        double houseWidth = Double.parseDouble(houseWidthField.getText());
        double houseHeight = Double.parseDouble(houseHeightField.getText());
        double houseX = Double.parseDouble(houseXField.getText());
        double houseY = Double.parseDouble(houseYField.getText());

        scaleConverter.setPixelsPerMeter((int) scale);

        land.setWidth(landWidth);
        land.setHeight(landHeight);
        House house = new House(houseX, houseY, houseWidth, houseHeight);
        land.setHouse(house);

        drawingPanel.setLand(land);
        drawingPanel.setScaleConverter(scaleConverter);

        List<Room> rooms = getRoomsFromInput();
        constraintManager.clear();
        setupConstraints(rooms, constraintManager);

        generator.generate(land, rooms, constraintManager);
        drawingPanel.repaint();
    }

    private void setupConstraints(List<Room> rooms, ConstraintManager cm) {
        for (int i = 0; i < constraintListModel.size(); i++) {
            String s = constraintListModel.get(i).toString();
            String[] parts = s.split(",");
            Room r1 = findRoomByName(rooms, parts[0]);
            Room r2 = findRoomByName(rooms, parts[2]);
            ConstraintType type = ConstraintType.valueOf(parts[1]);
            if (r1 != null && r2 != null) {
                cm.addRelationship(new io.github.jessytsiriniaina.model.Constraint(r1, r2, type));
            }
        }
    }

    private Room findRoomByName(List<Room> rooms, String name) {
        for (Room r : rooms) if (r.getName().equals(name)) return r;
        return null;
    }

    public List<Room> getRoomsFromInput() {
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < roomListModel.size(); i++) {
            rooms.add(roomListModel.get(i));
        }
        return rooms;
    }

    private void reset() {
        land.setHouse(null);
        constraintListModel.clear();
        roomListModel.clear();
        constraintManager.clear();
        //bottomPanel.clear();
        drawingPanel.repaint();
    }
}
