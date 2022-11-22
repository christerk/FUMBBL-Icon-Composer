package com.fumbbl.iconcomposer.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.controllers.MainController;
import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.*;

import javafx.geometry.Point2D;
import javafx.scene.image.WritableImage;

public class BaseRenderer {
	protected int width = 480;
	protected int height = 480;
	private Model model;
	protected Controller controller;
	protected double imageScale = 8.0;
	protected Color renderBackground = new Color(148,158,148);
	protected Color iconBackground = new Color(70, 125, 80);
	private Color gridColor = new Color(160,89,179);
	private ImageRenderer imageRenderer;
	
	public BaseRenderer(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;
		imageRenderer = new ImageRenderer(this);
	}

	public void renderPreview() {
		Skin skin = new Skin();

		ColourTheme theme = controller.viewState.getActiveColourTheme();

		Graphics2D g2 = controller.viewState.getPreviewGraphics2D();
		WritableImage image = controller.viewState.getPreviewImage();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, (int)image.getWidth(), (int)image.getHeight());

		controller.viewState.setActiveColourTheme(model.getColourTheme("template"));

		for (int y=0; y<2; y++) {
			for (int x = 0; x < 14; x++) {
				MainController mainController = controller.getMainController();
				for (Slot slot : model.getSlots()) {
					Collection<VirtualDiagram> diagrams = mainController.getDiagrams(slot);
					VirtualDiagram randomDiagram = random(diagrams);
					skin.setDiagram(slot, randomDiagram);
				}

				renderSkin(Perspective.Front, skin, x, y * 2);
				renderSkin(Perspective.Side, skin, x, y * 2 + 1);
			}

			controller.viewState.setActiveColourTheme(model.getColourTheme("away"));
		}

	}

	public static <T> T random(Collection<T> coll) {
		int num = (int) (Math.random() * coll.size());
		for(T t: coll) if (--num < 0) return t;
		throw new AssertionError();
	}

	public void renderSkin(Perspective perspective, Skin skin, int x, int y) {
		Skeleton skeleton = controller.viewState.getActiveSkeleton(perspective);

		if (skin == null || skeleton == null) {
			return;
		}

		Graphics2D g2 = controller.viewState.getPreviewGraphics2D();
		g2.setColor(iconBackground);
		g2.fillRect(x*65 + 5, y*65 + 5, 60, 60);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		skeleton.updateTransforms();
		drawIcon(g2, skeleton, skin, x*65+5, y*65+5);
	}

	public void renderDiagram(Perspective perspective, Diagram diagram) {
		Graphics2D g2 = controller.viewState.getDiagramGraphics2D(perspective);
		AffineTransform at = g2.getTransform();
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

		applyPixelTransform(diagram, g2);

		//controller.onColourThemeChanged(diagram.getTheme());
		render(perspective, diagram);
		g2.setTransform(at);
	}

	private void applyPixelTransform(Diagram diagram, Graphics2D g2) {
		double xFix = diagram.width % 2 == 0 ? 0.0 : 0.5;
		double yFix = diagram.height % 2 == 0 ? 0.0 : 0.5;
		g2.translate(30- diagram.width/2 - xFix,30- diagram.height/2 - yFix);
	}


	public void renderSkeleton(Perspective perspective, Skeleton skeleton, String currentBone) {
		Graphics2D g2 = controller.viewState.getSkeletonGraphics2D(perspective);
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (skeleton == null) {
			return;
		}

		int scale = 8;

		if (skeleton.width != 0) {
			scale = 8;
		}

		// Find center; should correspond to "root" bone.
		for (Bone b : skeleton.getBones()) {
			if ("root".equals(b.getName())) {
				skeleton.x = b.x;
				skeleton.y = -b.y;
			}
		}

		skeleton.updateTransforms();

		g2.setColor(gridColor);
		g2.drawRect(this.width / 2 + -scale*45/2, this.height / 2 - scale*45/2, scale*45, scale*45);

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
	
	public void renderCursor(Perspective perspective, double x, double y) {
		Graphics2D g2 = controller.viewState.getDiagramGraphics2D(perspective);

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

	private void drawIcon(Graphics2D g2, Skeleton skeleton, Skin skin, double x, double y) {
		AffineTransform originalTransform = g2.getTransform();
		
		AffineTransform at = g2.getTransform();
		ColourTheme theme = controller.getColourTheme();

		for (Slot slot : model.getSlots().stream().sorted(Slot.ReverseComparator).collect(Collectors.toList())) {
			VirtualDiagram virtualDiagram = skin.getDiagram(slot);
			if (virtualDiagram != null) {
				Diagram diagram = model.getDiagram(skeleton.id, virtualDiagram.getName());

				if (diagram != null) {
					//diagram.setColour(controller.getSvg(diagram.getImage().getName()), theme);
					g2.translate(x, y);
					renderDiagram(g2, diagram, skeleton, slot, theme);
				}
			}
			g2.setTransform(at);
		}

		g2.setTransform(originalTransform);
	}

	private void renderDiagram(Graphics2D g2, Diagram diagram, Skeleton skeleton, Slot slot, ColourTheme theme) {
		if (diagram != null) {
			AffineTransform at = g2.getTransform();

			skeleton.getTransform(slot.getBone().name, diagram);

			applyPixelTransform(diagram, g2);
			g2.translate(diagram.worldX, -diagram.worldY);

			renderDiagram(g2, diagram, theme);
			g2.setTransform(at);
		}
	}
	
	private void renderDiagram(Graphics2D g2, Diagram diagram, ColourTheme theme) {
		BufferedImage image = model.getImage(diagram.perspective.name()+"_"+diagram.getName());

		if (image == null) {
			return;
		}

		Position p = controller.viewState.getActivePosition();
		ColourTheme template = p.templateColours;

		if (template != theme) {
			BufferedImage clonedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			int w = image.getWidth();
			int h = image.getHeight();

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y);
					int newPixel = template.map(pixel, theme);
					clonedImage.setRGB(x, y, newPixel);
				}
			}
			image = clonedImage;
		}

		imageRenderer.renderImage(g2, image);
	}

	public void render(Perspective perspective, NamedItem image) {
		if (image instanceof NamedPng) {
			imageRenderer.renderImage(perspective, ((NamedPng)image).image);
		} else {
			imageRenderer.renderImage(perspective, model.getImage(perspective.name()+"_"+image.getName()));
		}
	}
}