/*
** Copyright © Bart Kampers
*/

package bka.graph.document;

import bka.graph.swing.*;
import java.util.*;


public class DiagramPage {

    
    public DiagramPage() {
    }


    public static DiagramPage createEmpty() {
        DiagramPage empty = new DiagramPage();
        empty.vertices = new ArrayList<>();
        empty.edges = new ArrayList<>();
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
        this.vertices = (vertices != null) ? new ArrayList<>(vertices) : null;
    }


    public Collection<EdgePicture> getEdges() {
        return (edges != null) ? new ArrayList<>(edges) : null;
    }

    
    public void setEdges(Collection<EdgePicture> edges) {
        this.edges = (edges != null) ? new ArrayList<>(edges) : null;
    }
    
    
    public void add(VertexPicture vertex) {
        vertices.add(vertex);
    }


    public void remove(VertexPicture vertex) {
        vertices.remove(vertex);
    }


    public void add(EdgePicture edge) {
        edges.add(edge);
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
    private Collection<VertexPicture> vertices;
    private Collection<EdgePicture> edges;

}
