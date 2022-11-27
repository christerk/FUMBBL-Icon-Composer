package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.Model;

import java.io.IOException;

public class LicenseStage extends BaseStage {

	public LicenseStage(Model model) throws IOException {
		super(model,"Licensing Information");
		setFxml("/ui/Licenses.fxml");
	}

}
