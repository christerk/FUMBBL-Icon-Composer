package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.model.types.NamedItem;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CellFactory<T extends NamedItem> {
	private Class<T> c;
	private static Controller controller;
	
	public CellFactory() {
	}
	
	public static void setController(Controller controller) {
		CellFactory.controller = controller;
	}
	
	public void apply(ListView<T> list, Class<T> c) {
		this.c = c;
		list.setCellFactory(this.create());
		
		list.setOnEditStart(event -> {
		});
		list.setOnEditCommit(event -> {
			T newObj = event.getNewValue();
			T target = event.getSource().getItems().get(event.getIndex()); 
			target.setName(newObj.getName());
			target.onRenamed(controller);
		});
		list.setOnEditCancel(event -> {
		});
		
	}
	
	private Callback<ListView<T>,ListCell<T>> create() {
		return p -> {
			TextFieldListCell<T> cell = new TextFieldListCell<T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);
					
					if (empty || item == null) {
						setText(null);
					} else {
						setText(item.getName());
					}
				}
			};
			
			cell.setConverter(new StringConverter<T>() {
				@Override
				public T fromString(String string) {
					T newItem;
					try {
						newItem = (T)c.newInstance();
						newItem.setName(string);
						return newItem;
					} catch (InstantiationException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public String toString(T object) {
					if (object != null) {
						return object.getName();
					}
					return null;
				}
			});
			
			return cell;
		};
	}
}
