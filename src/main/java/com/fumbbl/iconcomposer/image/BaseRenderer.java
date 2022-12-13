package com.fumbbl.iconcomposer.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	protected final int width = 480;
	protected final int height = 480;
	private final Model model;
	protected final Controller controller;
	protected final double imageScale = 8.0;
	protected final Color renderBackground = new Color(148,158,148);
	protected final Color iconBackground = new Color(70, 125, 80);
	private final Color gridColor = new Color(160,89,179);
	private final ImageRenderer imageRenderer;
	
	public BaseRenderer(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;
		imageRenderer = new ImageRenderer(this);
	}

	public void renderPreview() {
		ColourTheme theme = controller.viewState.getActiveColourTheme();

		Graphics2D g2 = controller.viewState.getPreviewGraphics2D();
		WritableImage image = controller.viewState.getPreviewImage();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, (int)image.getWidth(), (int)image.getHeight());

		controller.viewState.setActiveColourTheme(model.getColourTheme("template"));

		for (int y=0; y<2; y++) {
			for (int x = 0; x < 14; x++) {
				MainController mainController = controller.getMainController();
				Skin skin = new Skin();

				for (VirtualBone b : model.masterSkeleton.get().bones.values()) {
					for (VirtualSlot slot : b.slots.values()) {
						VirtualDiagram randomDiagram = random(slot.diagrams.values());
						skin.setDiagram(slot.getName(), randomDiagram);
					}
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
		Skeleton skeleton = model.getSkeleton(perspective);

		if (skin == null || skeleton == null) {
			return;
		}

		Graphics2D g2 = controller.viewState.getPreviewGraphics2D();
		g2.setColor(iconBackground);
		g2.fillRect(x*64 + 5, y*64 + 5, 59, 59);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		skeleton.updateTransforms();
		drawIcon(g2, skeleton, skin, x, y);
	}

	private void recenterSkeleton(Graphics2D g2, Skeleton skeleton) {
		Bone rootBone = skeleton.getBone("root");

		double cx = rootBone.x;
		double cy = rootBone.y;

		g2.translate(-cx, cy);
	}

	public void renderDiagram(Perspective perspective, Diagram diagram) {
		if (diagram == null) {
			return;
		}
		Graphics2D g2 = controller.viewState.getDiagramGraphics2D(perspective);
		AffineTransform at = g2.getTransform();
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);

		g2.setColor(gridColor);
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {5,3}, 0f));
		g2.drawLine(this.width / 2 - (int)(diagram.x * imageScale), 0, this.width / 2 - (int)(diagram.x * imageScale), this.height);
		g2.drawLine(0, this.height / 2 + (int)(diagram.y * imageScale), this.width, this.height / 2 + (int)(diagram.y * imageScale));

		//controller.onColourThemeChanged(diagram.getTheme());
		if (diagram != null) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			applyPixelTransform(diagram, g2);
			render(perspective, diagram);
		}
		g2.setTransform(at);
	}

	private void applyPixelTransform(Diagram diagram, Graphics2D g2) {
		if (diagram != null) {
			double xFix = diagram.width % 2 == 0 ? 0.0 : 0.5;
			double yFix = diagram.height % 2 == 0 ? 0.0 : 0.5;
			g2.translate(30 - diagram.width / 2 - xFix, 30 - diagram.height / 2 - yFix);
		}
	}


	public void renderSkeleton(Perspective perspective, Skeleton skeleton) {
		int scale = 8;

		Graphics2D g2 = controller.viewState.getSkeletonGraphics2D(perspective);
		g2.setColor(renderBackground);
		g2.fillRect(0, 0, width, height);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(gridColor);
		g2.drawRect(this.width / 2 + -scale*45/2, this.height / 2 - scale*45/2, scale*45, scale*45);

		if (skeleton == null) {
			return;
		}

		double centerX = 0;
		double centerY = 0;
		// Find center; should correspond to "root" bone.
		for (Bone b : skeleton.getBones()) {
			if ("root".equals(b.getName())) {
				centerX = b.x;
				centerY = b.y;
			}
		}

		skeleton.updateTransforms();

		VirtualDiagram vDiagram = model.getVirtualDiagram("skeleton");

		if (vDiagram != null) {
			Diagram skeletonDiagram = vDiagram.realDiagrams.get(perspective);
			if (skeletonDiagram != null) {
				AffineTransform at = g2.getTransform();
				g2.scale(scale, scale);
				g2.translate(-centerX, centerY);
				renderDiagram(g2, skeletonDiagram, skeleton, skeletonDiagram.getSlot(), controller.getColourTheme());
				g2.setTransform(at);
			}
		}

		for (Bone b : skeleton.getBones()) {
			int cx = (int) (this.width/2 + scale*(b.worldX - centerX));
			int cy = (int) (this.height/2 - scale*(b.worldY - centerY));

			if (b.parentBone != null) {
				Bone parent = b.parentBone;
				int px = (int) (this.width/2 + scale*(parent.worldX - centerX));
				int py = (int) (this.height/2 - scale*(parent.worldY - centerY));
				
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

		// Set center to middle of square
		g2.translate(5 + x * 64 + 29, 5 + y * 64 + 29);

		// Draw square bounds
		g2.setColor(Color.darkGray);
		g2.drawRect(-22, -22, 44, 44);

		recenterSkeleton(g2, skeleton);

		ColourTheme theme = controller.getColourTheme();

		Collection<Slot> allSlots = new ArrayList<>();

		model.masterSkeleton.get().bones.values().stream()
				.forEach(b -> allSlots.addAll(
						b.slots.values().stream()
								.map(vs -> vs.realSlots.get(skeleton.perspective))
								.collect(Collectors.toList())
				));
		List<Slot> orderedSlots = allSlots.stream().sorted(Slot.ReverseComparator).collect(Collectors.toList());
		for (Slot slot : orderedSlots) {
			if (slot == null) {
				continue;
			}
			VirtualDiagram virtualDiagram = skin.getDiagram(slot.getName());
			if (virtualDiagram != null) {
				Diagram diagram = virtualDiagram.realDiagrams.get(skeleton.perspective);

				if (diagram != null) {
					renderTransformedDiagram(g2, diagram, skeleton, slot, theme);
				}
			}
		}

		g2.setTransform(originalTransform);
	}

	private void renderTransformedDiagram(Graphics2D g2, Diagram diagram, Skeleton skeleton, Slot slot, ColourTheme theme) {
		if (diagram != null) {
			AffineTransform at = g2.getTransform();

			skeleton.getTransform(slot.getBone().name, diagram);

			g2.translate(diagram.worldX, -diagram.worldY);
			centerComponentImage(g2, diagram);

			renderDiagram(g2, diagram, theme);
			g2.setTransform(at);
		}
	}

	private void centerComponentImage(Graphics2D g2, Diagram diagram) {
		if (diagram != null) {
			double xFix = diagram.width % 2 == 0 ? 0.0 : 0.5;
			double yFix = diagram.height % 2 == 0 ? 0.0 : 0.5;
			g2.translate(- diagram.width / 2 - xFix, - diagram.height / 2 - yFix);
		}
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
		if (diagram == null || diagram.perspective == null) {
			return;
		}
		//BufferedImage image = model.getImage(diagram.perspective.name()+"_"+diagram.getName());

		VirtualDiagram vDiagram = model.getVirtualDiagram(diagram.getName());
		VirtualImage vImage = vDiagram.images.get(diagram.getName());
		NamedPng png = (NamedPng) vImage.realImages.get(diagram.perspective);
		BufferedImage image = png.image;

		if (image == null) {
			return;
		}

		Position p = model.selectedPosition.get();
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
			String imageName = image.getName();
			VirtualDiagram vDiagram = model.getVirtualDiagram(imageName);
			NamedPng png = (NamedPng) vDiagram.images.get(imageName).realImages.get(perspective);
			imageRenderer.renderImage(perspective, png.image);
		}
	}
}