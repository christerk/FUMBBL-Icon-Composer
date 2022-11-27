package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.Model;

import java.io.IOException;

public class OpenRosterStage extends BaseStage {

	public OpenRosterStage(Model model) throws IOException {
		super(model,"Open Roster");
		setFxml("/ui/OpenRoster.fxml");
	}

}
