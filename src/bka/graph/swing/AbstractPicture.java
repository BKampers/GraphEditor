/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;


import bka.awt.*;
import java.awt.*;
import java.awt.font.*;
import java.util.*;


public abstract class AbstractPicture {

    public static final String DRAW = "DRAW";
    public static final String FILL = "FILL";
    public static final String TEXT = "TEXT";


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


    public String[] getCustomizablePaints() {
        return new String[] { };
    }


    public Rectangle bounds() {
        int x = xWest();
        int y = yNorth();
        return new Rectangle(x, y, xEast() - x, ySouth() - y);
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


    protected Rectangle getRectangle() {
        return new Rectangle(xWest(), yNorth(), xEast() - xWest(), ySouth() - yNorth());
    }


    protected static int squareDistance(Point p, Point q) {
        int δx = p.x - q.x;
        int δy = p.y - q.y;
        return δx*δx + δy*δy;
    }


    protected static final Stroke DEFAULT_STROKE = new BasicStroke();
    protected static final Map<TextAttribute, Object> DEFAULT_FONT = Collections.EMPTY_MAP;
    
    private  Shape shape;

}
