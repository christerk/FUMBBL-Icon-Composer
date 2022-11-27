package com.fumbbl.iconcomposer.model.types;
import java.util.HashMap;
import java.util.Map;

public class Skin extends NamedItem {
	public int id;
	public String name;

	public Map<VirtualSlot, VirtualDiagram> diagrams;
	public Position position;

	public Skin() {
		diagrams = new HashMap<VirtualSlot, VirtualDiagram>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

	public void setDiagram(VirtualSlot slot, VirtualDiagram diagram) {
		diagrams.put(slot, diagram);
	}

	public VirtualDiagram getDiagram(VirtualSlot slot) {
		return diagrams.get(slot);
	}

	public void removeDiagram(VirtualSlot slot) {
		diagrams.remove(slot);
	}
}
