package com.fumbbl.iconcomposer.dto;

import com.fumbbl.iconcomposer.controllers.NamedItem;

public class DtoPosition implements NamedItem {
	public int id;
	public String title;
	
	@Override
	public String getName() {
		return title;
	}
}
