/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class EditDialog extends JDialog {


    public EditDialog(Frame owner, String title, AbstractEditPanel abstractEditPanel) {
        super(owner, title, true);
        initialize(abstractEditPanel);
    }
    
    
    public EditDialog(Dialog owner, String title, AbstractEditPanel abstractEditPanel) {
        super(owner, title, true);
        initialize(abstractEditPanel);
    }
    
    
    public void show(Point point) {
        setLocation(point);
        setVisible(true);
    }
    
    
    public void enableOkButton(boolean enabled) {
        okButton.setEnabled(enabled);
    }


    private void initialize(AbstractEditPanel abstractEditPanel) {
        contentPanel = abstractEditPanel;
        contentPanel.setEditDialog(this);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(contentPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        Dimension preferredSize = new Dimension(100, 25);
        okButton = new JButton("Ok");
        okButton.setPreferredSize(preferredSize);
        okButton.addActionListener((ActionEvent evt) -> {
            contentPanel.confirm();
            dispose();
        });
        buttonPanel.add(okButton);
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(preferredSize);
        cancelButton.addActionListener((ActionEvent evt) -> {
            dispose();
        });
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        pack();
    }
    
    
    private AbstractEditPanel contentPanel;
    
    private JButton okButton;
    private JButton cancelButton;
    
}
