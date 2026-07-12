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

    // Programmatic Multi-house UI Components
    private DefaultListModel<House> houseListModel = new DefaultListModel<>();
    private JList<House> houseListUI;

    private void createUIComponents() {
        drawingPanel = new DrawingPanel(new Land(), new ScaleConverter());
    }

    public MainFrame() {
        this.cardLayout = (CardLayout) changingPanel.getLayout();
        scaleConverter =  new ScaleConverter();
        constraintManager = new ConstraintManager();
        generator = new PlanGenerator();
        validator = new Validator();
        land = new Land();

        setup();

        scaleField.setText("10");
        landWidthField.setText("50");
        landHeightField.setText("50");
        houseWidthField.setText("30");
        houseHeightField.setText("30");
        houseXField.setText("10");
        houseYField.setText("10");

        // Set up initial default house
        House defaultHouse = new House(10.0, 10.0, 30.0, 30.0);
        defaultHouse.setName("Maison 1");
        houseListModel.addElement(defaultHouse);

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

        // Reorganize housePanel programmatically to support multiple houses
        buildMultiHousePanel();
    }

    private void buildMultiHousePanel() {
        housePanel.setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Dimension inputs row
        JPanel inputsPanel = new JPanel(new GridLayout(2, 4, 4, 4));
        inputsPanel.add(new JLabel("L (m):"));
        inputsPanel.add(houseWidthField);
        inputsPanel.add(new JLabel("x (m):"));
        inputsPanel.add(houseXField);

        inputsPanel.add(new JLabel("H (m):"));
        inputsPanel.add(houseHeightField);
        inputsPanel.add(new JLabel("y (m):"));
        inputsPanel.add(houseYField);

        container.add(inputsPanel);

        // Control Buttons
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addHouseButton = new JButton("Ajouter Maison");
        JButton removeHouseButton = new JButton("Supprimer Maison");
        actionButtonsPanel.add(addHouseButton);
        actionButtonsPanel.add(removeHouseButton);
        container.add(actionButtonsPanel);

        // Houses List
        houseListUI = new JList<>(houseListModel);
        JScrollPane scrollPane = new JScrollPane(houseListUI);
        scrollPane.setPreferredSize(new Dimension(180, 80));
        container.add(scrollPane);

        housePanel.removeAll();
        housePanel.add(container, BorderLayout.CENTER);

        // Add Listeners
        addHouseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double w = Double.parseDouble(houseWidthField.getText());
                    double h = Double.parseDouble(houseHeightField.getText());
                    double x = Double.parseDouble(houseXField.getText());
                    double y = Double.parseDouble(houseYField.getText());

                    House newHouse = new House(x, y, w, h);
                    newHouse.setName("Maison " + (houseListModel.size() + 1));
                    houseListModel.addElement(newHouse);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Veuillez entrer des valeurs numériques valides.");
                }
            }
        });

        removeHouseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = houseListUI.getSelectedIndex();
                if (selected != -1) {
                    if (houseListModel.size() > 1) {
                        houseListModel.remove(selected);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this, "Il doit y avoir au moins une maison.");
                    }
                }
            }
        });

        houseListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    House h = houseListUI.getSelectedValue();
                    if (h != null) {
                        houseWidthField.setText(String.valueOf(h.getWidth()));
                        houseHeightField.setText(String.valueOf(h.getHeight()));
                        houseXField.setText(String.valueOf(h.getX()));
                        houseYField.setText(String.valueOf(h.getY()));
                    }
                }
            }
        });
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
        double scale;
        double landWidth;
        double landHeight;
        try {
            scale = Double.parseDouble(scaleField.getText());
            landWidth = Double.parseDouble(landWidthField.getText());
            landHeight = Double.parseDouble(landHeightField.getText());
            if (scale <= 0 || landWidth <= 0 || landHeight <= 0) {
                JOptionPane.showMessageDialog(this, "L'échelle et les dimensions du terrain doivent être positives.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des valeurs numériques valides pour l'échelle et les dimensions du terrain.");
            return;
        }

        scaleConverter.setPixelsPerMeter((int) scale);

        land.setWidth(landWidth);
        land.setHeight(landHeight);

        // Retrieve and populate all houses into Land
        List<House> houses = new ArrayList<>();
        for (int i = 0; i < houseListModel.size(); i++) {
            houses.add(houseListModel.get(i));
        }
        land.setHouses(houses);

        drawingPanel.setLand(land);
        drawingPanel.setScaleConverter(scaleConverter);

        List<Room> rooms = getRoomsFromInput();
        constraintManager.clear();
        setupConstraints(rooms, constraintManager);

        generator.generate(land, rooms, constraintManager);
        drawingPanel.repaint();

        // Run validation and report feedback
        List<String> errors = validator.validate(land, constraintManager);
        if (errors.isEmpty()) {
            reportArea.setText("=== PLAN GÉNÉRÉ AVEC SUCCÈS ===\nFélicitations ! Le plan a été généré sans aucune erreur ou avertissement.");
            reportArea.setForeground(new Color(34, 139, 34)); // Dark green
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RAPPORT DE VALIDATION (" + errors.size() + " avertissement(s)/erreur(s)) ===\n");
            for (String err : errors) {
                sb.append("- ").append(err).append("\n");
            }
            reportArea.setText(sb.toString());
            reportArea.setForeground(Color.RED);
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
        land.setHouses(new ArrayList<>());
        houseListModel.clear();
        House defaultHouse = new House(10.0, 10.0, 30.0, 30.0);
        defaultHouse.setName("Maison 1");
        houseListModel.addElement(defaultHouse);

        constraintListModel.clear();
        roomListModel.clear();
        constraintManager.clear();
        drawingPanel.repaint();
        reportArea.setText("");
    }
}
