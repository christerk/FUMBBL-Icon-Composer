package com.fumbbl.iconcomposer.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.fumbbl.iconcomposer.ui.StageType;

import javafx.scene.control.TextArea;

public class LicenseController extends BaseController {

	public TextArea licenseTextArea;
	@Override
	public void onShow() {
		if (licenseTextArea.getText().length() == 0) {
			InputStream stream = getClass().getResourceAsStream("/ui/LICENSE-3RD-PARTY");
			String licenseText = String.join("\n", new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).lines().collect(Collectors.toList()));
			licenseTextArea.setText(licenseText);
		}
	}
	
	public void licensesClose() {
		controller.getStageManager().hide(StageType.licenses);
	}
}
