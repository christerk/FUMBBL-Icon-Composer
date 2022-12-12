package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.model.types.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Objects;

public class CellFactory<T extends NamedItem> {
	private Class<T> c;
	private static Controller controller;

	private final Image slotIcon;
	private final Image diagramIcon;
	private final Image imageIcon;
	private final Image skeletonIcon;
	private final Image boneIcon;

	public CellFactory() {
		slotIcon = new Image("/ui/Slot.png");
		diagramIcon = new Image("/ui/Diagram.png");
		imageIcon = new Image("/ui/Image.png");
		skeletonIcon = new Image("/ui/Skeleton.png");
		boneIcon = new Image("/ui/Bone.png");
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
		tree.setCellFactory(this.createTree(tree));

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

	private Callback<TreeView<T>,TreeCell<T>> createTree(TreeView<T> tree) {
		return p -> {
			TextFieldTreeCell<T> cell = new TextFieldTreeCell<T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					textProperty().unbind();
					super.updateItem(item, empty);

					if (empty || item == null) {
						//textProperty().unbind();
						setText("");
						setContextMenu(null);
					} else {
						textProperty().bind(item.nameProperty());

						//setText(item.getName());
						if (item instanceof VirtualSkeleton) {
							setGraphic(new ImageView(skeletonIcon));
						} else if (item instanceof VirtualBone) {
							setGraphic(new ImageView(boneIcon));
						} else if (item instanceof VirtualSlot) {
							setGraphic(new ImageView(slotIcon));
						} else if (item instanceof VirtualDiagram) {
							setGraphic(new ImageView(diagramIcon));
						} else if (item instanceof VirtualImage) {
							setGraphic(new ImageView(imageIcon));
						}
					}
				}
			};

			cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, tree));
			cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, tree));
			cell.setOnDragDropped((DragEvent event) -> drop(event, cell, tree));
			cell.setOnDragDone((DragEvent event) -> clearDropLocation());

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

	private static final String DROP_HINT_STYLE = "-fx-background-color: #eea82f;";
	private TextFieldTreeCell<T> dropZone;
	private void clearDropLocation() {
		if (dropZone != null) {
			dropZone.setStyle("");
			dropZone = null;
		}
	}

	private void drop(DragEvent event, TextFieldTreeCell<T> cell, TreeView<T> tree) {
		NamedItem target = cell.getItem();
		if (!(target instanceof VirtualBone)) {
			return;
		}

		Dragboard db = event.getDragboard();
		String slotName = db.getString();
		controller.relocateSlot(slotName, ((VirtualBone) target));
		clearDropLocation();
	}

	private void dragOver(DragEvent event, TextFieldTreeCell<T> cell, TreeView<T> tree) {
		NamedItem target = cell.getItem();
		if (!(target instanceof VirtualBone)) {
			clearDropLocation();
			return;
		}

		event.acceptTransferModes(TransferMode.MOVE);
		if (!Objects.equals(dropZone, cell)) {
			clearDropLocation();
			dropZone = cell;
			dropZone.setStyle(DROP_HINT_STYLE);
		}
	}

	private void dragDetected(MouseEvent event, TextFieldTreeCell<T> cell, TreeView<T> tree) {
		NamedItem item = cell.getItem();
		if (!(item instanceof VirtualSlot)) {
			return;
		}

		Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
		ClipboardContent content = new ClipboardContent();
		content.putString(cell.getText());
		db.setContent(content);
		db.setDragView(cell.snapshot(null, null));
		event.consume();
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
