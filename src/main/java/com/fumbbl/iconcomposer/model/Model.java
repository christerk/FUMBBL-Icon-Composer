package com.fumbbl.iconcomposer.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.fumbbl.*;
import com.fumbbl.iconcomposer.model.types.*;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;

public class Model {
	private final DataStore dataStore;
	
	private final Config cfg;
	private final DataLoader dataLoader;

	private Controller controller;

	public final ObservableList<VirtualDiagram> masterDiagrams;
	public final ObservableList<Position> masterPositions;
	public final ObservableList<VirtualSlot> masterSlots;
	public final ObservableList<NamedImage> masterImages;
	public final ObservableList<Bone> masterBones;

	public final SimpleObjectProperty<Skeleton> frontSkeleton;
	public final SimpleObjectProperty<Skeleton> sideSkeleton;

	public final SimpleObjectProperty<Position> selectedPosition;

	public Model() {
		dataStore = new DataStore();
		cfg = new Config();
		dataLoader = new DataLoader(cfg);

		masterPositions = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });
		masterBones = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });
		masterSlots = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });
		masterDiagrams = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });
		masterImages = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });

		frontSkeleton = new SimpleObjectProperty<>();
		sideSkeleton = new SimpleObjectProperty<>();
		selectedPosition = new SimpleObjectProperty<>();

		sortByName(masterPositions);
		sortByName(masterBones);
		sortByName(masterSlots);
		sortByName(masterDiagrams);

		selectedPosition.addListener((o, oldValue, newValue) -> loadPosition(newValue));
	}

	private <T extends NamedItem> void sortByName(ObservableList<T> list) {
		list.addListener(getChangeListener(list));
	}

	private <T extends NamedItem> ListChangeListener<T> getChangeListener(ObservableList<T> list) {
		return (ListChangeListener.Change<? extends T> c) -> {
			if (c.next() && (c.wasAdded() || c.wasRemoved())) {
				FXCollections.sort(list, NamedItem.Comparator);
			}
		};
	}

	public Config getConfig() {
		return cfg;
	}

	private void importPng(Path path, byte[] bytes) {
		try {
			String fileId = stripExtension(path.getFileName());
			Diagram diagram = null;

			String originalName = fileId;
			Skeleton skeleton = null;
			Perspective perspective = Perspective.Unknown;

			if (fileId.startsWith("front_")) {
				perspective = Perspective.Front;
				fileId = fileId.substring(6);
			} else if (fileId.startsWith("side_")) {
				perspective = Perspective.Side;
				fileId = fileId.substring(5);
			}
			diagram = getDiagram(perspective, fileId);

			if (diagram == null) {
				String finalFileId = fileId;
				VirtualSlot vSlot = masterSlots.stream().filter(s -> finalFileId.startsWith(s.getName())).findFirst().get();

				diagram = GenerateDiagram(perspective, vSlot, fileId);
				dataStore.addDiagram(getSkeleton(perspective), fileId, diagram);
				dataLoader.saveDiagram(diagram);
			}

			InputStream is = new ByteArrayInputStream(bytes);
			BufferedImage image = ImageIO.read(is);

			diagram.width = image.getWidth();
			diagram.height = image.getHeight();
			dataLoader.saveDiagram(diagram);

			NamedPng newImage = new NamedPng(originalName, image);
			dataStore.addImage(newImage);
			dataLoader.uploadFile(diagram.id, originalName, bytes);

			masterImages.add(dataStore.getImage(newImage.getName()));
		} catch (IOException ioe) {

		}
	}

	private void loadImage(Diagram diagram) {
		dataLoader.loadImage(diagram.imageId, image -> {
			String prefix = diagram.perspective == Perspective.Front ? "front_" : "side_";
			NamedPng newImage = new NamedPng(prefix+diagram.getName(), image);
			dataStore.addImage(newImage);
			masterImages.add(newImage);
			return image;
		});
	}

	private Diagram getDiagram(Perspective perspective, String slotName) {
		Skeleton s = getSkeleton(perspective);
		if (s != null) {
			return controller.getDiagram(s.id, slotName);
		}
		return null;
	}

	private String stripExtension(Path filename) {
		String s = filename.toString();
		
		return s.substring(0, s.lastIndexOf('.'));
	}

	public void loadRoster(int rosterId) {
		dataStore.clearDiagrams();
		dataStore.clearSlots();
		dataStore.clearPosition();

		DtoRoster roster = dataLoader.getRoster(rosterId);

		masterPositions.setAll(roster.toRoster().positions);
	}

	private void loadPosition(Position position) {
		frontSkeleton.set(null);
		sideSkeleton.set(null);
		masterDiagrams.clear();
		masterImages.clear();
		masterSlots.clear();
		masterBones.clear();
		dataStore.clearSlots();
		dataStore.clearDiagrams();
		dataStore.clearBones();
		dataStore.clearImages();

		loadSkeletons(position);

		controller.onProgressStart("Loading images");
		controller.startBatch();

		loadParts(frontSkeleton.get());
		loadParts(sideSkeleton.get());

		controller.runBatch();

		controller.viewState.setActivePosition(position);
	}

	private void loadParts(Skeleton skeleton) {
		if (skeleton != null) {
			loadBones(skeleton);
			loadSlots(skeleton);
			loadDiagrams(skeleton);
		}
	}

	public void setSkeleton(Skeleton skeleton) {
		switch (skeleton.perspective) {
			case Front:
				frontSkeleton.set(skeleton);
				break;
			case Side:
				sideSkeleton.set(skeleton);
				break;
		}
	}

	public Collection<Ruleset> loadRulesets() {
		Collection<DtoRuleset> list = dataLoader.getRulesets();
		Set<Ruleset> rulesets = new HashSet<>();
		list.forEach(r -> rulesets.add(r.toRuleset()));
		return rulesets;
	}
	
	public void loadRuleset(int rulesetId) {
		dataStore.setRuleset(dataLoader.getRuleset(rulesetId).toRuleset());
		controller.onRulesetLoaded(dataStore.getRuleset());
	}
	
	public void loadSkeletons(Position position) {
		if (position == null) {
			return;
		}
		DtoPositionData list = dataLoader.getPositionData(position.id);

		list.skeletons.forEach(s -> setSkeleton(s.toSkeleton()));

		list.data.forEach((col, rgb) -> {
			ColourType type = ColourTheme.ColourType.valueOf(col);
			position.templateColours.setColour(type, Color.decode(rgb));
		});
	}
	
	private void loadDiagrams(Skeleton skeleton) {
		Map<Integer,Diagram> diagramMap = new HashMap<>();

		Collection<DtoDiagram> diagrams = dataLoader.getDiagrams(skeleton);

		diagrams.forEach(d -> {
			Diagram diagram = d.toDiagram(skeleton.perspective);

			if (diagram.imageId > 0) {
				controller.runInBackground(() -> loadImage(diagram));
			}
			diagramMap.put(d.id, diagram);
		});

		for (DtoDiagram d : diagrams) {
			Diagram diagram = diagramMap.get(d.id);
			diagram.setSlot(dataStore.getSlot(d.slotId));
		}

		dataStore.addDiagrams(skeleton.id, diagramMap.values());

		HashSet<String> existing = masterDiagrams.stream().map(NamedItem::getName).collect(Collectors.toCollection(HashSet::new));
		masterDiagrams.addAll(diagramMap.values().stream().filter(d -> !existing.contains(d.getName())).map(VirtualDiagram::new).collect(Collectors.toList()));
	}
	
	private void loadBones(Skeleton skeleton) {
		if (skeleton == null) {
			return;
		}
		Collection<Bone> bones = dataLoader.getBones(skeleton.id);

		skeleton.setBones(bones);
		dataStore.addBones(bones);
		masterBones.addAll(bones);
	}

	private void loadSlots(Skeleton skeleton) {
		if (skeleton == null) {
			return;
		}
		Map<Integer,Slot> slots = new HashMap<>();
		Collection<DtoSlot> list = dataLoader.getSlots(skeleton.id);

		list.forEach(s -> slots.put(s.id, s.toSlot()));
		
		for (DtoSlot s : list) {
			Slot slot = slots.get(s.id);
			slot.setBone(dataStore.getBone(s.boneId));
			slot.setSkeleton(skeleton);
		}

		HashSet<String> existing = masterSlots.stream().map(NamedItem::getName).collect(Collectors.toCollection(HashSet::new));
		masterSlots.addAll(slots.values().stream().filter(s->!existing.contains(s.getName())).map(VirtualSlot::new).collect(Collectors.toList()));
		dataStore.addSlots(slots.values());
	}

	public void setupThemes() {
		ColourTheme homeColours = new ColourTheme("home");
		homeColours.setColour(ColourType.PRIMARY, 255, 0, 0);
		homeColours.setColour(ColourType.PRIMARYHI, 255, 128, 128);
		homeColours.setColour(ColourType.PRIMARYLO, 128, 0, 0);
		homeColours.setColour(ColourType.PRIMARYLINE, 64, 0, 0);

		homeColours.setColour(ColourType.SECONDARY, 166, 172, 186);
		homeColours.setColour(ColourType.SECONDARYHI, 183, 190, 201);
		homeColours.setColour(ColourType.SECONDARYLO, 139, 141, 153);
		homeColours.setColour(ColourType.SECONDARYLINE, 69, 71, 78);

		ColourTheme awayColours = new ColourTheme("away");
		awayColours.setColour(ColourType.PRIMARY, 0, 0, 255);
		awayColours.setColour(ColourType.PRIMARYHI, 128, 128, 255);
		awayColours.setColour(ColourType.PRIMARYLO, 0, 0, 128);
		awayColours.setColour(ColourType.PRIMARYLINE, 0, 0, 64);

		awayColours.setColour(ColourType.SECONDARY, 239, 193, 112);
		awayColours.setColour(ColourType.SECONDARYHI, 194, 153, 92);
		awayColours.setColour(ColourType.SECONDARYLO, 141, 109, 72);
		awayColours.setColour(ColourType.SECONDARYLINE, 70, 54, 36);

		ColourTheme templateColours = new ColourTheme("template");
		
		dataStore.addColourTheme("template", templateColours);
		dataStore.addColourTheme("home", homeColours);
		dataStore.addColourTheme("away", awayColours);
		
		controller.onColourThemesChanged(dataStore.getColourThemes());
	}

	public void setController(Controller controller) {
		this.controller = controller;
		dataLoader.setController(controller);
	}

	public Diagram getDiagram(int skeletonId, String diagramName) {
		return dataStore.getDiagram(skeletonId, diagramName);
	}

	public Slot getSlot(int skeletonId, String slotName) {
		return dataStore.getSlot(skeletonId, slotName);
	}

	public ColourTheme getColourTheme(String theme) {
		return dataStore.getColourTheme(theme);
	}

	public void authenticate() {
		boolean success = false;
		String clientId = getConfig().getClientId();
		String clientSecret = getConfig().getClientSecret();
		if (clientId != null && clientId.length() > 0) {
			success = dataLoader.authenticate(clientId, clientSecret);
		}
		controller.onAuthenticateChange(success);
	}

	public boolean isAuthorized() {
		return dataLoader.isAuthenticated();
	}

	public void handleDroppedFile(String path) {
		Runnable task = () -> {
			try {
				Path p = Paths.get(path);
				PathMatcher jsonMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
				PathMatcher pngMatcher = FileSystems.getDefault().getPathMatcher("glob:**.png");

				if (!Files.isReadable(p)) {
					return;
				}

				List<Runnable> delayedTasks = new LinkedList<>();

				long size = Files.size(p);
				if (size > 0 && size < 1024*1024*10) {
					byte[] bytes = Files.readAllBytes(p);

					if (jsonMatcher.matches(p)) {
						controller.onProgressStart("Importing Spine");
						try {
							SpineImporter importer = new SpineImporter();
							importer.importSkeleton(new String(bytes));

							Position pos = selectedPosition.get();
							Skeleton skeleton = importer.getSkeleton();

							if (p.getFileName().toString().contains("_front_")) {
								skeleton.perspective = Perspective.Front;
							} else if (p.getFileName().toString().contains("_side_")) {
								skeleton.perspective = Perspective.Side;
							} else {
								return;
							}

							setSkeleton(skeleton);

							dataStore.setSlots(skeleton.getSlots());
							dataStore.setBones(skeleton.getBones());

							controller.startBatch();
							dataLoader.saveSkeleton(pos, skeleton);
							dataLoader.setPerspective(pos, skeleton);
							dataLoader.saveBones(skeleton);
							dataLoader.saveSlots(skeleton);

							Collection<Diagram> diagrams = importer.getDiagrams();
							dataStore.setDiagrams(skeleton.id, diagrams);
							diagrams.forEach(d -> dataLoader.saveDiagram(d));

							Collection<Skin> skins = importer.getSkins();

							controller.runBatch();

							delayedTasks.add(() -> {
								controller.onBonesChanged(skeleton.getBones());
								masterSlots.setAll(skeleton.getSlots().stream().map(s -> new VirtualSlot(s)).collect(Collectors.toList()));

								masterDiagrams.clear();
								masterDiagrams.addAll(diagrams.stream().map(d -> new VirtualDiagram(d)).collect(Collectors.toList()));
							});

						} catch (Exception e) {
							controller.getMainController().onProgressComplete();
							controller.stopProgress();
						}
					} else if (pngMatcher.matches(p)) {
						controller.runInBackground(() -> importPng(p, bytes));
					}
				}

				controller.runInBackground(() -> Platform.runLater(() -> {
					for (Runnable task1 : delayedTasks) {
						task1.run();
					}
				}));
			} catch (IOException e) {
				controller.onProgress(1, true);
			}
		};
		
		controller.runInBackground(task);
	}

	public BufferedImage getImage(String imageName) {
		NamedItem image = dataStore.getImage(imageName.toLowerCase());
		if (image != null && image instanceof NamedPng) {
			return ((NamedPng)image).image;
		}
		return null;
	}

	public void onItemRenamed(NamedItem item, String oldName) {
		if (item instanceof Skeleton) {
			dataLoader.saveSkeleton(dataStore.getPosition(), (Skeleton)item);
		} else if (item instanceof NamedImage) {
			dataStore.renameImage(oldName, item.getName());
			//controller.onImagesChanged(dataStore.getImages());
		} else if (item instanceof Slot) {
			dataStore.renameSlot(oldName, item.getName());
			dataLoader.saveSlot((Slot)item);
		} else if (item instanceof VirtualDiagram) {
			dataStore.renameDiagram(oldName, item.getName());
			dataStore.renameDiagramImages(oldName, item.getName());
			dataLoader.saveDiagram(dataStore.getDiagram(getSkeleton(Perspective.Front).id, item.getName()));
			dataLoader.saveDiagram(dataStore.getDiagram(getSkeleton(Perspective.Side).id, item.getName()));
		}
	}

	public void setPerspective(Skeleton s)
	{
		dataLoader.setPerspective(dataStore.getPosition(), s);
	}

	public void clearDiagrams() {
		dataStore.clearDiagrams();
	}

	public Collection<NamedImage> getImagesForDiagram(VirtualDiagram d) {
		return dataStore.getImages().stream().filter(i->i.getName().endsWith(d.getName())).collect(Collectors.toList());
	}

	public void setPositionColour(Position p, ColourType type, String rgb) {
		dataLoader.setPositionVariable(p, type.name(), rgb);
	}

	public Skeleton getSkeleton(Perspective perspective) {
		if (perspective == Perspective.Front) {
			return frontSkeleton.get();
		} else if (perspective == Perspective.Side) {
			return sideSkeleton.get();
		}
		return null;
	}

	private Diagram GenerateDiagram(Perspective perspective, VirtualSlot slot, String name) {
		Diagram d = new Diagram();
		d.setName(name);
		d.perspective = perspective;
		d.setSlot(getSlot(getSkeleton(perspective).id, slot.getName()));
		return d;
	}

	public void createDiagram(VirtualSlot slot, String name) {
		Diagram frontDiagram = GenerateDiagram(Perspective.Front, slot, name);
		Diagram sideDiagram = GenerateDiagram(Perspective.Side, slot, name);

		dataStore.addDiagram(frontSkeleton.get(), name, frontDiagram);
		dataStore.addDiagram(sideSkeleton.get(), name, sideDiagram);
		dataLoader.saveDiagram(frontDiagram);
		dataLoader.saveDiagram(sideDiagram);

		masterDiagrams.add(new VirtualDiagram(slot, name));
	}

	public void saveDiagram(Diagram d) {
		dataLoader.saveDiagram(d);
	}
}
