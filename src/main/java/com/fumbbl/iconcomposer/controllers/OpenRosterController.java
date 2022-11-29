package com.fumbbl.iconcomposer.controllers;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.model.types.NamedItem;
import com.fumbbl.iconcomposer.model.types.Position;
import com.fumbbl.iconcomposer.model.types.Roster;
import com.fumbbl.iconcomposer.model.types.Ruleset;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.beans.binding.ListBinding;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class OpenRosterController extends BaseController implements Initializable {
	public ListView<Ruleset> rulesetList;
	public ListView<Roster> rosterList;
	public Button openButton;
	
	public void openRoster() {
		controller.loadRoster(rosterList.getSelectionModel().getSelectedItem());
		controller.getStageManager().hide(StageType.openRoster);
	}
	
	@Override
	public void onShow() {
		if (!initialized) {
			super.onShow();

			model.loadedRuleset.addListener((obj, oldValue, newValue) -> {
				rosterList.getItems().setAll(newValue.rosters);
				rosterList.getItems().sort(NamedItem.Comparator);
			});
		}

		Collection<Ruleset> rulesets = model.loadRulesets();

		ObservableList<Ruleset> items = rulesetList.getItems();
		items.clear();
		items.addAll(rulesets);
		items.sort(NamedItem.Comparator);
		rosterList.getSelectionModel().clearSelection();
		rulesetList.getSelectionModel().clearSelection();
		openButton.setDisable(true);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		rulesetList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			model.loadRuleset(newValue.id);
		});
		
		rosterList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> openButton.setDisable(newValue == null));
		
		rulesetList.setCellFactory(p -> new ListCell<Ruleset>() {
			@Override
			protected void updateItem(Ruleset item, boolean empty) {
				super.updateItem(item, empty);
				
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getName());
				}
			}
		});
		
		rosterList.setCellFactory(p -> new ListCell<Roster>() {
			@Override
			protected void updateItem(Roster item, boolean empty) {
				super.updateItem(item, empty);
				
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getName());
				}
			}
		});		
	}
}
