package com.fumbbl.iconcomposer.controllers;

import java.util.Comparator;

public interface NamedItem {
	Comparator<? super NamedItem> Comparator = new Comparator<NamedItem>() {
		@Override
		public int compare(NamedItem o1, NamedItem o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	};

	public String getName();
}
