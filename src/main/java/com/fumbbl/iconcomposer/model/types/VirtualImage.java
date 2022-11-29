package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class VirtualImage extends NamedItem {
    public ObservableMap<Perspective, NamedImage> realImages;

    public final VirtualDiagram diagram;

    public VirtualImage(VirtualDiagram diagram, String name) {
        super();
        setName(name);
        realImages = FXCollections.observableHashMap();
        this.diagram = diagram;
    }

    public static boolean valid(NamedImage image) {
        return image != null && (image.getName().startsWith("front_") || image.getName().startsWith("side_"));
    }

    public void set(NamedImage image) {
        if (valid(image)) {
            Perspective perspective = image.getName().startsWith("front_") ? Perspective.Front : Perspective.Side;
            realImages.put(perspective, image);
        }
    }

    public void clear(Perspective perspective) {
        if (perspective != null) {
            realImages.remove(perspective);
        }
    }

}
