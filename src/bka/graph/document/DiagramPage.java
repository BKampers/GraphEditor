/*
** Copyright © Bart Kampers
*/

package bka.graph.document;

import bka.graph.swing.*;
import java.util.*;
import java.util.logging.*;


public class DiagramPage {

    
    public DiagramPage() {
    }


    public static DiagramPage createEmpty() {
        DiagramPage empty = new DiagramPage();
        empty.vertices = new LinkedHashSet<>();
        empty.edges = new LinkedHashSet<>();
        return empty;
    }
    
    
    public String getTitle() {
        return title;
    }

    
    public void setTitle(String title) {
        this.title = title;
    }

    
    public Collection<VertexPicture> getVertices() {
        return (vertices != null) ? new ArrayList<>(vertices) : null;
    }

    
    public void setVertices(Collection<VertexPicture> vertices) {
        this.vertices = (vertices != null) ? new LinkedHashSet<>(vertices) : null;
    }


    public Collection<EdgePicture> getEdges() {
        return (edges != null) ? new ArrayList<>(edges) : null;
    }

    
    public void setEdges(Collection<EdgePicture> edges) {
        this.edges = (edges != null) ? new LinkedHashSet<>(edges) : null;
    }
    
    
    public void add(VertexPicture vertex) {
        if (! vertices.add(vertex)) {
            Logger.getLogger(DiagramPage.class.getName()).log(Level.WARNING, "Duplicate vertex {0}", vertex);
        }
    }


    public void remove(VertexPicture vertex) {
        vertices.remove(vertex);
    }


    public void add(EdgePicture edge) {
        if (! edges.add(edge)) {
            Logger.getLogger(DiagramPage.class.getName()).log(Level.WARNING, "Duplicate edge {0}", edge);
        }
    }


    public void remove(EdgePicture edge) {
        edges.remove(edge);
    }


    VertexPicture findContainer(VertexPicture vertex) {
        for (VertexPicture picture : vertices) {
            if (vertex != picture && picture.isLocatedAt(vertex.getLocation())) {
                return picture;
            }
        }
        return null;
    }


    private String title;
    // LinkedHashSet to keep drawing order and to avoid duplicate pictures
    private LinkedHashSet<VertexPicture> vertices;
    private LinkedHashSet<EdgePicture> edges;

}
