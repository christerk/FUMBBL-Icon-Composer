package com.fumbbl.iconcomposer.model.types;

import java.awt.*;
import java.awt.image.BufferedImage;

public class NamedPng extends NamedImage {
	public int id;
	public String name;
	public BufferedImage image;

	public NamedPng() {
		id = -1;
	}

	public NamedPng(String name, BufferedImage image) {
		this(-1, name, image);
	}

	public NamedPng(int id, String name, BufferedImage image) {
		this.id = id;
		this.name = name;
		this.image = image;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}
}
