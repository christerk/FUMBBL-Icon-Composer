package com.fumbbl.iconcomposer.controllers;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class CellFactory<T extends NamedItem> {
	public Callback<ListView<T>,ListCell<T>> create() {
		return p -> new ListCell<T>() {
			@Override
			protected void updateItem(T item, boolean empty) {
				super.updateItem(item, empty);
				
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getName());
				}
			}
		};
	}
}
