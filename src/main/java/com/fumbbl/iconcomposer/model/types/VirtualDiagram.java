package com.fumbbl.iconcomposer.model.types;

public class VirtualDiagram extends NamedItem {
    private String name;
    private Slot slot;

    public VirtualDiagram(Diagram d) {
        super();

        this.name = d.getName();
        this.slot = d.getSlot();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    public Slot getSlot() {
        return slot;
    }
}
