/*
** © Bart Kampers
*/

package bka.graph.swing;

import java.awt.*;


public abstract class VertexPopup {
    
    protected abstract String initialText();
    protected abstract Rectangle bounds();
    protected abstract void apply(String text);
    
}
