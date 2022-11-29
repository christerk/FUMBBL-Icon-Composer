package com.fumbbl.iconcomposer.model.types;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;

public class Ruleset extends NamedItem {
	public int id;
	public String name;
	public ObservableList<Roster> rosters;

	public Ruleset() {
		rosters = FXCollections.observableArrayList();
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
