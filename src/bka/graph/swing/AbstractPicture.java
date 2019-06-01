/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;


import bka.awt.*;
import bka.numeric.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;


public abstract class AbstractPicture {


    public enum DrawStyleKey { DRAW, FILL, TEXT };

    public abstract boolean isLocatedAt(Point point);
    
    protected abstract void paintText(Graphics2D g2d);
    protected abstract void paintShape(Graphics2D g2d);
    protected abstract Shape buildShape();

    protected abstract int xWest();
    protected abstract int xEast();
    protected abstract int yNorth();
    protected abstract int ySouth();
    
    protected abstract String getToolTipText();


    public final Shape getShape() {
        if (shape == null) {
            shape = buildShape();
        }
        return shape;
    }


    public final void paint(Graphics2D g2d) {
        paintShape(g2d);
        paintText(g2d);
    }


    public Object[] getCustomizablePaints() {
        return null;
    }


    public Rectangle bounds() {
        int x = xWest();
        int y = yNorth();
        return new Rectangle(x, y, xEast() - x, ySouth() - y);
    }


    public Object getAreaKey(Point point) {
        return null;
    }


    final Rectangle2D getTextArea(Object key) {
        return Objects.requireNonNull(textAreas.get(key));
    }
    

    protected void clearShape() {
        shape = null;
    }


    protected Color getColor(Object key) {
        return getDrawStyle().getColor(key);
    }


    protected DrawStyle.Gradient getGradient(Object key) {
        return getDrawStyle().getGradient(key);
    }


    protected Stroke getStroke(Object key) {
        Stroke stroke = getDrawStyle().getStroke(key);
        return (stroke != null) ? stroke : DEFAULT_STROKE;
    }


    protected Map<TextAttribute, Object> getFont(Object key) {
        Map<TextAttribute, Object> font = getDrawStyle().getFont(key);
        return (font != null) ? font : DEFAULT_FONT;
    }


    protected DrawStyle getDrawStyle() {
        return DrawStyleManager.getInstance().getDrawStyle(this);
    }


    protected Rectangle getBounds() {
        return new Rectangle(xWest(), yNorth(), xEast() - xWest(), ySouth() - yNorth());
    }
    
    
    protected static int squareDistance(Point p, Point q) {
        return Euclidean.squareDistance(q, q);
    }


    protected static final Stroke DEFAULT_STROKE = new BasicStroke();
    protected static final Map<TextAttribute, Object> DEFAULT_FONT = Collections.EMPTY_MAP;
    
    private  Shape shape;
    protected final Map<Object, Rectangle2D> textAreas = new HashMap<>();
    

}
