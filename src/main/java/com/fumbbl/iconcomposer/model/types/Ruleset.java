package com.fumbbl.iconcomposer.model.types;

import java.util.Collection;

public class Ruleset extends NamedItem {
	public int id;
	public String name;
	public Collection<Roster> rosters;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

}
