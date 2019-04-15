package com.fumbbl.iconcomposer.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.DtoPosition;
import com.fumbbl.iconcomposer.dto.DtoRoster;
import com.fumbbl.iconcomposer.dto.DtoRuleset;
import com.fumbbl.iconcomposer.model.types.Bone;
import com.fumbbl.iconcomposer.model.types.Diagram;
import com.fumbbl.iconcomposer.model.types.Skeleton;
import com.fumbbl.iconcomposer.model.types.Skin;
import com.fumbbl.iconcomposer.model.types.SkinCollection;
import com.fumbbl.iconcomposer.model.types.Slot;
import com.fumbbl.iconcomposer.model.types.SlotData;
import com.fumbbl.iconcomposer.model.types.Spine;
import com.fumbbl.iconcomposer.svg.SVGLoader;
import com.google.gson.Gson;
import com.kitfox.svg.SVGDiagram;

import javafx.application.Platform;

public class Model {
	private DataStore dataStore;
	
	private Config cfg;
	private DataLoader dataLoader;
	private SVGLoader svgLoader;
	
	private Controller controller;
    
	private DtoRoster roster;

	public Model() {
		dataStore = new DataStore();
		cfg = new Config();
		svgLoader = new SVGLoader();
		dataLoader = new DataLoader(cfg);
	}
	
	public Config getConfig() {
		return cfg;
	}
	
	private Runnable importSkeleton(String json) {
		Gson gson = new Gson();
		Spine data = gson.fromJson(json,  Spine.class);
		Skeleton skeleton = new Skeleton();

		organizeBones(data.bones);
		
		skeleton.setBones(data.bones);
		skeleton.setSlots(data.slots);
		skeleton.name = "Imported";
		
		DtoPosition position = dataStore.getPosition();
		if (position != null) {
			skeleton.id = dataLoader.saveSkeleton(position.id, skeleton);
			dataLoader.saveBones(skeleton);
			skeleton.updateSlots();
			dataLoader.saveSlots(skeleton);
		}

		dataStore.addSkeleton(skeleton);
		dataStore.setSlots(data.slots);

		importSkins(data.skins, skeleton);

		return new Runnable() {
			@Override
			public void run() {
				controller.onDiagramsChanged(dataStore.getDiagrams());
				controller.onSkinsChanged(dataStore.getSkins());
				controller.onBonesChanged(data.bones);
				controller.onSlotsChanged(data.slots);
				controller.onSkeletonsChanged(dataStore.getSkeletons());
				controller.onSkeletonChanged(skeleton);
			}
		};
	}
	
	private void importSkins(SkinCollection skins, Skeleton skeleton) {
		Set<String> storedDiagrams = new HashSet<String>();
		for (Entry<String, Skin>entry : skins.entrySet()) {
			Skin s = entry.getValue();
			s.skeleton = skeleton;
			s.skeletonId = -1;
			s.name = entry.getKey();
			if (!"default".equals(s.name)) {
				dataStore.addSkin(-1, s);
			}
			
			DtoPosition position = dataStore.getPosition();
			for(Entry<String,SlotData> slotEntry : s.entrySet()) {
				SlotData d = slotEntry.getValue();
				for (Entry<String,Diagram> attachmentEntry : d.entrySet()) {
					Diagram diagram = attachmentEntry.getValue();
					diagram.attachmentName = attachmentEntry.getKey();
					diagram.setSlot(dataStore.getSlot(slotEntry.getKey()));
					if (position != null && !storedDiagrams.contains(diagram.getImage())) {
						storedDiagrams.add(diagram.getImage());
						dataLoader.saveDiagram(diagram);
					}
					dataStore.addDiagram(diagram.getImage(), diagram);
				}
			}
		}
	}

	private Runnable importSvg(Path path) throws FileNotFoundException, IOException {
		String fileId = stripExtension(path.getFileName());

		SVGDiagram svg = svgLoader.loadSVG(path);
		
		dataStore.addSvg(new NamedSVG(fileId, svg));
		return new Runnable() {

			@Override
			public void run() {
				controller.onImagesChanged(dataStore.getSvgs());
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
		
		roster = dataLoader.getRoster(rosterId);
		controller.onPositionsChanged(roster.positions);
	}

	public void loadPosition(int positionId) {
		dataStore.clearSkeletons();
		dataStore.clearDiagrams();
		dataStore.clearSkins();
		
		dataStore.setPosition(dataLoader.getPosition(positionId));
		
		controller.onPositionChanged(dataStore.getPosition());
	}
	
	public DtoRuleset[] loadRulesets() {
		return dataLoader.getRulesets();
	}
	
	public void loadRuleset(int rulesetId) {
		dataStore.setRuleset(dataLoader.getRuleset(rulesetId));
		
		controller.onRulesetLoaded(dataStore.getRuleset());
	}
	
	public void loadSkeletons(int positionId) {
		Collection<Skeleton> skeletons = dataLoader.getSkeletons(positionId);
		
		dataStore.setSkeletons(skeletons);

		controller.onSkeletonsChanged(dataStore.getSkeletons());
	}
	
	public void loadDiagrams(int skeletonId) {
		Collection<Diagram> diagrams = dataLoader.getDiagrams(skeletonId);
		for (Diagram d : diagrams) {
			d.templateColours = new ColourTheme("template");
			d.setSlot(dataStore.getSlot(d.slotId));
			d.attachmentName = d.name;
		}
		dataStore.setDiagrams(diagrams);
		controller.onDiagramsChanged(diagrams);
	}
	
	public void loadSkins(int positionId) {
		Collection<Skin> skins = dataLoader.getSkins(positionId);
		
		for (Skin s : skins) {
			s.skeleton = dataStore.getSkeleton(s.skeletonId);
		}
		dataStore.setSkins(skins);
		controller.onSkinsChanged(skins);
	}

	public Collection<Bone> loadBones(int skeletonId) {
		Collection<Bone> bones = dataLoader.getBones(skeletonId);
		
		organizeBones(bones);
		
		return bones;
	}

	private void organizeBones(Collection<Bone> bones) {
		dataStore.setBones(bones);
		
		for (Bone b : bones) {
			if (b.parentId > 0) {
				Bone parentBone = dataStore.getBone(b.parentId);
				b.parent = parentBone.name;
				b.parentBone = parentBone;
			} else if (b.parent != null) {
				b.parentBone = dataStore.getBone(b.parent);
			}
			if (b.parentBone != null) {
				b.parentBone.addChildBone(b);
			}
		}
	}


	public Collection<Slot> loadSlots(int skeletonId) {
		Collection<Slot> slots = dataLoader.getSlots(skeletonId);
		
		for (Slot s : slots) {
			s.bone = dataStore.getBone(s.boneId).name;
		}
		
		dataStore.setSlots(slots);
		
		return slots;
	}

	public void addDiagram(NamedSVG svg) {
		Diagram d = new Diagram(svg);
		dataStore.addDiagram(svg.name, d);
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
	}

	public Spine getSpine() {
		return dataStore.getSpine();
	}

	public Diagram getDiagram(String image) {
		return dataStore.getDiagram(image);
	}

	public ColourTheme getColourTheme(String theme) {
		return dataStore.getColourTheme(theme);
	}

	public void setRoster(DtoRoster r) {
		this.roster = r;
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
		controller.onImportStart();
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					Path p = Paths.get(path);
					PathMatcher jsonMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
					PathMatcher svgMatcher = FileSystems.getDefault().getPathMatcher("glob:**.svg");

					if (!Files.isReadable(p)) {
						return;
					}
					
					List<Runnable> delayedTasks = new LinkedList<Runnable>();
					
					long size = Files.size(p);
					if (size > 0 && size < 1024*1024*10) {
						byte[] bytes = Files.readAllBytes(p);
						
						if (jsonMatcher.matches(p)) {
							delayedTasks.add(importSkeleton(new String(bytes)));
						} else if (svgMatcher.matches(p)) {
							delayedTasks.add(importSvg(p));
						}
					}
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							for (Runnable task : delayedTasks) {
								task.run();
							}
							controller.onImportComplete();
						}						
					});
				} catch (IOException e) {
					controller.onImportComplete();
				}
			}
		};
		
		controller.runInBackground(task);
	}

	public SVGDiagram getSvg(String svgName) {
		NamedSVG svg = dataStore.getSvg(svgName);
		if (svg != null) {
			return svg.diagram;
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
		
		dataStore.addSkin(-1, s);
		controller.onSkinsChanged(dataStore.getSkins());
	}

	public void setSkinDiagram(Skin skin, Slot slot, Diagram diagram) {
		SlotData sd = new SlotData();
		if (diagram != null) {
			sd.put(slot.attachment, diagram);
			skin.put(slot.name, sd);
		} else {
			sd.remove(slot.name);
		}
	}
}
