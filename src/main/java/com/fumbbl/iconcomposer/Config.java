package com.fumbbl.iconcomposer;

import java.util.prefs.Preferences;

public class Config {
	private Preferences prefs;
	private String clientId;
	private String clientSecret;

	private String apiBase;
	
	private static final String KEY_CLIENT_ID = "client_id";
	private static final String KEY_CLIENT_SECRET = "client_secret";
	private static final String KEY_API_BASE = "api_base";
	
	public Config() {
		prefs = Preferences.userNodeForPackage(this.getClass());
		this.clientId = prefs.get(KEY_CLIENT_ID, "");
		this.clientSecret = prefs.get(KEY_CLIENT_SECRET, "");
		this.apiBase = prefs.get(KEY_API_BASE, "https://fumbbl.com/api");
	}

	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
		prefs.put(KEY_CLIENT_ID, clientId);
	}

	public String getClientSecret() {
		return clientSecret;
	}
	
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		prefs.put(KEY_CLIENT_SECRET, clientSecret);
	}
	
	public String getApiBase() {
		return apiBase;
	}
}
