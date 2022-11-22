package com.fumbbl.iconcomposer.model.types;

import java.util.Comparator;

import com.fumbbl.iconcomposer.controllers.Controller;
import javafx.scene.control.TreeItem;

public class NamedItem {
	public static Comparator<? super NamedItem> Comparator = new Comparator<NamedItem>() {
		@Override
		public int compare(NamedItem o1, NamedItem o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	};

	public static Comparator<TreeItem<NamedItem>> TreeItemComparator = new Comparator<TreeItem<NamedItem>>() {
		@Override
		public int compare(TreeItem<NamedItem> o1, TreeItem<NamedItem> o2) {
			return NamedItem.Comparator.compare(o1.getValue(), o2.getValue());
		}
	};

	private String name;

	public void onRenamed(Controller controller, String oldName) {
		controller.onItemRenamed(this, oldName);
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String newName) {
		this.name = newName;
	}
}
