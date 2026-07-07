package io.github.jessytsiriniaina.gui;

import io.github.jessytsiriniaina.logic.ConstraintManager;
import io.github.jessytsiriniaina.logic.PlanGenerator;
import io.github.jessytsiriniaina.logic.ScaleConverter;
import io.github.jessytsiriniaina.logic.Validator;
import io.github.jessytsiriniaina.model.House;
import io.github.jessytsiriniaina.model.Land;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private JTextField landWidth;
    private JTextField landHeight;
    private JPanel scalePanel;
    private JTextField scale;
    private JPanel housePanel;
    private JTextField houseWidth;
    private JTextField houseHeight;
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
    private CardLayout cardLayout;
    JPanel constraintManagementPanel = new Constraint(this).getConstraintPanel();
    JPanel roomManagementPanel = new RoomManagement(this).getRoomManagementPanel();
    JPanel emptyPanel = new JPanel();

    private static final String EMPTY = "EMPTY";
    private static final String ROOM = "ROOM";
    private static final String CONSTRAINT = "CONSTRAINT";

    private void createUIComponents() {
        drawingPanel = new DrawingPanel();
    }

    public MainFrame() {
        this.cardLayout = (CardLayout) changingPanel.getLayout();
        setup();
        addRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showRoomManagementPanel();
            }
        });
        addConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showConstraintManagementPanel();
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
    }

    public void hideChangingPanel() {
        cardLayout.show(changingPanel, EMPTY);
        changingPanel.setVisible(false);
    }

    private void showRoomManagementPanel() {
        changingPanel.setVisible(true);
        cardLayout.show(changingPanel, ROOM);
    }

    private void showConstraintManagementPanel() {
        changingPanel.setVisible(true);
        cardLayout.show(changingPanel, CONSTRAINT);
    }
}
