package com.fumbbl.iconcomposer.dto.spine;

import java.util.HashMap;

import com.fumbbl.iconcomposer.model.types.Skin;

public class DtoSkin extends HashMap<String,DtoSlotData> {
	private static final long serialVersionUID = -3824191924422640630L;

	public Skin toSkin() {
		return new Skin();
	}
}
