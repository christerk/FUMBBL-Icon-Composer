package com.fumbbl.iconcomposer.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.dto.DtoPosition;
import com.fumbbl.iconcomposer.dto.DtoRoster;
import com.fumbbl.iconcomposer.dto.DtoRuleset;
import com.fumbbl.iconcomposer.dto.DtoSkin;
import com.fumbbl.iconcomposer.spine.Attachment;
import com.fumbbl.iconcomposer.spine.Bone;
import com.fumbbl.iconcomposer.spine.Skeleton;
import com.fumbbl.iconcomposer.spine.Skin;
import com.fumbbl.iconcomposer.spine.Slot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataLoader {
	private APIClient apiClient;
	private Gson gson;
	
	private static final Type boneListType = new TypeToken<List<Bone>>() {}.getType();
	private static final Type slotListType = new TypeToken<List<Slot>>() {}.getType();
	private static final Type skeletonListType = new TypeToken<List<Skeleton>>() {}.getType();
	private static final Type attachmentListType = new TypeToken<List<Attachment>>() {}.getType();
	
	public DataLoader(Config cfg) {
		apiClient = new APIClient(cfg.getApiBase());
		gson = new Gson();
	}
	
	public boolean authenticate(String clientId, String clientSecret) {
		return apiClient.authenticate(clientId, clientSecret);
	}

	public Collection<Skin> getSkins(int positionId) {
		String content = apiClient.get("/iconskeleton/skins/" + positionId);
		DtoSkin[] skins = gson.fromJson(content, DtoSkin[].class);
		
		List<Skin> list = new ArrayList<Skin>(skins.length);
		for (DtoSkin s : skins) {
			Skin skin = s.toSkin();
			list.add(skin);
		}
		
		return list;
	}
	
	public Collection<Attachment> getAttachments(int skinId) {
		String content = apiClient.get("/iconskeleton/attachments/" + skinId);
		return gson.fromJson(content, attachmentListType);		
	}
	
	public Collection<Slot> getSlots(int skeletonId) {
		String content = apiClient.get("/iconskeleton/slots/" + skeletonId);
		return gson.fromJson(content, slotListType);	
	}
	
	public Collection<Bone> getBones(int i) {
		String content = apiClient.get("/iconskeleton/bones/" + i);
		return gson.fromJson(content, boneListType);
	}

	public DtoRoster getRoster(int rosterId) {
		String content = apiClient.get("/roster/get/" + rosterId);
		return gson.fromJson(content, DtoRoster.class);
	}

	public DtoPosition getPosition(int positionId) {
		String content = apiClient.get("/position/get/" + positionId);
		return gson.fromJson(content, DtoPosition.class);
	}
	
	public DtoRuleset[] getRulesets() {
		String content = apiClient.post("/ruleset/list", null, true);
		return gson.fromJson(content, DtoRuleset[].class);
	}

	public DtoRuleset getRuleset(int rulesetId) {
		String content = apiClient.get("/ruleset/get/" + rulesetId);
		return gson.fromJson(content, DtoRuleset.class);
	}

	public boolean isAuthenticated() {
		return apiClient.isAuthenticated();
	}

	public Collection<Skeleton> getSkeletons(int positionId) {
		String content = apiClient.get("/iconskeleton/list/" + positionId);
		return gson.fromJson(content, skeletonListType);
	}
}
