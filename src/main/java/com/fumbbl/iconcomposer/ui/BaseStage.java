package com.fumbbl.iconcomposer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import com.fumbbl.iconcomposer.controllers.BaseController;
import com.fumbbl.iconcomposer.controllers.Controller;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BaseStage {
	protected Stage stage;
	protected Scene scene;
	protected BaseController controller;
	protected Controller mainController;
	protected FXMLLoader loader;
	private Parent root;
	private HashMap<String, ContextMenu> childControls;

	public BaseStage(String title) {
		stage = new Stage();
		stage.setTitle(title);
		childControls = new HashMap<>();
		stage.setOnShown(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				controller.onShow();
			}
		});
	}
	
	protected void applyTheme() {
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/ui/Icon16.png")));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/ui/Icon32.png")));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/ui/Icon64.png")));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/ui/Icon128.png")));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/ui/Icon256.png")));
		
		scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Muli");
		
		scene.getStylesheets().add(getClass().getResource("/ui/IconBuilder.css").toExternalForm());
		
		stage.setScene(scene);
		stage.sizeToScene();
	}
	
	public void setFxml(String path) throws IOException {
		loader = new FXMLLoader(getClass().getResource(path));
		root = loader.load();
		scene = new Scene(root);
		controller = (BaseController) loader.getController();
		controller.setStage(this);
	}

	public BaseController getController() {
		return controller;
	}
	
	public void setMainController(Controller controller) {
		this.mainController = controller;
	}

	public void show() {
		show(null);
	}
	public void show(Object data) {
		if (data != null) {
			stage.setUserData(data);
		}
		stage.show();
	}

	public Object getUserData() {
		return stage.getUserData();
	}

	public void hide() {
		stage.hide();
	}

	public ContextMenu getChildControl(String s) {
		return childControls.get(s);
	}
}
