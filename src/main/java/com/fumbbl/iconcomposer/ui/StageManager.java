package com.fumbbl.iconcomposer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fumbbl.iconcomposer.controllers.BaseController;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.controllers.ControllerManager;
import com.fumbbl.iconcomposer.model.Model;
import com.sun.applet2.AppletParameters;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.stage.Stage;

public class StageManager {

	private final Controller controller;
	private final Model model;
	private Map<StageType,BaseStage> stages;

	public StageManager(Model model, Controller controller, ControllerManager controllerManager) throws IOException {
		this.model = model;
		stages = new HashMap<StageType,BaseStage>();
		this.controller = controller;

		controller.setStageManager(this);

		stages.put(StageType.main, new MainStage(model));
		stages.put(StageType.prefs, new PrefsStage(model));
		stages.put(StageType.openRoster, new OpenRosterStage(model));
		stages.put(StageType.newDiagram, new NewDiagramStage(model));
		stages.put(StageType.about, new AboutStage(model));
		stages.put(StageType.licenses, new LicenseStage(model));
		stages.put(StageType.rename, new RenameStage(model));
		
		for (Entry<StageType, BaseStage> entry: stages.entrySet()) {
			StageType type = entry.getKey();
			BaseStage stage = entry.getValue();
			stage.applyTheme();
			stage.setMainController(controller);
			BaseController ctrl = stage.getController();
			if (ctrl != null) {
				controllerManager.registerController(type, ctrl);
			}
		}
	}

	public void show(StageType type) {
		show(type, null);
	}
	public void show(StageType type, Object data) {
		BaseStage stage = stages.get(type);
		stage.getController().setModel(model);
		stages.get(type).show(data);
	}

	public void hide(StageType type) {
		stages.get(type).hide();
	}
}
