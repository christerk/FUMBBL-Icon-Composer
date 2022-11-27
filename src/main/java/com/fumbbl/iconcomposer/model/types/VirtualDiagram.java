package com.fumbbl.iconcomposer.model.types;

public class VirtualDiagram extends NamedItem {
    private String name;
    private VirtualSlot slot;

    public VirtualDiagram(Diagram d) {
        super();

        this.name = d.getName();
        this.slot = new VirtualSlot(d.getSlot());
    }

    public VirtualDiagram(VirtualSlot slot, String name) {
        super();

        this.slot = slot;
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

    public VirtualSlot getSlot() {
        return slot;
    }
}
