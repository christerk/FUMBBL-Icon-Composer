package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.ColourTheme;

public class Position extends NamedItem {
	public final ColourTheme templateColours;
	public int id;
	public String name;
	
	public Position() {
		this(new ColourTheme("template"));
	}
	
	public Position(ColourTheme colours) {
		this.templateColours = colours;
	}
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String newName) {
		this.name = newName;
	}
}
