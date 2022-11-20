package com.fumbbl.iconcomposer.image;

import com.fumbbl.iconcomposer.model.Perspective;
import com.kitfox.svg.SVGException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageRenderer {
    private final int scale = 8;

    private final BaseRenderer renderer;

    public ImageRenderer(BaseRenderer renderer) {
        this.renderer = renderer;
    }

    public void renderImage(Perspective perspective, BufferedImage image) {
        Graphics2D g2 = renderer.controller.viewState.getDiagramGraphics2D(perspective);
        g2.setColor(renderer.renderBackground);
        g2.fillRect(0, 0, renderer.width, renderer.height);
        g2.scale(scale, scale);

        g2.translate((renderer.width/scale - image.getWidth()) / 2, (renderer.height/scale - image.getHeight())/2);

        try {
            renderImage(g2, image);
        } catch (SVGException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void renderImage(Graphics2D g2, BufferedImage image) throws SVGException {
        if (image == null) {
            return;
        }
        AffineTransform at = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(image, 0, 0, null);
        g2.setTransform(at);
    }
}
