package com.fumbbl.iconcomposer.dto;

import com.fumbbl.iconcomposer.spine.Skin;

public class DtoSkin {
	public int id;
	public int skeletonId;
	public String name;
	
	public Skin toSkin() {
		Skin s = new Skin();
		
		s.id = this.id;
		s.name = this.name;
		s.skeletonId = this.skeletonId;
		return s;
	}
}
