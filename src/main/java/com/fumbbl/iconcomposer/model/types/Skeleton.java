package com.fumbbl.iconcomposer.model.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Skeleton extends NamedItem {
	public int id;
	public String name;

	private Map<String,Bone> bones;
	private Map<String,Slot> slots;
	
	public double x = 0, y = 0;
	public double scaleX = 1, scaleY = 1;
	
	public Skeleton() {
		bones = new HashMap<String,Bone>();
		slots = new HashMap<String,Slot>();
		id = -1;
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
	
	public Bone getBone(int boneId) {
		for (Bone b : bones.values()) {
			if (b.id == boneId) {
				return b;
			}
		}
		return null;
	}
	
	public Collection<Bone> getBones() {
		return new ArrayList<Bone>(bones.values());
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
	
	public Slot getSlot(String slotName) {
		return slots.get(slotName);
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
		if (bone == null || !bone.isDirty()) {
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
