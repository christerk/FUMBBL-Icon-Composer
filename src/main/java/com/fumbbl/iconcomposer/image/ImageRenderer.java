package com.fumbbl.iconcomposer.image;

import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.model.Model;
import com.kitfox.svg.SVGException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static java.awt.RenderingHints.KEY_INTERPOLATION;

public class ImageRenderer {
    private final BaseRenderer renderer;

    public ImageRenderer(BaseRenderer renderer) {
        this.renderer = renderer;
    }

    public void renderImage(BufferedImage image) {
        Graphics2D g2 = renderer.controller.viewState.getGraphics2D();
        g2.setColor(renderer.renderBackground);
        g2.fillRect(0, 0, renderer.width, renderer.height);
        g2.scale(8.0, 8.0);

        try {
            renderImage(g2, image);
        } catch (SVGException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void renderImage(Graphics2D g2, BufferedImage image) throws SVGException {
        AffineTransform at = g2.getTransform();

        int scale = 8;

        renderer.imageScale = scale;

        int sw = image.getWidth();
        int sh = image.getHeight();

        int dw = sw * scale;
        int dh = sh * scale;

        int dx = (renderer.width - dw) / 2 + (sw % 2 == 0 ? 0 : -scale/2);
        int dy = (renderer.height - dh) / 2 + (sh % 2 == 0 ? 0 : -scale/2);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(image, 0, 0, null);
        //g2.drawImage(image, 0, 0, dw, dh, 0, 0, sw, sh, null);
        g2.setTransform(at);
    }
}
