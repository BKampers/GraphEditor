/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import bka.graph.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.*;
import java.util.*;
import javax.swing.*;


public class VertexPicture extends AbstractPicture {

    
    public VertexPicture() {
        size = new Dimension(10, 10);
    }


    public Vertex getVertex() {
        return vertex;
    }
    
    
    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    
    public Point getLocation() {
        return location;
    }
    
    
    public void setLocation(Point location) {
        this.location = location;
        initAttachmentPoints();
        clearShape();
    }
    
    
    public Dimension getSize() {
        return size;
    }    
    
    
    public void setSize(Dimension size) {
        this.size = size;
        initAttachmentPoints();
        clearShape();
    }
    
    
    public void resize(Location direction, Point point) {
        switch (direction) {
            case NORTH:
            case SOUTH:
                setSize(new Dimension(size.width, resizeHeigth(point)));
                break;
            case WEST:
            case EAST:
                setSize(new Dimension(resizeWidth(point), size.height));
                break;
            case NORTH_WEST: 
            case NORTH_EAST: 
            case SOUTH_WEST: 
            case SOUTH_EAST: 
                setSize(new Dimension(resizeWidth(point), resizeHeigth(point)));
                break;
        }
    }
    
    
    public Location locationOf(Point point) {
        if (insideBounds(point)) {
            return internalLocation(point);
        }
        return Location.EXTERN;
    }


    private Location internalLocation(Point point) {
        Location internal = Location.INTERIOR;
        if (point.y <= yNorth() + LOCATION_NEAR_DISTANCE) {
            internal = Location.NORTH;
        }
        else if (point.y >= ySouth() - LOCATION_NEAR_DISTANCE) {
            internal = Location.SOUTH;
        }
        if (point.x <= xWest() + LOCATION_NEAR_DISTANCE) {
            switch (internal) {
                case NORTH:
                    return Location.NORTH_WEST;
                case SOUTH:
                    return Location.SOUTH_WEST;
                default:
                    return Location.WEST;
            }
        }
        else if (point.x >= xEast() - LOCATION_NEAR_DISTANCE) {
            switch (internal) {
                case NORTH:
                    return Location.NORTH_EAST;
                case SOUTH:
                    return Location.SOUTH_EAST;
                default:
                    return Location.EAST;
            }
        }
        return internal;
    }

    
    @Override
    public boolean isLocatedAt(Point point) {
        return locationOf(point) != Location.EXTERN;
    }


    /**
     * @param picture any VertexPicture
     * @return true if given picture is located inside this picture
     */
    public boolean contains(VertexPicture picture) {
        Rectangle intersection = getRectangle().intersection(picture.getRectangle());
        if (intersection.isEmpty()) {
            return false;
        }
        int intersectionArea = intersection.width * intersection.height;
        return picture.getArea() / 2 < intersectionArea && intersectionArea < getArea();
    }


    // This class is public but can only initialized from within its package.
    final void initializeVertex() {
        vertex = createVertex();
    }


    final Image createImage() {
        BufferedImage image = new BufferedImage(size.width + 1, size.height + 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.translate(-xWest(), -yNorth());
        paint(g2d);
        return image;
    }


    Point nearestAttachmentPoint(Point point) {
        int index = nearestAttachmentIndex(point);
        if (index < 0) {
            return null;
        }
        return attachmentPoints[index];
    }


    int nearestAttachmentIndex(Point point) {
        int index = -1;
        int shortest = -1;
        for (int i = 0; i < attachmentPoints.length; ++i) {
            Point attachmentPoint = attachmentPoints[i];
            int distance = squareDistance(attachmentPoint, point);
            if (distance < ATTACHMENT_NEAR_DISTANCE && (shortest < 0 || distance < shortest)) {
                index = i;
                shortest = distance;
            }
        }
        return index;
    }


    Point getAttachmentPoint(int index) {
        if (0 <= index && index < attachmentPoints.length) {
            return attachmentPoints[index];
        }
        else {
            return null;
        }
    }


    protected Vertex createVertex() {
        return new NamedVertex();
    }
    
    
    protected void initAttachmentPoints() {
        attachmentPoints = new Point[1];
        attachmentPoints[0] = location;
    }
    
    
    protected AbstractEditPanel getEditPanel() {
        return null;
    }
    
    
    @Override
    protected final int yNorth() {
        return location.y - size.height / 2;
    }
    

    @Override
    protected final int ySouth() {
        return location.y + size.height / 2;
    }
    

    @Override
    protected final int xWest() {
        return location.x - size.width / 2;
    }
    

    @Override
    protected final int xEast() {
        return location.x + size.width / 2;
    }


    @Override
    protected void paintText(Graphics2D g2d) {
        paintText(g2d, vertex.getName(), new Point2D.Float(xEast(), yNorth()), getFont("TEXT"));
    }
    

    @Override
    protected void paintShape(Graphics2D g2d) {
        Paint paint = getFillPaint();
        if (paint != null) {
            g2d.setPaint(paint);
            g2d.fill(getShape());
        }
        Color drawColor = getColor(DRAW);
        if (drawColor != null) {
            g2d.setPaint(drawColor);
            g2d.setStroke(getStroke(DRAW));
            g2d.draw(getShape());
        }
    }


    @Override
    protected Shape buildShape() {
        return new Ellipse2D.Float(xWest(), yNorth(), size.width, size.height);
    }


    @Override 
    protected void clearShape() {
        super.clearShape();
        fillPaint = null;
    }


    @Override
    protected String getToolTipText() {
        return null;
    }

    protected Paint getFillPaint() {
        if (fillPaint == null) {
            fillPaint = getDrawStyle().createGradientPaint(FILL, new Rectangle2D.Float(xWest(), yNorth(), size.width, size.height));
            if (fillPaint == null) {
                fillPaint = getColor(FILL);
            }
        }
        return fillPaint;
    }


    protected final javax.swing.Icon createIcon(int width, int height) {
        return new ImageIcon(createImage(width, height));
    }
    
    
    protected final Image createImage(int width, int height) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintIcon(g2d, width, height);
        return image;
    }
    
    
    protected void paintIcon(Graphics2D g2d, int width, int height) {
        int diameter = Math.min(width, height) - 2;
        g2d.fillOval(1, 1, diameter, diameter);        
    }
    
    
    protected void paintText(Graphics2D g2d, String text) {
        paintText(g2d, text, TEXT);

    }


    protected void paintText(Graphics2D g2d, String text, Object fontKey) {
        paintText(g2d, text, new Point2D.Float(location.x, location.y), getFont(fontKey));

    }


    protected void paintText(Graphics2D g2d, String text, Point2D.Float position) {
        paintText(g2d, text, position, TEXT);

    }


    protected void paintText(Graphics2D g2d, String text, Point2D.Float position, Object fontKey) {
        paintText(g2d, text, position, getFont(fontKey));

    }


    protected void paintText(Graphics2D g2d, String text, int row) {
        paintText(g2d, text, row, TEXT);

    }


    protected void paintText(Graphics2D g2d, String text, int row, Object fontKey) {
        paintText(g2d, text, row, getFont(fontKey));

    }


    protected void paintText(Graphics2D g2d, String text, int row, Map<? extends Attribute, ?> attributes) {
        if (text != null && ! text.isEmpty()) {
            float x = location.x;
            float y = yNorth() + g2d.getFontMetrics().getHeight() * row;
            paintText(g2d, text, new Point2D.Float(x, y), attributes);
        }
    }


    protected void paintText(Graphics2D g2d, String text, Point2D.Float position, Map<? extends Attribute, ?> attributes) {
        if (text != null && ! text.isEmpty()) {
            AttributedString string = new AttributedString(text);
            string.addAttributes(attributes, 0, text.length());
            TextLayout layout = new TextLayout(string.getIterator(), g2d.getFontRenderContext());
            Rectangle2D bounds = layout.getBounds();
            float x = position.x - (float) bounds.getWidth() / 2.0f;
            float y = position.y - (float) bounds.getCenterY() / 2.0f;
            layout.draw(g2d, x, y);
        }
    }
    
    
   private int resizeWidth(Point point) {
        return Math.abs(point.x - location.x) * 2;
    }


    private int resizeHeigth(Point point) {
        return Math.abs(point.y - location.y) * 2;
    }


    private int getArea() {
        return size.width * size.height;
    }


    private boolean insideBounds(Point point) {
        return
            xWest() - LOCATION_NEAR_DISTANCE <= point.x && point.x <= xEast() + LOCATION_NEAR_DISTANCE &&
            yNorth() - LOCATION_NEAR_DISTANCE <= point.y && point.y <= ySouth() + LOCATION_NEAR_DISTANCE;
    }


    protected Point[] attachmentPoints;
    
    protected Point location;
    protected Vertex vertex;
     
    protected Dimension size;

    private Paint fillPaint;
    
    private static final int LOCATION_NEAR_DISTANCE = 3;
    private static final int ATTACHMENT_NEAR_DISTANCE = 100;

}
