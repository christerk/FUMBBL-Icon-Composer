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
		PRIMARYLINE,
		SECONDARY,
		SECONDARYHI,
		SECONDARYLO,
		SECONDARYLINE,
		SKIN,
		SKINHI,
		SKINLO,
		SKINLINE,
		HAIR,
		HAIRHI,
		HAIRLO,
		HAIRLINE;

		public static ColourType fromString(String activeColour) {
			switch (activeColour) {
			case "primaryLine":
				return PRIMARYLINE;
			case "primaryLo":
				return PRIMARYLO;
			case "primaryMid":
				return PRIMARY;
			case "primaryHi":
				return PRIMARYHI;
			case "secondaryLine":
				return SECONDARYLINE;
			case "secondaryLo":
				return SECONDARYLO;
			case "secondaryMid":
				return SECONDARY;
			case "secondaryHi":
				return SECONDARYHI;
			case "skinLine":
				return SKINLINE;
			case "skinLo":
				return SKINLO;
			case "skinMid":
				return SKIN;
			case "skinHi":
				return SKINHI;
			case "hairLine":
				return HAIRLINE;
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
	private final Map<ColourType,String> colourStringMap;
	private final Map<String,ColourType> reverseMap;
	private final Map<ColourType,Color> colourMap;
	private final Map<Integer,ColourType> intMap;

	public ColourTheme(String name) {
		this.name = name;
		colourStringMap = new HashMap<>();
		colourMap = new HashMap<>();
		reverseMap = new HashMap<>();
		intMap = new HashMap<>();
	}

	public void setColour(ColourType type, Color c) {
		resetColour(type);
		String colour = "rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
		colourStringMap.put(type, colour);
		colourMap.put(type, c);
		reverseMap.put(colour, type);
		intMap.put(c.getRGB(), type);
	}
	
	public void setColour(ColourType type, int r, int g, int b) {
		String colour = "rgb("+r+","+g+","+b+")";
		Color c = new Color(r,g,b);
		setColour(type, c);
	}

	public int map(int pixel, ColourTheme target) {
		if (intMap.containsKey(pixel)) {
			Color newColor = target.getColour(intMap.get(pixel));
			if (newColor != null) {
				return newColor.getRGB();
			}
		}
		return pixel;
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
		Color c = colourMap.remove(type);
		if (c != null) {
			intMap.remove(c.getRGB());
		}
	}
}
