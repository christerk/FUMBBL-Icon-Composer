package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PrefsController extends BaseController {

	public TextField clientId;
	public PasswordField clientSecret;

	@Override
	public void onShow() {
		Config cfg = controller.getConfig();
		clientId.setText(cfg.getClientId());
		clientSecret.setText(cfg.getClientSecret());
	}
	
	public void prefsClose() {
		Config cfg = controller.getConfig();
		
		String oldClientId = cfg.getClientId();
		String oldClientSecret = cfg.getClientSecret();
		cfg.setClientId(clientId.getText());
		cfg.setClientSecret(clientSecret.getText());
		
		if (!oldClientId.equals(clientId.getText()) || !oldClientSecret.contentEquals(clientSecret.getText())) {
			controller.authenticate();
		}
		
		controller.getStageManager().hide(StageType.prefs);
	}
}
