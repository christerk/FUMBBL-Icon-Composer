package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class VirtualBone extends NamedItem {
    public ObservableMap<String, VirtualSlot> slots;
    private ObservableMap<Perspective, Bone> realBones;
    public final VirtualSkeleton skeleton;

    public VirtualBone(VirtualSkeleton skeleton, String name) {
        super();
        setName(name);
        slots = FXCollections.observableHashMap();
        realBones = FXCollections.observableHashMap();
        this.skeleton = skeleton;
    }

    public void addSlot(VirtualSlot slot) {
        if (slot != null && slot.getName() != null) {
            slots.put(slot.getName(), slot);
        }
    }

    public void removeSlot(VirtualSlot slot) {
        if (slot != null && slot.getName() != null) {
            slots.remove(slot.getName());
        }
    }
    public static boolean valid(Bone bone) {
        return bone != null && bone.getSkeleton() != null && VirtualSkeleton.valid(bone.getSkeleton());
    }

    public void set(Bone bone) {
        if (valid(bone)) {
            realBones.put(bone.getSkeleton().perspective, bone);
        }
    }

    public void clear(Perspective perspective) {
        if (perspective != null) {
            realBones.remove(perspective);
        }
    }

    public boolean hasSlot(String slotName) {
        return slots.containsKey(slotName);
    }

    @Override
    public ObservableMap<String, ? extends NamedItem> getChildren() {
        return slots;
    }
}
