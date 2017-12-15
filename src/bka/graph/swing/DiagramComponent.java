/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import bka.awt.*;
import bka.graph.*;
import bka.graph.document.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class DiagramComponent extends JComponent {
    
    
    public static final Object HIGHLIGHT_BORDER = "BORDER";
    
    
    DiagramComponent(GraphEditor editor, DiagramPage page) {
        this.editor = editor;
        this.page = page;
        initialize();
        setTitle(page.getTitle());
        pictures.addAll(page.getVertices());
        pictures.addAll(page.getEdges());
        drawHistory = new DrawHistory(this);
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
        page.add(edgePicture);
        int index = findInsertIndex(edgePicture);
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
            paintHighlight(g2d, highlight.getKey(), highlight.getValue());
        }
        if (selectedPicture != null) {
            g2d.setColor(SELECTION_COLOR);
            g2d.setStroke(SELECTION_STROKE);
            g2d.draw(selectedPicture.getShape());
        }
        if (attachmentPoint != null) {
            g2d.setColor(attachmentPointColor);
            g2d.fillOval(attachmentPoint.x - attachmentPointWidth / 2, attachmentPoint.y - attachmentPointHeight / 2, attachmentPointWidth, attachmentPointHeight);
        }
    }

    
    private void paintHighlight(Graphics2D g2d, AbstractPicture picture, Collection<DrawStyle> drawStyles) {
        for (DrawStyle style : drawStyles) {
            Color color = style.getColor(HIGHLIGHT_BORDER);
            Stroke stroke = style.getStroke(HIGHLIGHT_BORDER);
            if (color != null && stroke != null) {
                g2d.setPaint(color);
                g2d.setStroke(stroke);
                g2d.draw(picture.getShape());
            }
        }
    }


    DrawHistory getDrawHistory() {
        return drawHistory;
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
        Collection<EdgePicture> edgePictures = allEdgePictures(vertexPicture);
        for (EdgePicture edgePicture : edgePictures) {
            page.remove(edgePicture);
            editor.vertexPictureRemoved(vertexPicture);
            pictures.remove(edgePicture);
        }
        pictures.remove(vertexPicture);
        page.remove(vertexPicture);
        editor.vertexPictureRemoved(vertexPicture);
        if (vertexPicture == selectedPicture) {
            selectedPicture = null;
        }
        repaint();
        drawHistory.addVertexDeletion(vertexPicture, edgePictures);
    }

    
    void removeEdge(EdgePicture edgePicture) {
        pictures.remove(edgePicture);
        page.remove(edgePicture);
        editor.edgePictureRemoved(edgePicture);
        if (edgePicture == selectedPicture) {
            selectedPicture = null;
        }
        repaint();
        drawHistory.addEdgeDeletion(edgePicture);
    }


    boolean setHighlighted(Vertex vertex, DrawStyle drawStyle) {
        boolean vertexHighlighted = false;
        for (AbstractPicture picture : pictures) {
            if (picture instanceof VertexPicture && ((VertexPicture) picture).getVertex() == vertex) {
                setHighlighted(picture, drawStyle);
                vertexHighlighted = true;
            }
        }
        return vertexHighlighted;
    }


    void setHighlighted(AbstractPicture picture, DrawStyle drawStyle) {
        if (picture == null || drawStyle == null) {
            throw new IllegalArgumentException();
        }
        Collection<DrawStyle> pictureHighlights = highlights.get(picture);
        if (pictureHighlights == null) {
            pictureHighlights = new ArrayList<>();
            highlights.put(picture, pictureHighlights);
        }
        pictureHighlights.add(drawStyle);
    }
    
    
    boolean resetHighlighted(AbstractPicture picture, DrawStyle drawStyle) {
        boolean reset = false;
        Collection<DrawStyle> pictureHighlights = highlights.get(picture);
        if (pictureHighlights != null) {
            reset |= pictureHighlights.remove(drawStyle);
            if (pictureHighlights.isEmpty()) {
                highlights.remove(picture);
            }
        }
        return reset;
    }


    boolean resetHighlighted(DrawStyle drawStyle) {
        boolean reset = false;
        for (Map.Entry<AbstractPicture, Collection<DrawStyle>> pictureHighlights : highlights.entrySet()) {
            reset |= pictureHighlights.getValue().remove(drawStyle);
       }
        return reset;
    }
    
    
    void insertPictures(Collection<VertexPicture> vertexPictures, Collection<EdgePicture> edgePictures) {
        for (VertexPicture vertexPicture : vertexPictures) {
            insertVertexPicture(vertexPicture);
        }
        for (EdgePicture edgePicture : edgePictures) {
            pictures.add(edgePicture);
            page.add(edgePicture);
        }
    }
    
    
    void removePictures(Collection<VertexPicture> vertexPictures, Collection<EdgePicture> edgePictures) {
        for (VertexPicture vertexPicture : vertexPictures) {
            removeVertexPicture(vertexPicture);
            editor.vertexPictureRemoved(vertexPicture);
        }
        for (EdgePicture edgePicture : edgePictures) {
            removeEdgePicture(edgePicture);
        }
    }


    void revertVertexMutation(VertexPicture destination, Point originalLocation, Dimension originalSize) {
        ensureDrawnLast(destination);
        int δx = originalLocation.x - destination.getLocation().x;
        int δy = originalLocation.y - destination.getLocation().y;
        setVertexLocation(destination, originalLocation);
        destination.setSize(originalSize);
        moveContainedPictures(destination, δx, δy);
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
        insertVertexPicture(vertexPicture);
        setComponentSize(vertexPicture.xEast(), vertexPicture.ySouth());
        setComponentSize(vertexPicture.xEast(), vertexPicture.ySouth());
        selectedPicture = vertexPicture;
        repaint();
        drawHistory.addVertexInsertion(vertexPicture);
    }
    
    
    private void insertVertexPicture(VertexPicture picture) {
        pictures.add(picture);
        page.add(picture);
        editor.vertexPictureAdded(this, picture);
    }


    private void startDrag(Point point) {
        dragInfo = new DragInfo();
        dragInfo.startPoint = point;
        Class edgePictureClass = editor.selectedEdgePictureClass();
        if (attachmentPoint != null && edgePictureClass != null) {
            startNewEdgeDrag(edgePictureClass);
        }
        else {
            EdgePicture edgePicture = getEdgePicture(dragInfo.startPoint);
            if (edgePicture != null) {
                startEdgeDrag(edgePicture);
            }
            else {
                VertexPicture vertexPicture = getVertexPicture(dragInfo.startPoint);
                if (vertexPicture != null) {
                    startVertexDrag(vertexPicture);
                }
            }
        }
    }


    private void startNewEdgeDrag(Class edgePictureClass) {
        dragInfo.edge = new EdgeDragInfo();
        try {
            dragInfo.edge.initializeNewEdgeDrag(edgePictureClass);
            pictures.add(dragInfo.edge.picture);
            selectedPicture = dragInfo.edge.picture;
            repaint();
        }
        catch (ReflectiveOperationException ex) {
            Logger.getLogger(DiagramComponent.class.getName()).log(Level.SEVERE, "Draw new edge", ex);
            dragInfo = null;
        }
    }


    private void startEdgeDrag(EdgePicture edgePicture) {
        dragInfo.edge = new EdgeDragInfo();
        dragInfo.edge.initializeDrag(edgePicture);
    }


    private void startVertexDrag(VertexPicture vertexPicture) {
        dragInfo.vertex = new VertexDragInfo();
        dragInfo.vertex.picture = vertexPicture;
        initializeVertexDragging();
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


    private void removePicture(AbstractPicture picture) {
        if (picture instanceof VertexPicture) {
            removeVertex((VertexPicture) picture);
        }
        else if (picture instanceof EdgePicture) {
            removeEdge((EdgePicture) picture);
        }
    }


    private void cleanupEdges() {
        for (EdgePicture edgePicture : getEdgePictures()) {
            if (edgePicture.getOriginPicture() == dragInfo.vertex.picture || edgePicture.getTerminusPicture() == dragInfo.vertex.picture) {
                edgePicture.cleanup();
            }
        }
    }
    

    private void dragEdge(Point point) {
        if (dragInfo.edge.picture.hasDragPoint()) {
            dragInfo.edge.picture.setDragLocation(point);
        }
        else {
            VertexPicture vertexPicture = getVertexPicture(point);
            if (vertexPicture != null) {
                attachmentPoint = vertexPicture.nearestAttachmentPoint(point);
            }
            else {
                attachmentPoint = null;
            }
            if (dragInfo.edge.picture.getTerminusPicture() == null) {
                dragInfo.edge.picture.setEndPoint(point);
            }
        }
        setComponentSize(dragInfo.edge.picture.xEast(), dragInfo.edge.picture.ySouth());
        repaint();
    }


    private void finishVertexDragging() {
        cleanupEdges();
        dragInfo.vertex.addMutationToHistory();
    }


    private void finishEdgeDragging(Point point) {
        if (dragInfo.edge.picture.hasDragPoint()) {
            dragInfo.edge.finishDrag();
        }
        else if (! finalizeNewEdge(point)) {
            pictures.remove(dragInfo.edge.picture);
            selectedPicture = null;
        }
    }


    private boolean finalizeNewEdge(Point point) {
        VertexPicture terminusPicture = getVertexPicture(point);
        if (terminusPicture != null) {
            int terminusAttachmentIndex = terminusPicture.nearestAttachmentIndex(point);
            if (terminusAttachmentIndex >= 0 && ! dragInfo.edge.picture.hasOrigin(terminusPicture, terminusAttachmentIndex)) {
                dragInfo.edge.picture.setTerminus(terminusPicture, terminusAttachmentIndex);
                page.add(dragInfo.edge.picture);
                editor.edgePictureAdded(this, dragInfo.edge.picture);
                drawHistory.addEdgeInsertion(dragInfo.edge.picture);
                return true;
            }
        }
        return false;
    }

    
    private void initializeVertexDragging() {
        if (hoverInfo != null && hoverInfo.location == Location.INTERIOR) {
            ensureDrawnLast(dragInfo.vertex.picture);
            dragInfo.vertex.distance = new Point(dragInfo.vertex.picture.getLocation().x - dragInfo.startPoint.x, dragInfo.vertex.picture.getLocation().y - dragInfo.startPoint.y);
        }
        dragInfo.vertex.originalLocation = dragInfo.vertex.picture.getLocation();
        dragInfo.vertex.originalSize = dragInfo.vertex.picture.getSize();
    }

    
    private void dragVertex(Point point) {
        if (hoverInfo.location == Location.INTERIOR) {
            setCursor(Cursor.MOVE_CURSOR);
            moveDraggingVertexPicture(new Point(point.x + dragInfo.vertex.distance.x, point.y + dragInfo.vertex.distance.y));
        }
        else {
            dragInfo.vertex.picture.resize(hoverInfo.location, point);
            correctEndPoints(dragInfo.vertex.picture);
        }
        if (attachmentPoint != null) {
            attachmentPoint = null;
        }
        repaint();
    }

    
    private void moveDraggingVertexPicture(Point destination) {
        Point containerPoint = dragInfo.vertex.picture.getLocation();
        int δx = destination.x - containerPoint.x;
        int δy = destination.y - containerPoint.y;
        setVertexLocation(dragInfo.vertex.picture, destination);
        moveContainedPictures(dragInfo.vertex.picture, δx, δy);
    }


    private void moveContainedPictures(VertexPicture containerPicture, int δx, int δy) {
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
                        if (containers.contains(containerPicture)) {
                            edgePicture.move(δx, δy);
                        }
                    }
                }
            }
            else if (vertexPicture == containerPicture) {
                containerPassed = true;
            }
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
    
    
    private void setVertexLocation(VertexPicture vertexPicture, Point location) {
        vertexPicture.setLocation(location);
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
        Location location = null;
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
            setToolTipText(picture.getToolTipText());
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
        if (needRepaint) {
            repaint();
        }
    }

    
    private void popupContextMenu(Point point) {
        JPopupMenu menu = contextMenu(point);
        if (menu != null) {
            menu.show(this, point.x, point.y);
        }
    }
    
    
    private JPopupMenu contextMenu(Point point) {
        EdgePicture edgePicture = getEdgePicture(point);
        if (edgePicture != null) {
            return editor.getEdgeMenu(edgePicture);
        }
        else {
            VertexPicture vertexPicture = getVertexPicture(point);
            if (vertexPicture != null) {
                return editor.getVertexMenu(vertexPicture);
            }
        }
        return null;
    }
    
    
    /**
     * @param point
     * @return Top most VertexPicture with point inside.
     */
    private VertexPicture getVertexPicture(Point point) {
        Iterator<AbstractPicture> it = pictures.descendingIterator();
        while (it.hasNext()) {
            AbstractPicture picture = it.next();
            if (picture instanceof VertexPicture) {
                Location location = ((VertexPicture) picture).locationOf(point);
                if (location != Location.EXTERN) {
                    return (VertexPicture) picture;
                }
            }
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
        ArrayList<VertexPicture> contained = allContainedVertices(vertexPicture);
        moveToEndOfList(vertexPicture);
        for (VertexPicture picture : contained) {
            moveToEndOfList(picture);
        }
    }


    private void moveToEndOfList(VertexPicture vertexPicture) {
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
            case INTERIOR:
                setCursor(Cursor.HAND_CURSOR);
                break;
            case NORTH:
                setCursor(Cursor.N_RESIZE_CURSOR);
                break;
            case SOUTH:
                setCursor(Cursor.S_RESIZE_CURSOR);
                break;
            case WEST:
                setCursor(Cursor.W_RESIZE_CURSOR);
                break;
            case EAST:
                setCursor(Cursor.E_RESIZE_CURSOR);
                break;
            case NORTH_WEST:
                setCursor(Cursor.NW_RESIZE_CURSOR);
                break;
            case NORTH_EAST:
                setCursor(Cursor.NE_RESIZE_CURSOR);
                break;
            case SOUTH_WEST:
                setCursor(Cursor.SW_RESIZE_CURSOR);
                break;
            case SOUTH_EAST:
                setCursor(Cursor.SE_RESIZE_CURSOR);
                break;
        }
    }


    private class DragInfo {
        VertexDragInfo vertex;
        EdgeDragInfo edge;
        Point startPoint;
    }


    private class VertexDragInfo {
        VertexPicture picture;
        Point distance;
        Point originalLocation;
        Dimension originalSize;

        boolean relocated() {
            return ! originalLocation.equals(picture.getLocation());
        }

        boolean resized() {
            return ! originalSize.equals(picture.getSize());
        }

        void addMutationToHistory() {
            if (relocated()) {
                drawHistory.addVertexRelocation(picture, originalLocation);
            }
            if (resized()) {
                drawHistory.addVertexResizement(picture, originalSize);
            }
        }

    }


    private class EdgeDragInfo {

        void initializeNewEdgeDrag(Class edgePictureClass) throws ReflectiveOperationException {
            VertexPicture vertexPicture = getVertexPicture(dragInfo.startPoint);
            if (vertexPicture != null) {
                dragNewEdge(edgePictureClass, vertexPicture, dragInfo.startPoint);
            }
        }

        void initializeDrag(EdgePicture picture) {
            this.picture = picture;
            originalXPoints = arrayCopy(picture.getXPoints());
            originalYPoints = arrayCopy(picture.getYPoints());
            picture.selectDragPoint(dragInfo.startPoint);
        }

        void finishDrag() {
            picture.finishDrag();
            if (transformed()) {
                drawHistory.addEdgeTransformation(picture, originalXPoints, originalYPoints);
            }
        }

        private void dragNewEdge(Class edgePictureClass, VertexPicture originPicture, Point point) throws ReflectiveOperationException {
            picture = (EdgePicture) edgePictureClass.newInstance();
            picture.setOrigin(originPicture, point);
            picture.setEndPoint(point);
        }

        private int[] arrayCopy(int[] original) {
            return Arrays.copyOf(original, original.length);
        }

        private boolean transformed() {
            return ! Arrays.equals(originalXPoints, picture.getXPoints()) || ! Arrays.equals(originalYPoints, picture.getYPoints());
        }

        EdgePicture picture;
        int[] originalXPoints;
        int[] originalYPoints;

    }
    
    
    private class HoverInfo {
        AbstractPicture picture;
        Location location;
    }
    
    
    /**
     * @param edgePicture
     * @return index of last vertex picture from pictures that is origin or terminus of edgePicture
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
        public void mousePressed(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1) {
                startDrag(evt.getPoint());
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent evt) {
            if (dragInfo != null && evt.getButton() == MouseEvent.BUTTON1) {
                if (dragInfo.vertex != null) {
                    finishVertexDragging();
                }
                if (dragInfo.edge != null) {
                    finishEdgeDragging(evt.getPoint());
                }
                dragInfo = null;
                setCursor(Cursor.DEFAULT_CURSOR);
                repaint(); // Selection might be changed, repaint entire diagram;
            }
        }
        
        @Override
        public void mouseClicked(MouseEvent evt) {
            requestFocus();
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
            if (keyboard.isDelete(evt)) {
                deleteSelectedPicture();
            }
            else if (keyboard.isUndo(evt)) {
                undoMutation();
            }
            else if (keyboard.isRedo(evt)) {
                redoMutation();
            }
        }

        private void deleteSelectedPicture() {
            if (selectedPicture != null) {
                removePicture(selectedPicture);
            }
        }

        private void undoMutation() {
            Mutation mutation = drawHistory.getUndo();
            if (mutation != null) {
                mutation.undo();
                repaint();
            }
        }

        private void redoMutation() {
            Mutation mutation = drawHistory.getRedo();
            if (mutation != null) {
                mutation.redo();
                repaint();
            }
        }

        private final Keyboard keyboard = Keyboard.getInstance();

    };

    
    private final GraphEditor editor;
    private final DiagramPage page;
    private final LinkedList<AbstractPicture> pictures = new LinkedList<>();

    private AbstractPicture selectedPicture;

    private DragInfo dragInfo;     
    private HoverInfo hoverInfo;
    
    private final Map<AbstractPicture, Collection<DrawStyle>> highlights = new HashMap<>();

    private Point attachmentPoint;
    
    private AbstractEditPanel editPanel;

    private final DrawHistory drawHistory;

    private final Color attachmentPointColor = Color.RED;
    private final int attachmentPointWidth = 4;
    private final int attachmentPointHeight = 4;

    private static final Color SELECTION_COLOR = new Color(0, 0, 128, 64);
    private static final BasicStroke SELECTION_STROKE = new BasicStroke(3.0f);

}
