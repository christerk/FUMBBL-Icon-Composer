package com.fumbbl.iconcomposer.model.types;

import java.awt.geom.Rectangle2D;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.image.SVGUtil;
import com.fumbbl.iconcomposer.model.Perspective;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

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

    private Slot slot;
	public ColourTheme templateColours;

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
	
	private static final String ATTR_COL = "fumbbl:colour";
	private static final String ATTR_ORIGFILL = "fumbbl:origFill";
	private static final String[] blacklistedStyles = new String[] {
			"fill-opacity"
	};
	
	public Diagram() {
	}
	
	public Diagram(NamedImage image) {
		this(image, new ColourTheme("template"));

		if (image instanceof NamedSVG) {
			Rectangle2D.Double viewbox = SVGUtil.getViewbox(((NamedSVG)image).diagram);
			this.width = viewbox.width;
			this.height = viewbox.height;
		} else {
			this.width = ((NamedPng)image).image.getWidth();
			this.height = ((NamedPng)image).image.getHeight();
		}
	}
	
	public Diagram(NamedImage image, ColourTheme template) {
		this.image = image;
		this.templateColours = template;
	}

	public NamedImage getImage() {
		return image;
	}
	
	public ColourTheme getTheme() {
		return templateColours;
	}
	
	public void refreshColours(NamedImage image) {
		try {
			if (image instanceof NamedSVG)
			tagColours(((NamedSVG)image).diagram);
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
