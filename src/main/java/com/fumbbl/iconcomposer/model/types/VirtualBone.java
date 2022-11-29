package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class VirtualBone extends NamedItem {
    private ObservableMap<String, VirtualSlot> slots;
    private ObservableMap<Perspective, Bone> realBones;

    public VirtualBone() {
        slots = FXCollections.observableHashMap();
        realBones = FXCollections.observableHashMap();
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
    public boolean valid(Bone bone) {
        return bone != null && bone.getSkeleton() != null && VirtualSkeleton.valid(bone.getSkeleton());
    }

    public void set(Bone bone) {
        if (valid(bone)) {
            realBones.put(bone.getSkeleton().perspective, bone);
        }
    }
}
