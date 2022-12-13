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
import com.fumbbl.iconcomposer.ui.SkeletonTreeItem;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.application.Platform;
import javafx.beans.binding.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
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
	public MenuItem treeContextDeleteItem;

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

		treeView.setShowRoot(false);

		new CellFactory<>().apply(treeView, NamedItem.class, controller);


		treeView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
			TreeItem<NamedItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				NamedItem item = selectedItem.getValue();
				boolean isRootBone = (item instanceof VirtualBone && item.getName().equals("root"));
				if (item instanceof VirtualImage || isRootBone) {
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
				if (newValue.getValue() instanceof VirtualDiagram) {
					VirtualDiagram diagram = (VirtualDiagram) newValue.getValue();
					controller.displayDiagrams(diagram.getName());
					slotChoices.getSelectionModel().select(diagram.slot);
					setSlotInfo(diagram);
				tabs.getSelectionModel().select(diagramTab);
			}
			} else {
				controller.displayDiagrams(null);
			}
		});

		slotChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Diagram d = controller.viewState.getActiveDiagram(Perspective.Front);
			if (d != null) {
				//d.setSlot(model.getSlot(model.masterSkeleton.get().realSkeletons.get(Perspective.Front).id, d.getSlot().getName()));
			}

			d = controller.viewState.getActiveDiagram(Perspective.Side);
			if (d != null) {
				//d.setSlot(model.getSlot(model.masterSkeleton.get().realSkeletons.get(Perspective.Side).id, d.getSlot().getName()));
			}
		});

		positionChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> model.selectedPosition.set(newValue));
	}

	public void onShow() {
		if (initialized) {
			return;
		}

		super.onShow();

		SkeletonTreeItem root = new SkeletonTreeItem(model.masterSkeleton.get());
		root.valueProperty().set(model.masterSkeleton.get());
		treeView.setRoot(root);
		root.setExpanded(true);

		model.masterSkeleton.addListener((obs, oldValue, newValue) -> {
			treeView.setRoot(new SkeletonTreeItem(model.masterSkeleton.get()));
		});

		treeView.setContextMenu(treeContext);

		positionChoice.setItems(model.masterPositions);

		BooleanBinding positionChoiceVisible = Bindings.createBooleanBinding(() -> !model.masterPositions.isEmpty(), model.masterPositions);
		positionChoice.visibleProperty().bind(positionChoiceVisible);

		StringBinding isAuthorized = Bindings.createStringBinding(() -> model.isAuthenticated() ? "Authenticated" : "Not Authenticated", model.dataLoader.isAuthenticated);
		apiStatus.textProperty().bind(isAuthorized);

		model.selectedPosition.addListener((o, oldValue, newValue) -> {
			if (newValue != null) {
				setColourTheme(newValue.templateColours);
			}
		});

		progressPane.visibleProperty().bind(model.taskManager.taskRunningProperty);
		labelProgress.textProperty().bind(model.taskManager.taskStateProperty);
		progressBar.progressProperty().bind(model.taskManager.taskPctProperty);

		model.addEventHandler(SkeletonChangedEvent.SKELETON_CHANGED, e -> {
			model.masterSkeleton.get().realSkeletons.values().forEach(s->renderSkeleton(s));
			renderPreview();
			Platform.runLater(() -> combinationsLabel.setText(String.format("%d", CountCombinations())));
		});
	}

	private long CountCombinations() {
		long combinations = 1;

		for (VirtualBone bone : model.masterSkeleton.get().bones.values()) {
			for (VirtualSlot slot : bone.slots.values()) {
				long count = slot.diagrams.size();
				if (count > 0) {
					combinations *= count;
				}
			}
		}
		return combinations;
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

	public void frontSkeletonClicked(MouseEvent e) {
		skeletonClicked(Perspective.Front, e);
	}

	public void sideSkeletonClicked(MouseEvent e) {
		skeletonClicked(Perspective.Side, e);
	}

	public void imageClicked(Perspective perspective, MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		MouseButton button = e.getButton();

		Position p = model.selectedPosition.get();

		if (p == null || model.getSkeleton(perspective) == null) {
			return;
		}

		NamedItem item = treeView.getSelectionModel().getSelectedItem().getValue();
		if (!(item instanceof VirtualDiagram)) {
			return;
		}

		Diagram d = ((VirtualDiagram)item).realDiagrams.get(perspective);

		ColourType type = controller.viewState.getActiveColourType();
		
		if (p != null && button == MouseButton.PRIMARY && controller != null && type != null) {
			Color c = controller.viewState.getPixelRGB(perspective, (int)x, (int)y);
			
			p.templateColours.setColour(type, c);
			controller.setPositionColour(p, type, "#"+Integer.toHexString(c.getRGB()).substring(2));
			setColourTheme(p.templateColours);
		} else if (d != null && button == MouseButton.SECONDARY) {
			Point2D point = controller.getRenderer().getImageOffset(x, y);
			d.x = point.getX();
			d.y = point.getY();
			model.saveDiagram(d);
			d.updateTransform();
			controller.displayDiagram(d);
		}
	}

	public void skeletonClicked(Perspective perspective, MouseEvent e) {
		double x = e.getX();
		double y = e.getY();

		Position p = model.selectedPosition.get();

		Skeleton skeleton = model.getSkeleton(perspective);
		if (p == null || skeleton == null) {
			return;
		}

		VirtualBone vBone = (VirtualBone) treeView.getSelectionModel().getSelectedItem().getValue();
		Bone rBone = vBone.realBones.get(perspective);

		double oldX = rBone.x;
		double oldY = rBone.y;

		double newX = Math.round((x - 480.0 / 2) / 8);
		double newY = Math.round((480.0 / 2 - y) / 8);
		model.setBonePosition(perspective, vBone, rBone, newX, newY, true);

		renderSkeleton(skeleton);
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
			setBones(null);

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

	public void showColourPane() {
		colourPane.setVisible(true);
	}

	public void hideColourPane() {
		colourPane.setVisible(false);
	}
	
	public void setSlotInfo(VirtualDiagram diagram) {
		slotChoices.setValue(diagram.slot);

		diagram.realDiagrams.forEach((perspective, d)->{
			(perspective==Perspective.Front ? frontDiagramX : sideDiagramX).setText(Integer.toString((int)d.x));
			(perspective==Perspective.Front ? frontDiagramY : sideDiagramY).setText(Integer.toString((int)d.y));
		});
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

	public void renderPreview() {
		controller.displayPreview();
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
		//VirtualSlot slot = (VirtualSlot) treeView.getSelectionModel().getSelectedItem().getValue();
		//controller.showNewComponentDialog(slot);
	}

	public void deleteItem(ActionEvent e) {
		NamedItem item = treeView.getSelectionModel().getSelectedItem().getValue();

		if (item instanceof VirtualSlot) {
			model.deleteSlot((VirtualSlot) item);
		} else if (item instanceof VirtualBone) {
			model.deleteBone((VirtualBone) item);
		}
	}
}
