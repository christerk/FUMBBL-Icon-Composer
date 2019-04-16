package com.fumbbl.iconcomposer.dto.spine;

import com.fumbbl.iconcomposer.model.types.Bone;

public class DtoBone {
	public String name;
	public String parent;
	public double x;
	public double y;
	public double length;
	public double rotation = 0;
	public double scaleX = 1;
	public double scaleY = 1;
	public double shearX = 0;
	public double shearY = 0;
	
	public Bone toBone() {
		Bone b = new Bone();
		b.id = -1;
		b.name = name;
		b.x = x;
		b.y = y;
		b.length = length;
		b.rotation = rotation;
		b.scaleX = scaleX;
		b.scaleY = scaleY;
		b.shearX = shearX;
		b.shearY = shearY;
		
		return b;
	}
}
