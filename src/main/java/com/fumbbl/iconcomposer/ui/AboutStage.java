package com.fumbbl.iconcomposer.ui;

import java.io.IOException;

public class AboutStage extends BaseStage {

	public AboutStage() throws IOException {
		super("About");
		setFxml("/ui/About.fxml");
		stage.setResizable(false);
	}

}
