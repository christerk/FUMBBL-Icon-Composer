package com.fumbbl.iconcomposer;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class ColourTheme {
	public enum ColourType {
		NONE,
		PRIMARY,
		PRIMARYHI,
		PRIMARYLO,
		SECONDARY,
		SECONDARYHI,
		SECONDARYLO,
		SKIN,
		SKINHI,
		SKINLO,
		HAIR,
		HAIRHI,
		HAIRLO;

		public static ColourType fromString(String activeColour) {
			switch (activeColour) {
			case "primaryLo":
				return PRIMARYLO;
			case "primaryMid":
				return PRIMARY;
			case "primaryHi":
				return PRIMARYHI;
			case "secondaryLo":
				return SECONDARYLO;
			case "secondaryMid":
				return SECONDARY;
			case "secondaryHi":
				return SECONDARYHI;
			case "skinLo":
				return SKINLO;
			case "skinMid":
				return SKIN;
			case "skinHi":
				return SKINHI;
			case "hairLo":
				return HAIRLO;
			case "hairMid":
				return HAIR;
			case "hairHi":
				return HAIRHI;
			case "none":
				return NONE;
			}
			return null;
		}
	}
	
	public final String name;
	
	@Expose
	private Map<ColourType,String> colourStringMap;
	private Map<String,ColourType> reverseMap;
	private Map<ColourType,Color> colourMap;
	
	public ColourTheme(String name) {
		this.name = name;
		colourStringMap = new HashMap<ColourType,String>();
		colourMap = new HashMap<ColourType,Color>();
		reverseMap = new HashMap<String,ColourType>();
	}

	public void setColour(ColourType type, Color c) {
		String colour = "rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
		colourStringMap.put(type, colour);
		colourMap.put(type, c);
		reverseMap.put(colour, type);
		
	}
	
	public void setColour(ColourType type, int r, int g, int b) {
		String colour = "rgb("+r+","+g+","+b+")";
		colourStringMap.put(type, colour);
		colourMap.put(type, new Color(r,g,b));
		reverseMap.put(colour, type);
	}
	
	public ColourType getTypeFor(String colour) {
		return reverseMap.get(colour);
	}
	
	public String getColourString(ColourType type) {
		return colourStringMap.get(type);
	}

	public Color getColour(ColourType type) {
		return colourMap.get(type);
	}

	public void resetColour(ColourType type) {
		String current = colourStringMap.get(type);
		if (current != null) {
			reverseMap.remove(current);
		}
		colourStringMap.remove(type);
		colourMap.remove(type);
	}
}
