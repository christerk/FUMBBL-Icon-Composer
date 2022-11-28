package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.types.Bone;

public class DtoBone {
	public int id;
	public String name;
	public int parentId;
	public final double x = 0;
	public final double y = 0;
	
	public Bone toBone() {
		Bone b = new Bone();
		b.id = id;
		b.name = name;
		b.x = x;
		b.y = y;
		
		return b;
	}
}
