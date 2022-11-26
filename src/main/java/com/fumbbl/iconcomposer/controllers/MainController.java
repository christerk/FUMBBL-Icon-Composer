package com.fumbbl.iconcomposer.controllers;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.*;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import javax.naming.Context;
import javax.swing.event.PopupMenuEvent;

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
	public ChoiceBox<Slot> slotChoices;
	public TextField frontDiagramX;
	public TextField frontDiagramY;
	public TextField sideDiagramX;
	public TextField sideDiagramY;
	public Label apiStatus;
	public Label combinationsLabel;

	public Label labelProgress;
	public HBox progressPane;
	public ProgressBar progressBar;
	
	public ListView<Skeleton> skeletonList;

	public TitledPane skeletonPane;

	public Menu menuColourThemes;
	public ContextMenu treeContext;

	private ObservableList<VirtualDiagram> masterDiagrams;
	private ObservableList<Position> masterPositions;
	private ObservableList<Slot> masterSlots;
	private ObservableList<NamedImage> masterImages;

	private LongProperty numCombinations;

	public MainController() {
		masterDiagrams = FXCollections.observableArrayList();
		masterPositions = FXCollections.observableArrayList();
		masterSlots = FXCollections.observableArrayList();
		masterImages = FXCollections.observableArrayList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colourPane.setVisible(false);

		numCombinations = new SimpleLongProperty(0);
		combinationsLabel.textProperty().bind(numCombinations.asString());

		new CellFactory<Skeleton>().apply(skeletonList, Skeleton.class, this);

		positionChoice.setItems(masterPositions);
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

		skeletonList.setEditable(true);

		slotChoices.setConverter(new StringConverter<Slot>() {
			@Override
			public String toString(Slot object) {
				return object.name;
			}

			@Override
			public Slot fromString(String string) {
				return null;
			}
		});

		TreeItem<NamedItem> root = new TreeItem<NamedItem>(new NamedItem());
		treeView.setRoot(root);
		root.setExpanded(true);
		treeView.setShowRoot(false);
		treeView.setContextMenu(treeContext);

		new CellFactory<NamedItem>().apply(treeView, NamedItem.class, this);

		treeView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				NamedItem item = treeView.getSelectionModel().getSelectedItem().getValue();
				if (item instanceof NamedImage) {
					event.consume();
				}
			}
		});

		treeView.setEditable(false);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && ((TreeItem)newValue).getValue() instanceof VirtualDiagram) {
				VirtualDiagram diagram = (VirtualDiagram) ((TreeItem)newValue).getValue();
				controller.displayDiagrams(diagram.getName());
				tabs.getSelectionModel().select(diagramTab);
			}
		});

		slotChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Diagram d = controller.viewState.getActiveDiagram(Perspective.Front);
			d.setSlot(newValue);

			d = controller.viewState.getActiveDiagram(Perspective.Side);
			if (d != null) {
				d.setSlot(newValue);
			}
		});
		
		positionChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				skeletonList.getItems().clear();
				skeletonPane.setText("Skeletons");
				
				controller.loadPosition(newValue.id);
				skeletonPane.setExpanded(true);
				//controller.viewState.setActivePosition(newValue);
			}
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

		Position p = controller.viewState.getActivePosition();
		Diagram d = controller.viewState.getActiveDiagram(perspective);
		ColourType type = controller.viewState.getActiveColourType();
		
		if (button == MouseButton.PRIMARY && controller != null && type != null) {
			Color c = controller.viewState.getPixelRGB(perspective, (int)x, (int)y);
			
			p.templateColours.setColour(type, c);
			d.refreshColours(d.getImage());
			controller.setPositionColour(p, type, "#"+Integer.toHexString(c.getRGB()).substring(2));
			controller.onColourThemeChanged(p.templateColours);
		} else if (button == MouseButton.SECONDARY) {
			Point2D point = controller.getRenderer().getImageOffset(x, y);
			d.x = point.getX();
			d.y = point.getY();
			controller.displayDiagram(d);
		}
	}

	public void activateColour(MouseEvent e) {
		controller.viewState.setActiveColourType(ColourType.fromString(((ImageView)e.getSource()).getId()));
		if (e.getButton() == MouseButton.SECONDARY) {
			Position p = controller.viewState.getActivePosition();
			ColourType type = controller.viewState.getActiveColourType();
			p.templateColours.resetColour(type);
			controller.onColourThemeChanged(p.templateColours);
		}
	}

	public void quit() {
		controller.shutdown();
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
	
	public void setSkeleton(Skeleton newValue) {
		if (newValue == null) {
			skeletonPane.setText("Skeletons");
			controller.setSkeleton(null);
			controller.displayBones(null);
			setSlots(null);
			setBones(null);
			setDiagrams(null);
			return;
			
		}

		Collection<Bone> bones = null;
		Collection<Slot> slots = null;
		
		if (newValue.id > 0) {
			bones = controller.loadBones(newValue);
			slots = controller.loadSlots(newValue);
		} else if (newValue.id == -1) {
			bones = newValue.getBones();
			slots = newValue.getSlots();
		}

		controller.setSkeleton(newValue);
		controller.setBones(newValue.perspective, bones);
		controller.setSlots(newValue.perspective, slots);
		
		setBones(bones);
		setSlots(slots);
		
		controller.loadDiagrams(newValue.perspective, newValue);
		
		controller.displayBones(null);
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
	
	public void setSlotInfo(Perspective perspective, Slot slot, double x, double y) {
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
		EventHandler<ActionEvent> action = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String theme = ((MenuItem) event.getSource()).getText();
				controller.setColourTheme(theme);
				renderPreview();
			}
		};
		for (ColourTheme s : themes) {
			MenuItem item = new MenuItem(s.name);
			menuItems.add(item);
			item.setOnAction(action);
		}
	}
	
	public void setSkeletons(Collection<Skeleton> skeletons) {
		ObservableList<Skeleton> items = skeletonList.getItems();

		items.setAll(skeletons);

		controller.clearDiagrams();
		for (Skeleton s : skeletons) {
			setSkeleton(s);
		}
	}
	
	public void setImages(Collection<NamedImage> images) {
		masterImages.setAll(images);
		refreshImages();
	}

	public void addImage(NamedImage newImage) {
		masterImages.add(newImage);
		refreshImages();
	}

	public void setSlots(Collection<Slot> slots) {
		masterSlots.setAll(slots);

		if (slots != null) {
			slotChoices.getItems().setAll(slots);
			
			slotChoices.getItems().sort(Slot.Comparator);
		} else {
			slotChoices.getItems().clear();
		}
		countCombinations();
	}
	
	public void setDiagrams(Collection<VirtualDiagram> diagrams) {
		if (diagrams == null) {
			masterDiagrams.clear();
			return;
		}
		TreeItem<NamedItem> root = treeView.getRoot();
		masterDiagrams.setAll(diagrams);

		HashSet<Slot> slotList = new HashSet<>();

		slotList.addAll(masterDiagrams.stream().map(d -> d.getSlot()).collect(Collectors.toList()));
		root.getChildren().clear();

		for (Slot s : slotList) {
			TreeItem slotItem = new TreeItem(s);

			for (VirtualDiagram d : getDiagrams(s)) {
				slotItem.getChildren().add(new TreeItem(d));
				Collection<NamedImage> images = controller.getImagesForDiagram(d);
			}

			root.getChildren().add(slotItem);
		}

		refreshDiagrams();
		countCombinations();
	}

	public void refreshDiagrams() {
		TreeItem<NamedItem> root = treeView.getRoot();

		root.getChildren().sort(NamedItem.TreeItemComparator);

		for (TreeItem<NamedItem> item : root.getChildren()) {
			item.getChildren().sort(NamedItem.TreeItemComparator);
		}
	}

	public void refreshImages() {
		TreeItem<NamedItem> root = treeView.getRoot();

		ObservableList<TreeItem<NamedItem>> slots = root.getChildren();
		for (TreeItem<NamedItem> slot : slots) {
			ObservableList<TreeItem<NamedItem>> diagrams = slot.getChildren();
			for (TreeItem<NamedItem> diagram : diagrams) {
				Collection images = controller.getImagesForDiagram((VirtualDiagram) diagram.getValue()).stream().map(i->new TreeItem(i)).collect(Collectors.toList());
				diagram.getChildren().clear();
				diagram.getChildren().addAll(images);
			}
		}
	}

	public void setBones(Collection<Bone> bones) {
		if (bones == null) {
			return;
		}
		Map<String,Bone> boneMap = new HashMap<String,Bone>();
		Map<String,TreeItem<String>> itemMap = new HashMap<String,TreeItem<String>>();

		for (Bone b : bones) {
			boneMap.put(b.name, b);
			itemMap.put(b.name, new TreeItem<String>(b.name));
		}
	}

	public void onPositionsChanged(Collection<Position> positions) {
		skeletonList.getItems().clear();
		skeletonPane.setText("Skeletons");

		positionChoice.getSelectionModel().clearSelection();
		masterPositions.clear();
		masterPositions.setAll(positions);
		positionChoice.setVisible(true);
	}

	public void onPositionChanged(Position position) {
		controller.loadSkeletons(position);
		controller.viewState.setActiveColourTheme(position.templateColours);
		controller.onColourThemeChanged(position.templateColours);
	}
	
	public void onSkeletonsChanged(Collection<Skeleton> skeletons) {
		setSkeletons(skeletons);
	}
	
	public void onProgressStart(String description) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				labelProgress.setText(description);
				progressPane.setVisible(true);
			}
		});
	}

	public void onProgressComplete() {
		progressPane.setVisible(false);
	}

	public void onProgress(double progress, boolean complete) {
		if (!complete) {
			progressBar.setProgress(progress);
		}
	}

	public void renderPreview() {
		controller.displayPreview();
	}

	public Collection<VirtualDiagram> getDiagrams(Slot slot) {
		return masterDiagrams.stream().filter(d -> d.getSlot() == slot).collect(Collectors.toList());
	}

	public void renameItem(NamedItem renamedObject, String newName) {
		String oldName = renamedObject.getName();
		renamedObject.setName(newName);
		controller.onItemRenamed(renamedObject, oldName);
	}

	public void countCombinations() {
		long combinations = 1;
		for (Slot slot : masterSlots) {
			long count = getDiagrams(slot).stream().count();
			if (count > 0) {
				combinations *= count;
			}
		}
		numCombinations.set(combinations);
	}

	/*
		Context Menus
	 */
	public void setFront(ActionEvent e) {
		Skeleton s = skeletonList.getSelectionModel().getSelectedItem();
		s.perspective = Perspective.Front;
		controller.setSkeleton(s);
		setSkeleton(s);
	}

	public void setSide(ActionEvent e) {
		Skeleton s = skeletonList.getSelectionModel().getSelectedItem();
		s.perspective = Perspective.Side;
		controller.setSkeleton(s);
		setSkeleton(s);
	}

	public void deleteSkeleton(ActionEvent e) {
		Skeleton s = skeletonList.getSelectionModel().getSelectedItem();
		controller.deleteSkeleton(s);
	}

	public void renameItem(ActionEvent e) {
		NamedItem item = treeView.getSelectionModel().getSelectedItem().getValue();
		controller.getStageManager().show(StageType.rename, item);
	}
}
