package io.github.jessytsiriniaina.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Constraint {
    private JComboBox firstRoom;
    private JPanel constraintPanel;
    private JComboBox constraintBox;
    private JComboBox secondRoom;
    private JButton cancelButton;
    private JButton OKButton;

    public Constraint(MainFrame parent) {
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.hideChangingPanel();
            }
        });
    }

    public JPanel getConstraintPanel() {
        return constraintPanel;
    }
}
