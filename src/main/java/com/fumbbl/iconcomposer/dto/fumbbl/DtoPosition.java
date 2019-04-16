package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.types.Position;

public class DtoPosition {
	public int id;
	public String title;
	
	public Position toPosition() {
		Position p = new Position();
		p.id = id;
		p.name = title;
		
		return p;
	}
}
