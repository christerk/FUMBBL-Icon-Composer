package com.fumbbl.iconcomposer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.Diagram;
import com.fumbbl.iconcomposer.model.types.Position;
import com.fumbbl.iconcomposer.model.types.Skeleton;
import com.fumbbl.iconcomposer.model.types.Skin;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class ViewState {

	private ColourType activeColourType;
	private HashMap<Perspective, Diagram> activeDiagrams;
	private ColourTheme activeColourTheme;
	private HashMap<Perspective, Skeleton> activeSkeletons;
	private Position activePosition;

	public Position getActivePosition() {
		return activePosition;
	}

	public void setActivePosition(Position position) {
		this.activePosition = position;
	}

	private enum ImageType {
		SkeletonFront,
		SkeletonSide,
		DiagramFront,
		DiagramSide,
		Preview;

		public static ImageType getDiagram(Perspective perspective) {
			return perspective == Perspective.Front ? DiagramFront : DiagramSide;
		}

		public static ImageType getSkeleton(Perspective perspective) {
			return perspective == Perspective.Front ? SkeletonFront : SkeletonSide;
		}
	}

	private class ImageData {
		public BufferedImage image;

		public ImageData(int size) {
			this(size, size);
		}
		public ImageData(int width, int height) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		public Graphics2D getGraphics2D() {
			return (Graphics2D) image.getGraphics();
		}
		public WritableImage getImage() {
			return SwingFXUtils.toFXImage(image, null);
		}
	}

	private HashMap<ImageType, ImageData> imageData;

    public ViewState() {
		activeSkeletons = new HashMap<>();
		activeDiagrams = new HashMap<>();

		imageData = new HashMap<>();
		imageData.put(ImageType.DiagramFront, new ImageData(480));
		imageData.put(ImageType.DiagramSide, new ImageData(480));
		imageData.put(ImageType.SkeletonFront, new ImageData(480));
		imageData.put(ImageType.SkeletonSide, new ImageData(480));
		imageData.put(ImageType.Preview, new ImageData(65*14+5, 65*4+5));
    }

	public Graphics2D getDiagramGraphics2D(Perspective perspective) {
		return imageData.get(ImageType.getDiagram(perspective)).getGraphics2D();
	}

	public Graphics2D getSkeletonGraphics2D(Perspective perspective) {
		return imageData.get(ImageType.getSkeleton(perspective)).getGraphics2D();
	}

	public Graphics2D getPreviewGraphics2D() {
		return imageData.get(ImageType.Preview).getGraphics2D();
	}

	public WritableImage getDiagramImage(Perspective perspective) {
		return imageData.get(ImageType.getDiagram(perspective)).getImage();
	}

	public WritableImage getSkeletonImage(Perspective perspective) {
		return imageData.get(ImageType.getSkeleton(perspective)).getImage();
	}

	public WritableImage getPreviewImage() {
		return imageData.get(ImageType.Preview).getImage();
	}

	public Color getPixelRGB(Perspective perspective, int x, int y) {
		int col = imageData.get(ImageType.getDiagram(perspective)).image.getRGB(x, y);
		int r = (col&0xff0000) >> 16;
		int g = (col&0x00ff00) >> 8;
		int b = (col&0x0000ff);
		
		return new Color(r,g,b);
	}

	public void setActiveDiagram(Perspective perspective, Diagram diagram) {
		activeDiagrams.put(perspective, diagram);
	}

	public Diagram getActiveDiagram(Perspective perspective) {
		return activeDiagrams.get(perspective);
	}

	public Skeleton getActiveSkeleton(Perspective perspective) {
		return activeSkeletons.get(perspective);
	}

	public void setActiveColourTheme(ColourTheme colourTheme) {
		activeColourTheme = colourTheme;
	}

	public void setActiveSkeleton(Skeleton skeleton) {
		activeSkeletons.put(skeleton.perspective, skeleton);
	}

	public ColourTheme getActiveColourTheme() {
		return activeColourTheme;
	}

	public ColourType getActiveColourType() {
		return activeColourType;
	}

	public void setActiveColourType(ColourType colourType) {
		activeColourType = colourType;
	}
}
