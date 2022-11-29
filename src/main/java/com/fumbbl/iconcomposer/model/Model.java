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
import com.fumbbl.iconcomposer.TaskManager;
import com.fumbbl.iconcomposer.dto.fumbbl.*;
import com.fumbbl.iconcomposer.events.SkeletonChangedEvent;
import com.fumbbl.iconcomposer.model.types.*;

import com.sun.javafx.event.EventHandlerManager;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;

public class Model extends EventHandlerManager {
	private final DataStore dataStore;
	
	private final Config cfg;
	public final DataLoader dataLoader;

	public final ObservableList<Position> masterPositions;
	public final ObservableList<VirtualSlot> masterSlots;
	public final ObservableList<NamedImage> masterImages;

	public final SimpleObjectProperty<VirtualSkeleton> masterSkeleton;

	public final SimpleObjectProperty<Position> selectedPosition;
	public final SimpleObjectProperty<Ruleset> loadedRuleset;
	public final TaskManager taskManager;

	public Model(Config config, DataLoader dataLoader, TaskManager taskManager) {
		super(Model.class);

		dataStore = new DataStore();
		this.dataLoader = dataLoader;
		cfg = config;

		this.taskManager = taskManager;

		masterPositions = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });
		masterSlots = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });
		masterImages = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });

		selectedPosition = new SimpleObjectProperty<>();
		loadedRuleset = new SimpleObjectProperty<>();
		masterSkeleton = new SimpleObjectProperty<>(new VirtualSkeleton());

		sortByName(masterPositions);
		sortByName(masterSlots);

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
			VirtualDiagram vDiagram = findVirtualDiagram(diagram);

			VirtualImage vImage = new VirtualImage(vDiagram, newImage.getName());
			vImage.set(newImage);
			vDiagram.addImage(vImage);

			dataStore.addImage(newImage);
			masterImages.add(newImage);
			return image;
		});
	}

	private Diagram getDiagram(Perspective perspective, String slotName) {
		Skeleton s = getSkeleton(perspective);
		if (s != null) {
			return dataStore.getDiagram(s.id, slotName);
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

		DtoRoster roster = dataLoader.getRoster(rosterId);

		masterPositions.setAll(roster.toRoster().positions);
	}

	private void loadPosition(Position position) {
		masterImages.clear();
		masterSlots.clear();

		dataStore.clearImages();
		dataStore.clearDiagrams();
		dataStore.clearSlots();
		dataStore.clearBones();

		taskManager.onProgressStart("Loading");
		taskManager.startBatch();

		loadSkeletons(position);

		masterSkeleton.get().realSkeletons.values().forEach(s->loadParts(s));

		SkeletonChanged();

		taskManager.runBatch();
	}

	private void SkeletonChanged() {
		taskManager.runInBackground(() -> {
			dispatchBubblingEvent(new SkeletonChangedEvent(SkeletonChangedEvent.SKELETON_CHANGED));
		});
	}

	private void loadParts(Skeleton skeleton) {
		if (skeleton != null) {
			loadBones(skeleton);
			loadSlots(skeleton);
			loadDiagrams(skeleton);
		}
	}

	public Collection<Ruleset> loadRulesets() {
		Collection<DtoRuleset> list = dataLoader.getRulesets();
		Set<Ruleset> rulesets = new HashSet<>();
		list.forEach(r -> rulesets.add(r.toRuleset()));
		return rulesets;
	}
	
	public void loadRuleset(int rulesetId) {
		loadedRuleset.set(dataLoader.getRuleset(rulesetId).toRuleset());
	}
	
	public void loadSkeletons(Position position) {
		if (position == null) {
			return;
		}
		VirtualSkeleton newSkeleton = new VirtualSkeleton();

		DtoPositionData list = dataLoader.getPositionData(position.id);

		list.skeletons.forEach(s -> {
			Skeleton realSkeleton = s.toSkeleton();
			//setSkeleton(s.toSkeleton());
			newSkeleton.set(realSkeleton);
		});

		list.data.forEach((col, rgb) -> {
			ColourType type = ColourTheme.ColourType.valueOf(col);
			position.templateColours.setColour(type, Color.decode(rgb));
		});

		masterSkeleton.set(newSkeleton);
	}
	
	private void loadDiagrams(Skeleton skeleton) {
		Map<Integer,Diagram> diagramMap = new HashMap<>();

		Collection<DtoDiagram> diagrams = dataLoader.getDiagrams(skeleton);

		diagrams.forEach(d -> diagramMap.put(d.id, d.toDiagram(skeleton.perspective)));

		for (DtoDiagram d : diagrams) {
			Diagram diagram = diagramMap.get(d.id);
			diagram.setSlot(dataStore.getSlot(d.slotId));
		}

		updateDiagrams(diagramMap.values());

		diagramMap.values().forEach(diagram -> {
			if (diagram.imageId > 0) {
				taskManager.runInBackground(() -> loadImage(diagram));
			}
		});

		dataStore.addDiagrams(skeleton.id, diagramMap.values());
	}

	private void updateDiagrams(Collection<Diagram> diagramMap) {
		VirtualSkeleton virtualSkeleton = masterSkeleton.get();
		diagramMap.forEach(d -> {
			String boneName = d.getSlot().getBone().getName();
			if (virtualSkeleton.hasBone(boneName)) {
				VirtualBone vBone = virtualSkeleton.bones.get(boneName);
				String slotName = d.getSlot().getName();
				if (vBone.hasSlot(slotName)) {
					VirtualSlot vSlot = vBone.slots.get(slotName);
					String diagramName = d.getName();
					VirtualDiagram vDiagram;
					if (!vSlot.hasDiagram(diagramName)) {
						vDiagram = new VirtualDiagram(vSlot, diagramName);
						vSlot.addDiagram(vDiagram);
					} else {
						vDiagram = vSlot.diagrams.get(diagramName);
					}
					vDiagram.set(d);
				}
			}
		});
	}

	private VirtualBone findVirtualBone(Bone bone) {
		VirtualSkeleton virtualSkeleton = masterSkeleton.get();
		String boneName = bone.getName();
		if (virtualSkeleton.hasBone(boneName)) {
			return virtualSkeleton.bones.get(boneName);
		}
		return null;
	}

	private VirtualSlot findVirtualSlot(Slot slot) {
		VirtualSkeleton virtualSkeleton = masterSkeleton.get();
		String boneName = slot.getBone().getName();
		if (virtualSkeleton.hasBone(boneName)) {
			VirtualBone vBone = virtualSkeleton.bones.get(boneName);
			String slotName = slot.getName();
			if (vBone.hasSlot(slotName)) {
				return vBone.slots.get(slotName);
			}
		}
		return null;
	}

	private VirtualDiagram findVirtualDiagram(Diagram diagram) {
		VirtualSkeleton virtualSkeleton = masterSkeleton.get();
		String boneName = diagram.getSlot().getBone().getName();
		if (virtualSkeleton.hasBone(boneName)) {
			VirtualBone vBone = virtualSkeleton.bones.get(boneName);
			String slotName = diagram.getSlot().getName();
			if (vBone.hasSlot(slotName)) {
				VirtualSlot vSlot = vBone.slots.get(slotName);
				String diagramName = diagram.getName();
				if (vSlot.hasDiagram(diagramName)) {
					return vSlot.diagrams.get(diagramName);
				}
			}
		}
		return null;
	}

	private void loadBones(Skeleton skeleton) {
		if (skeleton == null) {
			return;
		}
		Collection<Bone> bones = dataLoader.getBones(skeleton.id);
		skeleton.setBones(bones);
		dataStore.addBones(bones);

		updateBones(skeleton);
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

		updateSlots(slots.values());
		dataStore.addSlots(slots.values());
	}

	private void updateSlots(Collection<Slot> slots) {
		VirtualSkeleton virtualSkeleton = masterSkeleton.get();
		slots.forEach(s -> {
			String boneName = s.getBone().getName();
			if (virtualSkeleton.hasBone(boneName)) {
				VirtualBone vBone = virtualSkeleton.bones.get(boneName);
				String slotName = s.getName();
				VirtualSlot vSlot;
				if (!vBone.hasSlot(slotName)) {
					vSlot = new VirtualSlot(vBone, slotName);
					masterSlots.add(vSlot);
					vBone.addSlot(vSlot);
				} else {
					vSlot = vBone.slots.get(slotName);
				}
				vSlot.set(s);
			}
		});
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

	public boolean isAuthenticated() {
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
						taskManager.onProgressStart("Importing Spine");
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

							//setSkeleton(skeleton);

							dataStore.setSlots(skeleton.getSlots());
							dataStore.setBones(skeleton.getBones());

							taskManager.startBatch();
							dataLoader.saveSkeleton(pos, skeleton);
							dataLoader.setPerspective(pos, skeleton);
							dataLoader.saveBones(skeleton);
							dataLoader.saveSlots(skeleton);

							Collection<Diagram> diagrams = importer.getDiagrams();
							dataStore.setDiagrams(skeleton.id, diagrams);
							diagrams.forEach(d -> dataLoader.saveDiagram(d));

							Collection<Skin> skins = importer.getSkins();

							taskManager.runBatch();

							delayedTasks.add(() -> {
								masterSkeleton.get().set(skeleton);
								masterSlots.clear();
								masterSkeleton.get().realSkeletons.values().forEach(rSkeleton -> {
									updateBones(rSkeleton);
									updateSlots(rSkeleton.getSlots());
									updateDiagrams(diagrams);
								});
							});

							SkeletonChanged();

						} catch (Exception e) {
							taskManager.stopProgress();
						}
					} else if (pngMatcher.matches(p)) {
						taskManager.runInBackground(() -> importPng(p, bytes));
					}
				}

				taskManager.runInBackground(() -> Platform.runLater(() -> {
					for (Runnable task1 : delayedTasks) {
						task1.run();
					}
				}));
			} catch (IOException e) {
				taskManager.onProgress(1, true);
			}
		};

		taskManager.runInBackground(task);
	}

	private void updateBones(Skeleton rSkeleton) {
		VirtualSkeleton virtualSkeleton = masterSkeleton.get();
		rSkeleton.getBones().forEach(rBone -> {
			String boneName = rBone.getName();
			VirtualBone vBone;
			if (!virtualSkeleton.hasBone(boneName)) {
				vBone = new VirtualBone(virtualSkeleton, rBone.getName());
				virtualSkeleton.addBone(vBone);
			} else {
				vBone = virtualSkeleton.bones.get(boneName);
			}
			vBone.set(rBone);
		});
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
			dataLoader.saveSkeleton(selectedPosition.get(), (Skeleton)item);
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
		dataLoader.setPerspective(selectedPosition.get(), s);
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
		return masterSkeleton.get().realSkeletons.get(perspective);
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

		//dataStore.addDiagram(frontSkeleton.get(), name, frontDiagram);
		//dataStore.addDiagram(sideSkeleton.get(), name, sideDiagram);
		//dataLoader.saveDiagram(frontDiagram);
		//dataLoader.saveDiagram(sideDiagram);

	}

	public void saveDiagram(Diagram d) {
		dataLoader.saveDiagram(d);
	}

	public void authenticate() {
		dataLoader.authenticate();
	}
}
