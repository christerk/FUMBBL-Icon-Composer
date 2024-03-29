package com.fumbbl.iconcomposer;


import com.fumbbl.iconcomposer.controllers.CellFactory;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.controllers.ControllerManager;
import com.fumbbl.iconcomposer.model.DataLoader;
import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.ui.StageManager;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	private static HostServices hostServices;
	
	@Override
	public void start(Stage stage) throws Exception {
		Config config = new Config();
		TaskManager taskManager = new TaskManager();
		DataLoader dataLoader = new DataLoader(config, taskManager);
		Model model = new Model(config, dataLoader, taskManager);

		Controller controller = new Controller(model);
		CellFactory.setController(controller);
		ControllerManager controllerManager = new ControllerManager(model, controller);
		StageManager stageManager = new StageManager(model, controller, controllerManager);
		
		controller.setStageManager(stageManager);
		model.setupThemes();
		
		stageManager.show(StageType.main);

		dataLoader.authenticate();
    }

	public Main() {
		hostServices = getHostServices();
	}
	
	public static void showDocument(String url) {
		hostServices.showDocument(url);
	}
}
