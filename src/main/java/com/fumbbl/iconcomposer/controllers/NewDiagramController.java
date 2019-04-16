package com.fumbbl.iconcomposer.controllers;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import com.fumbbl.iconcomposer.model.types.NamedItem;
import com.fumbbl.iconcomposer.model.types.NamedSVG;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

public class NewDiagramController extends BaseController implements Initializable {
	public ChoiceBox<NamedSVG> imageChoiceBox;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageChoiceBox.setConverter(new StringConverter<NamedSVG>() {
			@Override
			public String toString(NamedSVG object) {
				return object.name;
			}

			@Override
			public NamedSVG fromString(String string) {
				return null;
			}
		});
	}
	
	@Override
	public void onShow() {
		
	}
	
	public void createDiagram() {
		NamedSVG svg = imageChoiceBox.getSelectionModel().selectedItemProperty().getValue();
		controller.createDiagram(svg);
		controller.getStageManager().hide(StageType.newDiagram);
	}

	public void setImages(Collection<NamedSVG> images) {
		ObservableList<NamedSVG> list = imageChoiceBox.getItems();
		list.setAll(images);
		list.sort(NamedItem.Comparator);
	}
}
