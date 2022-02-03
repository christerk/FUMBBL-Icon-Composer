package com.fumbbl.iconcomposer.controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.model.types.*;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
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
	public ImageView imageView;
	public Image image;
	
	public ImageView primaryLo;
	public ImageView primaryMid;
	public ImageView primaryHi;
	public ImageView secondaryLo;
	public ImageView secondaryMid;
	public ImageView secondaryHi;
	public ImageView hairLo;
	public ImageView hairMid;
	public ImageView hairHi;
	public ImageView skinLo;
	public ImageView skinMid;
	public ImageView skinHi;
	public GridPane colourPane;
	public ChoiceBox<Slot> slotChoices;
	public TextField diagramX;
	public TextField diagramY;
	public Label apiStatus;
	public ListView<Position> positionList;

	public Label labelProgress;
	public HBox progressPane;
	public ProgressBar progressBar;
	
	public ListView<Skeleton> skeletonList;
	public ListView<NamedImage> imageList;
	public ListView<Diagram> diagramList;
	public ListView<Skin> skinList;
	public ListView<Slot> slotList;
	
	public TitledPane positionPane;
	public TitledPane skeletonPane;
	public TitledPane imagePane;
	public TitledPane diagramPane;
	public TitledPane skinPane;
	public TitledPane slotPane;
	
	public FlowPane diagramChoicePane;
	public ChoiceBox<Diagram> diagramChoices;
	
	public Menu menuColourThemes;
	
	private ObservableList<Diagram> masterDiagrams;
	
	public MainController() {
		masterDiagrams = FXCollections.observableArrayList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colourPane.setVisible(false);
		diagramChoicePane.setVisible(false);
		
		new CellFactory<Position>().apply(positionList, Position.class);
		new CellFactory<Skeleton>().apply(skeletonList, Skeleton.class);
		new CellFactory<NamedImage>().apply(imageList, NamedImage.class);
		new CellFactory<Diagram>().apply(diagramList, Diagram.class);
		new CellFactory<Skin>().apply(skinList, Skin.class);
		new CellFactory<Slot>().apply(slotList, Slot.class);
		
		skeletonList.setEditable(true);
		imageList.setEditable(true);
		diagramList.setEditable(true);
		skinList.setEditable(true);
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

		diagramChoices.setConverter(new StringConverter<Diagram>() {
			@Override
			public String toString(Diagram object) {
				return object.getImage().getName();
			}

			@Override
			public Diagram fromString(String string) {
				return null;
			}
		});
		
		diagramChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Slot slot = slotList.getSelectionModel().getSelectedItem();
			Skin skin = controller.viewState.getActiveSkin();

			if (slot != null && skin != null) {
				controller.setSkinDiagram(skin, slot, newValue);
				controller.displaySkin(controller.viewState.getActiveSkin());
			}
		});

		slotChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Diagram d = controller.viewState.getActiveDiagram();
			d.setSlot(newValue);
		});
		
		positionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				diagramList.getItems().clear();
				skinList.getItems().clear();
				slotList.getItems().clear();
				skeletonList.getItems().clear();
				skeletonPane.setText("Skeletons");
				
				controller.loadPosition(newValue.id);
				skeletonPane.setExpanded(true);
				positionPane.setText("Positions - " + newValue.getName());
			} else {
				positionPane.setText("Positions");
			}
		});

		skeletonList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				setSkeleton(newValue);
				controller.loadDiagrams(newValue.id);
			} else {
				skeletonPane.setText("Skeletons");
				controller.setSkeleton(null);
			}
		});

		imageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				controller.displayImage(newValue);
			}
		});

		diagramList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				controller.displayDiagram(newValue);
			}
		});

		skinList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				controller.displaySkin(newValue);
			}
		});

		slotList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				showDiagramPane();
				
				FilteredList<Diagram> filteredDiagrams = new FilteredList<Diagram>(masterDiagrams, diagram -> {
					return newValue.equals(diagram.getSlot());
				});

				diagramChoices.setItems(new SortedList<Diagram>(filteredDiagrams, NamedItem.Comparator));

				controller.displaySkin(controller.viewState.getActiveSkin());
			}
		});
		
	}
	
	/*
	 *    Control Event Handlers
	 */
	
	public void imageClicked(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		MouseButton button = e.getButton();

		Diagram d = controller.viewState.getActiveDiagram();
		ColourType type = controller.viewState.getActiveColourType();
		
		if (button == MouseButton.PRIMARY && controller != null && type != null) {
			Color c = controller.viewState.getPixelRGB((int)x, (int)y);
			
			d.templateColours.setColour(type, c);
			d.refreshColours(d.getImage());
			controller.onColourThemeChanged(d.templateColours);
		} else if (button == MouseButton.SECONDARY) {
			Point2D point = controller.getRenderer().getImageOffset(x, y);
			d.x = point.getX();
			d.y = point.getY();
			controller.displayDiagram();
		}
	}

	public void activateColour(MouseEvent e) {
		controller.viewState.setActiveColourType(ColourType.fromString(((ImageView)e.getSource()).getId()));
		if (e.getButton() == MouseButton.SECONDARY) {
			Diagram d = controller.viewState.getActiveDiagram();
			ColourType type = controller.viewState.getActiveColourType();
			d.templateColours.resetColour(type);
			d.refreshColours(d.getImage());
			controller.onColourThemeChanged(d.templateColours);
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
	
	public void deleteSkeleton() {
		controller.deleteSkeleton(controller.viewState.getActiveSkeleton());
	}
	
	public void createSkin() {
		controller.createSkin(controller.viewState.getActiveSkeleton());
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
			setSkins(null);
			return;
			
		}
		skeletonPane.setText("Skeletons - " + newValue.name);
		
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
		controller.setBones(bones);
		controller.setSlots(slots);
		
		setBones(bones);
		setSlots(slots);
		
		controller.loadDiagrams(newValue.id);
		
		controller.displayBones(null);
	}
	
	public void setColourTheme(ColourTheme t) {
		if (t == null) {
			return;
		}
		setImageColour(primaryLo, t, ColourType.PRIMARYLO);
		setImageColour(primaryMid, t, ColourType.PRIMARY);
		setImageColour(primaryHi, t, ColourType.PRIMARYHI);

		setImageColour(secondaryLo, t, ColourType.SECONDARYLO);
		setImageColour(secondaryMid, t, ColourType.SECONDARY);
		setImageColour(secondaryHi, t, ColourType.SECONDARYHI);

		setImageColour(skinLo, t, ColourType.SKINLO);
		setImageColour(skinMid, t, ColourType.SKIN);
		setImageColour(skinHi, t, ColourType.SKINHI);

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

	public void setSlotInfo(Slot slot, double x, double y) {
		slotChoices.setValue(slot);
		diagramX.setText(Double.toString(x));
		diagramY.setText(Double.toString(y));
	}

	public void setImage(WritableImage image) {
		imageView.setImage(image);
	}
	
	public void setColourThemes(Collection<ColourTheme> themes) {
		ObservableList<MenuItem> menuItems = menuColourThemes.getItems();
		EventHandler<ActionEvent> action = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String theme = ((MenuItem) event.getSource()).getText();
				controller.setColourTheme(theme);
				controller.displaySkin(controller.viewState.getActiveSkin());
			}
		};
		for (ColourTheme s : themes) {
			MenuItem item = new MenuItem(s.name);
			menuItems.add(item);
			item.setOnAction(action);
		}
	}
	
	public void setSkins(Collection<Skin> skins) {
		ObservableList<Skin> children = this.skinList.getItems();
		
		if (skins == null) {
			children.clear();
			return;
		}
		
		children.setAll(skins);
		children.sort(NamedItem.Comparator);
	}
	
	public void setSkeletons(Collection<Skeleton> skeletons) {
		ObservableList<Skeleton> items = skeletonList.getItems();
		Skeleton currentSkeleton = controller.viewState.getActiveSkeleton();
		items.setAll(skeletons);
		if (currentSkeleton != null) {
			setSkeleton(currentSkeleton);
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
	
	public void setDiagrams(Collection<Diagram> diagrams) {
		ObservableList<Diagram> children = diagramList.getItems();
		
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
		skinList.getItems().clear();
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
		controller.loadSkins(position.id);
	}
	
	public void onSkeletonsChanged(Collection<Skeleton> skeletons) {
		setSkeletons(skeletons);
	}
	
	public void onSkinsChanged(Collection<Skin> skins) {
		setSkins(skins);
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
}
