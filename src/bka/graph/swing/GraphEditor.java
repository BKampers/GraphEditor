/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import bka.awt.*;
import bka.graph.*;
import bka.graph.document.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;


public class GraphEditor extends bka.swing.FrameApplication {


    public interface OnLoadDelegate {
        void onLoad();
    }
    
    
    public GraphEditor() {
        initComponents();
        addGraphButtons();
        vertexTreePanel = new VertexTreePanel(this);
        historyPanel = new HistoryPanel();
        diagramSplitPane.setLeftComponent(vertexTreePanel);
        diagramTabbedPane.addChangeListener(new DiagramTabChangeListener());
        documentPanelPanel.add(historyPanel);
    }


    public Book getBook() {
        return book;
    }


    public void addVertexButton(String name, Class vertexPictureClass) {
        JToggleButton button = new JToggleButton(name);
        button.addActionListener(pictureButtonListener);
        vertexPictureClasses.put(button, vertexPictureClass);
        pictureButtonPanel.add(button);
    }

    
    public void addEdgeButton(String name, Class edgePictureClass) {
        JToggleButton button = new JToggleButton(name);
        button.addActionListener(pictureButtonListener);
        edgePictureClasses.put(button, edgePictureClass);
        pictureButtonPanel.add(button);        
    }
    

    @Override
    public String applicationName() {
        return "GraphEditor";
    }


    @Override
    public String manufacturerName() {
        return "BartK";
    }


    public static void main(final String[] arguments) {
        EventQueue.invokeLater(() -> {
            DrawStyle drawStyle = new DrawStyle();
            drawStyle.setColor(AbstractPicture.FILL, Color.BLACK);
            drawStyle.setColor(AbstractPicture.DRAW, Color.BLACK);
            drawStyle.setStroke(AbstractPicture.DRAW, new BasicStroke());
            drawStyle.setStroke(EdgePicture.ARROW_HEAD, new BasicStroke());
            DrawStyleManager.getInstance().setDrawStyle(AbstractPicture.class, drawStyle);
            GraphEditor frame = new GraphEditor();
//                frame.initialize(arguments);
            frame.setVisible(true);
        });
    }


    protected Map<String, Class<? extends VertexPicture>> getVertexButtons() {
        HashMap<String, Class<? extends VertexPicture>> map = new HashMap<>();
        map.put("Vertex", VertexPicture.class);
        return map;
    }


    protected Map<String, Class<? extends EdgePicture>> getEdgeButtons() {
        HashMap<String, Class<? extends EdgePicture>> map = new HashMap<>();
        map.put("Edge", EdgePicture.class);
        return map;
    }


    @Override
    protected void opened() {
        book = new Book(getPersistenceDelegates());
        Object path = getProperty(DIAGRAM_FILE_PROPERTY);
        if (path != null) {
            diagramFile = new File(path.toString());
            load();
        }
        if (diagramFile != null && diagramFile.isDirectory()) {
            createEmptyBook();
        }
        else {
            updateFileStatus();
        }
    }
    
    
    protected Class selectedVertexPictureClass() {
        for (Map.Entry<JToggleButton, Class> entry : vertexPictureClasses.entrySet()) {
            if (entry.getKey().isSelected()) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    
    protected Class selectedEdgePictureClass() {
        for (Map.Entry<JToggleButton, Class> entry : edgePictureClasses.entrySet()) {
            if (entry.getKey().isSelected()) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    
    protected String toolTipText(VertexPicture picture) {
        return null;
    }


    protected String toolTipText(EdgePicture picture) {
        return null;
    }
    
    
    protected void vertexPictureAdded(DiagramComponent diagramComponent, VertexPicture vertexPicture) {
        vertexTreePanel.vertexAdded(vertexPicture, getSelectedDiagramComponent());
    }
    
    
    protected void vertexPictureRemoved(VertexPicture vertexPicture) {
        vertexTreePanel.vertexRemoved(vertexPicture);
    }
    
    
    protected void vertexPictureModified(VertexPicture vertexPicture) {
        vertexTreePanel.vertexModified(vertexPicture);
    }
    

    protected void vertexPictureClicked(VertexPicture vertexPicture, int count) {
    }


    protected void edgePictureAdded(DiagramComponent diagramComponent, EdgePicture edgePicture) {
    }


    protected void edgePictureModified(EdgePicture edgePicture) {
    }


    protected void edgePictureRemoved(EdgePicture edgePicture) {
    }


    protected void setHighlighted(AbstractPicture picture, DrawStyle drawStyle) {
        getSelectedDiagramComponent().setHighlighted(picture, drawStyle);
    }


    protected void resetHighlighted(AbstractPicture picture, DrawStyle drawStyle) {
        getSelectedDiagramComponent().resetHighlighted(picture, drawStyle);
    }


    protected void resetHighlighted(DrawStyle drawStyle) {
        getSelectedDiagramComponent().resetHighlighted(drawStyle);
    }


    protected VertexPicture getVertexPicture(Vertex vertex) {
        DiagramComponent diagramComponent = getSelectedDiagramComponent();
        if (diagramComponent != null) {
            for (VertexPicture picture : diagramComponent.getVertexPictures()) {
                if (vertex == picture.getVertex()) {
                    return picture;
                }
            }
        }
        return null;
    }
    
    
    protected JPopupMenu getEdgeMenu(final EdgePicture picture) {
        JPopupMenu menu = new JPopupMenu();
        for (String paintKey : picture.getCustomizablePaints()) {
            JMenuItem colorItem = new JMenuItem("Color: " + paintKey);
            colorItem.addActionListener((ActionEvent evt) -> {
                modifyColor(picture, paintKey);
            });
            menu.add(colorItem);
        }
        return menu;
    }


    protected Map<Class, java.beans.PersistenceDelegate> getPersistenceDelegates() {
        return null;
    }
    

    protected void diagramRepaint() {
        getSelectedDiagramComponent().repaint();
    }
    
    
    protected void clearHoverInfo() {
        getSelectedDiagramComponent().clearHoverInfo();
    }


    void diagramEntered(MouseEvent evt) {
        vertexTreePanel.diagramEntered(evt);
    }
    
    
    void diagramExited() {
        vertexTreePanel.diagramExited();
    }


    ArrayList<DiagramComponent> getDiagramComponents() {
        ArrayList<DiagramComponent> components = new ArrayList<>();
        int count = diagramTabbedPane.getTabCount();
        for (int i = 0; i < count; ++i) {
            components.add(diagramComponent(i));
        }
        return components;
    }
    
    
    void setSelected(DiagramComponent diagramComponent) {
        int index = indexOf(diagramComponent);
        if (index >= 0) {
            diagramTabbedPane.setSelectedIndex(index);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        diagramPopupMenu = new javax.swing.JPopupMenu();
        newDiagramMenuItem = new javax.swing.JMenuItem();
        renameDiagramMenuItem = new javax.swing.JMenuItem();
        diagramMoveLeftMenuItem = new javax.swing.JMenuItem();
        diagramMoveRightMenuItem = new javax.swing.JMenuItem();
        deleteDiagramMenuItem = new javax.swing.JMenuItem();
        pictureButtonPanel = new javax.swing.JPanel();
        documentPanelPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        diagramSplitPane = new javax.swing.JSplitPane();
        diagramTabbedPane = new javax.swing.JTabbedPane();

        newDiagramMenuItem.setText("New diagram");
        newDiagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDiagramMenuItem_actionPerformed(evt);
            }
        });
        diagramPopupMenu.add(newDiagramMenuItem);

        renameDiagramMenuItem.setText("Rename");
        renameDiagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameDiagramMenuItem_actionPerformed(evt);
            }
        });
        diagramPopupMenu.add(renameDiagramMenuItem);

        diagramMoveLeftMenuItem.setText("Move left");
        diagramMoveLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diagramMoveLeftMenuItem_actionPerformed(evt);
            }
        });
        diagramPopupMenu.add(diagramMoveLeftMenuItem);

        diagramMoveRightMenuItem.setText("Move right");
        diagramMoveRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diagramMoveRightMenuItem_actionPerformed(evt);
            }
        });
        diagramPopupMenu.add(diagramMoveRightMenuItem);

        deleteDiagramMenuItem.setText("Delete");
        deleteDiagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteDiagramMenuItem_actionPerformed(evt);
            }
        });
        diagramPopupMenu.add(deleteDiagramMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pictureButtonPanel.setPreferredSize(new java.awt.Dimension(10, 80));
        getContentPane().add(pictureButtonPanel, java.awt.BorderLayout.NORTH);

        documentPanelPanel.setLayout(new javax.swing.BoxLayout(documentPanelPanel, javax.swing.BoxLayout.Y_AXIS));

        newButton.setText("New");
        newButton.setMaximumSize(new java.awt.Dimension(85, 23));
        newButton.setMinimumSize(new java.awt.Dimension(85, 23));
        newButton.setPreferredSize(new java.awt.Dimension(85, 23));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButton_actionPerformed(evt);
            }
        });
        documentPanelPanel.add(newButton);

        loadButton.setText("Load");
        loadButton.setMaximumSize(new java.awt.Dimension(85, 23));
        loadButton.setMinimumSize(new java.awt.Dimension(85, 23));
        loadButton.setPreferredSize(new java.awt.Dimension(85, 23));
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButton_actionPerformed(evt);
            }
        });
        documentPanelPanel.add(loadButton);

        saveButton.setText("Save");
        saveButton.setMaximumSize(new java.awt.Dimension(85, 23));
        saveButton.setMinimumSize(new java.awt.Dimension(85, 23));
        saveButton.setPreferredSize(new java.awt.Dimension(85, 23));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButton_actionPerformed(evt);
            }
        });
        documentPanelPanel.add(saveButton);

        saveAsButton.setText("Save as");
        saveAsButton.setMaximumSize(new java.awt.Dimension(85, 23));
        saveAsButton.setMinimumSize(new java.awt.Dimension(85, 23));
        saveAsButton.setPreferredSize(new java.awt.Dimension(85, 23));
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsButton_actionPerformed(evt);
            }
        });
        documentPanelPanel.add(saveAsButton);

        getContentPane().add(documentPanelPanel, java.awt.BorderLayout.EAST);

        diagramSplitPane.setDividerLocation(100);

        diagramTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        diagramTabbedPane.setMinimumSize(new java.awt.Dimension(100, 100));
        diagramTabbedPane.setPreferredSize(null);
        diagramTabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                diagramTabbedPane_mouseClicked(evt);
            }
        });
        diagramSplitPane.setRightComponent(diagramTabbedPane);

        getContentPane().add(diagramSplitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private DiagramComponent getSelectedDiagramComponent() {
        int index = diagramTabbedPane.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        return diagramComponent(index);
    }


    private void addGraphButtons() {
        for (Map.Entry<String, Class<? extends VertexPicture>> entry : getVertexButtons().entrySet()) {
            addVertexButton(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Class<? extends EdgePicture>> entry : getEdgeButtons().entrySet()) {
            addEdgeButton(entry.getKey(), entry.getValue());
        }
    }


    private void saveButton_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButton_actionPerformed
        save();
    }//GEN-LAST:event_saveButton_actionPerformed

 
    private void diagramTabbedPane_mouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_diagramTabbedPane_mouseClicked
        int index = diagramTabbedPane.getSelectedIndex();
        book.setPageIndex(index);
        if (evt.getButton() == MouseEvent.BUTTON3 && evt.getClickCount() == 1) {
            diagramMoveLeftMenuItem.setEnabled(0 < index);
            diagramMoveRightMenuItem.setEnabled(index < diagramTabbedPane.getTabCount() - 1);
            diagramPopupMenu.show(this, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_diagramTabbedPane_mouseClicked

    
    private void renameDiagramMenuItem_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameDiagramMenuItem_actionPerformed
        renameDiagram();
    }//GEN-LAST:event_renameDiagramMenuItem_actionPerformed

    
    private void newDiagramMenuItem_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDiagramMenuItem_actionPerformed
        int index = diagramTabbedPane.getSelectedIndex() + 1;
        DiagramPage page = DiagramPage.createEmpty();
        addDiagramTab(new DiagramComponent(this, page), index);
        diagramTabbedPane.setSelectedIndex(index);
        renameDiagram();
        book.addPage(page);
        book.setPageIndex(index);
    }//GEN-LAST:event_newDiagramMenuItem_actionPerformed

    
    private void diagramMoveLeftMenuItem_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diagramMoveLeftMenuItem_actionPerformed
        int index = diagramTabbedPane.getSelectedIndex();
        Component component = diagramTabbedPane.getComponentAt(index);
        diagramTabbedPane.remove(component);
        diagramTabbedPane.add(component, index - 1);
        updateTabTitle(index - 1);
        updateTabTitle(index);
        diagramTabbedPane.setSelectedIndex(index - 1);
        vertexTreePanel.rebuild();
    }//GEN-LAST:event_diagramMoveLeftMenuItem_actionPerformed

    
    private void diagramMoveRightMenuItem_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diagramMoveRightMenuItem_actionPerformed
        int index = diagramTabbedPane.getSelectedIndex();
        Component component = diagramTabbedPane.getComponentAt(index);
        diagramTabbedPane.remove(component);
        diagramTabbedPane.add(component, index + 1);
        updateTabTitle(index);
        updateTabTitle(index + 1);
        diagramTabbedPane.setSelectedIndex(index + 1);
        vertexTreePanel.rebuild();
    }//GEN-LAST:event_diagramMoveRightMenuItem_actionPerformed

    
    private void deleteDiagramMenuItem_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDiagramMenuItem_actionPerformed
        diagramTabbedPane.remove(diagramTabbedPane.getSelectedComponent());
        vertexTreePanel.rebuild();        
    }//GEN-LAST:event_deleteDiagramMenuItem_actionPerformed

    
    private void saveAsButton_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsButton_actionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(XML_FILE_FILTER);
        File file = (diagramFile != null && ! diagramFile.isDirectory()) ? diagramFile : new File(diagramFile, "." + XML_EXTENSION);
        fileChooser.setSelectedFile(file);
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            diagramFile = fileChooser.getSelectedFile();
            String path = diagramFile.getPath();
            String extension = "." + XML_EXTENSION;
            if (! path.toLowerCase().endsWith(extension)) {
                path += extension;
                diagramFile = new File(path);
            }
            setProperty(DIAGRAM_FILE_PROPERTY, path);
            save();
            updateFileStatus();
        }
    }//GEN-LAST:event_saveAsButton_actionPerformed

    
    private void loadButton_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButton_actionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(XML_FILE_FILTER);
        fileChooser.setSelectedFile(diagramFile);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            diagramFile = fileChooser.getSelectedFile();
            setProperty(DIAGRAM_FILE_PROPERTY, diagramFile.getPath());
            diagramTabbedPane.removeAll();
            load();
            updateFileStatus();
        }
    }//GEN-LAST:event_loadButton_actionPerformed

    
    private void newButton_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButton_actionPerformed
        createEmptyBook();
    }//GEN-LAST:event_newButton_actionPerformed


    private void updateTabTitle(int tabIndex) {
        DiagramComponent diagramComponent = diagramComponent(tabIndex);
        diagramTabbedPane.setTitleAt(tabIndex, diagramComponent.getTitle());
        vertexTreePanel.diagramModified(diagramComponent);
    }
    
    private void renameDiagram() {
        final int index = diagramTabbedPane.getSelectedIndex();
        Rectangle bounds = diagramTabbedPane.getBoundsAt(index);
        bka.swing.PopupTextField popup = new bka.swing.PopupTextField(diagramTabbedPane.getTitleAt(index), bounds, EDIT_MIN_WIDTH);
        popup.addListener((String text) -> {
            diagramComponent(index).setTitle(text);
            updateTabTitle(index);
        });
        popup.show(diagramTabbedPane);
    }

    
    private void modifyColor(AbstractPicture picture, Object key) {
        DrawStyle drawStyle = DrawStyleManager.getInstance().getDrawStyle(picture);
        Color color = null;
        if (drawStyle != null) {
            color = drawStyle.getColor(key);
        }
        Color newColor = JColorChooser.showDialog(this, "Pick Color", color);
        if (newColor != null) {
            drawStyle = new DrawStyle(drawStyle);
            drawStyle.setColor(key, newColor);
            DrawStyleManager.getInstance().setDrawStyle(picture, drawStyle);
        }
        getSelectedDiagramComponent().clearHoverInfo();
    }
    

    private void createEmptyBook() {
        diagramTabbedPane.removeAll();
        book = new Book(getPersistenceDelegates());
        DiagramPage page = DiagramPage.createEmpty();
        book.addPage(page);
        addDiagramTab(new DiagramComponent(this, page));
        resetDiagramFile();
        updateFileStatus();
    }


    private void load() {
        try {
            book.load(diagramFile);
            ArrayList<DiagramPage> pages = book.getDiagramPages();
            for (DiagramPage page : pages) {
                DiagramComponent diagramComponent = new DiagramComponent(this, page);
                for (VertexPicture vertexPicture : diagramComponent.getVertexPictures()) {
                    vertexPicture.initAttachmentPoints();
                }
                addDiagramTab(diagramComponent);
            }
            diagramTabbedPane.setSelectedIndex(book.getPageIndex());
            vertexTreePanel.rebuild();
            if (onLoadDelegate != null) {
                onLoadDelegate.onLoad();
            }
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(GraphEditor.class.getName()).log(Level.INFO, diagramFile.toString(), ex);
            JOptionPane.showMessageDialog(this, "'" + diagramFile.getPath() + "' not found", "File not found", JOptionPane.ERROR_MESSAGE);
            resetDiagramFile();
        }
        catch (RuntimeException ex) {
            Logger.getLogger(GraphEditor.class.getName()).log(Level.SEVERE, diagramFile.toString(), ex);
            JOptionPane.showMessageDialog(this, "Error loading '" + diagramFile.getPath() + "'", "File error", JOptionPane.ERROR_MESSAGE);
            resetDiagramFile();
        }
    }

        
    private void save() {
        try {
            book.save(diagramFile);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(GraphEditor.class.getName()).log(Level.SEVERE, diagramFile.toString(), ex);
            JOptionPane.showMessageDialog(this, "Could not save '" + diagramFile.getPath() + "'", "File error", JOptionPane.ERROR_MESSAGE);
        }
        catch (RuntimeException ex) {
            Logger.getLogger(GraphEditor.class.getName()).log(Level.SEVERE, diagramFile.toString(), ex);
            JOptionPane.showMessageDialog(this, "Error saving '" + diagramFile.getPath() + "'", "File error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void updateFileStatus() {
        if (diagramFile != null  && ! diagramFile.isDirectory()) {
            setTitle(applicationName() + ": " + diagramFile.getName());
            saveButton.setEnabled(true);
        }
        else {
            setTitle(applicationName());
            saveButton.setEnabled(false);
        }
    }
    
    
    private void resetDiagramFile() {
        if (diagramFile != null && ! diagramFile.isDirectory()) {
            diagramFile = diagramFile.getParentFile();
        }
    }


    private int indexOf(DiagramComponent diagramComponent) {
        int count = diagramTabbedPane.getTabCount();
        for (int index = 0; index < count; ++index) {
            DiagramComponent component = diagramComponent(index);
            if (component == diagramComponent) {
                return index;
            }
        }
        return -1;
    }
    
    
    private DiagramComponent diagramComponent(int index) {
        JScrollPane pane = (JScrollPane) diagramTabbedPane.getComponentAt(index);
        return diagramComponent(pane);
    }
    
    
    private DiagramComponent diagramComponent(JScrollPane pane) {
        if (pane == null) {
            return null;
        }
        return (DiagramComponent) pane.getViewport().getComponent(0);
    }
    
    
    private void addDiagramTab(DiagramComponent diagramComponent) {
        diagramTabbedPane.addTab(diagramComponent.getTitle(), createDiagramPane(diagramComponent));
        vertexTreePanel.rebuild();
    }
    
    
    private void addDiagramTab(DiagramComponent diagramComponent, int index) {
        diagramTabbedPane.add(createDiagramPane(diagramComponent), index);
        vertexTreePanel.rebuild();
    }
    
    
    private JScrollPane createDiagramPane(DiagramComponent diagramComponent) {
        return new JScrollPane(diagramComponent);
    }


    private class DiagramTabChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent evt) {
            DiagramComponent diagramComponent = diagramComponent(diagramTabbedPane.getSelectedIndex());
            historyPanel.setDrawHistory(diagramComponent.getDrawHistory());
            diagramComponent.requestFocus();
        }

    }


    private class PictureButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            Object clickedButton = evt.getSource();
            for (JToggleButton button : vertexPictureClasses.keySet()) {
                if (button != clickedButton) {
                    button.setSelected(false);
                }
            }
            for (JToggleButton button : edgePictureClasses.keySet()) {
                if (button != clickedButton) {
                    button.setSelected(false);
                }
            }
        }
        
    }


    protected Book book;


    protected OnLoadDelegate onLoadDelegate;


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem deleteDiagramMenuItem;
    private javax.swing.JMenuItem diagramMoveLeftMenuItem;
    private javax.swing.JMenuItem diagramMoveRightMenuItem;
    private javax.swing.JPopupMenu diagramPopupMenu;
    private javax.swing.JSplitPane diagramSplitPane;
    private javax.swing.JTabbedPane diagramTabbedPane;
    private javax.swing.JPanel documentPanelPanel;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton newButton;
    private javax.swing.JMenuItem newDiagramMenuItem;
    private javax.swing.JPanel pictureButtonPanel;
    private javax.swing.JMenuItem renameDiagramMenuItem;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
    

//    private DiagramComponent selectedDiagramComponent;


    private final VertexTreePanel vertexTreePanel;
    private final HistoryPanel historyPanel;
    
    private final PictureButtonListener pictureButtonListener = new PictureButtonListener();


    private final Map<JToggleButton, Class> vertexPictureClasses = new HashMap<>();
    private final Map<JToggleButton, Class> edgePictureClasses = new HashMap<>();


    private File diagramFile;
    
    private static final String XML_EXTENSION = "xml";
    private static final javax.swing.filechooser.FileNameExtensionFilter XML_FILE_FILTER = new javax.swing.filechooser.FileNameExtensionFilter("XML Graphs", XML_EXTENSION);

    private static final String DIAGRAM_FILE_PROPERTY = "DiagramFile";
    
    private static final int EDIT_MIN_WIDTH = 50;

}
