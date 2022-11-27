package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.Model;

import java.io.IOException;

public class PrefsStage extends BaseStage {
	public PrefsStage(Model model) throws IOException {
		super(model, "Preferences");
		setFxml("/ui/Prefs.fxml");
	}
}
