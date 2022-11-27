package com.fumbbl.iconcomposer.model.types;

import java.util.Comparator;

public class VirtualSlot extends NamedItem {
    private final Skeleton skeleton;
    private String name;
    private int order;

    public VirtualSlot(Slot s) {
        super();

        this.name = s.getName();
        this.skeleton = s.getSkeleton();
    }

    public VirtualSlot(Skeleton skeleton, String name) {
        super();

        this.skeleton = skeleton;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public static java.util.Comparator<VirtualSlot> ReverseComparator = new Comparator<VirtualSlot>() {
        @Override
        public int compare(VirtualSlot o1, VirtualSlot o2) {
            int r = o1.order-o2.order;
            if (r == 0) {
                r = o2.getName().compareTo(o1.getName());
            }
            return r;
        }
    };
}
