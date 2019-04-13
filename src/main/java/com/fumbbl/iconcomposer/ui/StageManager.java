package com.fumbbl.iconcomposer.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.controllers.ControllerManager;
import com.fumbbl.iconcomposer.model.Model;

public class StageManager {

	private Map<StageType,BaseStage> stages;
	
	public StageManager(Model model, Controller controller, ControllerManager controllerManager) throws IOException {
		stages = new HashMap<StageType,BaseStage>();
		
		controller.setStageManager(this);
		
		stages.put(StageType.main, new MainStage());
		stages.put(StageType.prefs, new PrefsStage());
		stages.put(StageType.openRoster, new OpenRosterStage());
		stages.put(StageType.newDiagram, new NewDiagramStage());
		
		for (Entry<StageType, BaseStage> entry: stages.entrySet()) {
			StageType type = entry.getKey();
			BaseStage stage = entry.getValue();
			stage.applyTheme();
			stage.setMainController(controller);
			controllerManager.registerController(type, stage.getController());
		}
	}
	
	public void show(StageType type) {
		stages.get(type).show();
	}

	public void hide(StageType type) {
		stages.get(type).hide();
	}
}
