package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.Model;

import java.io.IOException;

public class AboutStage extends BaseStage {

	public AboutStage(Model model) throws IOException {
		super(model,"About");
		setFxml("/ui/About.fxml");
		stage.setResizable(false);
	}

}
