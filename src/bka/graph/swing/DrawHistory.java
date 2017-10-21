/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import java.util.*;


class DrawHistory {


    DrawHistory(DiagramComponent diagramComponent) {
        this.diagramComponent = diagramComponent;
    }


    void addVertexInsertion(VertexPicture vertexPicture) {
        addToHistory(new PictureInsertion(vertexPicture));
    }


    void addVertexMutation(VertexPicture source, VertexPicture destination) {
        addToHistory(new VertexMutation(source, destination));
    }


    void addVertexDeletion(VertexPicture vertexPicture, Collection<EdgePicture> edgePictures) {
        addToHistory(new PictureDeletion(vertexPicture, edgePictures));
    }


    void addEdgeInsertion(EdgePicture edgePicture) {
        addToHistory(new PictureInsertion(edgePicture));
    }


    void addEdgeMutation(EdgePicture source, EdgePicture destination) {
        addToHistory(new EdgeMutation(source, destination));
    }


    void addEdgeDeletion(EdgePicture edgePicture) {
        addToHistory(new PictureDeletion(edgePicture));
    }


    Mutation getUndo() {
        if (index > 0) {
            index--;
            return history.get(index);
        }
        return null;
    }


    Mutation getRedo() {
        if (index < history.size()) {
            Mutation mutation = history.get(index);
            index++;
            return mutation;
        }
        return null;
    }


    private void addToHistory(Mutation mutation) {
        while (index < history.size()) {
            history.removeLast();
        }
        history.add(mutation);
        index++;
    }


    private class PictureInsertion implements Mutation {

        PictureInsertion(VertexPicture picture) {
            vertexPictures.add(picture);
        }

        PictureInsertion(EdgePicture picture) {
            edgePictures.add(picture);
        }

        @Override
        public void undo() {
            diagramComponent.removePictures(vertexPictures, edgePictures);
        }

        @Override
        public void redo() {
            diagramComponent.insertPictures(vertexPictures, edgePictures);
        }

        private final Collection<VertexPicture> vertexPictures = new ArrayList<>();
        private final Collection<EdgePicture> edgePictures = new ArrayList<>();

    }


    private class PictureDeletion implements Mutation {

        PictureDeletion(EdgePicture edgePicture) {
            edgePictures.add(edgePicture);
        }

        PictureDeletion(VertexPicture vertexPicture, Collection<EdgePicture> edgePictures) {
            this.vertexPictures.add(vertexPicture);
            this.edgePictures.addAll(edgePictures);
        }

        @Override
        public void undo() {
            diagramComponent.insertPictures(vertexPictures, edgePictures);
        }

        @Override
        public void redo() {
            diagramComponent.removePictures(vertexPictures, edgePictures);
        }

        private final Collection<VertexPicture> vertexPictures = new ArrayList<>();
        private final Collection<EdgePicture> edgePictures = new ArrayList<>();

    }


    private class VertexMutation implements Mutation {

        VertexMutation(VertexPicture source, VertexPicture destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public void undo() {
            revertMutation();
        }

        @Override
        public void redo() {
            revertMutation();
        }

        private void revertMutation() {
            VertexPicture copy = new VertexPicture(destination);
            diagramComponent.revertVertexMutation(source, destination);
            source = copy;
        }

        private VertexPicture source;
        private final VertexPicture destination;
    }


    private class EdgeMutation implements Mutation {

        public EdgeMutation(EdgePicture source, EdgePicture destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public void undo() {
            revert();
        }

        @Override
        public void redo() {
            revert();
        }

        private void revert() {
            EdgePicture copy = new EdgePicture(destination);
            destination.setXPoints(source.getXPoints());
            destination.setYPoints(source.getYPoints());
            source = copy;
        }

        private EdgePicture source;
        private final EdgePicture destination;

    }


    private final DiagramComponent diagramComponent;
    private final LinkedList<Mutation> history = new LinkedList<>();

    private int index;

}
