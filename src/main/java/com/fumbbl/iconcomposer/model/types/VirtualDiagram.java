package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class VirtualDiagram extends NamedItem {
    public ObservableMap<String, VirtualImage> images;
    public ObservableMap<Perspective, Diagram> realDiagrams;

    public final VirtualSlot slot;

    public VirtualDiagram(VirtualSlot slot, String name) {
        super();
        setName(name);
        images = FXCollections.observableHashMap();
        realDiagrams = FXCollections.observableHashMap();
        this.slot = slot;
    }

    public void addImage(VirtualImage image) {
        if (image != null && image.getName() != null) {
            images.put(image.getName(), image);
        }
    }

    public void removeImage(VirtualImage image) {
        if (image != null && image.getName() != null) {
            images.remove(image.getName());
        }
    }

    public static boolean valid(Diagram diagram) {
        return diagram != null && diagram.getSlot() != null && VirtualSlot.valid(diagram.getSlot());
    }

    public void set(Diagram diagram) {
        if (valid(diagram)) {
            realDiagrams.put(diagram.perspective, diagram);
        }
    }

    public void clear(Perspective perspective) {
        if (perspective != null) {
            realDiagrams.remove(perspective);
        }
    }

    @Override
    public ObservableMap<String, ? extends NamedItem> getChildren() {
        return images;
    }
}
