package com.fumbbl.iconcomposer.model.types;

public class Position extends NamedItem {
	public int id;
	public String name;
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String newName) {
		this.name = newName;
	}
}
