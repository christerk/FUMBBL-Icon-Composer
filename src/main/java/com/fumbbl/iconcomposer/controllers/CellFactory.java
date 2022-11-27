package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.model.types.*;

import com.fumbbl.iconcomposer.ui.MenuType;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CellFactory<T extends NamedItem> {
	private Class<T> c;
	private static Controller controller;

	private Image slotIcon;
	private Image diagramIcon;
	private Image imageIcon;

	public CellFactory() {
		slotIcon = new Image("/ui/Slot.png");
		diagramIcon = new Image("/ui/Diagram.png");
		imageIcon = new Image("/ui/Image.png");
	}
	
	public static void setController(Controller controller) {
		CellFactory.controller = controller;
	}
	
	public void apply(ListView<T> list, Class<T> c, BaseController base) {
		this.c = c;
		list.setCellFactory(this.create());

		list.setOnEditStart(event -> {
		});
		list.setOnEditCommit(event -> {
			T newObj = event.getNewValue();
			T target = event.getSource().getItems().get(event.getIndex());
			String oldName = target.getName();
			if (newObj != null) {
				target.setName(newObj.getName());
				target.onRenamed(controller, oldName);
			}
		});
		list.setOnEditCancel(event -> {
		});
	}

	public void apply(TreeView<T> tree, Class<T> c, BaseController base) {
		this.c = c;
		tree.setCellFactory(this.createTree());

		tree.setOnEditStart(event -> {
		});
		tree.setOnEditCommit(event -> {
			T newObj = event.getNewValue();
			T target = event.getTreeItem().getValue();
			String oldName = target.getName();
			target.setName(newObj.getName());
			target.onRenamed(controller, oldName);
		});
		tree.setOnEditCancel(event -> {
		});
	}

	private Callback<TreeView<T>,TreeCell<T>> createTree() {
		return p -> {
			TextFieldTreeCell<T> cell = new TextFieldTreeCell<T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);

					if (empty || item == null) {
						setText(null);
						setContextMenu(null);
					} else {
						setText(item.getName());
						if (item instanceof VirtualSlot) {
							setGraphic(new ImageView(slotIcon));
						} else if (item instanceof VirtualDiagram) {
							setGraphic(new ImageView(diagramIcon));
						} else if (item instanceof NamedImage) {
							setGraphic(new ImageView(imageIcon));
						}
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

	private Callback<ListView<T>,ListCell<T>> create() {
		return p -> {
			TextFieldListCell<T> cell = new TextFieldListCell<T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);
					
					if (empty || item == null) {
						setText(null);
						setContextMenu(null);
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
