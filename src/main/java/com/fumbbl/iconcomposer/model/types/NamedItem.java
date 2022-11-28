package com.fumbbl.iconcomposer.model.types;

import java.util.Comparator;

import com.fumbbl.iconcomposer.controllers.Controller;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;

public class NamedItem {
	public static final Comparator<? super NamedItem> Comparator = (java.util.Comparator<NamedItem>) (o1, o2) -> o1.getName().compareTo(o2.getName());

	public static Comparator<TreeItem<NamedItem>> TreeItemComparator = (o1, o2) -> NamedItem.Comparator.compare(o1.getValue(), o2.getValue());

	private final StringProperty name;

	public NamedItem() {
		this.name = new SimpleStringProperty();
	}

	public void onRenamed(Controller controller, String oldName) {
		controller.onItemRenamed(this, oldName);
	}

	public StringProperty nameProperty() {
		return this.name;
	}

	public String getName() {
		return this.name.get();
	}
	public void setName(String newName) {
		this.name.set(newName);
	}
}
