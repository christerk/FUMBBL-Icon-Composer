package com.fumbbl.iconcomposer.controllers;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import com.fumbbl.iconcomposer.model.types.NamedImage;
import com.fumbbl.iconcomposer.model.types.NamedItem;
import com.fumbbl.iconcomposer.model.types.Slot;
import com.fumbbl.iconcomposer.model.types.VirtualSlot;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class NewDiagramController extends BaseController implements Initializable {
	public ChoiceBox<VirtualSlot> slotChoice;
	public TextField componentName;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		slotChoice.setConverter(new StringConverter<VirtualSlot>() {
			@Override
			public String toString(VirtualSlot object) {
				return object.getName();
			}

			@Override
			public VirtualSlot fromString(String string) {
				return null;
			}
		});
	}
	
	@Override
	public void onShow() {
		slotChoice.setItems(model.masterSlots);
	}
	
	public void createDiagram() {
		controller.createDiagram(slotChoice.getSelectionModel().getSelectedItem(), componentName.getText());
		controller.getStageManager().hide(StageType.newDiagram);
	}

	public void setSlot(VirtualSlot slot) {
		slotChoice.setValue(slot);
	}
}
