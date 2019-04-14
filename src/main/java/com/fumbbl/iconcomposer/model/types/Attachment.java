package com.fumbbl.iconcomposer.model.types;

public class Attachment {
	public String name;
	public String path;
	public int slotId;
	public double x = 0;
	public double y = 0;
	public double rotation = 0;
	public double width;
	public double height;
	
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
	
	public String getImage() {
		if (path != null) {
			return path;
		}
		return name;
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
		
		double oX = topLeftX;
		double oY = topLeftY;
		
		worldX = oX * bone.a + oY * bone.b + bone.worldX;
		worldY = oX * bone.c + oY * bone.d + bone.worldY;
	}

	public Diagram toDiagram() {
		Diagram d = new Diagram(getImage());
		d.x = this.x;
		d.y = this.y;
		d.width = this.width;
		d.height = this.height;
		return d;
	}
}
