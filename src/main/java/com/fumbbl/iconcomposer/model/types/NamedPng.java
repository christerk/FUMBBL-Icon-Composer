package com.fumbbl.iconcomposer.model.types;

import java.awt.image.BufferedImage;

public class NamedPng extends NamedImage {
	public final int id;
	public BufferedImage image;

	public NamedPng() {
		super();
		id = -1;
	}

	public NamedPng(String name, BufferedImage image) {
		this(-1, name, image);
	}

	public NamedPng(int id, String name, BufferedImage image) {
		this.id = id;
		setName(name);
		this.image = image;
	}
}
