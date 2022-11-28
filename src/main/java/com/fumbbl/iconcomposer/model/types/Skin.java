package com.fumbbl.iconcomposer.model.types;
import java.util.HashMap;
import java.util.Map;

public class Skin extends NamedItem {
	public int id;
	public String name;

	public final Map<String, VirtualDiagram> diagrams;
	public Position position;

	public Skin() {
		diagrams = new HashMap<>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

	public void setDiagram(String slotName, VirtualDiagram diagram) {
		diagrams.put(slotName, diagram);
	}

	public VirtualDiagram getDiagram(String slotName) {
		return diagrams.get(slotName);
	}

	public void removeDiagram(String slotName) {
		diagrams.remove(slotName);
	}
}
