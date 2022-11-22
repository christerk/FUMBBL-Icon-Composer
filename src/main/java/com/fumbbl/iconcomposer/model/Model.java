package com.fumbbl.iconcomposer.model;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoDiagram;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoRoster;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoRuleset;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSkeleton;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSlot;
import com.fumbbl.iconcomposer.model.types.*;

import javafx.application.Platform;

import javax.imageio.ImageIO;

public class Model {
	private DataStore dataStore;
	
	private Config cfg;
	private DataLoader dataLoader;

	private Controller controller;
    
	public Model() {
		dataStore = new DataStore();
		cfg = new Config();
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
		
		list.forEach(s -> {
			Skeleton skeleton = s.toSkeleton();
			skeletons.add(skeleton);
		});
		
		dataStore.setSkeletons(skeletons);
		controller.onSkeletonsChanged(dataStore.getSkeletons());
	}
	
	public void loadDiagrams(Perspective perspective, Skeleton skeleton) {
		Map<Integer,Diagram> diagramMap = new HashMap<Integer,Diagram>();
		
		Collection<DtoDiagram> diagrams = dataLoader.getDiagrams(skeleton);

		diagrams.forEach(d -> {
			Diagram diagram = d.toDiagram(perspective);
			diagramMap.put(d.id, d.toDiagram(perspective));
		});
		
		for (DtoDiagram d : diagrams) {
			Diagram diagram = diagramMap.get(d.id);
			diagram.setSlot(dataStore.getSlot(d.slotId));
		}

		dataStore.setDiagrams(skeleton.id, diagramMap.values());
		controller.onDiagramsChanged(diagramMap.values());
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
		//dataStore.addDiagram(image.getName(), d);
		//controller.onDiagramsChanged(dataStore.getDiagramNames());
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
							try {
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
								dataStore.setDiagrams(skeleton.id, diagrams);
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
									}
								});

							} catch (Exception e) {
								controller.getMainController().onProgressComplete();
							}
						} else if (pngMatcher.matches(p)) {
							delayedTasks.add(importPng(p));
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

	public BufferedImage getImage(String imageName) {
		NamedItem image = dataStore.getImage(imageName.toLowerCase());
		if (image != null && image instanceof NamedPng) {
			return ((NamedPng)image).image;
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

	public void setSkinDiagram(Skin skin, Slot slot, VirtualDiagram diagram) {
		if (diagram != null) {
			skin.setDiagram(slot, diagram);
		} else {
			skin.removeDiagram(slot);
		}
	}

	public Collection<Slot> getSlots() {
		return dataStore.getSlots();
	}

	public void onItemRenamed(NamedItem item, String oldName) {
		if (item instanceof Skeleton) {
			dataLoader.saveSkeleton(dataStore.getPosition(), (Skeleton)item);
			controller.onSkeletonsChanged(dataStore.getSkeletons());
		} else if (item instanceof NamedImage) {
			dataStore.renameImage(oldName, item.getName());
			controller.onImagesChanged(dataStore.getImages());
		} else if (item instanceof Slot) {
			controller.getMainController().refreshDiagrams();
		} else if (item instanceof VirtualDiagram) {
			dataStore.renameDiagram(oldName, item.getName());
			dataStore.renameDiagramImages(oldName, item.getName());
			controller.getMainController().refreshDiagrams();
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
}
