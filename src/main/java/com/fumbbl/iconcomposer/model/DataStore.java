package com.fumbbl.iconcomposer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.Diagram;
import com.fumbbl.iconcomposer.spine.Bone;
import com.fumbbl.iconcomposer.spine.Skeleton;
import com.fumbbl.iconcomposer.spine.Skin;
import com.fumbbl.iconcomposer.spine.Slot;
import com.fumbbl.iconcomposer.spine.Spine;
import com.google.gson.annotations.Expose;

public class DataStore {
	private Spine spine;
	//private Map<Integer,Skin> skins;
	private List<Skeleton> skeletons;
	@Expose
	private Map<String,Diagram> diagrams;
	@Expose
	private Map<String,ColourTheme> colourThemes;
	
	private Map<String,NamedSVG> svgs;
	
	public DataStore() {
		spine = new Spine();
		//skins = new HashMap<Integer,Skin>();
		skeletons = new LinkedList<Skeleton>();
		diagrams = new HashMap<String,Diagram>();
		colourThemes = new HashMap<String,ColourTheme>();
		svgs = new HashMap<String,NamedSVG>();
	}

	public Spine getSpine() {
		return spine;
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
	 * SVG
	 */
	
	public void addSvg(NamedSVG svg) {
		svgs.put(svg.name, svg);
	}
	
	public Collection<NamedSVG> getSvgs() {
		return svgs.values();
	}

	public NamedSVG getSvg(String name) {
		return svgs.get(name);
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

	/*
	 * Skin
	 */
	
	public Collection<String> getSkinNames() {
		return spine.skins.keySet();
	}

	public Skin getSkinData(String skinName) {
		return spine.getSkin(skinName);
	}

	public Skin getSkin(int skinId) {
		return spine.getSkin(skinId);
	}

	public void addSkin(int id, Skin skin) {
		spine.addSkin(id, skin);
	}

	public void clearSkins() {
		spine.clearSkins();
	}
	
	public Collection<Skin> getSkins() {
		return spine.skins.values();
	}
	
	public void setSkins(Collection<Skin> skins) {
		spine.clearSkins();
		for (Skin skin : skins) {
			spine.addSkin(-1, skin);
		}
	}

	/*
	 * Slot
	 */

	public void setSlots(Collection<Slot> slots) {
		spine.slots = slots;
	}

	public Slot getSlot(int slotId) {
		return spine.getSlot(slotId);
	}

	public Collection<Slot> getSlots() {
		return spine.slots;
	}

	/*
	 * Bone
	 */
	
	public Bone getBone(int boneId) {
		return spine.getBone(boneId);
	}

	public Bone getBone(String boneName) {
		return spine.getBone(boneName);
	}

	public void setBones(Collection<Bone> bones) {
		spine.bones = bones;
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
}
