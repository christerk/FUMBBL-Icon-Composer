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
import com.fumbbl.iconcomposer.Diagram;
import com.fumbbl.iconcomposer.dto.DtoPosition;
import com.fumbbl.iconcomposer.model.NamedSVG;
import com.fumbbl.iconcomposer.model.spine.Bone;
import com.fumbbl.iconcomposer.model.spine.Skeleton;
import com.fumbbl.iconcomposer.model.spine.Skin;
import com.fumbbl.iconcomposer.model.spine.Slot;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
	public ListView<DtoPosition> positionList;
	
	public ListView<Skeleton> skeletonList;
	public ListView<NamedSVG> imageList;
	public ListView<Diagram> diagramList;
	public ListView<Skin> skinList;
	public ListView<Slot> slotList;
	
	public TitledPane positionPane;
	public TitledPane skeletonPane;
	public TitledPane imagePane;
	public TitledPane diagramPane;
	public TitledPane skinPane;
	public TitledPane slotPane;
	
	public TreeView<String> tree;
	public TreeItem<String> root;
	public TreeItem<String> bones;
	private TreeItem<String> rootBone;
	
	public Menu menuColourThemes;
	
	public MainController() {
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colourPane.setVisible(false);
		bones = new TreeItem<String>("Bones");
		
		root = new TreeItem<String>("Root");
		root.getChildren().add(bones);
		tree.setShowRoot(false);
		tree.setRoot(root);
		
		positionList.setCellFactory(new CellFactory<DtoPosition>().create());
		skeletonList.setCellFactory(new CellFactory<Skeleton>().create());
		imageList.setCellFactory(new CellFactory<NamedSVG>().create());
		diagramList.setCellFactory(new CellFactory<Diagram>().create());
		skinList.setCellFactory(new CellFactory<Skin>().create());
		slotList.setCellFactory(new CellFactory<Slot>().create());
		
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
		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
				if (newValue != null) {
					TreeItem<String> section = getTreeSection(newValue);

					String value = (String)newValue.getValue();
					if (section == bones) {
						if (rootBone != null && value.equals(bones.getValue())) {
							controller.displayBones(rootBone.getValue());
						} else {
							controller.displayBones(value);
						}
					}
				}
			}

			private TreeItem<String> getTreeSection(TreeItem<String> treeItem) {
				TreeItem<String> current = treeItem;
				while (current.getParent() != root) {
					current = current.getParent();
				}
				return current;
			}
		});

		slotChoices.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Slot>() {

			@Override
			public void changed(ObservableValue<? extends Slot> observable, Slot oldValue, Slot newValue) {
				Diagram d = controller.viewState.getActiveDiagram();
				d.setSlot(newValue);
			}
			
		});
		
		positionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DtoPosition>() {
			@Override
			public void changed(ObservableValue<? extends DtoPosition> observable, DtoPosition oldValue, DtoPosition newValue) {
				if (newValue != null) {
					diagramList.getItems().clear();
					skinList.getItems().clear();
					skeletonList.getItems().clear();
					skeletonPane.setText("Skeletons");
					
					controller.loadPosition(newValue.id);
					skeletonPane.setExpanded(true);
					positionPane.setText("Positions - " + newValue.title);
				} else {
					positionPane.setText("Positions");
				}
			}
		});

		skeletonList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Skeleton>() {
			@Override
			public void changed(ObservableValue<? extends Skeleton> observable, Skeleton oldValue, Skeleton newValue) {
				if (newValue != null) {
					setSkeleton(newValue);
				} else {
					skeletonPane.setText("Skeletons");
					controller.setSkeleton(null);
				}
			}
		});

		imageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<NamedSVG>() {
			@Override
			public void changed(ObservableValue<? extends NamedSVG> observable, NamedSVG oldValue, NamedSVG newValue) {
				if (newValue != null) {
					controller.displayImage(newValue);
				}
			}
		});

		diagramList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Diagram>() {
			@Override
			public void changed(ObservableValue<? extends Diagram> observable, Diagram oldValue, Diagram newValue) {
				if (newValue != null) {
					controller.displayDiagram(newValue);
				}
			}
		});

		skinList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Skin>() {
			@Override
			public void changed(ObservableValue<? extends Skin> observable, Skin oldValue, Skin newValue) {
				if (newValue != null) {
					controller.displaySkin(newValue);
				}
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
			d.refreshColours(controller.getSvg(d.svgName));
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
			d.refreshColours(controller.getSvg(d.svgName));
			controller.onColourThemeChanged(d.templateColours);
		}
	}
	
	public void quit() {
		System.exit(0);
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
		skeletonPane.setText("Skeletons - " + newValue.name);
		
		Collection<Bone> bones = null;
		Collection<Slot> slots = null;
		
		if (newValue.id > 0) {
			bones = controller.loadBones(newValue.id);
			slots = controller.loadSlots(newValue.id);
		} else if (newValue.id == -1) {
			bones = newValue.getBones();
			slots = newValue.getSlots();
		}

		controller.setSkeleton(newValue);
		controller.setBones(bones);
		controller.setSlots(slots);
		
		setBones(bones);
		setSlots(slots);
		
		controller.displayBones(null);
	}
	
	public void setColourTheme(ColourTheme t) {
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
	}

	public void hideColourPane() {
		colourPane.setVisible(false);
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
		children.setAll(skins);
	}
	
	public void setSkeletons(Collection<Skeleton> skeletons) {
		ObservableList<Skeleton> items = skeletonList.getItems();
		items.setAll(skeletons);
	}
	
	public void setImages(Collection<NamedSVG> images) {
		ObservableList<NamedSVG> list = imageList.getItems();
		list.setAll(images);
	}

	public void setSlots(Collection<Slot> slots) {
		if (slots != null) {
			slotList.getItems().setAll(slots);
			slotChoices.getItems().setAll(slots);
		} else {
			slotList.getItems().clear();
			slotChoices.getItems().clear();
		}
	}
	
	public void setDiagrams(Collection<Diagram> diagrams) {
		ObservableList<Diagram> children = diagramList.getItems();
		children.setAll(diagrams);
	}
	
	public void setBones(Collection<Bone> bones) {
		Map<String,Bone> boneMap = new HashMap<String,Bone>();
		Map<String,TreeItem<String>> itemMap = new HashMap<String,TreeItem<String>>();

		for (Bone b : bones) {
			boneMap.put(b.name, b);
			itemMap.put(b.name, new TreeItem<String>(b.name));
		}

		rootBone = null;
		for (Bone b : boneMap.values()) {
			if (b.parent != null) {
				TreeItem<String> item = itemMap.get(b.name);
				TreeItem<String> parent = itemMap.get(b.parent);
				parent.getChildren().add(item);
			} else {
				rootBone = itemMap.get(b.name);
			}
		}
		
		this.bones.getChildren().clear();
		if (rootBone != null) {
			for (TreeItem<String> item : rootBone.getChildren()) {
				this.bones.getChildren().add(item);
			}
		}
	}

	public void onPositionsChanged(Collection<DtoPosition> positions) {
		ObservableList<DtoPosition> items = positionList.getItems();
		items.setAll(positions);
		positionPane.setExpanded(true);
	}

	public void onPositionChanged(DtoPosition position) {
		controller.loadSkeletons(position.id);
		controller.loadSkins(position.id);
	}
	
	public void onSkeletonsChanged(Collection<Skeleton> skeletons) {
		setSkeletons(skeletons);
	}
	
	public void onSkinsChanged(Collection<Skin> skins) {
		setSkins(skins);
	}
}
