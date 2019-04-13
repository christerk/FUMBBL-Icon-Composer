package com.fumbbl.iconcomposer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.fumbbl.iconcomposer.ColourTheme.ColourType;
import com.fumbbl.iconcomposer.spine.Skeleton;
import com.fumbbl.iconcomposer.spine.Skin;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class ViewState {

	private ColourType activeColourType;
	private Skin activeSkin;
	private Diagram activeDiagram;
	private ColourTheme activeColourTheme;
	private Skeleton activeSkeleton;
    private  BufferedImage buffer;

    public ViewState() {
		buffer = new BufferedImage(480, 480, BufferedImage.TYPE_INT_ARGB);
    }
    
	public Graphics2D getGraphics2D() {
		return (Graphics2D) buffer.getGraphics();
	}
	
	public Color getPixelRGB(int x, int y) {
		int col = buffer.getRGB(x, y);
		int r = (col&0xff0000) >> 16;
		int g = (col&0x00ff00) >> 8;
		int b = (col&0x0000ff);
		
		return new Color(r,g,b);
	}

	public WritableImage getImage() {
		return SwingFXUtils.toFXImage(buffer, null);
	}

	public void setActiveDiagram(Diagram diagram) {
		activeDiagram = diagram;
	}

	public void setActiveSkin(Skin skin) {
		activeSkin = skin;
	}

	public Diagram getActiveDiagram() {
		return activeDiagram;
	}

	public Skeleton getActiveSkeleton() {
		return activeSkeleton;
	}

	public void setActiveColourTheme(ColourTheme colourTheme) {
		activeColourTheme = colourTheme;
	}

	public void setActiveSkeleton(Skeleton skeleton) {
		activeSkeleton = skeleton;
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

	public Skin getActiveSkin() {
		return activeSkin;
	}

}
