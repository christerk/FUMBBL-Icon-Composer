package com.fumbbl.iconcomposer.model.types;

import java.util.Comparator;

public class Slot extends NamedItem {
	public int id;
	public String name;
	public int order;
	
	private Bone bone;
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

	public static Comparator<Slot> ReverseComparator = new Comparator<Slot>() {
		@Override
		public int compare(Slot o1, Slot o2) {
			int r = o1.order-o2.order;
			if (r == 0) {
				r = o2.getName().compareTo(o1.getName());
			}
			return r;
		}
	};
	
	public Slot() {
		this.id = -1;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String newName) {
		this.name = newName;
	}
	
	public void setBone(Bone bone) {
		this.bone = bone;
	}
	
	public Bone getBone() {
		return bone;
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
		
		int c = other.id - this.id;
		if (c == 0) {
			c = this.name.compareTo(other.name);
		}
		return c == 0;
	}
	
	@Override
	public int hashCode() {
		return this.id + (name != null ? name.hashCode() : 0);
	}
}
