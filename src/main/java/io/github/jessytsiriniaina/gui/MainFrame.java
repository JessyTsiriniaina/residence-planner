package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ConstraintManager;
import io.github.jessytsiriniaina.logic.PlanGenerator;
import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.logic.Validator;
import io.github.jessytsiriniaina.model.House;
import io.github.jessytsiriniaina.model.Land;
import io.github.jessytsiriniaina.model.Room;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class MainFrame extends JFrame {
    private Land land;
    private House house;
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
        drawingPanel = new DrawingPanel();
    }

    public MainFrame() {
        this.cardLayout = (CardLayout) changingPanel.getLayout();
        setup();

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
                if(selected != 1) constraintListModel.remove(selected);
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

    }
}
