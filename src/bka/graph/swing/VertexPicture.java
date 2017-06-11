/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import bka.graph.Vertex;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.*;
import javax.swing.*;


public class VertexPicture extends AbstractPicture {
    
    
    public static final int EXTERN     = 0x00;
    public static final int NORTH      = 0x01;
    public static final int SOUTH      = 0x02;
    public static final int WEST       = 0x04;
    public static final int EAST       = 0x08;
    public static final int NORTH_WEST = NORTH | WEST;
    public static final int NORTH_EAST = NORTH | EAST;
    public static final int SOUTH_WEST = SOUTH | WEST;
    public static final int SOUTH_EAST = SOUTH | EAST;
    public static final int INTERIOR   = NORTH | SOUTH | WEST | EAST;
    
    
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
    
    
    public void resize(int direction, Point point) {
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
    
    
    public int locationOf(Point point) {
        int loc = EXTERN;
        if (insideBounds(point)) {
            if (point.y <= yNorth() + NEAR_TOLERANCE) {
                loc |= NORTH;
            }
            else if (point.y >= ySouth() - NEAR_TOLERANCE) {
                loc |= SOUTH;
            }
            if (point.x <= xWest() + NEAR_TOLERANCE) { 
                loc |= WEST;
            }
            else if (point.x >= xEast() - NEAR_TOLERANCE) {
                loc |= EAST;
            }
            if (loc == EXTERN) {
                loc = INTERIOR;
            }
        }
        return loc;
    }

    
    @Override
    public boolean isLocatedAt(Point point) {
        return locationOf(point) != EXTERN;
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
        return attachmentPoints[nearestAttachmentIndex(point)];
    }


    int nearestAttachmentIndex(Point point) {
        int index = 0;
        Point nearest = attachmentPoints[0];
        int shortest = squareDistance(nearest, point);
        for (int i = 1; i < attachmentPoints.length; i++) {
            Point attachmentPoint = attachmentPoints[i];
            int distance = squareDistance(attachmentPoint, point);
            if (distance < shortest) {
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
        return new Vertex();
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
        paintText(g2d, vertex.getName(), new Point2D.Float(xEast(), yNorth()));
    }
    

    @Override
    protected void paintShape(Graphics2D g2d) {
        Paint fillPaint = getPaint(FILL);
        if (fillPaint != null) {
            g2d.setPaint(fillPaint);
            g2d.fill(getShape());
        }
        Paint drawPaint = getPaint(DRAW);
        if (drawPaint != null) {
            g2d.setPaint(drawPaint);
            g2d.setStroke(getStroke(DRAW));
            g2d.draw(getShape());
        }
    }


    @Override
    protected Shape buildShape() {
        return new Ellipse2D.Float(xWest(), yNorth(), size.width, size.height);
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
    
    
    protected void paintText(Graphics2D g2d, String text, int line) {
        paintText(g2d, text, line, null);
    }


    protected void paintText(Graphics2D g2d, String text, int line, Paint background) {
        if (text != null && ! text.isEmpty()) {
            float x = location.x;
            float y = yNorth() + g2d.getFontMetrics().getHeight() * line;
            paintText(g2d, text, new Point2D.Float(x, y), background);
        }
    }


    protected void paintUnderlinedText(Graphics2D g2d, String text, int line) {
        paintUnderlinedText(g2d, text, line, null);
    }


    protected void paintUnderlinedText(Graphics2D g2d, String text, int line, Color background) {
        if (text != null && ! text.isEmpty()) {
            float x = location.x;
            float y = yNorth() + g2d.getFontMetrics().getHeight() * line;
            paintText(g2d, text, new Point2D.Float(x, y), true, background);
        }
    }


    protected void paintText(Graphics2D g2d, String text) {
        paintText(g2d, text, new Point2D.Float(location.x, location.y));
    }


    protected void paintText(Graphics2D g2d, String text, Point2D.Float textLocation) {
        if (text != null && ! text.isEmpty()) {
            paintText(g2d, text, textLocation, null);
        }
    }


    private void paintText(Graphics2D g2d, String text, Point2D.Float textLocation, Paint background) {
        paintText(g2d, text, textLocation, false, background);
    }


    private void paintText(Graphics2D g2d, String text, Point2D.Float position, boolean underline, Paint background) {
        AttributedString string = new AttributedString(text);
        string.addAttribute(TextAttribute.FONT, g2d.getFont());
        FontMetrics metrics = g2d.getFontMetrics();
        if (background != null) {
            string.addAttribute(TextAttribute.BACKGROUND, background);
        }
        Paint foreground = getPaint(TEXT_FOREGROUND);
        string.addAttribute(TextAttribute.FOREGROUND, (foreground != null) ? foreground : Color.BLACK);
        if (underline) {
            string.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        TextLayout layout = new TextLayout(string.getIterator(), g2d.getFontRenderContext());
        float x = position.x - metrics.stringWidth(text) / 2.0f;
        float y = position.y + metrics.getHeight() - metrics.getAscent();
        layout.draw(g2d, x, y);
    }


     private int resizeWidth(Point point) {
        return Math.abs(point.x - location.x) * 2;
    }


    private int resizeHeigth(Point point) {
        return Math.abs(point.y - location.y) * 2;
    }


    private boolean insideBounds(Point point) {
        return
            xWest() - NEAR_TOLERANCE <= point.x && point.x <= xEast() + NEAR_TOLERANCE &&
            yNorth() - NEAR_TOLERANCE <= point.y && point.y <= ySouth() + NEAR_TOLERANCE;
    }


    protected Point[] attachmentPoints;
    
    protected Point location;
    protected Vertex vertex;
     
    protected Dimension size;
    
    private static final int NEAR_TOLERANCE = 3;

}
