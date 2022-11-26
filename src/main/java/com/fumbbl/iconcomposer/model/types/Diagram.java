package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.model.Perspective;

public class Diagram extends NamedItem {
	public int id;
	public String name;

	public double x;
	public double y;
	public double rotation = 0;
	public double width;
	public double height;
	public NamedImage image;
    public Perspective perspective;
	public int imageId;

    private Slot slot;

	// Render coordinates
	public double topLeftX;
	public double topLeftY;
	public double topRightX;
	public double topRightY;
	public double bottomLeftX;
	public double bottomLeftY;
	public double bottomRightX;
	public double bottomRightY;
	public double worldX;
	public double worldY;

	public Diagram() {
	}
	
	public Diagram(NamedImage image) {
		this.image = image;

		this.width = ((NamedPng)image).image.getWidth();
		this.height = ((NamedPng)image).image.getHeight();
	}
	
	public NamedImage getImage() {
		return image;
	}
	
	public void refreshColours(NamedImage image) {
	}
	
	public void setSlot(Slot slot) {
		this.slot = slot;
	}
	
	public Slot getSlot() {
		return this.slot;
	}

	public void updateTransform(Bone bone) {
		if (bone == null) {
			return;
		}
		
		double localX2 = width / 2;
		double localY2 = height / 2;
		
		double localX = -localX2;
		double localY = -localY2;
		
		double sin = Math.sin(Math.toRadians(rotation));
		double cos = Math.cos(Math.toRadians(rotation));
		
		double localXCos = localX * cos + x;
		double localXSin = localX * sin;
		double localYCos = localY * cos + y;
		double localYSin = localY * sin;
		double localX2Cos = localX2 * cos + x;
		double localX2Sin = localX2 * sin;
		double localY2Cos = localY2 * cos + y;
		double localY2Sin = localY2 * sin;
		
		topLeftX = localXCos - localY2Sin;
		topLeftY = localY2Cos + localXSin;
		
		topRightX = localX2Cos - localY2Sin;
		topRightY = localY2Cos + localX2Sin;
		
		bottomLeftX = localXCos - localYSin;
		bottomLeftY = localYCos + localXSin;
		
		bottomRightX = localX2Cos - localYSin;
		bottomRightY = localYCos + localX2Sin;
		
		double oX = (topLeftX + topRightX) / 2;
		double oY = (topLeftY + bottomLeftY) / 2;
		
		worldX = oX * bone.a + oY * bone.b + bone.worldX;
		worldY = oX * bone.c + oY * bone.d + bone.worldY;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String newName) {
		this.name = newName;
	}
}
