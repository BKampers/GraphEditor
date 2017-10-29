/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import java.awt.*;
import java.util.*;
import java.util.List;


class DrawHistory {


    interface Listener {
        void historyChanged(DrawHistory history);
    }


    DrawHistory(DiagramComponent diagramComponent) {
        this.diagramComponent = diagramComponent;
    }


    void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }


    void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }


    List<Mutation> getMutattions() {
        return Collections.unmodifiableList(history);
    }


    int getIndex() {
        return index;
    }


    void addVertexInsertion(VertexPicture vertexPicture) {
        addToHistory(new PictureInsertion(vertexPicture));
    }


    void addVertexRelocation(VertexPicture picture, Point originalLocation) {
        addToHistory(new VertexRelocation(picture, originalLocation));
    }


    void addVertexResizement(VertexPicture picture, Dimension originalSize) {
        addToHistory(new VertexResizement(picture, originalSize));
    }


    void addVertexDeletion(VertexPicture vertexPicture, Collection<EdgePicture> edgePictures) {
        addToHistory(new PictureDeletion(vertexPicture, edgePictures));
    }


    void addEdgeInsertion(EdgePicture edgePicture) {
        addToHistory(new PictureInsertion(edgePicture));
    }


    void addEdgeTransformation(EdgePicture picture, int[] originalXPoints, int[] originalYPoints) {
        addToHistory(new EdgeTransformation(picture, originalXPoints, originalYPoints));
    }


    void addEdgeDeletion(EdgePicture edgePicture) {
        addToHistory(new PictureDeletion(edgePicture));
    }


    Mutation getUndo() {
        if (index > 0) {
            index--;
            notifyListeners();
            return history.get(index);
        }
        return null;
    }


    Mutation getRedo() {
        if (index < history.size()) {
            Mutation mutation = history.get(index);
            index++;
            notifyListeners();
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
        notifyListeners();
    }


    private void notifyListeners() {
        synchronized (listeners) {
            for (Listener listener : listeners) {
                listener.historyChanged(DrawHistory.this);
            }
        }
    }


    private abstract class AbstractMutation implements Mutation {

        @Override
        public void undo() {
            revert();
        }

        @Override
        public void redo() {
            revert();
        }

        abstract protected void revert();
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


    private class VertexRelocation extends AbstractMutation {

        VertexRelocation(VertexPicture picture, Point originalLocation) {
            this.picture = picture;
            this.originalLocation = originalLocation;
        }

        @Override
        protected void revert() {
            Point currentLocation = picture.getLocation();
            diagramComponent.revertVertexMutation(picture, originalLocation, picture.getSize());
            originalLocation = currentLocation;
        }

        private final VertexPicture picture;
        private Point originalLocation;

    }


    private class VertexResizement extends AbstractMutation {

        public VertexResizement(VertexPicture picture, Dimension originalSize) {
            this.picture = picture;
            this.originalSize = originalSize;
        }

        @Override
        protected void revert() {
            Dimension currentSize = picture.getSize();
            diagramComponent.revertVertexMutation(picture, picture.getLocation(), originalSize);
            originalSize = currentSize;
        }

        private final VertexPicture picture;
        private Dimension originalSize;

    }


    private class EdgeTransformation extends AbstractMutation {

        public EdgeTransformation(EdgePicture picture, int[] originalXPoints, int[] originalYPoints) {
            this.picture = picture;
            this.originalXPoints = originalXPoints;
            this.originalYPoints = originalYPoints;
        }

        @Override
        protected void revert() {
            int[] currentXPoints = picture.getXPoints();
            int[] currentYPoints = picture.getYPoints();
            picture.setXPoints(originalXPoints);
            picture.setYPoints(originalYPoints);
            originalXPoints = currentXPoints;
            originalYPoints = currentYPoints;
        }

        private final EdgePicture picture;
        private int[] originalXPoints;
        private int[] originalYPoints;

    }


    private final DiagramComponent diagramComponent;
    private final LinkedList<Mutation> history = new LinkedList<>();
    private final Collection<Listener> listeners = new ArrayList<>();

    private int index;

}
