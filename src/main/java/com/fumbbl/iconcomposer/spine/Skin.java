package com.fumbbl.iconcomposer.spine;
import java.util.HashMap;

public class Skin extends HashMap<String,SlotData> {
	private static final long serialVersionUID = 1L;
	
	public int id;
	public int skeletonId;
	public String name;
	public Skeleton skeleton;
}
