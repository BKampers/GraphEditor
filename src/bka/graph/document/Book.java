/*
** Copyright © Bart Kampers
*/

package bka.graph.document;

import bka.awt.*;
import bka.graph.*;
import bka.graph.swing.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;


public class Book {


    public Book(Map<Class, PersistenceDelegate> persistenceDelegates) {
        this.persistenceDelegates = persistenceDelegates;
    }


    public void save(File file) throws FileNotFoundException {
        try (XMLEncoder xmlEncoder = createEncoder(file)) {
            xmlEncoder.writeObject(pages);
            xmlEncoder.writeObject(pageIndex);
            xmlEncoder.writeObject(DrawStyleManager.getInstance().getCustomizedDrawStyles());
        }
    }


   public void load(File file) throws FileNotFoundException {
        try (XMLDecoder xmlDecoder = createDecoder(file)) {
            pages.clear();
            Object read = readObject(xmlDecoder);
            if (read != null) {
                pages.addAll((ArrayList<DiagramPage>) read);
            }
            read = readObject(xmlDecoder);
            if (read != null) {
                pageIndex = (Integer) read;
            }
            read = readObject(xmlDecoder);
            if (read != null) {
                DrawStyleManager.getInstance().setDrawStyles((Map<Object, DrawStyle>) read);
            }
        }
    }


    private static Object readObject(XMLDecoder xmlDecoder) {
        try {
            return xmlDecoder.readObject();
        }
        catch (RuntimeException ex) {
            Logger.getLogger(Book.class.getName()).log(Level.WARNING, "read object", ex);
            return null;
        }
    }


    public ArrayList<DiagramPage> getDiagramPages() {
        return new ArrayList<>(pages);
    }


    public void addPage(DiagramPage page) {
        pages.add(page);
    }


    public int getPageIndex() {
        return pageIndex;
    }


    public void setPageIndex(int selectedIndex) {
        this.pageIndex = selectedIndex;
    }


    public Collection<Vertex> allVertices() {
        Collection<Vertex> vertices = new HashSet<>(); // HashSet avoids duplicates
        for (DiagramPage page : pages) {
            for (VertexPicture picture : page.getVertices()) {
                vertices.add(picture.getVertex());
            }
        }
        return vertices;
    }


    public Collection<Vertex> allVertices(Class<? extends Vertex> vertexClass) {
        Collection<Vertex> vertices = new HashSet<>(); // HashSet avoids duplicates
        for (DiagramPage page : pages) {
            for (VertexPicture picture : page.getVertices()) {
                Vertex vertex = picture.getVertex();
                if (vertex.getClass() == vertexClass) {
                    vertices.add(vertex);
                }
            }
        }
        return vertices;
    }


    public Collection<Edge<Vertex>> allEdges() {
        Collection<Edge<Vertex>> edges = new HashSet<>(); // HashSet avoids duplicates
        for (DiagramPage page : pages) {
            for (EdgePicture picture : page.getEdges()) {
                edges.add(picture.getEdge());
            }
        }
        return edges;
    }


    public Collection<Edge<Vertex>> allEdges(Class<? extends Edge> edgeClass) {
        Collection<Edge<Vertex>> edges = new HashSet<>(); // HashSet avoids duplicates
        for (DiagramPage page : pages) {
            for (EdgePicture  picture : page.getEdges()) {
                Edge edge = picture.getEdge();
                if (edge.getClass() == edgeClass) {
                    edges.add(edge);
                }
            }
        }
        return edges;
    }


    public Graph<Vertex, Edge<Vertex>> graph() {
        Graph <Vertex, Edge<Vertex>>graph = new Graph(allVertices(), allEdges());
        for (DiagramPage page : pages) {
            for (VertexPicture vertexPicture : page.getVertices()) {
                VertexPicture container = page.findContainer(vertexPicture);
                if (container != null) {
                    ContainerEdge containerEdge = new ContainerEdge(container.getVertex(), vertexPicture.getVertex());
                    if (! graph.contains(containerEdge)) {
                        graph.add(containerEdge);
                    }
                }
            }
        }
        return graph;
    }


    private XMLEncoder createEncoder(File file) throws FileNotFoundException {
        XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
        if (persistenceDelegates != null) {
            for (Map.Entry<Class, PersistenceDelegate> entry : persistenceDelegates.entrySet()) {
                xmlEncoder.setPersistenceDelegate(entry.getKey(), entry.getValue());
            }
        }
        return xmlEncoder;
    }


    private XMLDecoder createDecoder(File file) throws FileNotFoundException {
        return new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
    }


    private final Map<Class, PersistenceDelegate> persistenceDelegates;
    private final ArrayList<DiagramPage> pages = new ArrayList<>();
    private int pageIndex;

}
