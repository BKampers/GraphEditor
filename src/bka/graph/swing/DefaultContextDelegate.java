package bka.graph.swing;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;


public class DefaultContextDelegate<E extends GraphEditor> implements GraphEditor.ContextDelegate {

    
    public DefaultContextDelegate(E editor) {
        this.editor = editor;
    }
    
            
    @Override
    public java.util.List<JMenuItem> getVertexMenuItems(VertexPicture picture) {
        return Collections.EMPTY_LIST;
    }

    
    @Override
    public java.util.List<JMenuItem> getEdgeMenuItems(EdgePicture picture) {
        ArrayList<JMenuItem> items = new ArrayList<>();
        Object[] customizablePaints = picture.getCustomizablePaints();
        if (customizablePaints != null) {
            for (Object paintKey : customizablePaints) {
                items.add(createItem("Color: " + paintKey, (ActionEvent evt) -> {
                    editor.pickColor(picture, paintKey);
                }));
            }
        }
        return items;
    }
    
    
    protected JMenuItem createItem(String title, ActionListener listener) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(listener);
        return item;
    }


    protected JCheckBoxMenuItem createItem(String title, boolean isSelected, ActionListener listener) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(title);
        item.setSelected(isSelected);
        item.addActionListener(listener);
        return item;
    }


    protected final E editor;

}




