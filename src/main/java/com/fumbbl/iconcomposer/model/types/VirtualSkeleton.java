package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class VirtualSkeleton extends NamedItem {
    private ObservableMap<String, VirtualBone> bones;
    private ObservableMap<Perspective, Skeleton> realSkeletons;

    public VirtualSkeleton() {
        bones = FXCollections.observableHashMap();
        realSkeletons = FXCollections.observableHashMap();
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
}
