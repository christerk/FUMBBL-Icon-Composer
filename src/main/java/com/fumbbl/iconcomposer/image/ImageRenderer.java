package com.fumbbl.iconcomposer.image;

import com.fumbbl.iconcomposer.model.Perspective;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageRenderer {

    private final BaseRenderer renderer;

    public ImageRenderer(BaseRenderer renderer) {
        this.renderer = renderer;
    }

    public void renderImage(Perspective perspective, BufferedImage image) {
        if (image == null) {
            return;
        }
        Graphics2D g2 = renderer.controller.viewState.getDiagramGraphics2D(perspective);
        //g2.setColor(renderer.renderBackground);
        //g2.fillRect(0, 0, renderer.width, renderer.height);
        int scale = 8;
        g2.scale(scale, scale);

        g2.translate((renderer.width/ scale - image.getWidth()) / 2, (renderer.height/ scale - image.getHeight())/2);

        renderImage(g2, image);
    }

    public void renderImage(Graphics2D g2, BufferedImage image) {
        if (image == null) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(image, 0, 0, null);
    }
}
