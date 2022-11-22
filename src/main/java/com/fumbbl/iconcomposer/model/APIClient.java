package com.fumbbl.iconcomposer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class APIClient {
	private String accessToken;
	private LocalDateTime tokenExpiry;
	private String clientId;
	private String clientSecret;

	private String apiBase;
	private boolean authenticated;
	
	public APIClient(String apiBase) {
		this.apiBase = apiBase;
	}
	
	public String get(String uri) {
		try {
			HttpURLConnection con = openConnection(uri, "GET");
	
			String content = getResult(con);
			
			return content;
		} catch (IOException ioe) {
			
		}
		return null;
	}

	public String post(String uri, Map<String,String> params, boolean requireAuth) {
		try {
			HttpURLConnection con = openConnection(uri, "POST");
	
			if (requireAuth && authenticated) {
				if (LocalDateTime.now().isAfter(tokenExpiry)) {
					authenticate(this.clientId, this.clientSecret);
				}
				con.setRequestProperty("Authorization", "Bearer "+accessToken);
			}
			
			if (params != null && !params.isEmpty()) {
				byte[] postDataBytes = createPostData(params);
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				con.getOutputStream().write(postDataBytes);
			}
			
			String content = getResult(con);
			return content;
		} catch (IOException ioe) {
			
		}
		return null;
	}

	private byte[] createPostData(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,String> param : params.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			if (param.getValue() != null) {
				postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
			}
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		return postDataBytes;
	}

	private HttpURLConnection openConnection(String uri, String method) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(apiBase + uri);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		con.setDoOutput(true);
		return con;
	}
	
	private String getResult(HttpURLConnection con) throws IOException {
		con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}
	
	public boolean authenticate(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("grant_type", "client_credentials");
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);

		String result = post("/oauth/token", params, false);
		
		JsonParser p = new JsonParser();
		JsonObject o = p.parse(result).getAsJsonObject();
		JsonElement tokenElement = o.get("access_token");
		if (tokenElement != null) {
			accessToken = tokenElement.getAsString();
			LocalDateTime now = LocalDateTime.now();
			int seconds = o.get("expires_in").getAsInt();
			tokenExpiry = now.plusSeconds(seconds);
			authenticated = true;
		} else {
			authenticated = false;
		}
		return authenticated;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}
}
