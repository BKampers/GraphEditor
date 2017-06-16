/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import bka.awt.*;
import bka.graph.document.*;
import bka.graph.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class DiagramComponent extends JComponent {
    
    
    DiagramComponent(GraphEditor editor, DiagramPage page) {
        this.editor = editor;
        this.page = page;
        initialize();
        setTitle(page.getTitle());
        pictures.addAll(page.getVertices());
        pictures.addAll(page.getEdges());
    }


    public final void addVertexPicture(VertexPicture vertexPicture, Point point) {
        addVertexPicture(vertexPicture, point, false);
    }


    public final void addVertexPicture(VertexPicture vertexPicture, Point point, boolean selected) {
        vertexPicture.initializeVertex();
        setVertexLocation(vertexPicture, point);
        addVertexPicture(vertexPicture);
        if (selected) {
            setSelected(vertexPicture);
        }
    }


    public final void removeVertexPicture(VertexPicture vertexPicture) {
        if (selectedPicture == vertexPicture) {
            selectedPicture = null;
        }
        pictures.remove(vertexPicture);
        highlights.remove(vertexPicture);
        page.remove(vertexPicture);
    }


    public final void addEdgePicture(EdgePicture edgePicture) {
        addEdgePicture(edgePicture, false);
    }


    public final void addEdgePicture(EdgePicture edgePicture, boolean selected) {
        int index = findInsertIndex(edgePicture);
        page.add(edgePicture);
        pictures.add(index, edgePicture);
        if (selected) {
            setSelected(edgePicture);
        }
    }


    public final void removeEdgePicture(EdgePicture edgePicture) {
        if (selectedPicture == edgePicture) {
            selectedPicture = null;
        }
        pictures.remove(edgePicture);
        highlights.remove(edgePicture);
        page.remove(edgePicture);
    }

    
    public final ArrayList<VertexPicture> getVertexPictures() {
        ArrayList<VertexPicture> vertices = new ArrayList<>();
        for (AbstractPicture picture : pictures) {
            if (picture instanceof VertexPicture) {
                vertices.add((VertexPicture) picture);
            }
        }
        return vertices;
    }


    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        for (AbstractPicture picture : pictures) {
            try {
                picture.paint(g2d);
            }
            catch (RuntimeException ex) {
                Logger.getLogger(DiagramComponent.class.getName()).log(Level.SEVERE, "Eelement paint", ex);
            }
        }
        for (Map.Entry<AbstractPicture, Collection<DrawStyle>> highlight : highlights.entrySet()) {
            for (DrawStyle style : highlight.getValue()) {
                Color color = style.getColor("BORDER");
                Stroke stroke = style.getStroke("BORDER");
                if (color != null && stroke != null) {
                    g2d.setPaint(color);
                    g2d.setStroke(stroke);
                    g2d.draw(highlight.getKey().getShape());
                }
            }
        }
        if (selectedPicture != null) {
            g2d.setColor(new Color(0, 0, 128, 64));
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.draw(selectedPicture.getShape());
        }
        if (attachmentPoint != null) {
            g2d.setColor(attachmentPointColor);
            g2d.fillOval(attachmentPoint.x - attachmentPointWidth / 2, attachmentPoint.y - attachmentPointHeight / 2, attachmentPointWidth, attachmentPointHeight);
        }
    }
    
    
    boolean contains(AbstractPicture picture) {
        return pictures.contains(picture);
    }


    void addVertexPictureCopy(VertexPicture vertexPicture, Point locationOnScreen) {
        if (! pictures.contains(vertexPicture)) {
            try {
                Point diagramLocation = getLocationOnScreen();
                VertexPicture copy = (VertexPicture) vertexPicture.getClass().newInstance();
                copy.setVertex(vertexPicture.getVertex());
                copy.setLocation(new Point(locationOnScreen.x - diagramLocation.x, locationOnScreen.y - diagramLocation.y));
                copy.setSize(new Dimension(vertexPicture.getSize()));
                addVertexPicture(copy);
            }
            catch (IllegalAccessException | InstantiationException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    void removeVertex(VertexPicture vertexPicture) {
        for (EdgePicture edgePicture : allEdgePictures(vertexPicture)) {
            page.remove(edgePicture);
            editor.vertexPictureRemoved(vertexPicture);
            pictures.remove(edgePicture);
        }
        page.remove(vertexPicture);
        editor.vertexPictureRemoved(vertexPicture);
    }

    
    void removeEdge(EdgePicture edgePicture) {
        page.remove(edgePicture);
        editor.edgePictureRemoved(edgePicture);
    }


    void setHighlighted(AbstractPicture picture, DrawStyle drawStyle) {
       Collection<DrawStyle> pictureHighlights = highlights.get(picture);
       if (pictureHighlights == null) {
           pictureHighlights = new ArrayList<>();
           highlights.put(picture, pictureHighlights);
       }
       pictureHighlights.add(drawStyle);
    }


    void resetHighlighted(AbstractPicture picture, DrawStyle drawStyle) {
       Collection<DrawStyle> pictureHighlights = highlights.get(picture);
       if (pictureHighlights != null) {
           pictureHighlights.remove(drawStyle);
           if (pictureHighlights.isEmpty()) {
               highlights.remove(picture);
           }
       }
    }


    void resetHighlighted(DrawStyle drawStyle) {
        for (Map.Entry<AbstractPicture, Collection<DrawStyle>> pictureHighlights : highlights.entrySet()) {
            pictureHighlights.getValue().remove(drawStyle);
       }
    }


    protected final String getTitle() {
        return page.getTitle();
    }

    
    protected final void setTitle(String title) {
        page.setTitle(title);
    }

    
    protected final void setVertexPictures(Collection<VertexPicture> vertexPictures) {
        if (vertexPictures != null) {
            pictures.addAll(vertexPictures);
            int southMost = Integer.MIN_VALUE;
            int eastMost = Integer.MIN_VALUE;
            for (AbstractPicture picture : pictures) {
                southMost = Math.max(picture.ySouth(), southMost);
                eastMost = Math.max(picture.xEast(), eastMost);
            }
            setComponentSize(eastMost, southMost);
        }
    }


    protected final ArrayList<EdgePicture> getEdgePictures() {
        ArrayList<EdgePicture> edges = new ArrayList<>();
        for (AbstractPicture picture : pictures) {
            if (picture instanceof EdgePicture) {
                edges.add((EdgePicture) picture);
            }
        }
        return edges;
    }

    
    protected final void setEdgePictures(Collection<EdgePicture> edgePictures) {
        if (edgePictures != null) {
            for (EdgePicture picture : edgePictures) {
                addEdgePicture(picture);
            }
        }
    }
    
    
    protected VertexPicture findContainer(VertexPicture vertexPicture) {
        int index = pictures.indexOf(vertexPicture);
        while (index > 0) {
            index--;
            AbstractPicture picture = pictures.get(index);
            if (picture instanceof VertexPicture && picture.isLocatedAt(vertexPicture.getLocation())) {
                return (VertexPicture) picture;
            }
        }
        return null;
    }
    
    
    protected ArrayList<VertexPicture> containerPath(VertexPicture vertexPicture) {
        ArrayList<VertexPicture> path = new ArrayList<>();
        while (vertexPicture != null) {
            VertexPicture container = findContainer(vertexPicture);
            if (container != null) {
                path.add(container);
            }
            vertexPicture = container;
        }
        return path;
    }
    
    
    protected void clearHoverInfo() {
        hoverInfo = null;
        repaint();
    }

    
    protected void setSelected(AbstractPicture picture) {
        selectedPicture = picture;
    }


    private void initialize() {
        setSize(0, 0);
        addMouseListener(MOUSE_ADAPTER);
        addMouseMotionListener(MOUSE_ADAPTER);
        addKeyListener(KEY_ADAPTER);
    }


    private void setComponentSize(int width, int height) {
        Dimension dimension = getSize();
        dimension.width = Math.max(dimension.width, width);
        dimension.height = Math.max(dimension.height, height);
        setPreferredSize(dimension);
        setSize(dimension);
    }


    private void vertexPictureClicked(VertexPicture vertexPicture, int count) {
        if (count == 1) {
            setSelected(vertexPicture);
        }
        else {
            editPanel = vertexPicture.getEditPanel();
            if (editPanel != null) {
                openDialog(vertexDialogTitle(vertexPicture.getVertex()));
                editor.vertexPictureModified(vertexPicture);
            }                
        }
        editor.vertexPictureClicked(vertexPicture, count);
    }
    
    
    private void addNewVertexPicture(Class vertexPictureClass, Point point) {
        try {
            VertexPicture vertexPicture = (VertexPicture) vertexPictureClass.newInstance();
            addVertexPicture(vertexPicture, point);
        }
        catch (ReflectiveOperationException ex) {
            Logger.getLogger(DiagramComponent.class.getName()).log(Level.SEVERE, vertexPictureClass.getName(), ex);
        }
    }


    private void addVertexPicture(VertexPicture vertexPicture) {
        pictures.add(vertexPicture);
        page.add(vertexPicture);
        setComponentSize(vertexPicture.xEast(), vertexPicture.ySouth());
        setComponentSize(vertexPicture.xEast(), vertexPicture.ySouth());
        selectedPicture = vertexPicture;
        repaint();
        editor.vertexPictureAdded(this, vertexPicture);
    }
    
    
    private void startDrag(Point point) {
        dragInfo = new DragInfo();
        dragInfo.startPoint = point;
        Class edgePictureClass = editor.selectedEdgePictureClass();
        if (edgePictureClass != null) {
            prepareEdgeDragging(edgePictureClass);
        }
        else {
            dragInfo.edge = getEdgePicture(dragInfo.startPoint);
            if (dragInfo.edge != null) {
                initializeEdgeDragging();
            }
            else {
                dragInfo.vertex = getVertexPicture(dragInfo.startPoint);
                if (dragInfo.vertex != null) {
                    initializeVertexDragging();
                }
            }
        }
        requestFocus();
    }

    
    private void dragNewEdge(Class edgePictureClass, VertexPicture originPicture, Point point) {
        try {
            dragInfo.edge = (EdgePicture) edgePictureClass.newInstance();
            dragInfo.edge.setOrigin(originPicture, point);
            dragInfo.edge.setEndPoint(point);
            addEdgePicture(dragInfo.edge);
            selectedPicture = dragInfo.edge;
            repaint();
        }
        catch (IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void edgePictureClicked(EdgePicture edgePicture, int count) {
        if (count == 1) {
            setSelected(edgePicture);
        }
        else {
            editPanel = edgePicture.getEditPanel();
            if (editPanel != null) {
                openDialog(edgeDialogTitle(edgePicture.getEdge()));
                editor.edgePictureModified(edgePicture);
            }
        }
    }
    
    
    private void cleanupEdges() {
        for (EdgePicture edgePicture : getEdgePictures()) {
            if (edgePicture.getOriginPicture() == dragInfo.vertex || edgePicture.getTerminusPicture() == dragInfo.vertex) {
                edgePicture.cleanup();
            }
        }
    }
    

    private void prepareEdgeDragging(Class edgePictureClass) {
        VertexPicture vertexPicture = getVertexPicture(dragInfo.startPoint);
        if (vertexPicture != null) {
            if (dragInfo.edge != null) {
                dragInfo.edge.setTerminus(vertexPicture, dragInfo.startPoint);
            }
            else {
                dragNewEdge(edgePictureClass, vertexPicture, dragInfo.startPoint);
            }
        }
    }
    
    
    private void initializeEdgeDragging() {
        if (dragInfo.edge.getTerminusPicture() != null) {
            dragInfo.edge.selectDragPoint(dragInfo.startPoint);
        }
    }
    
    
    private void dragEdge(Point point) {
        if (dragInfo.edge.hasDragPoint()) {
            dragInfo.edge.setDragLocation(point);
        }
        else {
            VertexPicture vertexPicture = getVertexPicture(point);
            if (vertexPicture != null) {
                attachmentPoint = vertexPicture.nearestAttachmentPoint(point);
            }
            else if (attachmentPoint != null) {
                attachmentPoint = null;
            }
            if (dragInfo.edge.getTerminusPicture() == null) {
                dragInfo.edge.setEndPoint(point);
            }
        }
        setComponentSize(dragInfo.edge.xEast(), dragInfo.edge.ySouth());
        repaint();
    }
    
    
    private void finishEdgeDragging(Point point) {
        if (dragInfo.edge.hasDragPoint()) {
            dragInfo.edge.finishDrag();
        }
        else if (! finalizeNewEdge(point)) {
            pictures.remove(dragInfo.edge);
            selectedPicture = null;
        }
    }


    private boolean finalizeNewEdge(Point point) {
        VertexPicture terminusPicture = getVertexPicture(point);
        if (terminusPicture != null) {
            int terminusAttachmentIndex = terminusPicture.nearestAttachmentIndex(point);
            if (! dragInfo.edge.hasOrigin(terminusPicture, terminusAttachmentIndex)) {
                dragInfo.edge.setTerminus(terminusPicture, terminusAttachmentIndex);
                editor.edgePictureAdded(this, dragInfo.edge);
                return true;
            }
        }
        return false;
    }

    
    private void initializeVertexDragging() {
        assert hoverInfo != null;
        if (hoverInfo.location == VertexPicture.INTERIOR) {
            dragInfo.distance = new Point(dragInfo.vertex.getLocation().x - dragInfo.startPoint.x, dragInfo.vertex.getLocation().y - dragInfo.startPoint.y);
            ArrayList<VertexPicture> contained = allContainedVertices(dragInfo.vertex);
            ensureDrawnLast(dragInfo.vertex);
            for (VertexPicture picture : contained) {
                ensureDrawnLast(picture);
            }
        }
    }

    
    private void dragVertex(Point point) {
        if (hoverInfo.location == VertexPicture.INTERIOR) {
            setCursor(Cursor.MOVE_CURSOR);
            moveDraggingVertexPicture(new Point(point.x + dragInfo.distance.x, point.y + dragInfo.distance.y));
        }
        else {
            dragInfo.vertex.resize(hoverInfo.location, point);
            correctEndPoints(dragInfo.vertex);
        }
        if (attachmentPoint != null) {
            attachmentPoint = null;
        }
        repaint();
    }
    
    
    private void moveDraggingVertexPicture(Point point) {
        Point containerPoint = dragInfo.vertex.getLocation();
        int δx = point.x - containerPoint.x;
        int δy = point.y - containerPoint.y;
        setVertexLocation(dragInfo.vertex, point);
        ArrayList<VertexPicture> vertices = getVertexPictures();
        ArrayList<EdgePicture> edges = getEdgePictures();
        boolean containerPassed = false;
        for (VertexPicture vertexPicture : vertices) {
            if (containerPassed) {
                Point containedLocation = new Point(vertexPicture.getLocation());
                containedLocation.x += δx;
                containedLocation.y += δy;
                setVertexLocation(vertexPicture, containedLocation);
                for (EdgePicture edgePicture : edges) {
                    if (edgePicture.getOriginPicture() == vertexPicture) {
                        ArrayList containers = containerPath(edgePicture.getTerminusPicture());
                        if (containers.contains(dragInfo.vertex)) {
                            edgePicture.move(δx, δy);
                        }
                    }
                }
            }
            else if (vertexPicture == dragInfo.vertex) {
                containerPassed = true;
            }
        }
    }
    

    private void setToolTip(AbstractPicture picture) {
        if (picture instanceof VertexPicture) {
            setToolTipText(editor.toolTipText((VertexPicture) picture));
        }
        else if (picture instanceof EdgePicture) {
            setToolTipText(editor.toolTipText((EdgePicture) picture));
        }
        else {
            setToolTipText(null);
        }
    }
    
    
    private void setDiagramCursor(AbstractPicture picture, Point point) {
        if (picture instanceof VertexPicture) {
            setVertexCursor();
        }
        else if (picture instanceof EdgePicture) {
            setCursor(Cursor.HAND_CURSOR);
        }
        else {
            setCursor(Cursor.DEFAULT_CURSOR);
        }
    }
    
    
    private void setAttachmentPoint(AbstractPicture picture, Point point) {
        if (picture instanceof VertexPicture && editor.selectedEdgePictureClass() != null) {
            attachmentPoint = ((VertexPicture) picture).nearestAttachmentPoint(point);
            repaint();
        }
        else if (attachmentPoint != null) {
            attachmentPoint = null;
            repaint();
        }
    }
    
    
    private void setVertexLocation(VertexPicture vertexPicture, Point point) {
        vertexPicture.setLocation(point);
        correctEndPoints(vertexPicture);
        setComponentSize(vertexPicture.xEast(), vertexPicture.ySouth());
    }
    
    
    private void correctEndPoints(VertexPicture vertexPicture) {
        for (EdgePicture edgePicture : getEdgePictures()) {
            if (edgePicture.getOriginPicture() == vertexPicture || edgePicture.getTerminusPicture() == vertexPicture) {
                edgePicture.correctEndPoint(vertexPicture);
            }
        }
    }
    
    
    private void openDialog(String dialogTitle) {
        editPanel.setEnvironment(editor);
        EditDialog dialog = new EditDialog(editor, dialogTitle, editPanel);
        dialog.setVisible(true);
        repaint(); // Any picture might be changed, repaint entire diagram.
    }


    private String vertexDialogTitle(Vertex vertex) {
        StringBuilder title = new StringBuilder(vertex.getClass().getSimpleName());
        String name = vertex.getName();
        if (name != null) {
            title .append(": ");
            title.append(name);
        }
        return title.toString();
    }


    private String edgeDialogTitle(Edge edge) {
        return edge.getClass().getSimpleName();
    }


    private void diagramClicked(int count, Point point) {
        Class vertexPictureClass = editor.selectedVertexPictureClass();
        if (count == 1 && vertexPictureClass != null) {
            addNewVertexPicture(vertexPictureClass, point);
        }
        else {
            EdgePicture edgePicture = getEdgePicture(point);
            if (edgePicture != null) {
                edgePictureClicked(edgePicture, count);
            }
            else {
                VertexPicture vertexPicture = getVertexPicture(point);
                if (vertexPicture != null) {
                    vertexPictureClicked(vertexPicture, count);
                }
            }
        }
    }

    
    private void hoverDiagram(Point point) {
        boolean needRepaint = false;
        if (hoverInfo != null && hoverInfo.picture instanceof EdgePicture) {
            ((EdgePicture) hoverInfo.picture).setHoverPoint(null);
            needRepaint = true;
        }
        int location = -1;
        AbstractPicture picture = getEdgePicture(point);
        if (picture == null) {
            picture = getVertexPicture(point);
            if (picture != null) {
                location = ((VertexPicture) picture).locationOf(point);
            }
        }
        else {
            ((EdgePicture) picture).setHoverPoint(point);
            needRepaint = true;
        }
        if (picture != null) {
            if (hoverInfo == null) {
                hoverInfo = new HoverInfo();
            }
            hoverInfo.picture = picture;
            hoverInfo.location = location;
        }
        else {
            hoverInfo = null;
        }
        if (editor.selectedEdgePictureClass() == null) {
            setDiagramCursor(picture, point);
        }
        else {
            setAttachmentPoint(picture, point);
            needRepaint = true;
        }
        setToolTip(picture);
        if (needRepaint) {
            repaint();
        }
    }

    
    private void popupContextMenu(Point point) {
        EdgePicture edgePicture = getEdgePicture(point);
        if (edgePicture != null) {
            JPopupMenu menu = editor.getEdgeMenu(edgePicture);
            menu.show(this, point.x, point.y);
        }
    }
    
    
    /**
     * @param point
     * @return Top most VertexPicture with point inside.
     */
    private VertexPicture getVertexPicture(Point point) {
        int i = pictures.size() - 1;
        while (i >= 0) {
            AbstractPicture picture = pictures.get(i);
            if (picture instanceof VertexPicture) {
                int location = ((VertexPicture) picture).locationOf(point);
                if (location != VertexPicture.EXTERN) {
                    return (VertexPicture) picture;
                }
            }
            --i;
        }
        return null;
    }
    
    
    private EdgePicture getEdgePicture(Point point) {
        for (AbstractPicture picture : pictures) {
            if (picture instanceof EdgePicture && picture.isLocatedAt(point)) {
                return (EdgePicture) picture;
            }
        }
        return null;
    }
    
    
    private void ensureDrawnLast(VertexPicture vertexPicture) {
        ArrayList<AbstractPicture> picturesToMove = new ArrayList<>();
        picturesToMove.add(vertexPicture);
        for (AbstractPicture picture : pictures) {
            if (picture instanceof EdgePicture && (((EdgePicture) picture).getOriginPicture() == vertexPicture || ((EdgePicture) picture).getTerminusPicture() == vertexPicture)) {
                picturesToMove.add(picture);
            }
        }
        pictures.removeAll(picturesToMove);
        pictures.addAll(picturesToMove);
    }
    
    
    private ArrayList<VertexPicture> containedVertices(VertexPicture container) {
        ArrayList<VertexPicture> contained = new ArrayList<>();
        for (AbstractPicture picture : pictures) {
            if (picture instanceof VertexPicture && findContainer((VertexPicture) picture) == container) {
                contained.add((VertexPicture) picture);
            }
        }
        return contained;
    }

    
    private ArrayList<VertexPicture> allContainedVertices(VertexPicture container) {
        ArrayList<VertexPicture> all = new ArrayList<>();
        ArrayList<VertexPicture> contained = containedVertices(container);
        all.addAll(contained);
        for (VertexPicture vertex : contained) {
            all.addAll(allContainedVertices(vertex));
        }
        return all;
    }


    private Collection<EdgePicture> allEdgePictures(VertexPicture vertexPicture) {
        Collection<EdgePicture> all = new ArrayList<>();
        for (EdgePicture edgePicture : page.getEdges()) {
            if (edgePicture.getOriginPicture() == vertexPicture || edgePicture.getTerminusPicture() == vertexPicture) {
                all.add(edgePicture);
            }
        }
        return all;
    }

    
    private void setCursor(int type) {
        if (getCursor().getType() != type) {
            setCursor(new Cursor(type));
        }
    }
    
    
    private void setVertexCursor() {
        switch (hoverInfo.location) {
            case VertexPicture.INTERIOR:
                setCursor(Cursor.HAND_CURSOR);
                break;
            case VertexPicture.NORTH:
                setCursor(Cursor.N_RESIZE_CURSOR);
                break;
            case VertexPicture.SOUTH :
                setCursor(Cursor.S_RESIZE_CURSOR);
                break;
            case VertexPicture.WEST:
                setCursor(Cursor.W_RESIZE_CURSOR);
                break;
            case VertexPicture.EAST:
                setCursor(Cursor.E_RESIZE_CURSOR);
                break;
            case VertexPicture.NORTH_WEST:
                setCursor(Cursor.NW_RESIZE_CURSOR);
                break;
            case VertexPicture.NORTH_EAST:
                setCursor(Cursor.NE_RESIZE_CURSOR);
                break;
            case VertexPicture.SOUTH_WEST:
                setCursor(Cursor.SW_RESIZE_CURSOR);
                break;
            case VertexPicture.SOUTH_EAST:
                setCursor(Cursor.SE_RESIZE_CURSOR);
                break;
        }
    }
    
    
    private class DragInfo {
        VertexPicture vertex = null;
        EdgePicture edge = null;
        Point startPoint = null;
        Point distance = null;
    }
    
    
    private class HoverInfo {
        AbstractPicture picture = null;
        int location = Integer.MIN_VALUE;
    }
    
    
    /**
     * @param edgePicture
     * @return index of last vertex picture from pictures that origin or terminus of edgePicture
     */
    private int findInsertIndex(EdgePicture edgePicture) {
        VertexPicture origin = edgePicture.getOriginPicture();
        VertexPicture terminus = edgePicture.getTerminusPicture();
        for (int index = pictures.size() - 1; index >= 0;  --index) {
            AbstractPicture abstractPicture = pictures.get(index);
            if (origin == abstractPicture || terminus == abstractPicture) {
                return index;
            }
        }
        throw new IllegalStateException("No vertex found for edge.");
    }


    private final MouseAdapter MOUSE_ADAPTER = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent evt) {
            switch (evt.getButton()) {
                case MouseEvent.BUTTON1:
                    diagramClicked(evt.getClickCount(), evt.getPoint());
                    break;
                case MouseEvent.BUTTON3:
                    popupContextMenu(evt.getPoint());
                    break;
            }
        }
        
        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1) {
                startDrag(evt.getPoint());
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1) {
                Point point = evt.getPoint();
                if (dragInfo != null) {
                    if (dragInfo.vertex != null) {
                        cleanupEdges();
                    }
                    if (dragInfo.edge != null) {
                        finishEdgeDragging(point);
                    }
                    dragInfo = null;
                    setCursor(Cursor.DEFAULT_CURSOR);
                    repaint(); // Selection might be changed, repaint entire diagram;
                }
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent evt) {
            hoverDiagram(evt.getPoint());
        }
        
        @Override
        public void mouseDragged(MouseEvent evt) {
            if (dragInfo != null) {
                if (dragInfo.vertex != null) {
                    dragVertex(evt.getPoint());
                }
                else if (dragInfo.edge != null) {
                    dragEdge(evt.getPoint());
                }
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent evt) {
            editor.diagramEntered(evt);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            editor.diagramExited();
        }

    };
    
    
    private final KeyAdapter KEY_ADAPTER = new KeyAdapter() {

        @Override
        public void keyReleased(KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_DELETE && selectedPicture != null) {
                if (selectedPicture instanceof VertexPicture) {
                    removeVertex((VertexPicture) selectedPicture);
                }
                else if (selectedPicture instanceof EdgePicture) {
                    removeEdge((EdgePicture) selectedPicture);
                }
                pictures.remove(selectedPicture);
                selectedPicture = null;
                repaint();
            }
        }

    };

    
    private final GraphEditor editor;
    private final DiagramPage page;
    private final ArrayList<AbstractPicture> pictures = new ArrayList<>();

    private AbstractPicture selectedPicture;

    private DragInfo dragInfo;     
    private HoverInfo hoverInfo;
    
    private final Map<AbstractPicture, Collection<DrawStyle>> highlights = new HashMap<>();

    private Point attachmentPoint;
    
    private final Color attachmentPointColor = Color.RED;
    private final int attachmentPointWidth = 4;
    private final int attachmentPointHeight = 4;
    
    private AbstractEditPanel editPanel;

}
