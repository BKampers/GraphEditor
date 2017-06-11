/*
** Copyright © Bart Kampers
*/

package bka.graph.swing;

import bka.awt.*;
import java.util.*;


public class DrawStyleManager {


     public static DrawStyleManager getInstance() {
        if (instance == null) {
            instance = new DrawStyleManager();
        }
        return instance;
    }


   public DrawStyle getDrawStyle(AbstractPicture picture) {
        synchronized (styles) {
            DrawStyle style = styles.get(picture);
            Class cls = picture.getClass();
            while (style == null && cls != null) {
                style = styles.get(cls);
                if (style == null) {
                    cls = cls.getSuperclass();
                }
            }
            return style;
        }
    }


    public void setDrawStyle(Class<? extends AbstractPicture> key, DrawStyle style) {
        synchronized (styles) {
            styles.put(key, style);
        }
    }


    public void setDrawStyle(AbstractPicture key, DrawStyle style) {
        synchronized (styles) {
            styles.put(key, style);
        }
    }


    public Map<Object, DrawStyle> getCustomizedDrawStyles() {
        Map<Object, DrawStyle> customizedDrawStyles = new HashMap<>();
        for (Map.Entry<Object, DrawStyle> entry : styles.entrySet()) {
            if (! (entry.getKey() instanceof Class)) {
                customizedDrawStyles.put(entry.getKey(), entry.getValue());
            }
        }
        return customizedDrawStyles;
    }


    public void setDrawStyles(Map<Object, DrawStyle> drawStyles) {
        styles.putAll(drawStyles);
    }


    private DrawStyleManager() {
    }


    private final Map<Object, DrawStyle> styles = new HashMap<>();


    private static DrawStyleManager instance;

}
