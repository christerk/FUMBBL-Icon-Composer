package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.ui.StageType;

public class AboutController extends BaseController {
	public void aboutClose() {
		controller.getStageManager().hide(StageType.about);
	}
	
	public void showLicenses() {
		controller.getStageManager().hide(StageType.about);
		controller.getStageManager().show(StageType.licenses);
	}
}
