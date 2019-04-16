package com.fumbbl.iconcomposer.controllers;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import com.fumbbl.iconcomposer.model.types.NamedItem;
import com.fumbbl.iconcomposer.model.types.Roster;
import com.fumbbl.iconcomposer.model.types.Ruleset;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.beans.value.ChangeListener;
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
		Collection<Ruleset> rulesets = controller.loadRulesets();
		
		ObservableList<Ruleset> items = rulesetList.getItems();
		items.clear();
		for (Ruleset r : rulesets) {
			items.add(r);
		}
		rosterList.getSelectionModel().clearSelection();
		rulesetList.getSelectionModel().clearSelection();
		openButton.setDisable(true);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		rulesetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Ruleset>() {
			@Override
			public void changed(ObservableValue<? extends Ruleset> observable, Ruleset oldValue, Ruleset newValue) {
				loadRosters(newValue);
			}
		});
		
		rosterList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Roster>() {

			@Override
			public void changed(ObservableValue<? extends Roster> observable, Roster oldValue, Roster newValue) {
				if (newValue != null) {
					openButton.setDisable(false);
				} else {
					openButton.setDisable(true);
				}
			}
		});
		
		rulesetList.setCellFactory(p -> new ListCell<Ruleset>() {
			@Override
			protected void updateItem(Ruleset item, boolean empty) {
				super.updateItem(item, empty);
				
				if(getIndex() == 0) {
					this.getStyleClass().add("first");
			    }
				
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
	
	protected void loadRosters(Ruleset ruleset) {
		if (ruleset == null) {
			rosterList.getItems().clear();
			return;
		}
		
		controller.loadRuleset(ruleset.id);
	}

	public void onRulesetLoaded(Ruleset ruleset) {
		ObservableList<Roster> items = rosterList.getItems();
		items.setAll(ruleset.rosters);
		items.sort(NamedItem.Comparator);
	}
}
