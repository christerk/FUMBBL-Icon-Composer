package com.fumbbl.iconcomposer.model.spine;

import com.fumbbl.iconcomposer.controllers.NamedItem;

public class Slot implements NamedItem {
	public int id;
	public int boneId;
	public String name;
	public String bone;
	public String attachment;
	
	private Skeleton skeleton;
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}
	
	public Skeleton getSkeleton() {
		return skeleton;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Slot)) {
			return false;
		}
		Slot other = (Slot)o;
		
		return other.id == this.id;
	}
	
	@Override
	public final int hashCode() {
		return this.id + (name != null ? name.hashCode() : 0);
	}
}
