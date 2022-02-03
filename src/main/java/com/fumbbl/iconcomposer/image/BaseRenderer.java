package com.fumbbl.iconcomposer.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.model.types.*;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

import javafx.geometry.Point2D;

public class BaseRenderer {
	protected int width = 480;
	protected int height = 480;
	private Model model;
	protected Controller controller;
	protected double imageScale;
	protected Color renderBackground = new Color(148,158,148);
	private Color gridColor = new Color(160,89,179);
	private SvgRenderer svgRenderer;
	private ImageRenderer imageRenderer;
	
	public BaseRenderer(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;
		svgRenderer = new SvgRenderer(this);
		imageRenderer = new ImageRenderer(this);
	}

	public double getImageScale() {
		return imageScale;
	}
	
	public void renderSkin(Skin skin) {
		if (skin == null || skin.skeleton == null) {
			return;
		}
		Graphics2D g2 = controller.viewState.getGraphics2D();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		try {
			skin.skeleton.updateTransforms();
			drawIcon(g2, skin, 0, 0, 480);
		} catch (SVGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}

	public void renderDiagram(Diagram diagram) {
		Graphics2D g2 = controller.viewState.getGraphics2D();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);

		g2.setColor(gridColor);
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {5,3}, 0f));
		g2.drawLine(this.width / 2, 0, this.width / 2, this.height);
		g2.drawLine(0, this.height / 2, this.width, this.height / 2);

		g2.drawLine(this.width / 2 - 1, 0, this.width / 2 - 1, this.height);
		g2.drawLine(0, this.height / 2 - 1, this.width, this.height / 2 - 1);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//g2.translate(-diagram.width/2, -diagram.height/2);
		applyPixelTransform(diagram, g2);

		try {
			diagram.resetColour(controller.getSvg(diagram.getImage().getName()));
			controller.onColourThemeChanged(diagram.getTheme());
			renderDiagram(g2, diagram);
		} catch (SVGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void applyPixelTransform(Diagram diagram, Graphics2D g2) {
		g2.scale(8.0, 8.0);
		double xFix = diagram.width % 2 == 0 ? 0.0 : 0.5;
		double yFix = diagram.height % 2 == 0 ? 0.0 : 0.5;
		g2.translate(30- diagram.width/2 - xFix,30- diagram.height/2 - yFix);
	}

	public void renderSkeleton(Skeleton skeleton, String currentBone) {
		Graphics2D g2 = controller.viewState.getGraphics2D();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (skeleton == null) {
			return;
		}

		double scale = 8;

		if (skeleton.width != 0) {
			scale = 8;
		}

		skeleton.updateTransforms();
		for (Bone b : skeleton.getBones()) {
			int cx = (int) (this.width/2 + scale*b.worldX/2);
			int cy = (int) (this.height/2 - scale*b.worldY/2);

			if (b.parentBone != null) {
				Bone parent = b.parentBone;
				int px = (int) (this.width/2 + scale*parent.worldX/2);
				int py = (int) (this.height/2 - scale*parent.worldY/2);
				
				g2.setColor(gridColor);
				g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1f, new float[] {1, 4}, 0));
				g2.drawLine(px, py, cx, cy);
			}
			
			g2.setColor(Color.gray);
			g2.setStroke(new BasicStroke(2));
			g2.drawLine(cx-10, cy, cx+10, cy);
			g2.drawLine(cx, cy-10, cx, cy+10);
			
			g2.setStroke(new BasicStroke(1));
			g2.setColor(Color.white);
			g2.fillOval(cx-3, cy-3, 6, 6);
			g2.setColor(Color.black);
			g2.drawOval(cx-3, cy-3, 6, 6);

			if (b.name.equals(currentBone)) {
				g2.drawString(b.name, cx+4, cy-2);
			}
		}
	}
	
	public void renderCursor(double x, double y) {
		Graphics2D g2 = controller.viewState.getGraphics2D();
		g2.setColor(Color.blue);

		int cx = (int) (240.0 - x*imageScale);
		int cy = (int) (240.0 + y*imageScale);
		
		g2.setColor(Color.black);
		g2.setStroke(new BasicStroke(3));
		g2.drawLine(cx-8, cy-8, cx+8, cy+8);
		g2.drawLine(cx+8, cy-8, cx-8, cy+8);
		
		g2.setColor(Color.pink);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(cx-8, cy-8, cx+8, cy+8);
		g2.drawLine(cx+8, cy-8, cx-8, cy+8);

		g2.drawLine(cx-9, cy-8, cx+7, cy+8);
		g2.drawLine(cx+7, cy-8, cx-9, cy+8);

		g2.drawLine(cx-8, cy-9, cx+8, cy+7);
		g2.drawLine(cx+8, cy-9, cx-8, cy+7);

		g2.drawLine(cx-9, cy-9, cx+7, cy+7);
		g2.drawLine(cx+7, cy-9, cx-9, cy+7);
	}
	
	public Point2D getImageOffset(double x, double y) {
		double oX = Math.round(-(x - 240) / imageScale);
		double oY = Math.round((y - 240) / imageScale);
		
		return new Point2D(oX, oY);
	}

	private void drawIcon(Graphics2D g2, Skin skin, double x, double y, double size) throws SVGException {
		AffineTransform originalTransform = g2.getTransform();
		
		AffineTransform at = g2.getTransform();
		ColourTheme theme = controller.getColourTheme();
		
		for (Slot slot : model.getSlots().stream().sorted(Slot.ReverseComparator).collect(Collectors.toList())) {
			Diagram diagram = skin.getDiagram(slot);

			if (diagram != null) {
				diagram.setColour(controller.getSvg(diagram.getImage().getName()), theme);
				g2.translate(x, y);
				renderDiagram(g2, diagram, skin.skeleton, slot, size);
			}
			g2.setTransform(at);
		}
		
		g2.setTransform(originalTransform);
	}

	private void renderDiagram(Graphics2D g2, Diagram diagram, Skeleton skeleton, Slot slot, double size) throws SVGException {
		if (diagram != null) {
			AffineTransform at = g2.getTransform();

			skeleton.getTransform(slot.getBone().name, diagram);

			applyPixelTransform(diagram, g2);
			g2.translate(-diagram.worldX, -diagram.worldY);

			renderDiagram(g2, diagram);
			g2.setTransform(at);
		}
	}
	
	private void renderDiagram(Graphics2D g2, Diagram diagram) throws SVGException {
		NamedImage image = diagram.getImage();

		if (image instanceof NamedSVG) {
			svgRenderer.renderSvg(((NamedSVG) image).diagram);
		} else if (image instanceof NamedPng) {
			imageRenderer.renderImage(g2, ((NamedPng)image).image);
		}
	}

	public void render(NamedItem image) {
		if (image instanceof NamedSVG) {
			svgRenderer.renderSvg(((NamedSVG)image).diagram);
		} else if (image instanceof NamedPng) {
			imageRenderer.renderImage(((NamedPng)image).image);
		}
	}
}