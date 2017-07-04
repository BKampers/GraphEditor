/*
** Copyright © Bart Kampers
*/


package bka.graph.document;


import bka.graph.*;


/**
 * ContainerEdges are added to a graph when one Vertex is located inside another.
 * The origin is the container vertex and the terminus is the contained vertex.
 * @see VertexPicture.contains
 */
public class ContainerEdge extends DirectedEdge {
    
    
    ContainerEdge(Vertex container, Vertex contained) {
        super(container, contained);
    }
    
    
}
