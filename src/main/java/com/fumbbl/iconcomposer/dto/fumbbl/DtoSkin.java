package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.types.Skin;

public class DtoSkin {
	public int id;
	public String name;
	
	public Skin toSkin() {
		Skin s = new Skin();
		
		s.id = this.id;
		s.name = this.name;
		return s;
	}
}
