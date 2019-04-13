package com.fumbbl.iconcomposer.model.spine;

import com.fumbbl.iconcomposer.controllers.NamedItem;

public class Slot implements NamedItem {
	public int id;
	public int boneId;
	public String name;
	public String bone;
	public String attachment;
	
	@Override
	public String getName() {
		return name;
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
		return this.id;
	}
}
