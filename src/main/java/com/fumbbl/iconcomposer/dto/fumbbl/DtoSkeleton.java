package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.Skeleton;

public class DtoSkeleton {
	public int id;
	public String name;
	String perspective;
	
	public Skeleton toSkeleton() {
		Skeleton s = new Skeleton();
		s.id = id;
		s.name = name;
		s.perspective = Perspective.valueOf(perspective);
		
		return s;
	}
}
