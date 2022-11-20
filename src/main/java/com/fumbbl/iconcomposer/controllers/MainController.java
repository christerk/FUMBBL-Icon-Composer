package com.fumbbl.iconcomposer.controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.*;
import com.fumbbl.iconcomposer.model.types.Skin;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
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
	public ListView<Position> positionList;

	public Label labelProgress;
	public HBox progressPane;
	public ProgressBar progressBar;
	
	public ListView<Skeleton> skeletonList;
	public ListView<NamedImage> imageList;
	public ListView<VirtualDiagram> diagramList;
	public ListView<Slot> slotList;
	
	public TitledPane positionPane;
	public TitledPane skeletonPane;
	public TitledPane imagePane;
	public TitledPane diagramPane;
	public TitledPane slotPane;
	
	public FlowPane diagramChoicePane;
	public ChoiceBox<VirtualDiagram> diagramChoices;
	
	public Menu menuColourThemes;

	private ObservableList<VirtualDiagram> masterDiagrams;
	
	public MainController() {
		masterDiagrams = FXCollections.observableArrayList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colourPane.setVisible(false);
		diagramChoicePane.setVisible(false);
		
		new CellFactory<Position>().apply(positionList, Position.class, this);
		new CellFactory<Skeleton>().apply(skeletonList, Skeleton.class, this);
		new CellFactory<NamedImage>().apply(imageList, NamedImage.class, this);
		new CellFactory<VirtualDiagram>().apply(diagramList, VirtualDiagram.class, this);
		new CellFactory<Slot>().apply(slotList, Slot.class, this);
		
		skeletonList.setEditable(true);
		imageList.setEditable(true);
		diagramList.setEditable(true);
		slotList.setEditable(true);
		
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

		diagramChoices.setConverter(new StringConverter<VirtualDiagram>() {
			@Override
			public String toString(VirtualDiagram object) {
				return object.getName();
			}

			@Override
			public VirtualDiagram fromString(String string) {
				return null;
			}
		});
		
		diagramChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Slot slot = slotList.getSelectionModel().getSelectedItem();
		});

		slotChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Diagram d = controller.viewState.getActiveDiagram(Perspective.Front);
			d.setSlot(newValue);

			d = controller.viewState.getActiveDiagram(Perspective.Side);
			if (d != null) {
				d.setSlot(newValue);
			}
		});
		
		positionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				diagramList.getItems().clear();
				slotList.getItems().clear();
				skeletonList.getItems().clear();
				skeletonPane.setText("Skeletons");
				
				controller.loadPosition(newValue.id);
				skeletonPane.setExpanded(true);
				positionPane.setText("Positions - " + newValue.getName());
				controller.viewState.setActivePosition(newValue);
			} else {
				positionPane.setText("Positions");
			}
		});

		skeletonList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				//setSkeleton(newValue);
				//controller.loadDiagrams(Perspective.Front, newValue.id);
				//controller.loadDiagrams(Perspective.Side, newValue.id);
			} else {
				//skeletonPane.setText("Skeletons");
				//controller.setSkeleton(null);
			}
		});

		imageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				controller.displayImage(Perspective.Front, newValue);
			}
		});

		diagramList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				controller.displayDiagrams(newValue.getName());
				tabs.getSelectionModel().select(diagramTab);
			}
		});

		slotList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
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
	
	public void setSkeleton(Perspective perspective, Skeleton newValue) {
		if (newValue == null) {
			skeletonPane.setText("Skeletons");
			controller.setSkeleton(perspective,null);
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

		controller.setSkeleton(perspective, newValue);
		controller.setBones(perspective, bones);
		controller.setSlots(perspective, slots);
		
		setBones(bones);
		setSlots(slots);
		
		controller.loadDiagrams(perspective, newValue);
		
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
		diagramChoicePane.setVisible(false);
	}

	public void showDiagramPane() {
		diagramChoicePane.setVisible(true);
		colourPane.setVisible(false);
	}
	
	public void hideColourPane() {
		colourPane.setVisible(false);
	}
	
	public void hideDiagramPane() {
		diagramChoicePane.setVisible(false);
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
			setSkeleton(s.perspective, s);
		}
	}
	
	public void setImages(Collection<NamedImage> images) {
		ObservableList<NamedImage> list = imageList.getItems();
		list.setAll(images);
		list.sort(NamedItem.Comparator);
	}

	public void setSlots(Collection<Slot> slots) {
		if (slots != null) {
			slotList.getItems().setAll(slots);
			slotChoices.getItems().setAll(slots);
			
			slotList.getItems().sort(Slot.Comparator);
			slotChoices.getItems().sort(Slot.Comparator);
		} else {
			slotList.getItems().clear();
			slotChoices.getItems().clear();
		}
	}
	
	public void setDiagrams(Collection<VirtualDiagram> diagrams) {
		ObservableList<VirtualDiagram> children = diagramList.getItems();
		
		if (diagrams == null) {
			masterDiagrams.clear();
			children.clear();
			return;
		}
		masterDiagrams.setAll(diagrams);
		children.setAll(diagrams);
		children.sort(NamedItem.Comparator);
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
		diagramList.getItems().clear();
		slotList.getItems().clear();
		skeletonList.getItems().clear();
		skeletonPane.setText("Skeletons");
		positionPane.setText("Positions");
		
		positionList.getItems().setAll(positions);
		positionPane.setExpanded(true);
	}

	public void onPositionChanged(Position position) {
		controller.loadSkeletons(position.id);
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

	/*
		Context Menus
	 */
	public void setFront(ActionEvent e) {
		Skeleton s = skeletonList.getSelectionModel().getSelectedItem();
		controller.setSkeleton(Perspective.Front, s);
		setSkeleton(Perspective.Front, s);
	}

	public void setSide(ActionEvent e) {
		Skeleton s = skeletonList.getSelectionModel().getSelectedItem();
		controller.setSkeleton(Perspective.Side, s);
		setSkeleton(Perspective.Side, s);
	}

	public void deleteSkeleton(ActionEvent e) {
		Skeleton s = skeletonList.getSelectionModel().getSelectedItem();
		controller.deleteSkeleton(s);
	}
}
