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
	private Map<Integer,Map<String,Slot>> slotsByName;
	private Map<Integer,Bone> bones;
	private Map<Integer,Slot> slots;

	public DataStore() {
		//skins = new HashMap<Integer,Skin>();
		skeletons = new LinkedList<Skeleton>();
		diagrams = new HashMap<>();
		colourThemes = new HashMap<String,ColourTheme>();
		images = new HashMap<>();
		slotsByName = new HashMap<>();
		skins = new HashMap<Integer,Skin>();
		bones = new HashMap<Integer,Bone>();
		slots = new HashMap<Integer, Slot>();
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

	public void clearImages() {
		images.clear();
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
		this.diagrams.clear();
		this.addDiagrams(skeletonId, newDiagrams);
	}

	public void addDiagrams(int skeletonId, Collection<Diagram> newDiagrams) {
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
	 * Slot
	 */

	private Map<String, Slot> getSlotsForSkeleton(int skeletonId) {
		Map<String, Slot> result;
		if (!slotsByName.containsKey(skeletonId)) {
			result = new HashMap<>();
			slotsByName.put(skeletonId, result);
		} else {
			result = slotsByName.get(skeletonId);
		}

		return result;
	}

	public Slot getSlot(int skeletonId, String slotName) {
		return getSlotsForSkeleton(skeletonId).get(slotName);
	}

	public Slot getSlot(int slotId) {
		return slots.get(slotId);
	}

	public void setSlots(Collection<Slot> slots) {
		this.slotsByName.clear();
		this.slots.clear();
		this.addSlots(slots);
	}

	public void addSlots(Collection<Slot> slots) {
		slots.forEach(s -> {
			int skeletonId = s.getSkeleton().id;
			if (!this.slotsByName.containsKey(skeletonId)) {
				this.slotsByName.put(skeletonId, new HashMap<>());
			}
			this.slotsByName.get(skeletonId).put(s.getName(), s);
			this.slots.put(s.id, s);
		});
	}

	public void clearSlots() {
		slotsByName.clear();
	}

	/*
	 * Bone
	 */
	
	public Bone getBone(int boneId) {
		return bones.get(boneId);
	}

	public void setBones(Collection<Bone> bones) {
		clearBones();
		addBones(bones);
	}

	public void addBones(Collection<Bone> bones) {
		bones.forEach(b -> this.bones.put(b.id, b));
	}

	public void clearBones() {
		this.bones.clear();
	}

	/*
	 * Skeleton 
	 */
	
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
