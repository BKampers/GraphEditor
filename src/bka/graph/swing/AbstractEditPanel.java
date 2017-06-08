/*
** Copyright © Bart Kampers
*/


package bka.graph.swing;


public abstract class AbstractEditPanel extends javax.swing.JPanel {

    
    abstract public void confirm();
    
    
    public void setEnvironment(GraphEditor environment) {
       this.environment = environment;
    }
    
    
    public void setEditDialog(EditDialog editDialog) {
        this.editDialog = editDialog;
    }
    
    
    public EditDialog getEditDialog() {
        return editDialog;
    }
    
    
    protected GraphEditor environment = null;
    
    private EditDialog editDialog;
    
}
