package com.fumbbl.iconcomposer.model;

import java.util.*;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.model.types.*;
import com.google.gson.annotations.Expose;

public class DataStore {
	//private Map<Integer,Skin> skins;
	private List<Skeleton> skeletons;
	@Expose
	private Map<Integer, Map<String,Diagram>> diagrams;
	@Expose
	private Map<String,ColourTheme> colourThemes;

	private Map<String, NamedPng> images;
	private Ruleset ruleset;
	private Position position;
	
	private Map<Integer,Skin> skins;
	private Map<Integer,Slot> slots;
	private Map<Integer,Bone> bones;
	
	public DataStore() {
		//skins = new HashMap<Integer,Skin>();
		skeletons = new LinkedList<Skeleton>();
		diagrams = new HashMap<>();
		colourThemes = new HashMap<String,ColourTheme>();
		images = new HashMap<>();
		skins = new HashMap<Integer,Skin>();
		slots = new HashMap<Integer,Slot>();
		bones = new HashMap<Integer,Bone>();
	}

	/*
	 * ColourTheme
	 */
	public ColourTheme getColourTheme(String theme) {
		return colourThemes.get(theme);
	}

	public Collection<ColourTheme> getColourThemes() {
		return colourThemes.values();
	}

	public void addColourTheme(String name, ColourTheme theme) {
		colourThemes.put(name, theme);
	}

	/*
	 * Image
	 */

	public void addImage(NamedPng image) {
		images.put(image.getName().toLowerCase(), image);
	}

	public Collection<NamedImage> getImages() {
		return images.values().stream().collect(Collectors.toList());
	}

	public NamedItem getImage(String name) {
		return images.get(name.toLowerCase());
	}

	/*
	 * Diagram
	 */

	private Map<String, Diagram> getDiagramsForSkeleton(int skeletonId) {
		Map<String, Diagram> result;
		if (!diagrams.containsKey(skeletonId)) {
			result = new HashMap<>();
			diagrams.put(skeletonId, result);
		} else {
			result = diagrams.get(skeletonId);
		}

		return result;
	}

	public Diagram getDiagram(int skeletonId, String image) {
		return getDiagramsForSkeleton(skeletonId).get(image.toLowerCase());
	}
	
	public void addDiagram(Skeleton skeleton, String name, Diagram diagram) {
		getDiagramsForSkeleton(skeleton.id).put(name.toLowerCase(), diagram);
	}

	public Collection<String> getDiagramNames() {
		HashSet<String> result = new HashSet<>();

		diagrams.forEach((skeletonId, map) -> result.addAll(map.keySet()));
		return result;
	}

	public void clearDiagrams() {
		diagrams.clear();
	}

	public void setDiagrams(int skeletonId, Collection<Diagram> newDiagrams) {
		Map<String,Diagram> skeletonDiagrams = getDiagramsForSkeleton(skeletonId);

		newDiagrams.forEach(d -> skeletonDiagrams.put(d.name.toLowerCase(), d));
	}

	private void renameDiagramImages(String prefix, String oldName, String newName) {
		String oldKey = prefix+oldName.toLowerCase();
		String newKey = prefix+newName.toLowerCase();

		if (images.containsKey(oldKey)) {
			NamedPng image = images.remove(oldKey);
			image.setName(prefix+newName);
			images.put(newKey, image);
		}
	}

	public void renameDiagramImages(String oldName, String newName) {
		renameDiagramImages("front_", oldName, newName);
		renameDiagramImages("side_", oldName, newName);
	}

	public void renameImage(String oldName, String newName) {
		String oldKey = oldName.toLowerCase();
		String newKey = newName.toLowerCase();

		if (images.containsKey(oldKey)) {
			NamedPng image = images.remove(oldKey);
			image.setName(newName);
			images.put(newKey, image);
		}
	}

	public void renameDiagram(String oldName, String name) {
		String oldKey = oldName.toLowerCase();
		String newKey = name.toLowerCase();
		diagrams.forEach((skeletonId, map) -> {
			if (map.containsKey(oldKey)) {
				Diagram d = map.remove(oldKey);
				d.setName(name);
				map.put(newKey, d);
			}
		});
	}

	/*
	 * Skin
	 */
	
	public Collection<String> getSkinNames() {
		return skins.values().stream().map(s -> s.name).collect(Collectors.toList());
	}

	public Skin getSkin(int skinId) {
		return skins.get(skinId);
	}

	public void addSkin(Skin skin) {
		skins.put(skin.id, skin);
	}

	public void clearSkins() {
		skins.clear();
	}
	
	public Collection<Skin> getSkins() {
		return skins.values();
	}
	
	public void setSkins(Collection<Skin> skins) {
		this.skins.clear();
		skins.forEach(s -> this.skins.put(s.id, s));
	}

	/*
	 * Slot
	 */

	public void setSlots(Collection<Slot> slots) {
		this.slots.clear();
		slots.forEach(s -> this.slots.put(s.id, s));
	}

	public Slot getSlot(int slotId) {
		return slots.get(slotId);
	}

	public Collection<Slot> getSlots() {
		return slots.values();
	}
	
	public void clearSlots() {
		slots.clear();
	}

	/*
	 * Bone
	 */
	
	public Bone getBone(int boneId) {
		return bones.get(boneId);
	}

	public void setBones(Collection<Bone> bones) {
		this.bones.clear();
		bones.forEach(b -> this.bones.put(b.id, b));
	}

	/*
	 * Skeleton 
	 */
	
	public void clearSkeletons() {
		skeletons.clear();
	}

	public void addSkeleton(Skeleton s) {
		skeletons.add(s);
	}

	public Collection<Skeleton> getSkeletons() {
		return skeletons;
	}
	
	public void setSkeletons(Collection<Skeleton> skeletons) {
		this.skeletons.clear();
		if (skeletons != null) {
			this.skeletons.addAll(skeletons);
		}
	}
	
	public void removeSkeleton(Skeleton skeleton) {
		skeletons.remove(skeleton);
	}
	
	public Skeleton getSkeleton(int skeletonId) {
		for (Skeleton s : skeletons) {
			if (s.id == skeletonId) {
				return s;
			}
		}
		return null;
	}

	/*
	 * Ruleset 
	 */
	
	public void setRuleset(Ruleset ruleset) {
		this.ruleset = ruleset;
	}
	
	public Ruleset getRuleset() {
		return ruleset;
	}

	/*
	 * Position
	 */
	
	public void setPosition(Position position) {
		this.position = position;
	}
	
	public Position getPosition() {
		return this.position;
	}

	public void clearPosition() {
		this.position = null;
	}
}
