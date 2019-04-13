package com.fumbbl.iconcomposer.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.Diagram;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.spine.Attachment;
import com.fumbbl.iconcomposer.spine.Bone;
import com.fumbbl.iconcomposer.spine.Skeleton;
import com.fumbbl.iconcomposer.spine.Skin;
import com.fumbbl.iconcomposer.spine.Slot;
import com.fumbbl.iconcomposer.spine.SlotData;
import com.fumbbl.iconcomposer.spine.Spine;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

import javafx.geometry.Point2D;

public class SVGRenderer {
	private int width = 480;
	private int height = 480;
	private Model model;
	private Controller controller;
	private double imageScale;
	private Color renderBackground = new Color(148,158,148);
	private Color gridColor = new Color(160,89,179);
	
	public SVGRenderer(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;
	}

	public double getImageScale() {
		return imageScale;
	}
	
	public void renderSkin(Skin skin) {
		if (skin == null || skin.skeleton == null) {
			return;
		}
		Graphics2D g2 = controller.viewState.getGraphics2D();
		Spine spine = model.getSpine();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		try {
			skin.skeleton.updateTransforms();
			drawIcon(g2, spine, skin, 0, 0, 480);
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
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		try {
			diagram.resetColour(controller.getSvg(diagram.svgName));
			controller.onColourThemeChanged(diagram.getTheme());
			renderDiagram(g2, diagram);
		} catch (SVGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void renderSvg(SVGDiagram svg) {
		Graphics2D g2 = controller.viewState.getGraphics2D();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		try {
			renderSvg(g2, svg);
		} catch (SVGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		skeleton.updateTransforms();
		for (Bone b : skeleton.getBones()) {
			int cx = (int) (this.width/2 + b.worldX/2);
			int cy = (int) (this.height - b.worldY/2);

			if (b.parent != null) {
				Bone parent = b.parentBone;
				int px = (int) (this.width/2 + parent.worldX/2);
				int py = (int) (this.height - parent.worldY/2);
				
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
		g2.setStroke(new BasicStroke(5));
		g2.drawLine(cx-10, cy, cx+10, cy);
		g2.drawLine(cx, cy-10, cx, cy+10);
		
		g2.setColor(Color.pink);
		g2.setStroke(new BasicStroke(3));
		g2.drawLine(cx-10, cy, cx+10, cy);
		g2.drawLine(cx, cy-10, cx, cy+10);
	}
	
	public Point2D getImageOffset(double x, double y) {
		double oX = -(x - 240) / imageScale;
		double oY = (y - 240) / imageScale;
		
		return new Point2D(oX, oY);
	}

	private void drawIcon(Graphics2D g2, Spine spine, Skin skin, double x, double y, double size) throws SVGException {
		AffineTransform originalTransform = g2.getTransform();
		
		AffineTransform at = g2.getTransform();
		ColourTheme theme = controller.getColourTheme();
		
		for (Slot slot : spine.slots) {
			String attachment = slot.attachment;
			
			SlotData slotData = skin.get(slot.name);

			if (slotData != null) {
				Attachment a = slotData.get(attachment);
				Diagram diagram = model.getDiagram(a.getImage());
				if (diagram != null) {
					diagram.setColour(controller.getSvg(diagram.svgName), theme);
	
					g2.translate(x, y);
					renderDiagram(g2, diagram, skin.skeleton, slot, a, size);
				}
			}
			g2.setTransform(at);
		}
		
		g2.setTransform(originalTransform);
	}

	private void renderDiagram(Graphics2D g2, Diagram diagram, Skeleton skeleton, Slot slot, Attachment a, double size) throws SVGException {
		AffineTransform at = g2.getTransform();
		double scale = size / 960.0;
		g2.translate(width/2 - size/2, height/2 - size/2);
		g2.scale(scale, scale);
		
		skeleton.getTransform(slot.bone, a);

		SVGDiagram d = controller.getSvg(diagram.svgName);
		
		if (d != null) {
			g2.translate(a.worldX + 480, 960-a.worldY);
			g2.scale(a.width / d.getWidth(), a.height / d.getHeight());
			d.render(g2);
			g2.setTransform(at);
		}
	}
	
	private void renderDiagram(Graphics2D g2, Diagram diagram) throws SVGException {
		AffineTransform at = g2.getTransform();
		SVGDiagram d = controller.getSvg(diagram.svgName);
		if (d == null) {
			return;
		}
		String viewBox = d.getRoot().getPresAbsolute("viewBox").getStringValue();
		String[] list = viewBox.split(" ");

		double dw = Double.parseDouble(list[2]);
		double dh = Double.parseDouble(list[3]);
		
		imageScale = Math.min(this.width / dw, this.height / dh);
		g2.translate((this.width - dw*imageScale)/2.0, (this.height - dh*imageScale)/2.0);
		g2.scale(imageScale * dw / d.getWidth(), imageScale * dh / d.getHeight());
		d.render(g2);
		g2.setTransform(at);
	}
	
	private void renderSvg(Graphics2D g2, SVGDiagram diagram) throws SVGException {
		AffineTransform at = g2.getTransform();
		String viewBox = diagram.getRoot().getPresAbsolute("viewBox").getStringValue();
		String[] list = viewBox.split(" ");

		double dw = Double.parseDouble(list[2]);
		double dh = Double.parseDouble(list[3]);
		
		imageScale = Math.min(this.width / dw, this.height / dh);
		g2.translate((this.width - dw*imageScale)/2.0, (this.height - dh*imageScale)/2.0);
		g2.scale(imageScale * dw / diagram.getWidth(), imageScale * dh / diagram.getHeight());
		diagram.render(g2);
		g2.setTransform(at);
	}	
}