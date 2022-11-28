package com.fumbbl.iconcomposer.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.ui.StageType;

public class ControllerManager {
	private final Controller controller;
	private final Map<StageType,BaseController> controllers;
	
	public ControllerManager(Model model, Controller controller) {
		this.controller = controller;
		controllers = new HashMap<>();
		
		controller.setControllerManager(this);
	}
	
	public void registerController(StageType type, BaseController controller) {
		controllers.put(type, controller);
		controller.setController(this.controller);
	}
	
	public BaseController get(StageType type) {
		return controllers.get(type);
	}
	
	public MainController getMain() {
		return (MainController) controllers.get(StageType.main);
	}
}
