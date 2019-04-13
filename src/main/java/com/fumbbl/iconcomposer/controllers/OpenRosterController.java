package com.fumbbl.iconcomposer.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.fumbbl.iconcomposer.dto.DtoRoster;
import com.fumbbl.iconcomposer.dto.DtoRuleset;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class OpenRosterController extends BaseController implements Initializable {
	public ListView<DtoRuleset> rulesetList;
	public ListView<DtoRoster> rosterList;
	public Button openButton;
	
	public void openRoster() {
		controller.loadRoster(rosterList.getSelectionModel().getSelectedItem());
		controller.getStageManager().hide(StageType.openRoster);
	}
	
	@Override
	public void onShow() {
		DtoRuleset[] rulesets = controller.loadRulesets();
		
		ObservableList<DtoRuleset> items = rulesetList.getItems();
		items.clear();
		for (DtoRuleset r : rulesets) {
			items.add(r);
		}
		rosterList.getSelectionModel().clearSelection();
		rulesetList.getSelectionModel().clearSelection();
		openButton.setDisable(true);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		rulesetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DtoRuleset>() {
			@Override
			public void changed(ObservableValue<? extends DtoRuleset> observable, DtoRuleset oldValue, DtoRuleset newValue) {
				loadRosters(newValue);
			}
		});
		
		rosterList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DtoRoster>() {

			@Override
			public void changed(ObservableValue<? extends DtoRoster> observable, DtoRoster oldValue, DtoRoster newValue) {
				if (newValue != null) {
					openButton.setDisable(false);
				} else {
					openButton.setDisable(true);
				}
			}
		});
		
		rulesetList.setCellFactory(p -> new ListCell<DtoRuleset>() {
			@Override
			protected void updateItem(DtoRuleset item, boolean empty) {
				super.updateItem(item, empty);
				
				if(getIndex() == 0) {
					this.getStyleClass().add("first");
			    }
				
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.value);
				}
			}
		});
		
		rosterList.setCellFactory(p -> new ListCell<DtoRoster>() {
			@Override
			protected void updateItem(DtoRoster item, boolean empty) {
				super.updateItem(item, empty);
				
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.value);
				}
			}
		});		
	}
	
	protected void loadRosters(DtoRuleset ruleset) {
		if (ruleset == null) {
			rosterList.getItems().clear();
			return;
		}
		
		ruleset = controller.loadRuleset(ruleset.id);;
		
		ObservableList<DtoRoster> items = rosterList.getItems();
		items.clear();
		for (DtoRoster r : ruleset.rosters) {
			items.add(r);
		}
	}
}
