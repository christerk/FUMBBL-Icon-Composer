package com.fumbbl.iconcomposer.model.types;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.controllers.NamedItem;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

public class Diagram implements NamedItem {
	public String svgName;

	public ColourTheme templateColours;
	private Slot slot;
	public double x;
	public double y;
	
	private static final String ATTR_COL = "fumbbl:colour";
	private static final String ATTR_ORIGFILL = "fumbbl:origFill";
	private static final String[] blacklistedStyles = new String[] {
			"fill-opacity"
	};
	
	public Diagram(String svgName) {
		this(svgName, new ColourTheme("template"));
	}
	
	public Diagram(String svgName, ColourTheme template) {
		this.svgName = svgName;
		this.templateColours = template;
	}

	public ColourTheme getTheme() {
		return templateColours;
	}
	
	public void refreshColours(SVGDiagram svgDiagram) {
		try {
			tagColours(svgDiagram);
		} catch (SVGElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void tagColours(SVGDiagram svg) throws SVGElementException {
		SVGRoot root = svg.getRoot();
		for (SVGElement e : root.getChildren(null)) {
			tagColours(e);
		}
	}

	private void tagColours(SVGElement e) throws SVGElementException {
		for (SVGElement child : e.getChildren(null)) {
			tagColours(child);
		}
		
		if (e.hasAttribute(ATTR_COL, AnimationElement.AT_XML)) {
			e.removeAttribute(ATTR_COL, AnimationElement.AT_XML);
		}
		
		for (String style : blacklistedStyles) {
			if (e.hasAttribute(style, AnimationElement.AT_CSS)) {
				e.removeAttribute(style, AnimationElement.AT_CSS);
			}
		}

		StyleAttribute sa = e.getStyleAbsolute("fill");
		if (sa != null) {
			String colour = sa.getStringValue();

			ColourType type = templateColours.getTypeFor(colour);

			if (type != null) {
				e.addAttribute(ATTR_COL, AnimationElement.AT_XML, type.toString());
				if (!e.hasAttribute(ATTR_ORIGFILL, AnimationElement.AT_XML)) {
					e.addAttribute(ATTR_ORIGFILL, AnimationElement.AT_XML, colour);
				}
			}
		}
	}
	
	public void setSlot(Slot slot) {
		this.slot = slot;
	}
	
	public Slot getSlot() {
		return this.slot;
	}
	
	public void resetColour(SVGDiagram svgDiagram) throws SVGElementException {
		setColour(svgDiagram, null);
	}
	
	public void setColour(SVGDiagram svgDiagram, ColourTheme theme) throws SVGElementException {
		if (svgDiagram != null) {
			SVGRoot root = svgDiagram.getRoot();
			for (SVGElement e : root.getChildren(null)) {
				replaceColour(e, theme);
			}
		}
	}
	
	private void replaceColour(SVGElement e, ColourTheme theme) throws SVGElementException {
		for (SVGElement child : e.getChildren(null)) {
			replaceColour(child, theme);
		}
		
		if (e.hasAttribute(ATTR_COL, AnimationElement.AT_XML)) {
			StyleAttribute col = e.getPresAbsolute(ATTR_COL);
			
			if (col != null) {
				StyleAttribute sa = e.getStyleAbsolute("fill");
				String colour = null;
				
				if (theme != null) {
					ColourType type = Enum.valueOf(ColourType.class, col.getStringValue());
					colour = theme.getColourString(type);
				}
				
				if (colour != null)	 {
					sa.setStringValue(colour);
				} else {
					StyleAttribute origCol = e.getPresAbsolute(ATTR_ORIGFILL);
					sa.setStringValue(origCol.getStringValue());
				}
			}
		}
	}

	@Override
	public String getName() {
		return svgName;
	}	
}
