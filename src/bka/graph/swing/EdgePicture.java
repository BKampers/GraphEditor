/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;


import bka.awt.*;
import bka.graph.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.logging.*;


public class EdgePicture extends AbstractPicture {

    public static final String ARROW_HEAD = "ARROW_HEAD";


    public EdgePicture() {
    }
    
    
    public final Edge getEdge() {
        return edge;
    }
    
    
    public final void setEdge(Edge edge) {
        this.edge = edge;
    }
    
    
    public final VertexPicture getOriginPicture() {
        return originPicture;
    }
    
    
    public final void setOriginPicture(VertexPicture vertexPicture) {
        originPicture = vertexPicture;
    }
    
    
    public final VertexPicture getTerminusPicture() {
        return terminusPicture;
    }
    
    
    public final void setTerminusPicture(VertexPicture vertexPicture) {
        terminusPicture = vertexPicture;
    }
    
    
    public final int[] getXPoints() {
        return xPoints;
    }
    
    
    public final void setXPoints(int[] points) {
        xPoints = points;
    }
    
    
    public final int[] getYPoints() {
        return yPoints;
    }
    
    
    public final void setYPoints(int[] points) {
        yPoints = points;
    }
    
    
    public int getOriginAttachmentIndex() {
        return originAttachmentIndex;
    }
    
    
    public void setOriginAttachmentIndex(int index) {
        originAttachmentIndex = index;
    }
    
    
    public int getTerminusAttachmentIndex() {
        return terminusAttachmentIndex;
    }
   
    
    public void setTerminusAttachmentIndex(int index) {
        terminusAttachmentIndex = index;
    }
   
    
    @Override
    public String[] getCustomizablePaints() {
        return new String[] { AbstractPicture.DRAW };
    }
        

    public java.lang.Class edgeClass() {
        return Edge.class;
    }

    
    @Override
    public boolean isLocatedAt(Point point) {
        return indexNear(point) != NO_INDEX;
    }
    
    
    public final void setOrigin(VertexPicture originPicture, Point point) {
        setOrigin(originPicture, originPicture.nearestAttachmentIndex(point));
    }
    
    
    public final void setTerminus(VertexPicture terminusPicture, Point point) {
        setTerminus(terminusPicture, terminusPicture.nearestAttachmentIndex(point));
    }


    final void setOrigin(VertexPicture originPicture, int originAttachmentIndex) {
        this.originPicture = originPicture;
        this.originAttachmentIndex = originAttachmentIndex;
        Point point = originPoint();
        xPoints[0] = point.x;
        yPoints[0] = point.y;
        if (originPicture != null && terminusPicture != null) {
            edge = createEdge();
        }
    }


    final void setTerminus(VertexPicture terminusPicture, int terminusAttachmentIndex) {
        this.terminusPicture = terminusPicture;
        this.terminusAttachmentIndex = terminusAttachmentIndex;
        Point point = terminusPoint();
        int last = getPointCount() - 1;
        xPoints[last] = point.x;
        yPoints[last] = point.y;
        clearShape();
        if (originPicture != null && terminusPicture != null) {
            edge = createEdge();
        }
    }


    final void setEndPoint(Point point) {
        terminusPicture = null;
        dragPoint = point;
        int last = getPointCount() - 1;
        xPoints[last] = point.x;
        yPoints[last] = point.y;
        clearShape();
    }
    
    
    final void setHoverPoint(Point point) {
        hoverIndex = NO_INDEX;
        if (point != null) {
            int index = indexNear(point);
            if (index != NO_INDEX) {
                if (squareDistance(new Point(xPoints[index], yPoints[index]), point) < NEAR_TOLERANCE) {
                    hoverIndex = index;
                }
                else if (squareDistance(new Point(xPoints[index + 1], yPoints[index + 1]), point) < NEAR_TOLERANCE) {
                    hoverIndex = index + 1;
                }
            }
        }
    }
    
    
    final void selectDragPoint(Point point) {
        int index = indexNear(point);
        if (index != NO_INDEX) {
            if (squareDistance(new Point(xPoints[index], yPoints[index]), point) < NEAR_TOLERANCE) {
                dragIndex = index;
                avoidTerminalDrag(point);
            }
            else if (squareDistance(new Point(xPoints[index + 1], yPoints[index + 1]), point) < NEAR_TOLERANCE) {
                dragIndex = index + 1;
                avoidTerminalDrag(point);
            }
            else {
                dragIndex = index + 1;
                insertPoint(point, dragIndex);
            }
        }
    }
    
    
    final boolean hasDragPoint() {
        return dragIndex != NO_INDEX;
    }
    
    
    final void setDragLocation(Point point) {
        xPoints[dragIndex] = point.x;
        yPoints[dragIndex] = point.y;
        clearShape();
    }
    
    
    final void finishDrag() {
        hoverIndex= NO_INDEX;
        dragIndex = NO_INDEX;
        cleanup();
    }
    
    
    final void cleanup() {
        removeTwins();
        removeExtremeAngles();
        clearShape();
    }
    
    
    final void correctEndPoint(VertexPicture vertexPicture) {
        assert vertexPicture != null;
        if (vertexPicture == originPicture) {
            Point point = originPoint();
            xPoints[0] = point.x;
            yPoints[0] = point.y;
            clearShape();
        }
        if (vertexPicture == terminusPicture) {
            Point point = terminusPoint();
            int last = getPointCount() - 1;
            xPoints[last] = point.x;
            yPoints[last] = point.y;
            clearShape();
        }
        
    }
    
    
    final boolean hasOrigin(VertexPicture vertexPicture, int index) {
        return vertexPicture == originPicture && index == originAttachmentIndex;
    }
    
    
    final void move(int deltaX, int deltaY) {
        for (int i = 1; i < getPointCount() - 1; ++i) {
            xPoints[i] += deltaX;
            yPoints[i] += deltaY;
        }
    }


   @Override
    protected final int yNorth() {
        int northMost = Integer.MAX_VALUE;
        for (int y : yPoints) {
            northMost = Math.min(y, northMost);
        }
        return northMost;
    }
    

    @Override
    protected final int ySouth() {
        int southMost = Integer.MIN_VALUE;
        for (int y : yPoints) {
            southMost = Math.max(y, southMost);
        }
        return southMost;
    }
    

    @Override
    protected final int xWest() {
        int westMost = Integer.MAX_VALUE;
        for (int x : xPoints) {
            westMost = Math.min(x, westMost);
        }
        return westMost;
    }
    

    @Override
    protected final int xEast() {
        int eastMost = Integer.MIN_VALUE;
        for (int x : xPoints) {
            eastMost = Math.max(x, eastMost);
        }
        return eastMost;
    }
    
    
    protected String getText() {
        return null;
    }


   protected final Point originPoint() {
        if (originPicture != null) {
            return originPicture.getAttachmentPoint(originAttachmentIndex);
        }
        else {
            return dragPoint;
        }
    }
    
    
    protected final Point terminusPoint() {
        if (terminusPicture != null) {
            return terminusPicture.getAttachmentPoint(terminusAttachmentIndex);
        }
        else {
            return dragPoint;
        }
    }
    
    
    protected final int getPointCount() {
        return xPoints.length;
    }
    
    
    protected Edge createEdge() {
        Edge newEdge = null;
        try {
            newEdge = (Edge) edgeClass().newInstance();
            newEdge.init(getOriginPicture().getVertex(), getTerminusPicture().getVertex());
        }
        catch (ReflectiveOperationException ex) {
            Logger.getLogger(EdgePicture.class.getName()).log(Level.SEVERE, "Cannot create edge", ex);
        }
        return newEdge;
    }
    
    
    protected AbstractEditPanel getEditPanel() {
        return null;
    }


    @Override
    protected void paintShape(Graphics2D g2d, DrawStyle drawStyle) {
        Paint drawPaint = drawStyle.getPaint(DRAW);
        if (drawPaint != null) {
            g2d.setPaint(drawPaint);
            Stroke stroke = drawStyle.getStroke(DRAW);
            g2d.setStroke((stroke != null) ? stroke : DEFAULT_STROKE);
            g2d.draw(getShape());
        }
        if (edge == null || edge.isDirected()) {
            int index = arrowHeadLineIndex();
            double angle = angle(index);
            Point location = arrowheadLocation();
            if (location != null) {
                DrawStyle style = DrawStyleManager.getInstance().getDrawStyle(this);
                g2d.setStroke(style.getStroke(ARROW_HEAD));
                g2d.translate(location.x, location.y);
                g2d.rotate(angle);
                paintArrowhead(g2d);
                g2d.rotate(- angle);
                g2d.translate(- location.x, - location.y);
            }
        }
        if (hoverIndex != NO_INDEX) {
            g2d.setColor(Color.BLACK);
            g2d.drawOval(xPoints[hoverIndex] - 2, yPoints[hoverIndex] - 2, 5, 5);
        }
        paintText(g2d);
    }


    @Override
    protected Shape buildShape() {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < xPoints.length; ++i) {
            path.lineTo(xPoints[i], yPoints[i]);
        }
        return path;
    }


    protected void paintArrowhead(Graphics2D g2d) {
        int[] xCoordinates = {-5, 0,  5};
        int[] yCoordinates = {-5, 5, -5};
        g2d.fillPolygon(xCoordinates, yCoordinates, xCoordinates.length);
    }


    protected int arrowHeadLineIndex() {
        int n = getPointCount();
        int index = n / 2;
        if (n % 2 == 0) {
            index--;
        }
        return index;
    }


    protected Point arrowheadLocation() {
        return coordinateOnLine(arrowHeadLineIndex(), 0.5);
    }


    private void paintText(Graphics2D g2d) {
        String text = getText();
        if (text != null && ! text.isEmpty()) {
            int left = Math.min(xPoints[0], xPoints[xPoints.length - 1]);
            int right = Math.max(xPoints[0], xPoints[xPoints.length - 1]);
            int top = Math.min(yPoints[0], yPoints[yPoints.length - 1]);
            int bottom = Math.max(yPoints[0], yPoints[yPoints.length - 1]);
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(text, null);
            float x = left + (right - left) / 2.0f - (float) bounds.getCenterX();
            float y = top + (bottom - top) / 2.0f - (float) bounds.getCenterY();
            g2d.drawString(text, x, y);
        }
    }
    
    
    private int deltaX(int index) {
        return xPoints[index+1] - xPoints[index];
    }


    private int deltaY(int index) {
        return yPoints[index+1] - yPoints[index];
    }

    
    protected double angle(int index) {
        double angle = 0.5 * Math.PI;
        int dx = deltaX(index);
        int dy = deltaY(index);
        if (dy > 0) {
            angle = - Math.atan((double) dx / (double) dy);
        }
        else if (dy < 0) {
            angle = - Math.atan((double) dx / (double) dy) + Math.PI;
        }
        else if (dx > 0) {
            angle = -0.5 * Math.PI;
        }
        return angle;
    }
    
    
    private void insertPoint(Point point, int index) {
        int[] xOld = xPoints;
        int[] yOld = yPoints;
        int count = getPointCount();
        xPoints = new int[count + 1];
        yPoints = new int[count + 1];
        arraycopy(xOld, yOld, 0, 0, index);
        xPoints[index] = point.x;
        yPoints[index] = point.y;
        arraycopy(xOld, yOld, index, index + 1, count - index);
    }
    
    
   private void removePoint(int index) {
        int[] xOld = xPoints;
        int[] yOld = yPoints;
        int count = getPointCount();
        xPoints = new int[count - 1];
        yPoints = new int[count - 1];
        arraycopy(xOld, yOld, 0, 0, index);
        arraycopy(xOld, yOld, index + 1, index, count - 1 - index);
   }
   
   
    private void avoidTerminalDrag(Point point) {
        if (dragIndex == 0) {
            dragIndex = 1;
            insertPoint(point, dragIndex);
        }
        else if (dragIndex == getPointCount() - 1) {
            insertPoint(point, dragIndex);
        }
    }

    
    private void removeTwins() {
        if (getPointCount() > 2) {
            int i = 0;
            while (i < getPointCount() - 1) {
                if (squareDistance(point(i), point(i + 1)) < NEAR_TOLERANCE) {
                    if (i == 0) {
                        removePoint(1);
                    }
                    else {
                        removePoint(i);
                    }
                }
                else {
                    i++;
                }
            }
        }
    }
    
    
    private void removeExtremeAngles() {
        int i = 1;
        while (i < getPointCount() - 1) {
            double cosine = vectorCosine(i);
            if (cosine < -0.99 || 0.99 < cosine) {
                removePoint(i);
            }
            else {
                i++;
            }
        }
    }
    
    
    private Point point(int index) {
        return new Point(xPoints[index], yPoints[index]);
    }
    
    
    /**
     * @param index: index start point the line in xPoints, yPoints
     * @param position: [0..1] relative position on vector; 0 = start point, 1 = end point
     * @return Coordinate of the relative position on the line starting at (xPoints[index], yPoints[index])
     */
    private Point coordinateOnLine(int index, double position) {
        return new Point(
            (int) Math.round(xPoints[index] + deltaX(index) * position),
            (int) Math.round(yPoints[index] + deltaY(index) * position));
    }
    
    
    private int indexNear(Point point) {
        int count = getPointCount() - 1;
        int index = 0;
        while (index < count) {
            int x1 = xPoints[index];
            int y1 = yPoints[index];
            int x2 = xPoints[index+1];
            int y2 = yPoints[index+1];
            int xRangeMin = Math.min(x1, x2) - NEAR_TOLERANCE;
            int xRangeMax = Math.max(x1, x2) + NEAR_TOLERANCE;
            int yRangeMin = Math.min(y1, y2) - NEAR_TOLERANCE;
            int yRangeMax = Math.max(y1, y2) + NEAR_TOLERANCE;
            if (xRangeMin < point.x && point.x < xRangeMax && yRangeMin < point.y && point.y < yRangeMax) {
                int dx = deltaX(index);
                int dy = deltaY(index);
                if (-NEAR_TOLERANCE < dx && dx < NEAR_TOLERANCE || -NEAR_TOLERANCE < dy && dy < NEAR_TOLERANCE) {
                    return index;
                }
                else {
                    double a = (double) dy / dx;
                    double b = -a * x1 + y1; 
                    if (dy < dx) {
                        double y = a * point.x + b;
                        if (y - NEAR_TOLERANCE < point.y && point.y < y + NEAR_TOLERANCE) {
                            return index;
                        }
                    }
                    else {
                        double x = (b - point.y) / -a;
                        if (x - NEAR_TOLERANCE < point.x && point.x < x + NEAR_TOLERANCE) {
                            return index;
                        }
                    }
                }
            }
            index++;
        }
        return NO_INDEX;
    }
    
    
    private double vectorCosine(int index) {
        Vector v1 = new Vector(xPoints[index] - xPoints[index - 1], yPoints[index] - yPoints[index - 1]);
        Vector v2 = new Vector(xPoints[index + 1] - xPoints[index], yPoints[index + 1] - yPoints[index]);
        return Vector.cosine(v1, v2);
    }
    
    
   private void arraycopy(int[] x, int[] y, int sourceIndex, int destinationIndex, int count) {
       System.arraycopy(x, sourceIndex, xPoints, destinationIndex, count);
       System.arraycopy(y, sourceIndex, yPoints, destinationIndex, count);
   }
    
    
    protected Edge edge;
    protected VertexPicture originPicture;
    protected VertexPicture terminusPicture;
    
    private Point dragPoint;
    private int dragIndex = NO_INDEX;
    
    private int hoverIndex = NO_INDEX;
    
    private int originAttachmentIndex;
    private int terminusAttachmentIndex;
    
    private int[] xPoints = new int[2];
    private int[] yPoints = new int[2];
    
    private static final int NEAR_TOLERANCE = 7;
    
    private static final int NO_INDEX = -1;

}
