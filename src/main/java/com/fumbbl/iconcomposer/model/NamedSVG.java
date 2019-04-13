package com.fumbbl.iconcomposer.model;

import com.kitfox.svg.SVGDiagram;

public class NamedSVG {
	public int id;
	public String name;
	public SVGDiagram diagram;

	public NamedSVG(String name, SVGDiagram svg) {
		this(-1, name, svg);
	}

	public NamedSVG(int id, String name, SVGDiagram svg) {
		this.id = id;
		this.name = name;
		this.diagram = svg;
	}
}
