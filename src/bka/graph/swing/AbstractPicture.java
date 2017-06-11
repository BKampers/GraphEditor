/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;


import bka.awt.*;
import java.awt.*;


public abstract class AbstractPicture {

    public static final String DRAW = "DRAW";
    public static final String FILL = "FILL";
    public static final String TEXT_FOREGROUND = "TEXT_FOREGROUND";
    public static final String TEXT_BACKGROUND = "TEXT_BACKGROUND";


    public abstract boolean isLocatedAt(Point point);
    
    protected abstract void paintText(Graphics2D g2d);
    protected abstract void paintShape(Graphics2D g2d);
    protected abstract Shape buildShape();

    protected abstract int xWest();
    protected abstract int xEast();
    protected abstract int yNorth();
    protected abstract int ySouth();


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


    protected final void clearShape() {
        shape = null;
    }


    protected Paint getPaint(Object key) {
        return getDrawStyle().getPaint(key);
    }


    protected Stroke getStroke(Object key) {
        Stroke stroke = getDrawStyle().getStroke(key);
        return (stroke != null) ? stroke : DEFAULT_STROKE;
    }


    private DrawStyle getDrawStyle() {
        return DrawStyleManager.getInstance().getDrawStyle(this);
    }


    protected static int squareDistance(Point p, Point q) {
        int δx = p.x - q.x;
        int δy = p.y - q.y;
        return δx*δx + δy*δy;
    }


    protected static final Stroke DEFAULT_STROKE = new BasicStroke();
    
    private  Shape shape;

}
