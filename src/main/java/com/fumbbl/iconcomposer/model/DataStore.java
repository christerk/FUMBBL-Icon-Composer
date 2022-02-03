package com.fumbbl.iconcomposer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.model.types.*;
import com.google.gson.annotations.Expose;

public class DataStore {
	//private Map<Integer,Skin> skins;
	private List<Skeleton> skeletons;
	@Expose
	private Map<String,Diagram> diagrams;
	@Expose
	private Map<String,ColourTheme> colourThemes;

	private Map<String, NamedImage> images;
	private Ruleset ruleset;
	private Position position;
	
	private Map<Integer,Skin> skins;
	private Map<Integer,Slot> slots;
	private Map<Integer,Bone> bones;
	
	public DataStore() {
		//skins = new HashMap<Integer,Skin>();
		skeletons = new LinkedList<Skeleton>();
		diagrams = new HashMap<String,Diagram>();
		colourThemes = new HashMap<String,ColourTheme>();
		images = new HashMap<String,NamedImage>();
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

	public void addImage(NamedImage image) {
		images.put(image.getName(), image);
	}

	public Collection<NamedImage> getImages() {
		return images.values();
	}

	public NamedItem getImage(String name) {
		return images.get(name);
	}

	/*
	 * Diagram
	 */
	
	public Diagram getDiagram(String image) {
		return diagrams.get(image);
	}
	
	public void addDiagram(String name, Diagram diagram) {
		diagrams.put(name, diagram);
	}

	public Collection<String> getDiagramNames() {
		return diagrams.keySet();
	}
	
	public Collection<Diagram> getDiagrams() {
		return diagrams.values();
	}

	public void clearDiagrams() {
		diagrams.clear();
	}

	public void setDiagrams(Collection<Diagram> diagrams) {
		this.diagrams.clear();
		for (Diagram d : diagrams) {
			this.diagrams.put(d.getName(), d);
		}
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
