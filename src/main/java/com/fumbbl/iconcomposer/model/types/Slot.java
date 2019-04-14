package com.fumbbl.iconcomposer.model.types;

import java.util.Comparator;

import com.fumbbl.iconcomposer.controllers.NamedItem;

public class Slot implements NamedItem {
	public int id;
	public int boneId;
	public int order;
	public String name;
	public String bone;
	public String attachment;
	
	private Skeleton skeleton;
	
	public static Comparator<Slot> Comparator = new Comparator<Slot>() {
		@Override
		public int compare(Slot o1, Slot o2) {
			int r = o2.order-o1.order;
			if (r == 0) {
				r = o1.getName().compareTo(o2.getName());
			}
			return r;
		}
	};
	
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
