package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class VirtualSlot extends NamedItem {
    public ObservableMap<String, VirtualDiagram> diagrams;
    public ObservableMap<Perspective, Slot> realSlots;
    public final VirtualBone bone;

    public VirtualSlot(VirtualBone bone, String name) {
        super();
        setName(name);
        diagrams = FXCollections.observableHashMap();
        realSlots = FXCollections.observableHashMap();
        this.bone = bone;
    }

    public void addDiagram(VirtualDiagram diagram) {
        if (diagram != null && diagram.getName() != null) {
            diagrams.put(diagram.getName(), diagram);
        }
    }

    public void removeDiagram(VirtualDiagram diagram) {
        if (diagram != null && diagram.getName() != null) {
            diagrams.remove(diagram.getName());
        }
    }
    public static boolean valid(Slot slot) {
        return slot != null && slot.getBone() != null && VirtualBone.valid(slot.getBone());
    }

    public void set(Slot slot) {
        if (valid(slot)) {
            realSlots.put(slot.getBone().getSkeleton().perspective, slot);
        }
    }

    public void clear(Perspective perspective) {
        if (perspective != null) {
            realSlots.remove(perspective);
        }
    }

    public boolean hasDiagram(String diagramName) {
        return diagrams.containsKey(diagramName);
    }

    @Override
    public ObservableMap<String, ? extends NamedItem> getChildren() {
        return diagrams;
    }
}
