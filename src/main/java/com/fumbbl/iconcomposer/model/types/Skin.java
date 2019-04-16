package com.fumbbl.iconcomposer.model.types;
import java.util.HashMap;
import java.util.Map;

public class Skin extends NamedItem {
	public int id;
	public String name;
	public Skeleton skeleton;
	
	public Map<Slot,Diagram> diagrams;
	
	public Skin() {
		diagrams = new HashMap<Slot,Diagram>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

	public void setDiagram(Slot slot, Diagram diagram) {
		diagrams.put(slot, diagram);
	}

	public Diagram getDiagram(Slot slot) {
		return diagrams.get(slot);
	}

	public void removeDiagram(Slot slot) {
		diagrams.remove(slot);
	}
}
