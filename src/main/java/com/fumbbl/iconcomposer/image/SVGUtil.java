package com.fumbbl.iconcomposer.image;

import java.awt.geom.Rectangle2D;

import com.kitfox.svg.SVGDiagram;

public class SVGUtil {
	public static Rectangle2D.Double getViewbox(SVGDiagram diagram) {
		String viewBox = diagram.getRoot().getPresAbsolute("viewBox").getStringValue();
		String[] list = viewBox.split(" ");

		double x = Double.parseDouble(list[0]);
		double y = Double.parseDouble(list[1]);
		double w = Double.parseDouble(list[2]);
		double h = Double.parseDouble(list[3]);
		
		return new java.awt.geom.Rectangle2D.Double(x, y, w, h);
	}
}
