package com.fumbbl.iconcomposer.model.types;

import com.kitfox.svg.SVGDiagram;

public class NamedSVG extends NamedItem {
	public int id;
	public String name;
	public SVGDiagram diagram;

	public NamedSVG() {
		id = -1;
	}
	
	public NamedSVG(String name, SVGDiagram svg) {
		this(-1, name, svg);
	}

	public NamedSVG(int id, String name, SVGDiagram svg) {
		this.id = id;
		this.name = name;
		this.diagram = svg;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}
}
