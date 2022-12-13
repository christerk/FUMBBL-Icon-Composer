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
import javafx.collections.ObservableMap;

import javax.imageio.ImageIO;

public class Model extends EventHandlerManager {
	private final Config cfg;
	public final DataLoader dataLoader;

	public final ObservableList<Position> masterPositions;

	public final SimpleObjectProperty<VirtualSkeleton> masterSkeleton;

	public final ObservableMap<String, ColourTheme> masterThemes;

	public final SimpleObjectProperty<Position> selectedPosition;
	public final SimpleObjectProperty<Ruleset> loadedRuleset;
	public final TaskManager taskManager;

	public Model(Config config, DataLoader dataLoader, TaskManager taskManager) {
		super(Model.class);

		this.dataLoader = dataLoader;
		cfg = config;

		this.taskManager = taskManager;

		masterPositions = FXCollections.observableArrayList(item -> new Observable[] { item.nameProperty() });

		selectedPosition = new SimpleObjectProperty<>();
		loadedRuleset = new SimpleObjectProperty<>();
		masterSkeleton = new SimpleObjectProperty<>(new VirtualSkeleton());
		masterThemes = FXCollections.observableHashMap();

		sortByName(masterPositions);

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
			VirtualDiagram vDiagram = null;

			if (diagram == null) {
				VirtualSlot vSlot = getVirtualSlotForFile(fileId);

				if (vSlot != null) {
					vDiagram = vSlot.diagrams.get(fileId);

					if (vDiagram != null) {
						diagram = GenerateDiagram(perspective, vSlot, fileId);
						vDiagram.set(diagram);

						dataLoader.saveDiagram(diagram);
					}
				}
			}

			if (diagram == null) {
				return;
			}

			InputStream is = new ByteArrayInputStream(bytes);
			BufferedImage image = ImageIO.read(is);

			diagram.width = image.getWidth();
			diagram.height = image.getHeight();
			dataLoader.saveDiagram(diagram);

			NamedPng newImage = new NamedPng(originalName, image);

			if (vDiagram != null) {
				VirtualImage vImage;
				if (vDiagram.hasImage(vDiagram.getName())) {
					vImage = vDiagram.images.get(vDiagram.getName());
				} else {
					vImage = new VirtualImage(vDiagram, vDiagram.getName());
				}
				vImage.set(newImage);
			}

			dataLoader.uploadFile(diagram.id, originalName, bytes);
		} catch (IOException ioe) {

		}
	}

	private VirtualSlot getVirtualSlotForFile(String finalFileId) {
		for (VirtualBone b : masterSkeleton.get().bones.values()) {
			for (VirtualSlot s : b.slots.values()) {
				if (finalFileId.startsWith(s.getName())) {
					return s;
				}
			}
		}
		return null;
	}

	private void loadImage(Diagram diagram) {
		dataLoader.loadImage(diagram.imageId, image -> {
			String prefix = diagram.perspective == Perspective.Front ? "front_" : "side_";
			NamedPng newImage = new NamedPng(prefix+diagram.getName(), image);
			VirtualDiagram vDiagram = findVirtualDiagram(diagram);

			if (vDiagram == null) {
				return null;
			}

			VirtualImage vImage;
			String imageName = vDiagram.getName();
			if (!vDiagram.hasImage(imageName)) {
				vImage = new VirtualImage(vDiagram, imageName);
				vDiagram.addImage(vImage);
			} else {
				vImage = vDiagram.images.get(imageName);
			}

			vImage.set(newImage);

			return image;
		});
	}

	private Diagram getDiagram(Perspective perspective, String slotName) {
		VirtualDiagram vDiagram = masterSkeleton.get().findDiagram(slotName);
		if (vDiagram != null) {
			return vDiagram.realDiagrams.get(perspective);
		}

		return null;
	}

	private String stripExtension(Path filename) {
		String s = filename.toString();
		
		return s.substring(0, s.lastIndexOf('.'));
	}

	public void loadRoster(int rosterId) {
		DtoRoster roster = dataLoader.getRoster(rosterId);

		masterPositions.setAll(roster.toRoster().positions);
	}

	private void loadPosition(Position position) {
		taskManager.onProgressStart("Loading");
		taskManager.startBatch();

		loadSkeletons(position);

		masterSkeleton.get().realSkeletons.values().forEach(s->loadParts(s));

		skeletonChanged();

		taskManager.runBatch();
	}

	public void skeletonChanged() {
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
			Slot slot = masterSkeleton.get().getSlotById(d.slotId);
			diagram.setSlot(slot);
		}

		updateDiagrams(diagramMap.values());

		diagramMap.values().forEach(diagram -> {
			if (diagram.imageId > 0) {
				taskManager.runInBackground(() -> loadImage(diagram));
			}
		});
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
			Bone bone = masterSkeleton.get().getBoneById(s.boneId);
			slot.setBone(bone);
			slot.setSkeleton(skeleton);
		}

		updateSlots(slots.values());
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


		masterThemes.put("template", templateColours);
		masterThemes.put("home", homeColours);
		masterThemes.put("away", awayColours);

	}

	public ColourTheme getColourTheme(String theme) {
		return masterThemes.get(theme);
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

				long size = Files.size(p);
				if (size > 0 && size < 1024*1024*10) {
					byte[] bytes = Files.readAllBytes(p);

					if (jsonMatcher.matches(p)) {
						importSkeleton(p, bytes);
					} else if (pngMatcher.matches(p)) {
						taskManager.runInBackground(() -> importPng(p, bytes));
					}
				}

			} catch (IOException e) {
				taskManager.onProgress(1, true);
			}
		};

		taskManager.runInBackground(task);
	}

	private void importSkeleton(Path p, byte[] bytes) {
		List<Runnable> delayedTasks = new LinkedList<>();

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

			masterSkeleton.get().set(skeleton);

			//setSkeleton(skeleton);

			taskManager.startBatch();
			dataLoader.saveSkeleton(pos, skeleton);
			dataLoader.setPerspective(pos, skeleton);
			dataLoader.saveBones(skeleton);
			dataLoader.saveSlots(skeleton);

			Collection<Diagram> diagrams = importer.getDiagrams();
			diagrams.forEach(d -> dataLoader.saveDiagram(d));

			Collection<Skin> skins = importer.getSkins();

			taskManager.runBatch();

			delayedTasks.add(() -> {
				masterSkeleton.get().set(skeleton);
				masterSkeleton.get().realSkeletons.values().forEach(rSkeleton -> {
					updateBones(rSkeleton);
					updateSlots(rSkeleton.getSlots());
					updateDiagrams(diagrams);
				});
			});

			skeletonChanged();

			taskManager.runInBackground(() -> Platform.runLater(() -> {
				for (Runnable task1 : delayedTasks) {
					task1.run();
				}
			}));

		} catch (Exception e) {
			taskManager.stopProgress();
		}
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

	public void setBonePosition(Perspective perspective, VirtualBone vBone, Bone rBone, double newX, double newY, boolean repositionDiagrams) {
		Bone rootBone = rBone.getSkeleton().getBone("root");

		double dX = (rootBone.x + newX - (rBone.parentBone != null ? rBone.parentBone.worldX : 0)) - rBone.x;
		double dY = (rootBone.y + newY - (rBone.parentBone != null ? rBone.parentBone.worldY : 0)) - rBone.y;

		for (VirtualBone vb : masterSkeleton.get().bones.values()) {
			Bone b = vb.realBones.get(perspective);
			if (b == null) {
				continue;
			}
			if (b.parentBone == rBone) {
				setBonePosition(perspective, vb, b, b.x - dX, b.y - dY, false);
			}
		}

		rBone.x += dX;
		rBone.y += dY;
		if (repositionDiagrams) {
			repositionDiagrams(perspective, vBone, -dX, -dY);
		}
		saveBone(rBone);
	}

	public void relocateSlot(VirtualSlot slot, VirtualBone targetBone) {
		VirtualBone oldBone = slot.bone;

		oldBone.removeSlot(slot);
		targetBone.addSlot(slot);

		for (Slot s : slot.realSlots.values()) {
			s.setBone(targetBone.realBones.get(s.getSkeleton().perspective));
			dataLoader.saveSlot(s);
		}

		relocateSlot(slot, targetBone, oldBone, Perspective.Front);
		relocateSlot(slot, targetBone, oldBone, Perspective.Side);

		skeletonChanged();
	}

	private void relocateSlot(VirtualSlot slot, VirtualBone targetBone, VirtualBone oldBone, Perspective perspective) {
		Bone rTargetBone = targetBone.realBones.get(perspective);
		Bone rOldBone = oldBone.realBones.get(perspective);
		double dX = rTargetBone.worldX - rOldBone.worldX;
		double dY = rTargetBone.worldY - rOldBone.worldY;
		repositionDiagrams(perspective, slot, dX, dY);
	}

	public void repositionDiagrams(Perspective perspective, VirtualBone vBone, double deltaX, double deltaY) {
		for (VirtualSlot vSlot : vBone.slots.values()) {
			repositionDiagrams(perspective, vSlot, deltaX, deltaY);
		}
	}

	private void repositionDiagrams(Perspective perspective, VirtualSlot vSlot, double deltaX, double deltaY) {
		for (VirtualDiagram vDiagram : vSlot.diagrams.values()) {
			Diagram rDiagram = vDiagram.realDiagrams.get(perspective);
			if (rDiagram != null) {
				rDiagram.x += deltaX;
				rDiagram.y += deltaY;
				saveDiagram(rDiagram);
			}
		}
	}

	public void onItemRenamed(NamedItem item, String oldName) {
		if (item instanceof Skeleton) {
			dataLoader.saveSkeleton(selectedPosition.get(), (Skeleton)item);
		} else if (item instanceof VirtualDiagram) {
			VirtualDiagram vDiagram = (VirtualDiagram)item;
			vDiagram.realDiagrams.values().forEach(d -> {
				d.setName(item.getName());
				dataLoader.saveDiagram(d);
			});
		} else if (item instanceof VirtualBone) {
			for (Bone b : ((VirtualBone)item).realBones.values()) {
				b.name = item.getName();
				dataLoader.saveBone(b);
			}
		}
	}

	public void setPerspective(Skeleton s)
	{
		dataLoader.setPerspective(selectedPosition.get(), s);
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
		d.setSlot(slot.realSlots.get(perspective));
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

	public void saveBone(Bone b) {
		dataLoader.saveBone(b);
	}

	public void saveDiagram(Diagram d) {
		dataLoader.saveDiagram(d);
	}

	public void authenticate() {
		dataLoader.authenticate();
	}

	public void deleteSlot(VirtualSlot vSlot) {
		vSlot.realSlots.values().forEach(slot -> dataLoader.deleteSlot(slot));
		vSlot.bone.removeSlot(vSlot);
		skeletonChanged();
	}

	public void deleteBone(VirtualBone vBone) {
		vBone.realBones.values().forEach(bone -> dataLoader.deleteBone(bone));
		vBone.skeleton.removeBone(vBone);
		skeletonChanged();
	}

	public VirtualSlot getVirtualSlot(String slotName) {
		for (VirtualBone b : masterSkeleton.get().bones.values()) {
			if (b.hasSlot(slotName)) {
				return b.slots.get(slotName);
			}
		}
		return null;
	}

	public VirtualDiagram getVirtualDiagram(String diagramName) {
		for (VirtualBone b : masterSkeleton.get().bones.values()) {
			for (VirtualSlot s : b.slots.values()) {
				if (s.hasDiagram(diagramName)) {
					return s.diagrams.get(diagramName);
				}
			}
		}
		return null;
	}

}
