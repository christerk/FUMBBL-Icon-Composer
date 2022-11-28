package com.fumbbl.iconcomposer.model.types;

public class VirtualDiagram extends NamedItem {
    private final VirtualSlot slot;

    public VirtualDiagram(Diagram d) {
        super();

        setName(d.getName());
        this.slot = new VirtualSlot(d.getSlot());
    }

    public VirtualDiagram(VirtualSlot slot, String name) {
        super();

        this.slot = slot;
        setName(name);
    }

    public VirtualSlot getSlot() {
        return slot;
    }
}
