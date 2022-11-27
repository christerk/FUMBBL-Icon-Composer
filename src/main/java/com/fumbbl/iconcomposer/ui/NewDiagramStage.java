package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.Model;

import java.io.IOException;

public class NewDiagramStage extends BaseStage {

	public NewDiagramStage(Model model) throws IOException {
		super(model,"Create Diagram");
		setFxml("/ui/NewDiagram.fxml");
	}

}
