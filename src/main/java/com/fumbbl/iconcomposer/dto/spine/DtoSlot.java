package com.fumbbl.iconcomposer.dto.spine;

import com.fumbbl.iconcomposer.model.types.Slot;

public class DtoSlot {
	public String name;
	public String bone;
	public String attachment;
	
	public Slot toSlot() {
		Slot s = new Slot();
		s.name = name;
		s.attachment = attachment;
		return s;
	}
}
