package com.fumbbl.iconcomposer.model.types;

import java.util.Comparator;

import com.fumbbl.iconcomposer.controllers.Controller;

public abstract class NamedItem {
	public static Comparator<? super NamedItem> Comparator = new Comparator<NamedItem>() {
		@Override
		public int compare(NamedItem o1, NamedItem o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	};

	public void onRenamed(Controller controller) {
		controller.onItemRenamed(this);
	}
	
	public abstract String getName();
	public abstract void setName(String newName);
}
