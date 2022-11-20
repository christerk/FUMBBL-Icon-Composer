package com.fumbbl.iconcomposer.dto.fumbbl;

import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.Diagram;

public class DtoDiagram {
	public int id;
	public String name;
	public int slotId;
	public double x;
	public double y;
	public double width;
	public double height;
	public String svg;
	
	public Diagram toDiagram(Perspective perspective) {
		Diagram d = new Diagram();
		d.id = id;
		d.name = name;
		d.x = x;
		d.y = y;
		d.width = width;
		d.height = height;
		d.setName(svg);
		d.perspective = perspective;
		
		return d;
	}
}
