package com.fumbbl.iconcomposer.dto.spine;

import com.fumbbl.iconcomposer.model.types.Diagram;

public class DtoAttachment {
	public String path;
	public String name;
	public double x;
	public double y;
	public double rotation;
	public double width;
	public double height;
	
	public Diagram toDiagram() {
		Diagram d = new Diagram();
		if (path != null) {
			d.setName(path);
		} else {
			d.setName(name);
		}
		d.x = x;
		d.y = y;
		d.rotation = rotation;
		d.width = width;
		d.height = height;
		
		return d;
	}
}
