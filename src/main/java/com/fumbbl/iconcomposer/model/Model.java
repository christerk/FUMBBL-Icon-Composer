package com.fumbbl.iconcomposer.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map.Entry;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.Diagram;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.DtoPosition;
import com.fumbbl.iconcomposer.dto.DtoRoster;
import com.fumbbl.iconcomposer.dto.DtoRuleset;
import com.fumbbl.iconcomposer.spine.Attachment;
import com.fumbbl.iconcomposer.spine.Bone;
import com.fumbbl.iconcomposer.spine.Skeleton;
import com.fumbbl.iconcomposer.spine.Skin;
import com.fumbbl.iconcomposer.spine.SkinCollection;
import com.fumbbl.iconcomposer.spine.Slot;
import com.fumbbl.iconcomposer.spine.SlotData;
import com.fumbbl.iconcomposer.spine.Spine;
import com.fumbbl.iconcomposer.svg.SVGLoader;
import com.google.gson.Gson;
import com.kitfox.svg.SVGDiagram;

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
	
	private void importSkeleton(String json) {
		Gson gson = new Gson();
		Spine data = gson.fromJson(json,  Spine.class);
		Skeleton skeleton = new Skeleton();

		organizeBones(data.bones);
		
		skeleton.setBones(data.bones);
		skeleton.setSlots(data.slots);
		skeleton.id = -1;
		skeleton.name = "Imported";

		dataStore.addSkeleton(skeleton);
		dataStore.setSlots(data.slots);

		importSkins(data.skins, skeleton);
		
		controller.onBonesChanged(data.bones);
		controller.onSlotsChanged(data.slots);
		controller.onSkeletonsChanged(dataStore.getSkeletons());
		controller.onSkeletonChanged(skeleton);
	}
	
	private void importSkins(SkinCollection skins, Skeleton skeleton) {
		for (Entry<String, Skin>entry : skins.entrySet()) {
			Skin s = entry.getValue();
			s.skeleton = skeleton;
			s.skeletonId = -1;
			s.name = entry.getKey();
			if (!"default".equals(s.name)) {
				dataStore.addSkin(-1, s);
			}
			
			for(Entry<String,SlotData> slotEntry : s.entrySet()) {
				SlotData d = slotEntry.getValue();
				for (Entry<String,Attachment> attachmentEntry : d.entrySet()) {
					Attachment a = attachmentEntry.getValue();
					String svgName = a.path;
					if (svgName == null) {
						svgName = a.name;
					}
					Diagram diagram = a.toDiagram();
					diagram.setSlot(slotEntry.getKey());
					dataStore.addDiagram(a.getImage(), diagram);
				}
			}
		}
		controller.onDiagramsChanged(dataStore.getDiagrams());
		controller.onSkinsChanged(dataStore.getSkins());
	}

	private void importSvg(Path path) throws FileNotFoundException, IOException {
		String fileId = stripExtension(path.getFileName());

		SVGDiagram svg = svgLoader.loadSVG(path);
		
		dataStore.addSvg(new NamedSVG(fileId, svg));
		controller.onImagesChanged(dataStore.getSvgs());
	}
	
	private String stripExtension(Path filename) {
		String s = filename.toString();
		
		return s.substring(0, s.lastIndexOf('.'));
	}

	public void loadRoster(int rosterId) {
		roster = dataLoader.getRoster(rosterId);
		controller.onPositionsChanged(roster.positions);
	}

	public DtoPosition loadPosition(int positionId) {
		DtoPosition position = dataLoader.getPosition(positionId);
		return position;
	}
	
	public DtoRuleset[] loadRulesets() {
		return dataLoader.getRulesets();
	}
	
	public DtoRuleset loadRuleset(int rulesetId) {
		return dataLoader.getRuleset(rulesetId);
	}
	
	public Collection<Skeleton> loadSkeletons(int positionId) {
		Collection<Skeleton> skeletons = dataLoader.getSkeletons(positionId);
		
		dataStore.clearSkeletons();
		for(Skeleton s : skeletons) {
			dataStore.addSkeleton(s);
		}
		
		return skeletons;
	}
	
	public Collection<Skin> loadSkins(int positionId) {
		Collection<Skin> skins = dataLoader.getSkins(positionId);
		
		for(Skin s : skins) {
			int skinId = s.id;
			
			Collection<Slot> slots = dataLoader.getSlots(s.skeletonId);
			
			Collection<Attachment> attachments = dataLoader.getAttachments(skinId);
			Collection<Skeleton> skeletons = loadSkeletons(positionId);

			dataStore.setSlots(slots);
			
			for (Skeleton skeleton : skeletons) {
				if (skeleton.id == s.skeletonId) {
					s.skeleton = skeleton;
					skeleton.setBones(loadBones(skeleton.id));
					skeleton.setSlots(loadSlots(skeleton.id));
				}
			}
			
			for (Attachment a : attachments) {
				int slotId = a.slotId;

				Slot slot = dataStore.getSlot(slotId);
				
				SlotData sd = new SlotData();
				sd.put(a.name, a);
				s.put(slot.name, sd);

				Diagram d = a.toDiagram();
				d.setSlot(dataStore.getSlot(slotId).name);
				dataStore.addDiagram(a.getImage(), d);
			}
		}

		dataStore.setSkins(skins);
		controller.onSlotsChanged(dataStore.getSlots());
		controller.onDiagramsChanged(dataStore.getDiagrams());
		controller.onSkinsChanged(skins);
		
		return skins;
	}
	
	public Collection<Bone> loadBones(int skeletonId) {
		Collection<Bone> bones = dataLoader.getBones(skeletonId);
		
		organizeBones(bones);
		
		return bones;
	}

	private void organizeBones(Collection<Bone> bones) {
		dataStore.setBones(bones);
		
		for (Bone b : bones) {
			if (b.parentId != 0) {
				Bone parentBone = dataStore.getBone(b.parentId);
				b.parent = parentBone.name;
				b.parentBone = parentBone;
			} else if (b.parent != null) {
				b.parentBone = dataStore.getBone(b.parent);
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
		Diagram d = new Diagram(svg.name);
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

	public void setSlots(Collection<Slot> slots) {
	}

	public void handleDroppedFile(String path) {
		try {
			Path p = Paths.get(path);
			PathMatcher jsonMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
			PathMatcher svgMatcher = FileSystems.getDefault().getPathMatcher("glob:**.svg");

			if (!Files.isReadable(p)) {
				return;
			}
			
			long size = Files.size(p);
			if (size > 0 && size < 1024*1024*10) {
				byte[] bytes = Files.readAllBytes(p);
				
				if (jsonMatcher.matches(p)) {
					importSkeleton(new String(bytes));
				} else if (svgMatcher.matches(p)) {
					importSvg(p);
				}
			}
			
		} catch (IOException e) {
			
		}
	}

	public SVGDiagram getSvg(String svgName) {
		NamedSVG svg = dataStore.getSvg(svgName);
		if (svg != null) {
			return svg.diagram;
		}
		return null;
	}
}
