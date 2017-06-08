/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;


import bka.awt.*;
import java.awt.*;


public abstract class AbstractPicture {

    public static final String DRAW = "DRAW";
    public static final String FILL = "FILL";



    public abstract boolean isLocatedAt(Point point);
    
    protected abstract void paintShape(Graphics2D g2d, DrawStyle drawStyle);
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
        DrawStyle drawStyle = DrawStyleManager.getInstance().getDrawStyle(this);
        paintShape(g2d, drawStyle);
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


    
    protected static int squareDistance(Point p, Point q) {
        int δx = p.x - q.x;
        int δy = p.y - q.y;
        return δx*δx + δy*δy;
    }


    protected static final Stroke DEFAULT_STROKE = new BasicStroke();
    
    private  Shape shape;

}
