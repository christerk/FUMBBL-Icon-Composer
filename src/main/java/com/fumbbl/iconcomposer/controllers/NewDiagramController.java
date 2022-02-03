package com.fumbbl.iconcomposer.controllers;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import com.fumbbl.iconcomposer.model.types.NamedImage;
import com.fumbbl.iconcomposer.model.types.NamedItem;
import com.fumbbl.iconcomposer.model.types.NamedSVG;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

public class NewDiagramController extends BaseController implements Initializable {
	public ChoiceBox<NamedImage> imageChoiceBox;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageChoiceBox.setConverter(new StringConverter<NamedImage>() {
			@Override
			public String toString(NamedImage object) {
				return object.getName();
			}

			@Override
			public NamedImage fromString(String string) {
				return null;
			}
		});
	}
	
	@Override
	public void onShow() {
		
	}
	
	public void createDiagram() {
		NamedImage image = imageChoiceBox.getSelectionModel().selectedItemProperty().getValue();
		controller.createDiagram(image);
		controller.getStageManager().hide(StageType.newDiagram);
	}

	public void setImages(Collection<NamedImage> images) {
		ObservableList<NamedImage> list = imageChoiceBox.getItems();
		list.setAll(images);
		list.sort(NamedItem.Comparator);
	}
}
