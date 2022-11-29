package com.fumbbl.iconcomposer.controllers;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.events.SkeletonChangedEvent;
import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.*;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class MainController extends BaseController implements Initializable {
	public ImageView frontDiagram;
	public ImageView sideDiagram;

	public ImageView frontSkeleton;
	public ImageView sideSkeleton;

	public ImageView preview;

	public Image image;

	public TabPane tabs;
	public Tab skeletonTab;
	public Tab diagramTab;
	public Tab previewTab;

	public TreeView<NamedItem> treeView;

	public ComboBox<Position> positionChoice;

	public ImageView primaryLine;
	public ImageView primaryLo;
	public ImageView primaryMid;
	public ImageView primaryHi;
	public ImageView secondaryLine;
	public ImageView secondaryLo;
	public ImageView secondaryMid;
	public ImageView secondaryHi;
	public ImageView hairLine;
	public ImageView hairLo;
	public ImageView hairMid;
	public ImageView hairHi;
	public ImageView skinLine;
	public ImageView skinLo;
	public ImageView skinMid;
	public ImageView skinHi;
	public GridPane colourPane;
	public ChoiceBox<VirtualSlot> slotChoices;
	public TextField frontDiagramX;
	public TextField frontDiagramY;
	public TextField sideDiagramX;
	public TextField sideDiagramY;
	public Label apiStatus;
	public Label combinationsLabel;

	public Label labelProgress;
	public HBox progressPane;
	public ProgressBar progressBar;
	
	public Menu menuColourThemes;
	public ContextMenu treeContext;
	public MenuItem treeContextNewComponent;

	public MainController() {
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colourPane.setVisible(false);

		positionChoice.setConverter(new StringConverter<Position>() {
			@Override
			public String toString(Position object) {
				return object.getName();
			}

			@Override
			public Position fromString(String string) {
				return null;
			}
		});

		slotChoices.setConverter(new StringConverter<VirtualSlot>() {
			@Override
			public String toString(VirtualSlot object) {
				return object.getName();
			}

			@Override
			public VirtualSlot fromString(String string) {
				return null;
			}
		});

		TreeItem<NamedItem> root = new TreeItem<>(new NamedItem());
		treeView.setRoot(root);
		root.setExpanded(true);
		treeView.setShowRoot(false);
		treeView.setContextMenu(treeContext);

		new CellFactory<>().apply(treeView, NamedItem.class, this);

		treeView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
			TreeItem<NamedItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				NamedItem item = selectedItem.getValue();
				if (item instanceof NamedImage) {
					event.consume();
				}
				treeContextNewComponent.setVisible(item instanceof VirtualSlot);
			} else {
				event.consume();
			}
		});

		treeView.setEditable(false);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				if (((TreeItem<NamedItem>)newValue).getValue() instanceof VirtualDiagram) {
					VirtualDiagram diagram = (VirtualDiagram) ((TreeItem<NamedItem>) newValue).getValue();
					controller.displayDiagrams(diagram.getName());
					slotChoices.getSelectionModel().select(slotChoices.getItems().stream().filter(s->s.getName().equals(diagram.getSlot().getName())).findFirst().get());
				tabs.getSelectionModel().select(diagramTab);
			}
			} else {
				controller.displayDiagrams(null);
			}
		});

		slotChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Diagram d = controller.viewState.getActiveDiagram(Perspective.Front);
			if (d != null) {
				d.setSlot(model.getSlot(model.frontSkeleton.get().id, d.getSlot().getName()));
			}

			d = controller.viewState.getActiveDiagram(Perspective.Side);
			if (d != null) {
				d.setSlot(model.getSlot(model.sideSkeleton.get().id, d.getSlot().getName()));
			}
		});

		positionChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> model.selectedPosition.set(newValue));
	}

	public void onShow() {
		if (initialized) {
			return;
		}

		super.onShow();

		positionChoice.setItems(model.masterPositions);

		BooleanBinding positionChoiceVisible = Bindings.createBooleanBinding(() -> !model.masterPositions.isEmpty(), model.masterPositions);
		positionChoice.visibleProperty().bind(positionChoiceVisible);

		StringBinding isAuthorized = Bindings.createStringBinding(() -> model.isAuthenticated() ? "Authenticated" : "Not Authenticated", model.dataLoader.isAuthenticated);
		apiStatus.textProperty().bind(isAuthorized);

		slotChoices.setItems(model.masterSlots);

		model.selectedPosition.addListener((o, oldValue, newValue) -> setColourTheme(newValue.templateColours));

		progressPane.visibleProperty().bind(model.taskManager.taskRunningProperty);
		labelProgress.textProperty().bind(model.taskManager.taskStateProperty);
		progressBar.progressProperty().bind(model.taskManager.taskPctProperty);

		LongBinding b = Bindings.createLongBinding(() -> {
			long combinations = 1;
			for (VirtualSlot slot : model.masterSlots) {
				long count = getDiagrams(slot).size();
				if (count > 0) {
					combinations *= count;
				}
			}
			return combinations;
		}, model.masterSlots, model.masterDiagrams);
		combinationsLabel.textProperty().bind(Bindings.format("%d", b));

		model.masterSlots.addListener((ListChangeListener<VirtualSlot>) c -> {
			TreeItem<NamedItem> root = treeView.getRoot();
			ObservableList<TreeItem<NamedItem>> children = root.getChildren();
			while (c.next()) {
				if (c.wasPermutated()) {
					for (int i = c.getFrom(); i < c.getTo(); ++i) {
						//permutate
					}
				} else if (c.wasUpdated()) {
				} else {
					for (VirtualSlot remitem : c.getRemoved()) {
						List<TreeItem<NamedItem>> x = children.stream().filter(s -> s.getValue() == remitem).collect(Collectors.toList());

						children.removeAll(x);
					}
					for (VirtualSlot additem : c.getAddedSubList()) {
						children.add(new TreeItem<>(additem));
					}
				}
			}
		});

		model.masterDiagrams.addListener((ListChangeListener<VirtualDiagram>) c -> {
			TreeItem<NamedItem> root = treeView.getRoot();
			ObservableList<TreeItem<NamedItem>> children = root.getChildren();
			while (c.next()) {
				if (c.wasPermutated()) {
					for (int i = c.getFrom(); i < c.getTo(); ++i) {
						//permutate
					}
				} else if (c.wasUpdated()) {
					//update item
				} else {
					for (VirtualDiagram remitem : c.getRemoved()) {
						VirtualSlot slot = remitem.getSlot();
						Optional<TreeItem<NamedItem>> opt = children.stream().filter(s -> s.getValue().getName().equals(slot.getName())).findFirst();
						if (opt.isPresent()) {
							TreeItem<NamedItem> parent = opt.get();

							List<TreeItem<NamedItem>> x = children.stream().filter(s -> s.getValue() == remitem).collect(Collectors.toList());
							parent.getChildren().removeAll(x);
						}
					}
					for (VirtualDiagram additem : c.getAddedSubList()) {
						VirtualSlot slot = additem.getSlot();
						Optional<TreeItem<NamedItem>> opt = children.stream().filter(s -> s.getValue().getName().equals(slot.getName())).findFirst();
						if (opt.isPresent()) {
							TreeItem<NamedItem> parent = opt.get();
							parent.getChildren().add(new TreeItem<>(additem));
						}

					}
				}
			}
		});

		model.masterImages.addListener((ListChangeListener<NamedImage>) c -> {
			TreeItem<NamedItem> root = treeView.getRoot();
			ObservableList<TreeItem<NamedItem>> children = root.getChildren();
			while (c.next()) {
				if (c.wasPermutated()) {
					for (int i = c.getFrom(); i < c.getTo(); ++i) {
						//permutate
					}
				} else if (c.wasUpdated()) {
					//update item
				} else {
					for (NamedImage remitem : c.getRemoved()) {
						String diagramName = remitem.getName().replaceFirst("(front_|side_)", "");

						Optional<TreeItem<NamedItem>> opt = children.stream().filter(s -> diagramName.startsWith(s.getValue().getName())).findFirst();
						if (opt.isPresent()) {
							TreeItem<NamedItem> parentSlot = opt.get();

							Optional<TreeItem<NamedItem>> opt2 = parentSlot.getChildren().stream().filter(i -> i.getValue().getName().equals(diagramName)).findFirst();
							if (opt2.isPresent()) {
								TreeItem<NamedItem> parentDiagram = opt2.get();

								List<TreeItem<NamedItem>> x = parentDiagram.getChildren().stream().filter(s -> s.getValue() == remitem).collect(Collectors.toList());
								parentDiagram.getChildren().removeAll(x);
							}
						}
					}
					for (NamedImage additem : c.getAddedSubList()) {
						String diagramName = additem.getName().replaceFirst("(front_|side_)", "");

						Optional<TreeItem<NamedItem>> opt = children.stream().filter(s -> diagramName.startsWith(s.getValue().getName())).findFirst();
						if (opt.isPresent()) {
							TreeItem<NamedItem> parentSlot = opt.get();

							Optional<TreeItem<NamedItem>> opt2 = parentSlot.getChildren().stream().filter(i -> i.getValue().getName().equals(diagramName)).findFirst();
							if (opt2.isPresent()) {
								TreeItem<NamedItem> parentDiagram = opt2.get();
								parentDiagram.getChildren().add(new TreeItem<>(additem));
							}
						}
					}
				}
			}
		});

		model.addEventHandler(SkeletonChangedEvent.SKELETON_CHANGED, e -> {
			renderSkeleton(model.frontSkeleton.get());
			renderSkeleton(model.sideSkeleton.get());
			renderPreview();
		});
	}

	/*
	 *    Control Event Handlers
	 */

	public void frontImageClicked(MouseEvent e) {
		imageClicked(Perspective.Front, e);
	}

	public void sideImageClicked(MouseEvent e) {
		imageClicked(Perspective.Side, e);
	}

	public void imageClicked(Perspective perspective, MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		MouseButton button = e.getButton();

		Position p = model.selectedPosition.get();

		Diagram d = model.getDiagram(model.getSkeleton(perspective).id, treeView.getSelectionModel().getSelectedItem().getValue().getName());

		ColourType type = controller.viewState.getActiveColourType();
		
		if (button == MouseButton.PRIMARY && controller != null && type != null) {
			Color c = controller.viewState.getPixelRGB(perspective, (int)x, (int)y);
			
			p.templateColours.setColour(type, c);
			controller.setPositionColour(p, type, "#"+Integer.toHexString(c.getRGB()).substring(2));
			setColourTheme(p.templateColours);
		} else if (button == MouseButton.SECONDARY) {
			Point2D point = controller.getRenderer().getImageOffset(x, y);
			d.x = point.getX();
			d.y = point.getY();
			model.saveDiagram(d);
			d.updateTransform();
			controller.displayDiagram(d);
		}
	}

	public void activateColour(MouseEvent e) {
		controller.viewState.setActiveColourType(ColourType.fromString(((ImageView)e.getSource()).getId()));
		if (e.getButton() == MouseButton.SECONDARY) {
			Position p = model.selectedPosition.get();
			ColourType type = controller.viewState.getActiveColourType();
			p.templateColours.resetColour(type);
			setColourTheme(p.templateColours);
		}
	}

	public void quit() {
		model.taskManager.shutdown();
	}
	
	public void showPreferences() {
		controller.getStageManager().show(StageType.prefs);
	}
	
	public void openRoster() {
		controller.getStageManager().show(StageType.openRoster);
	}
	
	public void createDiagram() {
		controller.getStageManager().show(StageType.newDiagram);
	}
	
	public void showAbout() {
		controller.getStageManager().show(StageType.about);
	}
	
	/*
	 *   Update Methods
	 */
	
	public void renderSkeleton(Skeleton newValue) {
		if (newValue == null) {
			controller.setSkeleton(null);
			controller.displayBones();
			model.masterSlots.clear();
			setBones(null);

			model.masterDiagrams.clear();
			return;
		}

		controller.displayBones();
	}
	
	public void setColourTheme(ColourTheme t) {
		if (t == null) {
			return;
		}
		setImageColour(primaryLine, t, ColourType.PRIMARYLINE);
		setImageColour(primaryLo, t, ColourType.PRIMARYLO);
		setImageColour(primaryMid, t, ColourType.PRIMARY);
		setImageColour(primaryHi, t, ColourType.PRIMARYHI);

		setImageColour(secondaryLine, t, ColourType.SECONDARYLINE);
		setImageColour(secondaryLo, t, ColourType.SECONDARYLO);
		setImageColour(secondaryMid, t, ColourType.SECONDARY);
		setImageColour(secondaryHi, t, ColourType.SECONDARYHI);

		setImageColour(skinLine, t, ColourType.SKINLINE);
		setImageColour(skinLo, t, ColourType.SKINLO);
		setImageColour(skinMid, t, ColourType.SKIN);
		setImageColour(skinHi, t, ColourType.SKINHI);

		setImageColour(hairLine, t, ColourType.HAIRLINE);
		setImageColour(hairLo, t, ColourType.HAIRLO);
		setImageColour(hairMid, t, ColourType.HAIR);
		setImageColour(hairHi, t, ColourType.HAIRHI);
	}	

	private void setImageColour(ImageView view, ColourTheme theme, ColourType type) {
		BufferedImage i = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)i.getGraphics();
		g.setColor(theme.getColour(type));
		g.fillRect(0, 0, 20, 20);
		g.setColor(Color.black);
		g.drawRect(0, 0, 20, 20);
		view.setImage(SwingFXUtils.toFXImage(i, null));
	}

	public void setApiStatus(String status) {
		apiStatus.setText(status);
	}

	public void showColourPane() {
		colourPane.setVisible(true);
	}

	public void hideColourPane() {
		colourPane.setVisible(false);
	}
	
	public void setSlotInfo(Perspective perspective, VirtualSlot slot, double x, double y) {
		slotChoices.setValue(slot);

		(perspective==Perspective.Front ? frontDiagramX : sideDiagramX).setText(Integer.toString((int)x));
		(perspective==Perspective.Front ? frontDiagramY : sideDiagramY).setText(Integer.toString((int)y));
	}

	public void setFrontDiagramImage(WritableImage image) {
		frontDiagram.setImage(image);
	}

	public void setSideDiagramImage(WritableImage image) {
		sideDiagram.setImage(image);
	}

	public void setFrontSkeletonImage(WritableImage image) {
		frontSkeleton.setImage(image);
	}

	public void setSideSkeletonImage(WritableImage image) {
		sideSkeleton.setImage(image);
	}

	public void setPreviewImage(WritableImage image) {
		preview.setImage(image);
	}

	public void setColourThemes(Collection<ColourTheme> themes) {
		ObservableList<MenuItem> menuItems = menuColourThemes.getItems();
		EventHandler<ActionEvent> action = event -> {
			String theme = ((MenuItem) event.getSource()).getText();
			controller.setColourTheme(theme);
			renderPreview();
		};
		for (ColourTheme s : themes) {
			MenuItem item = new MenuItem(s.name);
			menuItems.add(item);
			item.setOnAction(action);
		}
	}
	
	public void setImages(Collection<NamedImage> images) {
		model.masterImages.setAll(images);
	}

	public void setBones(Collection<Bone> bones) {
		if (bones == null) {
			return;
		}
		Map<String,Bone> boneMap = new HashMap<>();
		Map<String,TreeItem<String>> itemMap = new HashMap<>();

		for (Bone b : bones) {
			boneMap.put(b.name, b);
			itemMap.put(b.name, new TreeItem<>(b.name));
		}
	}

	public void onProgressStart(String description) {
		Platform.runLater(() -> {
			labelProgress.setText(description);
			progressPane.setVisible(true);
		});
	}

	public void onProgressComplete() {
		Platform.runLater(() -> progressPane.setVisible(false));
	}

	public void onProgress(double progress, boolean complete) {
		if (!complete) {
			progressBar.setProgress(progress);
		}
	}

	public void renderPreview() {
		controller.displayPreview();
	}

	public Collection<VirtualDiagram> getDiagrams(VirtualSlot slot) {
		return model.masterDiagrams.stream().filter(d -> d.getSlot().getName().equals(slot.getName())).collect(Collectors.toList());
	}

	public void renameItem(NamedItem renamedObject, String newName) {
		String oldName = renamedObject.getName();
		renamedObject.setName(newName);
		controller.onItemRenamed(renamedObject, oldName);
	}

	/*
		Context Menus
	 */
	public void renameItem(ActionEvent e) {
		NamedItem item = treeView.getSelectionModel().getSelectedItem().getValue();
		controller.getStageManager().show(StageType.rename, item);
	}

	public void newComponent(ActionEvent e) {
		VirtualSlot slot = (VirtualSlot) treeView.getSelectionModel().getSelectedItem().getValue();
		controller.showNewComponentDialog(slot);
	}

	public void deleteItem(ActionEvent e) {
		NamedItem item = treeView.getSelectionModel().getSelectedItem().getValue();


	}
}
