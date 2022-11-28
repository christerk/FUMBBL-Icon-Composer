package com.fumbbl.iconcomposer.model.types;

public class VirtualSlot extends NamedItem {
    private final Skeleton skeleton;
    private int order;

    public VirtualSlot(Slot s) {
        super();

        setName(s.getName());
        this.skeleton = s.getSkeleton();
    }

    public VirtualSlot(Skeleton skeleton, String name) {
        super();

        this.skeleton = skeleton;
        setName(name);
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }
}
