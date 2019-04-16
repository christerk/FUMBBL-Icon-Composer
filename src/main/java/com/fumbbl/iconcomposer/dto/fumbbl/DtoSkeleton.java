package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.types.Skeleton;

public class DtoSkeleton {
	public int id;
	public String name;
	
	public Skeleton toSkeleton() {
		Skeleton s = new Skeleton();
		s.id = id;
		s.name = name;
		
		return s;
	}
}
