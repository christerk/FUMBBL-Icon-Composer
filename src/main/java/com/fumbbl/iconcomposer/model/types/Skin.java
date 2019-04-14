package com.fumbbl.iconcomposer.model.types;
import java.util.HashMap;

import com.fumbbl.iconcomposer.controllers.NamedItem;

public class Skin extends HashMap<String,SlotData> implements NamedItem {
	private static final long serialVersionUID = 1L;
	
	public int id;
	public int skeletonId;
	public String name;
	public Skeleton skeleton;
	
	@Override
	public String getName() {
		return name;
	}
}
