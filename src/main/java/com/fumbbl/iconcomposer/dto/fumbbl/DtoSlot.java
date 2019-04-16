package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.types.Slot;

public class DtoSlot {
	public int id;
	public int boneId;
	public String name;
	public String attachment;
	public int order;
	
	public Slot toSlot() {
		Slot s = new Slot();
		
		s.id = id;
		s.name = name;
		s.attachment = attachment;
		s.order = order;
		
		return s;
	}
}
