package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.model.Perspective;
import javafx.beans.Observable;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Skeleton extends NamedItem {
	public int id;
	public String name;
	public Perspective perspective;

	private final ObservableMap<String,Bone> bones;
	private final ObservableMap<String,Slot> slots;
	
	public double x = 0, y = 0;
	public final double scaleX = 1;
	public final double scaleY = 1;
	public double width = 0, height = 0;
	
	public Skeleton() {
		super();
		bones = FXCollections.observableHashMap();
		slots = FXCollections.observableHashMap();
		id = -1;
		perspective = Perspective.Unknown;
	}
	
	public void setBones(Collection<Bone> bones) {
		this.bones.clear();
		
		if (bones != null) {
			for (Bone b : bones) {
				this.bones.put(b.name, b);
				b.setSkeleton(this);
			}
		}
	}
	
	public Bone getBone(String boneName) {
		return bones.get(boneName);
	}
	
	public Collection<Bone> getBones() {
		return bones.values();
	}
	
	public void setSlots(Collection<Slot> slots) {
		this.slots.clear();
		
		if (slots != null) {
			for (Slot s : slots) {
				this.slots.put(s.name, s);
				s.setSkeleton(this);
			}
		}
	}
	
	public Collection<Slot> getSlots() {
		return slots.values();
	}
	
	public void getTransform(String bone, Diagram d) {
		Bone b = getBone(bone);
		updateTransform(b);
		d.updateTransform(b);
	}
	
	public void updateTransform(Bone bone) {
		if (bone == null) {
			return;
		}
		
		Bone parent = bone.parentBone;

		if (parent != null) {
			updateTransform(parent);
		}

		bone.updateWorldTransform();
		bone.setDirty(false);
	}

	public void updateTransforms() {
		for (Bone b : this.bones.values()) {
			b.setDirty(true);
		}
		
		for (Bone b : this.bones.values()) {
			updateTransform(b);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		name = newName;
	}
}
