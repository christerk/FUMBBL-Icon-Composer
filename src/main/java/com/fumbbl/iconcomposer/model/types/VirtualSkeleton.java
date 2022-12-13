package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.ui.SkeletonTreeItem;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class VirtualSkeleton extends NamedItem {
    public ObservableMap<String, VirtualBone> bones;
    public ObservableMap<Perspective, Skeleton> realSkeletons;

    public VirtualSkeleton() {
        super();
        bones = FXCollections.observableHashMap();
        realSkeletons = FXCollections.observableHashMap();
        setName("Skeleton");
    }

    public void addBone(VirtualBone bone) {
        if (bone != null && bone.getName() != null) {
            bones.put(bone.getName(), bone);
        }
    }

    public void removeBone(VirtualBone bone) {
        if (bone != null && bone.getName() != null) {
            bones.remove(bone.getName());
        }
    }

    public static boolean valid(Skeleton skeleton) {
        return skeleton != null && skeleton.perspective != null && skeleton.perspective != Perspective.Unknown;
    }

    public void set(Skeleton skeleton) {
        if (valid(skeleton)) {
            realSkeletons.put(skeleton.perspective, skeleton);
        }
    }

    public void clear(Perspective perspective) {
        if (perspective != null) {
            realSkeletons.remove(perspective);
        }
    }

    public boolean hasBone(String name) {
        return bones.containsKey(name);
    }

    @Override
    public ObservableMap<String, ? extends NamedItem> getChildren() {
        return bones;
    }

    public VirtualDiagram findDiagram(String diagramName) {
        for (VirtualBone bone : bones.values()) {
            for (VirtualSlot slot : bone.slots.values()) {
                if (slot.diagrams.containsKey(diagramName)) {
                    return slot.diagrams.get(diagramName);
                }
            }
        }
        return null;
    }

    public Bone getBoneById(int boneId) {
        for (VirtualBone bone : bones.values()) {
            for (Bone b : bone.realBones.values()) {
                if (b.id == boneId) {
                    return b;
                }
            }
        }
        return null;
    }

    public Slot getSlotById(int slotId) {
        for (VirtualBone bone : bones.values()) {
            for (VirtualSlot slot : bone.slots.values()) {
                for (Slot s : slot.realSlots.values()) {
                    if (s.id == slotId) {
                        return s;
                    }
                }
            }
        }
        return null;
    }
}
