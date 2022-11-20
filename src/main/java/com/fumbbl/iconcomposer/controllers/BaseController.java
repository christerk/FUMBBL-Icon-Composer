package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.ui.BaseStage;
import com.fumbbl.iconcomposer.ui.MenuType;
import javafx.scene.control.ContextMenu;

import java.util.HashMap;

public class BaseController {
	protected Controller controller;
	protected BaseStage stage;

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public void onShow() {
		// TODO Auto-generated method stub
		
	}

	public ContextMenu getContextMenu(MenuType type) {
		return stage.getController().getContextMenu(type);
	}

	public void setStage(BaseStage stage) {
		this.stage = stage;
	}
}
