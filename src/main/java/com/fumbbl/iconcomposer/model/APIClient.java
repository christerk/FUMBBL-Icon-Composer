package com.fumbbl.iconcomposer.model;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Callback;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import javax.imageio.ImageIO;

public class APIClient {
	private String accessToken;
	private LocalDateTime tokenExpiry;
	private String clientId;
	private String clientSecret;

	private final String siteBase;
	private final String apiBase;
	private boolean authenticated;
	
	public APIClient(String siteBase, String apiBase) {
		this.siteBase = siteBase;
		this.apiBase = apiBase;
	}
	
	public String get(String uri) {
		try {
			HttpURLConnection con = openConnection(uri, "GET");
	
			return getResult(con);
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

			return getResult(con);
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
		return postData.toString().getBytes(StandardCharsets.UTF_8);
	}

	private HttpURLConnection openConnection(String uri, String method) throws IOException {
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
		StringBuilder content = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}
	
	public boolean authenticate(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		
		Map<String,String> params = new HashMap<>();
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

	public void uploadIconGraphic(int diagramId, String fileId, byte[] image) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(apiBase + "/iconskeleton/image");

		if (authenticated) {
			if (LocalDateTime.now().isAfter(tokenExpiry)) {
				authenticate(this.clientId, this.clientSecret);
			}
			post.addHeader("Authorization", "Bearer "+accessToken);
		}

		ByteArrayBody file = new ByteArrayBody(image, fileId);
		StringBody skeleton = new StringBody(Integer.toString(diagramId), ContentType.TEXT_PLAIN);

		HttpEntity reqEntity = MultipartEntityBuilder.create()
				.addPart("file", file)
				.addPart("diagramId", skeleton)
				.build();

		post.setEntity(reqEntity);

		try {
			client.execute(post, response -> {
				HttpEntity resEntity = response.getEntity();
				EntityUtils.consume(response.getEntity());
				return null;
			});
		}
		catch(IOException ioe)
		{
		}
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void loadImage(int imageId, Callback<BufferedImage, BufferedImage> callback) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(siteBase+"/i/"+imageId);
		if (imageId==702252) {
			System.out.println();
		}
		get.addHeader("Authorization", "Bearer "+accessToken);
		try {
			client.execute(get, response -> {
				HttpEntity resEntity = response.getEntity();

				try {
					BufferedImage image = ImageIO.read(resEntity.getContent());
					EntityUtils.consume(response.getEntity());
					return callback.call(image);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				return null;
			});
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
}
