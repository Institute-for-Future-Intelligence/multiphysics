package org.energy2d.system;

import org.energy2d.event.ManipulationEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Charles Xie
 */
class LinksDialog extends JDialog {

    private JTextField prevSimField, nextSimField;

    LinksDialog(final System2D s2d, boolean modal) {

        super(JOptionPane.getFrameForComponent(s2d.view), "Links", modal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                s2d.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
                s2d.view.repaint();
                dispose();
            }
        });

        ActionListener okListener = e -> {
            String prevSim = prevSimField.getText();
            if (prevSim != null) {
                if (prevSim.trim().equals("")) {
                    s2d.setPreviousSimulation(null);
                } else {
                    if (!prevSim.endsWith(".e2d")) {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(s2d.view), "Previous simulation name must end with .e2d: \"" + prevSim + "\"", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    s2d.setPreviousSimulation(prevSim);
                }
            }
            String nextSim = nextSimField.getText();
            if (nextSim != null) {
                if (nextSim.trim().equals("")) {
                    s2d.setNextSimulation(null);
                } else {
                    if (!nextSim.endsWith(".e2d")) {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(s2d.view), "Next simulation name must end with .e2d: \"" + nextSim + "\"", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    s2d.setNextSimulation(nextSim);
                }
            }
            s2d.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
            s2d.view.repaint();
            dispose();
        };

        JPanel panel = new JPanel(new BorderLayout());
        setContentPane(panel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("OK");
        button.addActionListener(okListener);
        buttonPanel.add(button);

        button = new JButton("Cancel");
        button.addActionListener(e -> dispose());
        buttonPanel.add(button);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(box, BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder("Type file names (.e2d)"));
        box.add(p);

        p.add(new JLabel("Previous simulation:"));
        prevSimField = new JTextField(s2d.getPreviousSimulation(), 15);
        prevSimField.addActionListener(okListener);
        p.add(prevSimField);

        p.add(new JLabel("Next simulation:"));
        nextSimField = new JTextField(s2d.getNextSimulation(), 15);
        nextSimField.addActionListener(okListener);
        p.add(nextSimField);

        p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        box.add(p);

        p.add(new JLabel("<html><hr width=100 align=left>* When the Control Panel is present on the screen, its Previous and Next Buttons<br>will be set to link to the above files.</html>"));

        pack();
        setLocationRelativeTo(s2d.view);

    }

}