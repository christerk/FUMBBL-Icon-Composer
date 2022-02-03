package com.fumbbl.iconcomposer.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class SVGLoader {
	private SVGUniverse universe;

	public SVGLoader() {
		universe = new SVGUniverse();
	}
	
	public SVGDiagram loadSVG(Path path) throws FileNotFoundException, IOException {
		InputStream in = Files.newInputStream(path);
		universe.loadSVG(in, path.toUri().toString());
		SVGDiagram diagram = universe.getDiagram(path.toUri());
		
		diagram.setIgnoringClipHeuristic(true);

		return diagram;
	}
}
