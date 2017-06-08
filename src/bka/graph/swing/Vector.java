package bka.graph.swing;


class Vector {

    
    Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    
    Vector(Vector other) {
        x = other.x;
        y = other.y;
    }
    
    
    void normalize() {
        double magnitude = magnitude();
        x /= magnitude;
        y /= magnitude;
    }


    double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    
    void add(Vector other) {
        x += other.x;
        y += other.y;
    }

    
    void subtract(Vector other) {
        x -= other.x;
        y -= other.y;
    }
    
    
    void scale(double factor) {
        x *= factor;
        y *= factor;
    }


    static double dotProduct(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }


    static double cosine(Vector v1, Vector v2) {
        Vector v1copy = new Vector(v1);
        Vector v2copy = new Vector(v2);
        v1copy.normalize();
        v2copy.normalize();
        return dotProduct(v1copy, v2copy);
    }


    private double x;
    private double y;
    
}
