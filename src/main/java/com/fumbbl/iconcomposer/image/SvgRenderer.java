package com.fumbbl.iconcomposer.image;

import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.model.Model;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class SvgRenderer {
    private final BaseRenderer renderer;

    public SvgRenderer(BaseRenderer renderer) {
        this.renderer = renderer;
    }

    public void renderSvg(SVGDiagram svg) {
        Graphics2D g2 = renderer.controller.viewState.getGraphics2D();
        g2.setColor(renderer.renderBackground);
        g2.fillRect(0, 0, renderer.width, renderer.height);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        try {
            renderSvg(g2, svg);
        } catch (SVGException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void renderSvg(Graphics2D g2, SVGDiagram diagram) throws SVGException {
        AffineTransform at = g2.getTransform();
        String viewBox = diagram.getRoot().getPresAbsolute("viewBox").getStringValue();
        String[] list = viewBox.split(" ");

        double dw = Double.parseDouble(list[2]);
        double dh = Double.parseDouble(list[3]);

        renderer.imageScale = Math.min(renderer.width / dw, renderer.height / dh);
        g2.translate((renderer.width - dw*renderer.imageScale)/2.0, (renderer.height - dh*renderer.imageScale)/2.0);
        g2.scale(renderer.imageScale * dw / diagram.getWidth(), renderer.imageScale * dh / diagram.getHeight());
        diagram.render(g2);
        g2.setTransform(at);
    }
}
