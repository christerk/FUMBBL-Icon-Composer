package com.fumbbl.iconcomposer.spine;

public class Bone {
	public int id;
	public String name;
	public int parentId;
	public String parent;
	public double length;
	public double x = 0;
	public double y = 0;
	public double rotation = 0;
	public double scaleX = 1;
	public double scaleY = 1;
	public double shearX = 0;
	public double shearY = 0;
	
	public double a, b, worldX;
	public double c, d, worldY;
	public Bone parentBone;
	
	private Skeleton skeleton;
	
	private boolean dirty;

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}
	
	public void updateWorldTransform() {
		if (parentBone == null) {
			double rotationY = rotation + 90 + shearY;
			double sx = skeleton.scaleX;
			double sy = skeleton.scaleY;
			a = Math.cos(Math.toRadians(rotation + shearX)) * scaleX * sx;
			b = Math.cos(Math.toRadians(rotationY)) * scaleY * sy;
			c = Math.sin(Math.toRadians(rotation + shearX)) * scaleX * sx;
			d = Math.sin(Math.toRadians(rotationY)) * scaleY * sy;
			
			worldX = x * sx + skeleton.x;
			worldY = y * sy + skeleton.y;
			return;
		}
		
		double pa = parentBone.a;
		double pb = parentBone.b;
		double pc = parentBone.c;
		double pd = parentBone.d;
		worldX = pa * x + pb * y + parentBone.worldX;
		worldY = pc * x + pd * y + parentBone.worldY;
		
		// Transform mode NORMAL
		double rotationY = rotation + 90 + shearY;
		double la = Math.cos(Math.toRadians(rotation + shearX)) * scaleX;
		double lb = Math.cos(Math.toRadians(rotationY)) * scaleY;
		double lc = Math.sin(Math.toRadians(rotation + shearX)) * scaleX;
		double ld = Math.sin(Math.toRadians(rotationY)) * scaleY;
		a = pa * la + pb * lc;
		b = pa * lb + pb * ld;
		c = pc * la + pd * lc;
		d = pc * lb + pd * ld;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
}
