package com.fumbbl.iconcomposer.model;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoDiagram;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoRoster;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoRuleset;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSkeleton;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSkin;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSlot;
import com.fumbbl.iconcomposer.model.types.*;
import com.fumbbl.iconcomposer.image.SVGLoader;
import com.kitfox.svg.SVGDiagram;

import javafx.application.Platform;

import javax.imageio.ImageIO;

public class Model {
	private DataStore dataStore;
	
	private Config cfg;
	private DataLoader dataLoader;
	private SVGLoader svgLoader;
	
	private Controller controller;
    
	public Model() {
		dataStore = new DataStore();
		cfg = new Config();
		svgLoader = new SVGLoader();
		dataLoader = new DataLoader(cfg);
	}
	
	public Config getConfig() {
		return cfg;
	}

	private Runnable importPng(Path path) throws FileNotFoundException, IOException {
		String fileId = stripExtension(path.getFileName());

		BufferedImage image = ImageIO.read(path.toFile());

		dataStore.addImage(new NamedPng(fileId, image));
		return new Runnable() {
			@Override
			public void run() {
				controller.onImagesChanged(dataStore.getImages());
			}
		};
	}
	private Runnable importSvg(Path path) throws FileNotFoundException, IOException {
		String fileId = stripExtension(path.getFileName());

		SVGDiagram svg = svgLoader.loadSVG(path);
		
		dataStore.addImage(new NamedSVG(fileId, svg));
		return new Runnable() {

			@Override
			public void run() {
				controller.onImagesChanged(dataStore.getImages());
			}
		};
	}
	
	private String stripExtension(Path filename) {
		String s = filename.toString();
		
		return s.substring(0, s.lastIndexOf('.'));
	}

	public void loadRoster(int rosterId) {
		dataStore.clearSkeletons();
		dataStore.clearDiagrams();
		dataStore.clearSkins();
		dataStore.clearSlots();
		dataStore.clearPosition();
		
		DtoRoster roster = dataLoader.getRoster(rosterId);
		
		controller.onPositionsChanged(roster.toRoster().positions);
	}

	public void loadPosition(int positionId) {
		dataStore.clearSkeletons();
		dataStore.clearDiagrams();
		dataStore.clearSkins();
		
		dataStore.setPosition(dataLoader.getPosition(positionId).toPosition());
		
		controller.onPositionChanged(dataStore.getPosition());
	}
	
	public Collection<Ruleset> loadRulesets() {
		Collection<DtoRuleset> list = dataLoader.getRulesets();
		Set<Ruleset> rulesets = new HashSet<Ruleset>();
		list.forEach(r -> rulesets.add(r.toRuleset()));
		return rulesets;
	}
	
	public void loadRuleset(int rulesetId) {
		dataStore.setRuleset(dataLoader.getRuleset(rulesetId).toRuleset());
		controller.onRulesetLoaded(dataStore.getRuleset());
	}
	
	public void loadSkeletons(int positionId) {
		Collection<DtoSkeleton> list = dataLoader.getSkeletons(positionId);
		Collection<Skeleton> skeletons = new LinkedList<Skeleton>();
		
		list.forEach(s -> skeletons.add(s.toSkeleton())); 
		
		dataStore.setSkeletons(skeletons);
		controller.onSkeletonsChanged(dataStore.getSkeletons());
	}
	
	public void loadDiagrams(int skeletonId) {
		Map<Integer,Diagram> diagramMap = new HashMap<Integer,Diagram>();
		
		Collection<DtoDiagram> diagrams = dataLoader.getDiagrams(skeletonId);
		
		diagrams.forEach(d -> diagramMap.put(d.id, d.toDiagram()));
		
		for (DtoDiagram d : diagrams) {
			Diagram diagram = diagramMap.get(d.id);
			diagram.setSlot(dataStore.getSlot(d.slotId));
			diagram.templateColours = new ColourTheme("template");
		}
		dataStore.setDiagrams(diagramMap.values());
		controller.onDiagramsChanged(diagramMap.values());
	}
	
	public void loadSkins(int positionId) {
		Collection<DtoSkin> list = dataLoader.getSkins(positionId);

		Map<Integer,Skin> skinMap = new HashMap<Integer,Skin>();
		
		list.forEach(s -> skinMap.put(s.id, s.toSkin()));
		
		for (DtoSkin s : list) {
			Skin skin = skinMap.get(s.id);
			skin.skeleton = dataStore.getSkeleton(s.skeletonId);
		}
		
		dataStore.setSkins(skinMap.values());
		controller.onSkinsChanged(skinMap.values());
	}

	public Collection<Bone> loadBones(Skeleton skeleton) {
		Collection<Bone> bones = dataLoader.getBones(skeleton.id);

		bones.forEach(b -> b.setSkeleton(skeleton));
		dataStore.setBones(bones);
		
		return bones;
	}

	public Collection<Slot> loadSlots(Skeleton skeleton) {
		Map<Integer,Slot> slots = new HashMap<Integer,Slot>();
		Collection<DtoSlot> list = dataLoader.getSlots(skeleton.id);

		list.forEach(s -> slots.put(s.id, s.toSlot()));
		
		for (DtoSlot s : list) {
			Slot slot = slots.get(s.id);
			slot.setBone(dataStore.getBone(s.boneId));
			slot.setSkeleton(skeleton);
		}
		
		dataStore.setSlots(slots.values());
		return slots.values();
	}

	public void addDiagram(NamedImage image) {
		Diagram d = new Diagram(image);
		dataStore.addDiagram(image.getName(), d);
		controller.onDiagramsChanged(dataStore.getDiagrams());
	}
	
	public void setupThemes() {
		ColourTheme homeColours = new ColourTheme("home");
		homeColours.setColour(ColourType.PRIMARY, 255, 0, 0);
		homeColours.setColour(ColourType.PRIMARYHI, 255, 128, 128);
		homeColours.setColour(ColourType.PRIMARYLO, 128, 0, 0);
		
		homeColours.setColour(ColourType.SECONDARY, 166, 172, 186);
		homeColours.setColour(ColourType.SECONDARYHI, 183, 190, 201);
		homeColours.setColour(ColourType.SECONDARYLO, 139, 141, 153);

		ColourTheme awayColours = new ColourTheme("away");
		awayColours.setColour(ColourType.PRIMARY, 0, 0, 255);
		awayColours.setColour(ColourType.PRIMARYHI, 128, 128, 255);
		awayColours.setColour(ColourType.PRIMARYLO, 0, 0, 128);
		
		awayColours.setColour(ColourType.SECONDARY, 239, 193, 112);
		awayColours.setColour(ColourType.SECONDARYHI, 194, 153, 92);
		awayColours.setColour(ColourType.SECONDARYLO, 141, 109, 72);

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

	public Diagram getDiagram(String image) {
		return dataStore.getDiagram(image);
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
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					Path p = Paths.get(path);
					PathMatcher jsonMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
					PathMatcher svgMatcher = FileSystems.getDefault().getPathMatcher("glob:**.svg");
					PathMatcher pngMatcher = FileSystems.getDefault().getPathMatcher("glob:**.png");

					if (!Files.isReadable(p)) {
						return;
					}
					
					List<Runnable> delayedTasks = new LinkedList<Runnable>();
					
					long size = Files.size(p);
					if (size > 0 && size < 1024*1024*10) {
						byte[] bytes = Files.readAllBytes(p);
						
						if (jsonMatcher.matches(p)) {
							controller.onProgressStart("Importing Spine");
							SpineImporter importer = new SpineImporter();
							importer.importSkeleton(new String(bytes));

							Position pos = dataStore.getPosition();
							Skeleton skeleton = importer.getSkeleton();

							dataStore.addSkeleton(skeleton);
							dataStore.setSlots(skeleton.getSlots());
							dataStore.setBones(skeleton.getBones());

							controller.startBatch();
							dataLoader.saveSkeleton(pos, skeleton);
							dataLoader.saveBones(skeleton);
							dataLoader.saveSlots(skeleton);

							Collection<Diagram> diagrams = importer.getDiagrams();
							dataStore.setDiagrams(diagrams);
							diagrams.forEach(d -> dataLoader.saveDiagram(d));

							Collection<Skin> skins = importer.getSkins();
							dataStore.setSkins(skins);
							skins.forEach(s -> dataLoader.saveSkin(pos, s));

							controller.runBatch();

							delayedTasks.add(new Runnable() {
								@Override
								public void run() {
									controller.onSkeletonsChanged(dataStore.getSkeletons());
									controller.onSkeletonChanged(skeleton);
									controller.onBonesChanged(skeleton.getBones());
									controller.onSlotsChanged(skeleton.getSlots());
									controller.onDiagramsChanged(diagrams);
									controller.onSkinsChanged(skins);
								}
							});
						} else if (pngMatcher.matches(p)) {
							delayedTasks.add(importPng(p));
						} else if (svgMatcher.matches(p)) {
							delayedTasks.add(importSvg(p));
						}
					}

					controller.runInBackground(new Runnable() {
						@Override
						public void run() {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									for (Runnable task : delayedTasks) {
										task.run();
									}
								}						
							});
						}
					});
				} catch (IOException e) {
					controller.onProgress(1, true);
				}
			}
		};
		
		controller.runInBackground(task);
	}

	public SVGDiagram getSvg(String svgName) {
		NamedItem image = dataStore.getImage(svgName);
		if (image != null && image instanceof NamedSVG) {
			return ((NamedSVG)image).diagram;
		}
		return null;
	}
	
	public void deleteSkeleton(Skeleton skeleton) {
		if (skeleton != null) {
			dataStore.removeSkeleton(skeleton);
			dataLoader.deleteSkeleton(skeleton);
			controller.onSkeletonChanged(null);
			controller.onSkeletonsChanged(dataStore.getSkeletons());
		}
	}
	
	public void createSkin(Skeleton skeleton) {
		Skin s = new Skin();
		s.id = -1;
		s.skeleton = skeleton;
		s.name = "NewSkin";
		
		dataLoader.saveSkin(dataStore.getPosition(), s);
		
		dataStore.addSkin(s);
		controller.onSkinsChanged(dataStore.getSkins());
	}

	public void setSkinDiagram(Skin skin, Slot slot, Diagram diagram) {
		if (diagram != null) {
			skin.setDiagram(slot, diagram);
		} else {
			skin.removeDiagram(slot);
		}
	}

	public Collection<Slot> getSlots() {
		return dataStore.getSlots();
	}

	public void onItemRenamed(NamedItem item) {
		if (item instanceof Skin) {
			dataLoader.saveSkin(dataStore.getPosition(), (Skin)item);
			controller.onSkinsChanged(dataStore.getSkins());
		} else if (item instanceof Skeleton) {
			dataLoader.saveSkeleton(dataStore.getPosition(), (Skeleton)item);
			controller.onSkeletonsChanged(dataStore.getSkeletons());
		} else if (item instanceof NamedSVG) {
			controller.onImagesChanged(dataStore.getImages());
		}
	}
}
