package com.fumbbl.iconcomposer.model.types;

public class VirtualSlot extends NamedItem {
    private final Skeleton skeleton;

    public VirtualSlot(Slot s) {
        super();

        setName(s.getName());
        this.skeleton = s.getSkeleton();
    }

}
