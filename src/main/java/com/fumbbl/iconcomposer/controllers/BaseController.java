package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.ui.BaseStage;

public class BaseController {
	protected Controller controller;
	protected BaseStage stage;
	protected Model model;
	protected boolean initialized;

	public void setController(Controller controller) {
		this.controller = controller;
	}
	public void setModel(Model model) {
		this.model = model;
	}

	public void onShow() {
		this.initialized = true;
	}

	public void setStage(BaseStage stage) {
		this.stage = stage;
	}
}
